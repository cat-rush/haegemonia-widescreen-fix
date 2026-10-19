#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <locale.h>
#include <setjmp.h>

#define C_RESET  "\033[0m"
#define C_BOLD   "\033[1m"
#define C_RED    "\033[31m"
#define C_GREEN  "\033[32m"
#define C_YELLOW "\033[33m"
#define C_CYAN   "\033[36m"

static int g_vt = 0;
static int g_interactive = 0;
static char g_errmsg[512];
static jmp_buf g_jmp;

static void enable_vt(void)
{
    HANDLE h = GetStdHandle(STD_OUTPUT_HANDLE);
    DWORD mode = 0;
    if (GetConsoleMode(h, &mode))
    {
        if (SetConsoleMode(h, mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING))
            g_vt = 1;
    }
}

#define CC(x) (g_vt ? x : "")

typedef struct { unsigned char type; char *str; float flt; } Value;
typedef struct { char *key; Value *vals; int nvals; } Item;
typedef struct Section Section;
struct Section { char *name; Item *items; int nitems; Section **subs; int nsubs; };

static const uint32_t MAGIC = 0xFFFFA5AD;

static void die(const char *msg)
{
    if (g_interactive)
    {
        strncpy(g_errmsg, msg, sizeof(g_errmsg) - 1);
        g_errmsg[sizeof(g_errmsg) - 1] = 0;
        longjmp(g_jmp, 1);
    }
    fprintf(stderr, "%serror: %s%s\n", CC(C_RED), msg, CC(C_RESET));
    exit(1);
}

static void *xmalloc(size_t n)
{
    void *p = malloc(n ? n : 1);
    if (!p) die("out of memory");
    return p;
}

static char *xstrdup(const char *s)
{
    char *p = xmalloc(strlen(s) + 1);
    strcpy(p, s);
    return p;
}

static void *xrealloc(void *p, size_t n)
{
    void *q = realloc(p, n);
    if (!q) die("out of memory");
    return q;
}

/* ---------------- float formatting ---------------- */

static char *expand_e(const char *s)
{
    const char *e = strpbrk(s, "eE");
    if (!e) return xstrdup(s);
    char mant[64];
    size_t ml = e - s;
    if (ml >= sizeof(mant)) ml = sizeof(mant) - 1;
    memcpy(mant, s, ml);
    mant[ml] = 0;
    int exp = atoi(e + 1);
    char *dot = strchr(mant, '.');
    char digits[80];
    int neg = 0;
    char *src = mant;
    if (*src == '-') { neg = 1; src++; }
    if (*src == '+') src++;
    char *d = digits;
    for (char *p = src; *p; p++)
    {
        if (*p == '.') continue;
        *d++ = *p;
    }
    int nd = (int)(d - digits);
    size_t cap = nd + (exp > 0 ? exp : -exp) + 16;
    char *out = xmalloc(cap);
    char *o = out;
    if (neg) *o++ = '-';
    if (exp >= 0)
    {
        memcpy(o, digits, nd);
        o += nd;
        for (int i = 0; i < exp; i++) *o++ = '0';
        *o = 0;
    }
    else if (-exp >= nd)
    {
        *o++ = '0';
        *o++ = '.';
        for (int i = 0; i < -exp - nd; i++) *o++ = '0';
        memcpy(o, digits, nd);
        o += nd;
        *o = 0;
    }
    else
    {
        int ip = nd + exp;
        memcpy(o, digits, ip);
        o += ip;
        *o++ = '.';
        strcpy(o, digits + ip);
    }
    return out;
}

static char *fmt_float(float f)
{
    for (int p = 1; p <= 9; p++)
    {
        char buf[64];
        snprintf(buf, sizeof(buf), "%.*g", p, (double)f);
        char *plain = expand_e(buf);
        char *end;
        float back = strtof(plain, &end);
        if (*end == 0 && *(uint32_t *)&back == *(uint32_t *)&f)
            return plain;
        free(plain);
    }
    char buf[64];
    snprintf(buf, sizeof(buf), "%.9g", (double)f);
    return expand_e(buf);
}

/* ---------------- text output ---------------- */

static int is_number_like(const char *s)
{
    char *end;
    strtof(s, &end);
    if (*end != 0) return 0;
    if (strpbrk(s, "0123456789") == 0) return 0;
    return 1;
}

static int is_safe_unquoted(const char *s)
{
    if (!*s) return 0;
    if (is_number_like(s)) return 0;
    for (const unsigned char *p = (const unsigned char *)s; *p; p++)
    {
        unsigned char c = *p;
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
            continue;
        if (c == '_' || c == '.' || c == '/' || c == '\\' || c == '-') continue;
        return 0;
    }
    return 1;
}

static void write_value(FILE *w, const Value *v)
{
    if (v->type == 0)
    {
        if (is_safe_unquoted(v->str))
            fputs(v->str, w);
        else
        {
            fputc('"', w);
            for (const char *p = v->str; *p; p++)
            {
                if (*p == '"') fputs("\\\"", w);
                else fputc(*p, w);
            }
            fputc('"', w);
        }
    }
    else
    {
        char *s = fmt_float(v->flt);
        fputs(s, w);
        free(s);
    }
}

static void write_items(FILE *w, const Item *items, int n, int depth)
{
    for (int i = 0; i < n; i++)
    {
        for (int d = 0; d < depth; d++) fputc('\t', w);
        fputs(items[i].key, w);
        fputc('=', w);
        for (int j = 0; j < items[i].nvals; j++)
        {
            if (j > 0) fputc(',', w);
            write_value(w, &items[i].vals[j]);
        }
        fputs("\r\n", w);
    }
}

static void write_sections(FILE *w, Section *const *secs, int n, int depth)
{
    for (int i = 0; i < n; i++)
    {
        for (int d = 0; d < depth; d++) fputc('\t', w);
        fprintf(w, "[%s]\r\n", secs[i]->name);
        for (int d = 0; d < depth; d++) fputc('\t', w);
        fputs("{\r\n", w);
        write_items(w, secs[i]->items, secs[i]->nitems, depth + 1);
        write_sections(w, secs[i]->subs, secs[i]->nsubs, depth + 1);
        for (int d = 0; d < depth; d++) fputc('\t', w);
        fputs("}\r\n", w);
    }
}

/* ---------------- binary reader ---------------- */

typedef struct { unsigned char *buf; size_t len; size_t pos; } Bin;

static void *b_alloc(Bin *b, size_t n)
{
    if (b->pos + n > b->len) die("truncated binary file");
    void *p = b->buf + b->pos;
    b->pos += n;
    return p;
}

static uint8_t b_u8(Bin *b) { return *(uint8_t *)b_alloc(b, 1); }
static uint16_t b_u16(Bin *b) { uint16_t v; memcpy(&v, b_alloc(b, 2), 2); return v; }
static uint32_t b_u32(Bin *b) { uint32_t v; memcpy(&v, b_alloc(b, 4), 4); return v; }
static float b_f32(Bin *b) { uint32_t v = b_u32(b); float f; memcpy(&f, &v, 4); return f; }

static char **g_strings;
static int g_nstrings;

static const char *get_str(uint16_t idx)
{
    if (idx >= g_nstrings) die("string index out of range");
    return g_strings[idx];
}

static void free_items(Item *items, int n)
{
    for (int i = 0; i < n; i++)
    {
        free(items[i].key);
        for (int j = 0; j < items[i].nvals; j++)
            if (items[i].vals[j].type == 0) free(items[i].vals[j].str);
        free(items[i].vals);
    }
    free(items);
}

static void free_sections(Section **secs, int n);

static Item *read_items(Bin *b, uint32_t count)
{
    Item *items = xmalloc((count ? count : 1) * sizeof(Item));
    for (uint32_t i = 0; i < count; i++)
    {
        items[i].key = xstrdup(get_str(b_u16(b)));
        uint8_t nv = b_u8(b);
        items[i].nvals = nv;
        items[i].vals = xmalloc((nv ? nv : 1) * sizeof(Value));
        for (int j = 0; j < nv; j++)
        {
            uint8_t type = b_u8(b);
            items[i].vals[j].type = type;
            if (type == 0)
            {
                items[i].vals[j].str = xstrdup(get_str(b_u16(b)));
                items[i].vals[j].flt = 0;
            }
            else if (type == 1)
            {
                items[i].vals[j].str = 0;
                items[i].vals[j].flt = b_f32(b);
            }
            else
                die("unknown value type in binary");
        }
    }
    return items;
}

static Section **read_sections(Bin *b, uint32_t count)
{
    Section **secs = xmalloc((count ? count : 1) * sizeof(Section *));
    for (uint32_t i = 0; i < count; i++)
    {
        Section *s = xmalloc(sizeof(Section));
        secs[i] = s;
        s->name = xstrdup(get_str(b_u16(b)));
        uint32_t ni = b_u32(b);
        uint32_t ns = b_u32(b);
        s->items = read_items(b, ni);
        s->nitems = ni;
        s->subs = read_sections(b, ns);
        s->nsubs = ns;
    }
    return secs;
}

static Section *load_binary(const char *path)
{
    FILE *f = fopen(path, "rb");
    if (!f) die("cannot open file");
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    unsigned char *buf = xmalloc(sz + 1);
    if (fread(buf, 1, sz, f) != (size_t)sz) die("read error");
    fclose(f);
    Bin b = { buf, (size_t)sz, 0 };
    if (b_u32(&b) != MAGIC) die("not a binary inf (bad magic)");
    uint32_t dataSize = b_u32(&b);
    uint32_t rootItems = b_u32(&b);
    uint32_t rootSections = b_u32(&b);
    if (dataSize + 4 > (uint32_t)sz) die("corrupt header");
    uint32_t stringCount = *(uint32_t *)(buf + dataSize);
    g_nstrings = stringCount;
    g_strings = xmalloc((stringCount ? stringCount : 1) * sizeof(char *));
    size_t sp = dataSize + 4;
    for (uint32_t i = 0; i < stringCount; i++)
    {
        if (sp >= (size_t)sz) die("string table overrun");
        size_t end = sp;
        while (end < (size_t)sz && buf[end]) end++;
        char *s = xmalloc(end - sp + 1);
        memcpy(s, buf + sp, end - sp);
        s[end - sp] = 0;
        g_strings[i] = s;
        sp = end + 1;
    }
    Section *root = xmalloc(sizeof(Section));
    root->name = xstrdup("");
    root->items = read_items(&b, rootItems);
    root->nitems = rootItems;
    root->subs = read_sections(&b, rootSections);
    root->nsubs = rootSections;
    for (int i = 0; i < (int)stringCount; i++) free(g_strings[i]);
    free(g_strings);
    free(buf);
    return root;
}

static void free_root(Section *root)
{
    free_items(root->items, root->nitems);
    free_sections(root->subs, root->nsubs);
    free(root->name);
    free(root);
}

static void free_sections(Section **secs, int n)
{
    for (int i = 0; i < n; i++)
    {
        free(secs[i]->name);
        free_items(secs[i]->items, secs[i]->nitems);
        free_sections(secs[i]->subs, secs[i]->nsubs);
        free(secs[i]);
    }
    free(secs);
}

/* ---------------- text parser ---------------- */

typedef struct { const char *text; size_t pos; size_t len; int line; } Txt;

static int t_eof(Txt *t) { return t->pos >= t->len; }
static char t_peek(Txt *t) { return t->text[t->pos]; }
static char t_next(Txt *t) { return t->text[t->pos++]; }

static void t_skip_ws(Txt *t)
{
    for (;;)
    {
        if (t_eof(t)) return;
        char c = t_peek(t);
        if (c == '\n') { t->line++; t->pos++; }
        else if (c == '\r' || c == ' ' || c == '\t') t->pos++;
        else if (c == ';')
        {
            while (!t_eof(t) && t_peek(t) != '\n') t->pos++;
        }
        else return;
    }
}

static char *t_token(Txt *t)
{
    size_t start = t->pos;
    while (!t_eof(t))
    {
        char c = t_peek(t);
        if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == ',' || c == '=' || c == '{' || c == '}' || c == ';')
            break;
        t->pos++;
    }
    size_t n = t->pos - start;
    char *s = xmalloc(n + 1);
    memcpy(s, t->text + start, n);
    s[n] = 0;
    return s;
}

static char *t_section_name(Txt *t)
{
    t_skip_ws(t);
    if (t_eof(t) || t_peek(t) != '[') die("expected '['");
    t->pos++;
    size_t start = t->pos;
    while (!t_eof(t) && t_peek(t) != ']' && t_peek(t) != '\n') t->pos++;
    if (t_eof(t) || t_peek(t) != ']') die("unterminated section name");
    size_t n = t->pos - start;
    char *s = xmalloc(n + 1);
    memcpy(s, t->text + start, n);
    s[n] = 0;
    t->pos++;
    while (n > 0 && (s[n - 1] == ' ' || s[n - 1] == '\t')) s[--n] = 0;
    return s;
}

static Value *t_values(Txt *t, int *count)
{
    Value *vals = 0;
    int n = 0, cap = 0;
    {
        size_t save = t->pos;
        int saveline = t->line;
        while (!t_eof(t) && (t_peek(t) == ' ' || t_peek(t) == '\t')) t->pos++;
        if (t_eof(t) || t_peek(t) == '\n' || t_peek(t) == '\r' || t_peek(t) == '}')
        {
            t->pos = save;
            t->line = saveline;
            *count = 0;
            return 0;
        }
        t->pos = save;
    }
    for (;;)
    {
        t_skip_ws(t);
        if (t_eof(t)) break;
        char c = t_peek(t);
        if (n == cap) { cap = cap ? cap * 2 : 4; vals = xrealloc(vals, cap * sizeof(Value)); }
        if (c == '"')
        {
            t->pos++;
            size_t start = t->pos;
            while (!t_eof(t) && t_peek(t) != '"')
            {
                if (t_peek(t) == '\n') t->line++;
                t->pos++;
            }
            if (t_eof(t)) die("unterminated string");
            size_t len = t->pos - start;
            vals[n].type = 0;
            vals[n].str = xmalloc(len + 1);
            memcpy(vals[n].str, t->text + start, len);
            vals[n].str[len] = 0;
            vals[n].flt = 0;
            n++;
            t->pos++;
        }
        else
        {
            char *tok = t_token(t);
            if (!*tok) { free(tok); break; }
            char *end;
            float f = strtof(tok, &end);
            if (*end == 0 && strpbrk(tok, "0123456789") != 0)
            {
                vals[n].type = 1;
                vals[n].str = 0;
                vals[n].flt = f;
            }
            else
            {
                vals[n].type = 0;
                vals[n].str = tok;
                vals[n].flt = 0;
                tok = 0;
            }
            free(tok);
            n++;
        }
        t_skip_ws(t);
        if (!t_eof(t) && t_peek(t) == ',') { t->pos++; continue; }
        break;
    }
    *count = n;
    return vals;
}

static Item *t_items(Txt *t, int *count)
{
    Item *items = 0;
    int n = 0, cap = 0;
    for (;;)
    {
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) == '}' || t_peek(t) == '[') break;
        char *key = t_token(t);
        if (!*key) { free(key); break; }
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) != '=') die("expected '=' after key");
        t->pos++;
        if (n == cap) { cap = cap ? cap * 2 : 8; items = xrealloc(items, cap * sizeof(Item)); }
        items[n].key = key;
        items[n].vals = t_values(t, &items[n].nvals);
        if (items[n].nvals == 0)
        {
            items[n].vals = xmalloc(sizeof(Value));
            items[n].vals[0].type = 0;
            items[n].vals[0].str = xstrdup("");
            items[n].vals[0].flt = 0;
            items[n].nvals = 1;
        }
        n++;
    }
    *count = n;
    return items;
}

static Section **t_sections(Txt *t, int *count)
{
    Section **secs = 0;
    int n = 0, cap = 0;
    for (;;)
    {
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) == '}') break;
        char *name = t_section_name(t);
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) != '{') die("expected '{' after section name");
        t->pos++;
        if (n == cap) { cap = cap ? cap * 2 : 8; secs = xrealloc(secs, cap * sizeof(Section *)); }
        Section *s = xmalloc(sizeof(Section));
        secs[n] = s;
        s->name = name;
        s->items = t_items(t, &s->nitems);
        s->subs = t_sections(t, &s->nsubs);
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) != '}') die("missing '}'");
        t->pos++;
        n++;
    }
    *count = n;
    return secs;
}

static Section *load_text(const char *path)
{
    FILE *f = fopen(path, "rb");
    if (!f) die("cannot open file");
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    char *buf = xmalloc(sz + 1);
    if (fread(buf, 1, sz, f) != (size_t)sz) die("read error");
    fclose(f);
    buf[sz] = 0;
    Txt t = { buf, 0, (size_t)sz, 1 };
    Section *root = xmalloc(sizeof(Section));
    root->name = xstrdup("");
    root->items = t_items(&t, &root->nitems);
    root->subs = t_sections(&t, &root->nsubs);
    t_skip_ws(&t);
    if (!t_eof(&t)) die("unexpected trailing content");
    free(buf);
    return root;
}

/* ---------------- binary writer ---------------- */

typedef struct { char **list; int n, cap; } StrTab;

static uint16_t st_get(StrTab *st, const char *s)
{
    for (int i = 0; i < st->n; i++)
        if (strcmp(st->list[i], s) == 0) return (uint16_t)i;
    if (st->n >= 65535) die("too many strings");
    if (st->n == st->cap) { st->cap = st->cap ? st->cap * 2 : 64; st->list = xrealloc(st->list, st->cap * sizeof(char *)); }
    st->list[st->n++] = xstrdup(s);
    return (uint16_t)(st->n - 1);
}

typedef struct { unsigned char *data; size_t len, cap; } Buf;

static void buf_put(Buf *b, const void *p, size_t n)
{
    if (b->len + n > b->cap)
    {
        b->cap = (b->len + n) * 2 + 64;
        b->data = xrealloc(b->data, b->cap);
    }
    memcpy(b->data + b->len, p, n);
    b->len += n;
}

static void buf_u8(Buf *b, uint8_t v) { buf_put(b, &v, 1); }
static void buf_u16(Buf *b, uint16_t v) { buf_put(b, &v, 2); }
static void buf_u32(Buf *b, uint32_t v) { buf_put(b, &v, 4); }
static void buf_f32(Buf *b, float f) { uint32_t v; memcpy(&v, &f, 4); buf_u32(b, v); }

static void w_items(Buf *b, const Item *items, int n, StrTab *st)
{
    for (int i = 0; i < n; i++)
    {
        buf_u16(b, st_get(st, items[i].key));
        buf_u8(b, (uint8_t)items[i].nvals);
        for (int j = 0; j < items[i].nvals; j++)
        {
            buf_u8(b, items[i].vals[j].type);
            if (items[i].vals[j].type == 0)
                buf_u16(b, st_get(st, items[i].vals[j].str));
            else
                buf_f32(b, items[i].vals[j].flt);
        }
    }
}

static void w_sections(Buf *b, Section *const *secs, int n, StrTab *st)
{
    for (int i = 0; i < n; i++)
    {
        buf_u16(b, st_get(st, secs[i]->name));
        buf_u32(b, (uint32_t)secs[i]->nitems);
        buf_u32(b, (uint32_t)secs[i]->nsubs);
        w_items(b, secs[i]->items, secs[i]->nitems, st);
        w_sections(b, secs[i]->subs, secs[i]->nsubs, st);
    }
}

static Buf save_binary(Section *root)
{
    Buf b = { 0, 0, 0 };
    StrTab st = { 0, 0, 0 };
    buf_u32(&b, MAGIC);
    buf_u32(&b, 0);
    buf_u32(&b, (uint32_t)root->nitems);
    buf_u32(&b, (uint32_t)root->nsubs);
    w_items(&b, root->items, root->nitems, &st);
    w_sections(&b, root->subs, root->nsubs, &st);
    uint32_t dataSize = (uint32_t)b.len;
    buf_u32(&b, (uint32_t)st.n);
    for (int i = 0; i < st.n; i++)
    {
        buf_put(&b, st.list[i], strlen(st.list[i]) + 1);
        free(st.list[i]);
    }
    free(st.list);
    memcpy(b.data + 4, &dataSize, 4);
    return b;
}

/* ---------------- commands ---------------- */

static void cmd_decode(const wchar_t *inp, const wchar_t *outp)
{
    char mb[4096];
    wcstombs(mb, inp, sizeof(mb));
    Section *root = load_binary(mb);
    FILE *f = outp ? _wfopen(outp, L"wb") : stdout;
    if (!f) die("cannot open output");
    write_items(f, root->items, root->nitems, 0);
    write_sections(f, root->subs, root->nsubs, 0);
    if (outp)
    {
        fclose(f);
        printf("%sdecoded%s %ls\n", CC(C_GREEN), CC(C_RESET), outp);
    }
    free_root(root);
}

static void cmd_encode(const wchar_t *inp, const wchar_t *outp)
{
    char mb[4096];
    wcstombs(mb, inp, sizeof(mb));
    Section *root = load_text(mb);
    Buf b = save_binary(root);
    const wchar_t *o = outp ? outp : inp;
    FILE *f = _wfopen(o, L"wb");
    if (!f) die("cannot open output");
    fwrite(b.data, 1, b.len, f);
    fclose(f);
    printf("%sencoded%s %ls (%zu bytes)\n", CC(C_GREEN), CC(C_RESET), o, b.len);
    free(b.data);
    free_root(root);
}

static void count_tree(Section *s, int *items, int *secs)
{
    *items += s->nitems;
    for (int i = 0; i < s->nsubs; i++)
    {
        (*secs)++;
        count_tree(s->subs[i], items, secs);
    }
}

static void cmd_check(const wchar_t *inp)
{
    char mb[4096];
    wcstombs(mb, inp, sizeof(mb));
    FILE *f = fopen(mb, "rb");
    if (!f) die("cannot open file");
    unsigned char head[4];
    size_t rd = fread(head, 1, 4, f);
    fclose(f);
    if (rd < 4) die("file too small");
    uint32_t magic;
    memcpy(&magic, head, 4);
    Section *root = magic == MAGIC ? load_binary(mb) : load_text(mb);
    int items = 0, secs = 0;
    count_tree(root, &items, &secs);
    printf("%s  sections=%d items=%d\n", magic == MAGIC ? "binary" : "text", secs, items);
    free_root(root);
}

static int has_inf_ext(const wchar_t *name)
{
    size_t n = wcslen(name);
    return n > 4 && (name[n - 4] == L'.') &&
           (name[n - 3] == L'i' || name[n - 3] == L'I') &&
           (name[n - 2] == L'n' || name[n - 2] == L'N') &&
           (name[n - 1] == L'f' || name[n - 1] == L'F');
}

static int cmd_decode_file(const wchar_t *inp, const wchar_t *outp)
{
    FILE *hdr = _wfopen(inp, L"rb");
    if (!hdr) die("cannot open input");
    unsigned char head[4];
    size_t rd = fread(head, 1, 4, hdr);
    fclose(hdr);
    if (rd < 4) return 0;
    uint32_t magic;
    memcpy(&magic, head, 4);
    if (magic != MAGIC) return 0;
    char mb[4096];
    wcstombs(mb, inp, sizeof(mb));
    Section *root = load_binary(mb);
    FILE *f = _wfopen(outp, L"wb");
    if (!f) die("cannot open output");
    write_items(f, root->items, root->nitems, 0);
    write_sections(f, root->subs, root->nsubs, 0);
    fclose(f);
    free_root(root);
    return 1;
}

static void cmd_decodeall_rec(const wchar_t *indir, const wchar_t *outdir, int *ok, int *fail, int *skip)
{
    wchar_t pattern[MAX_PATH * 2];
    _snwprintf(pattern, _countof(pattern), L"%ls\\*", indir);
    WIN32_FIND_DATAW fd;
    HANDLE h = FindFirstFileW(pattern, &fd);
    if (h == INVALID_HANDLE_VALUE) return;
    do
    {
        if (wcscmp(fd.cFileName, L".") == 0 || wcscmp(fd.cFileName, L"..") == 0)
            continue;
        wchar_t inpath[MAX_PATH * 2], outpath[MAX_PATH * 2];
        _snwprintf(inpath, _countof(inpath), L"%ls\\%ls", indir, fd.cFileName);
        _snwprintf(outpath, _countof(outpath), L"%ls\\%ls", outdir, fd.cFileName);
        if (fd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
        {
            CreateDirectoryW(outpath, 0);
            cmd_decodeall_rec(inpath, outpath, ok, fail, skip);
        }
        else if (has_inf_ext(fd.cFileName))
        {
            int r = cmd_decode_file(inpath, outpath);
            if (r == 1) (*ok)++;
            else if (r == 0) (*skip)++;
            else (*fail)++;
        }
    } while (FindNextFileW(h, &fd));
    FindClose(h);
}

static void cmd_decodeall(const wchar_t *indir, const wchar_t *outdir)
{
    CreateDirectoryW(outdir, 0);
    int ok = 0, fail = 0, skip = 0;
    cmd_decodeall_rec(indir, outdir, &ok, &fail, &skip);
    printf("%sdecoded%s %d files", CC(C_GREEN), CC(C_RESET), ok);
    if (skip) printf(", %d skipped (not binary)", skip);
    if (fail) printf(", %d failed", fail);
    printf("\n");
}

static void cmd_roundtrip(const wchar_t *inp)
{
    char mb[4096];
    wcstombs(mb, inp, sizeof(mb));
    Section *root = load_binary(mb);
    Buf b = save_binary(root);
    FILE *f = fopen(mb, "rb");
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    unsigned char *orig = xmalloc(sz);
    fread(orig, 1, sz, f);
    fclose(f);
    int same = (sz == (long)b.len) && memcmp(orig, b.data, sz) == 0;
    printf("%s  orig=%ld re-encoded=%zu\n", same ? "IDENTICAL" : "DIFF", sz, b.len);
    free(orig);
    free(b.data);
    free_root(root);
}

static void banner(void)
{
    printf("%s%s  Haegemonia InfTool%s\n", CC(C_BOLD), CC(C_CYAN), CC(C_RESET));
    printf("%s  binary <-> text inf converter\n\n%s", CC(C_CYAN), CC(C_RESET));
}

static void usage(void)
{
    printf("%scommands:%s\n", CC(C_BOLD), CC(C_RESET));
    printf("  %sdecode%s     <file.inf> [out.inf]   binary -> text\n", CC(C_GREEN), CC(C_RESET));
    printf("  %sencode%s     <file.inf> [out.inf]   text -> binary (in place if no out)\n", CC(C_GREEN), CC(C_RESET));
    printf("  %scheck%s      <file.inf>             parse and show summary\n", CC(C_GREEN), CC(C_RESET));
    printf("  %sdecodeall%s  <indir> <outdir>         decode a directory tree\n", CC(C_GREEN), CC(C_RESET));
    printf("  %sroundtrip%s  <file.inf>              decode + re-encode byte-compare\n", CC(C_GREEN), CC(C_RESET));
    printf("  %shelp%s                              this screen\n", CC(C_GREEN), CC(C_RESET));
    printf("  %sexit%s                              quit interactive mode\n", CC(C_GREEN), CC(C_RESET));
    printf("\n%stip:%s running without arguments opens interactive mode\n", CC(C_YELLOW), CC(C_RESET));
}

static int tokenize(wchar_t *line, wchar_t **toks, int maxtoks)
{
    int n = 0;
    wchar_t *p = line;
    while (*p && n < maxtoks)
    {
        while (*p == L' ' || *p == L'\t' || *p == L'\r') p++;
        if (!*p || *p == L'\n') break;
        if (*p == L'"')
        {
            p++;
            wchar_t *start = p;
            while (*p && *p != L'"' && *p != L'\n') p++;
            if (*p) *p++ = 0;
            toks[n++] = start;
        }
        else
        {
            wchar_t *start = p;
            while (*p && *p != L' ' && *p != L'\t' && *p != L'\r' && *p != L'\n') p++;
            if (*p) *p++ = 0;
            toks[n++] = start;
        }
    }
    return n;
}

static void run_command(const wchar_t *cmd, int n, wchar_t **args)
{
    if (wcscmp(cmd, L"decode") == 0)
    {
        if (n < 1) die("usage: decode <file.inf> [out.inf]");
        cmd_decode(args[0], n >= 2 ? args[1] : 0);
    }
    else if (wcscmp(cmd, L"encode") == 0)
    {
        if (n < 1) die("usage: encode <file.inf> [out.inf]");
        cmd_encode(args[0], n >= 2 ? args[1] : 0);
    }
    else if (wcscmp(cmd, L"check") == 0)
    {
        if (n < 1) die("usage: check <file.inf>");
        cmd_check(args[0]);
    }
    else if (wcscmp(cmd, L"decodeall") == 0)
    {
        if (n < 2) die("usage: decodeall <indir> <outdir>");
        cmd_decodeall(args[0], args[1]);
    }
    else if (wcscmp(cmd, L"roundtrip") == 0)
    {
        if (n < 1) die("usage: roundtrip <file.inf>");
        cmd_roundtrip(args[0]);
    }
    else
        die("unknown command, type 'help' for the command list");
}

static void interactive(void)
{
    g_interactive = 1;
    printf("%stype a command, or 'exit' to quit\n\n%s", CC(C_YELLOW), CC(C_RESET));
    for (;;)
    {
        printf("%sinf> %s", CC(C_CYAN), CC(C_RESET));
        fflush(stdout);
        wchar_t line[4096];
        if (!fgetws(line, 4096, stdin))
        {
            printf("\n");
            break;
        }
        wchar_t *toks[32];
        int n = tokenize(line, toks, 32);
        if (n == 0) continue;
        if (wcscmp(toks[0], L"exit") == 0 || wcscmp(toks[0], L"quit") == 0) break;
        if (wcscmp(toks[0], L"help") == 0 || wcscmp(toks[0], L"?") == 0)
        {
            usage();
            continue;
        }
        if (setjmp(g_jmp) == 0)
            run_command(toks[0], n - 1, toks + 1);
        else
            printf("%serror: %s\n%s", CC(C_RED), g_errmsg, CC(C_RESET));
        printf("\n");
    }
    printf("bye\n");
}

int wmain(int argc, wchar_t **argv)
{
    setlocale(LC_NUMERIC, "C");
    enable_vt();
    if (argc < 2)
    {
        banner();
        usage();
        interactive();
        return 0;
    }
    if (wcscmp(argv[1], L"help") == 0 || wcscmp(argv[1], L"--help") == 0 || wcscmp(argv[1], L"-h") == 0)
    {
        banner();
        usage();
        return 0;
    }
    run_command(argv[1], argc - 2, argv + 2);
    return 0;
}
