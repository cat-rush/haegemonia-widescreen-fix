#include "ttcore.h"
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const uint32_t MAGIC = 0xFFFFA5AD;

static char g_err[512];

static int fail(char *err, size_t errsz, const char *msg)
{
    if (err && errsz)
    {
        strncpy(err, msg, errsz - 1);
        err[errsz - 1] = 0;
    }
    return 0;
}

static void *xmalloc(size_t n)
{
    void *p = malloc(n ? n : 1);
    if (!p) fail(g_err, sizeof(g_err), "out of memory");
    return p;
}

static char *xstrdup(const char *s)
{
    char *p = xmalloc(strlen(s) + 1);
    if (p) strcpy(p, s);
    return p;
}

static void *xrealloc(void *p, size_t n)
{
    void *q = realloc(p, n);
    if (!q) fail(g_err, sizeof(g_err), "out of memory");
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

char *tt_fmt_float(float f)
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
        char *s = tt_fmt_float(v->flt);
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
    if (b->pos + n > b->len) { fail(g_err, sizeof(g_err), "truncated binary file"); return 0; }
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
    if (idx >= g_nstrings) { fail(g_err, sizeof(g_err), "string index out of range"); return ""; }
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

static void free_sections_rec(Section **secs, int n);

static Item *read_items(Bin *b, uint32_t count)
{
    Item *items = xmalloc((count ? count : 1) * sizeof(Item));
    if (!items) return 0;
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
            {
                fail(g_err, sizeof(g_err), "unknown value type in binary");
                free_items(items, count);
                return 0;
            }
        }
    }
    return items;
}

static Section **read_sections(Bin *b, uint32_t count, Section *parent)
{
    Section **secs = xmalloc((count ? count : 1) * sizeof(Section *));
    if (!secs) return 0;
    for (uint32_t i = 0; i < count; i++)
    {
        Section *s = xmalloc(sizeof(Section));
        secs[i] = s;
        s->name = xstrdup(get_str(b_u16(b)));
        s->parent = parent;
        uint32_t ni = b_u32(b);
        uint32_t ns = b_u32(b);
        s->items = read_items(b, ni);
        s->nitems = ni;
        s->subs = read_sections(b, ns, s);
        s->nsubs = ns;
        if (g_err[0]) return 0;
    }
    return secs;
}

static Section *load_binary(const wchar_t *path, char *err, size_t errsz)
{
    g_err[0] = 0;
    FILE *f = _wfopen(path, L"rb");
    if (!f) { fail(err, errsz, "cannot open file"); return 0; }
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    unsigned char *buf = xmalloc(sz + 1);
    if (fread(buf, 1, sz, f) != (size_t)sz) { fclose(f); fail(err, errsz, "read error"); free(buf); return 0; }
    fclose(f);
    Bin b = { buf, (size_t)sz, 0 };
    if (b_u32(&b) != MAGIC) { free(buf); fail(err, errsz, "not a binary inf (bad magic)"); return 0; }
    uint32_t dataSize = b_u32(&b);
    uint32_t rootItems = b_u32(&b);
    uint32_t rootSections = b_u32(&b);
    if (dataSize + 4 > (uint32_t)sz) { free(buf); fail(err, errsz, "corrupt header"); return 0; }
    uint32_t stringCount = *(uint32_t *)(buf + dataSize);
    g_nstrings = stringCount;
    g_strings = xmalloc((stringCount ? stringCount : 1) * sizeof(char *));
    size_t sp = dataSize + 4;
    for (uint32_t i = 0; i < stringCount; i++)
    {
        if (sp >= (size_t)sz) { free(buf); fail(err, errsz, "string table overrun"); return 0; }
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
    root->parent = 0;
    root->items = read_items(&b, rootItems);
    root->nitems = rootItems;
    root->subs = read_sections(&b, rootSections, root);
    root->nsubs = rootSections;
    for (int i = 0; i < (int)stringCount; i++) free(g_strings[i]);
    free(g_strings);
    free(buf);
    if (g_err[0]) { tt_free(root); fail(err, errsz, g_err); return 0; }
    return root;
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
    if (t_eof(t) || t_peek(t) != '[') { fail(g_err, sizeof(g_err), "expected '['"); return 0; }
    t->pos++;
    size_t start = t->pos;
    while (!t_eof(t) && t_peek(t) != ']' && t_peek(t) != '\n') t->pos++;
    if (t_eof(t) || t_peek(t) != ']') { fail(g_err, sizeof(g_err), "unterminated section name"); return 0; }
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
            if (t_eof(t)) { fail(g_err, sizeof(g_err), "unterminated string"); return 0; }
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
        if (t_eof(t) || t_peek(t) != '=') { fail(g_err, sizeof(g_err), "expected '=' after key"); free(key); return 0; }
        t->pos++;
        if (n == cap) { cap = cap ? cap * 2 : 8; items = xrealloc(items, cap * sizeof(Item)); }
        items[n].key = key;
        items[n].vals = t_values(t, &items[n].nvals);
        if (g_err[0]) return 0;
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

static Section **t_sections(Txt *t, int *count, Section *parent)
{
    Section **secs = 0;
    int n = 0, cap = 0;
    for (;;)
    {
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) == '}') break;
        char *name = t_section_name(t);
        if (!name) return 0;
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) != '{') { fail(g_err, sizeof(g_err), "expected '{' after section name"); free(name); return 0; }
        t->pos++;
        if (n == cap) { cap = cap ? cap * 2 : 8; secs = xrealloc(secs, cap * sizeof(Section *)); }
        Section *s = xmalloc(sizeof(Section));
        secs[n] = s;
        s->name = name;
        s->parent = parent;
        s->items = t_items(t, &s->nitems);
        if (g_err[0]) return 0;
        s->subs = t_sections(t, &s->nsubs, s);
        if (g_err[0]) return 0;
        t_skip_ws(t);
        if (t_eof(t) || t_peek(t) != '}') { fail(g_err, sizeof(g_err), "missing '}'"); return 0; }
        t->pos++;
        n++;
    }
    *count = n;
    return secs;
}

static Section *load_text(const wchar_t *path, char *err, size_t errsz)
{
    g_err[0] = 0;
    FILE *f = _wfopen(path, L"rb");
    if (!f) { fail(err, errsz, "cannot open file"); return 0; }
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    char *buf = xmalloc(sz + 1);
    if (fread(buf, 1, sz, f) != (size_t)sz) { fclose(f); fail(err, errsz, "read error"); free(buf); return 0; }
    fclose(f);
    buf[sz] = 0;
    Txt t = { buf, 0, (size_t)sz, 1 };
    Section *root = xmalloc(sizeof(Section));
    root->name = xstrdup("");
    root->parent = 0;
    root->items = t_items(&t, &root->nitems);
    if (g_err[0]) { free(buf); tt_free(root); fail(err, errsz, g_err); return 0; }
    root->subs = t_sections(&t, &root->nsubs, root);
    if (g_err[0]) { free(buf); tt_free(root); fail(err, errsz, g_err); return 0; }
    t_skip_ws(&t);
    if (!t_eof(&t)) { free(buf); tt_free(root); fail(err, errsz, "unexpected trailing content"); return 0; }
    free(buf);
    return root;
}

/* ---------------- binary writer ---------------- */

typedef struct { char **list; int n, cap; } StrTab;

static uint16_t st_get(StrTab *st, const char *s)
{
    for (int i = 0; i < st->n; i++)
        if (strcmp(st->list[i], s) == 0) return (uint16_t)i;
    if (st->n >= 65535) { fail(g_err, sizeof(g_err), "too many strings"); return 0; }
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

/* ---------------- public API ---------------- */

Section *tt_load_file(const wchar_t *path, char *err, size_t errsz)
{
    g_err[0] = 0;
    FILE *f = _wfopen(path, L"rb");
    if (!f) { fail(err, errsz, "cannot open file"); return 0; }
    unsigned char head[4];
    size_t rd = fread(head, 1, 4, f);
    fclose(f);
    uint32_t magic = 0;
    if (rd >= 4) memcpy(&magic, head, 4);
    if (magic == MAGIC) return load_binary(path, err, errsz);
    return load_text(path, err, errsz);
}

int tt_save_text(const wchar_t *path, Section *root, char *err, size_t errsz)
{
    g_err[0] = 0;
    FILE *f = _wfopen(path, L"wb");
    if (!f) { fail(err, errsz, "cannot open output"); return 0; }
    write_items(f, root->items, root->nitems, 0);
    write_sections(f, root->subs, root->nsubs, 0);
    fclose(f);
    return 1;
}

int tt_save_binary(const wchar_t *path, Section *root, char *err, size_t errsz)
{
    g_err[0] = 0;
    Buf b = save_binary(root);
    if (g_err[0]) { fail(err, errsz, g_err); free(b.data); return 0; }
    FILE *f = _wfopen(path, L"wb");
    if (!f) { fail(err, errsz, "cannot open output"); free(b.data); return 0; }
    fwrite(b.data, 1, b.len, f);
    fclose(f);
    free(b.data);
    return 1;
}

static void free_sections_rec(Section **secs, int n)
{
    for (int i = 0; i < n; i++)
    {
        free(secs[i]->name);
        free_items(secs[i]->items, secs[i]->nitems);
        free_sections_rec(secs[i]->subs, secs[i]->nsubs);
        free(secs[i]->subs);
        free(secs[i]);
    }
}

void tt_free(Section *root)
{
    if (!root) return;
    free_items(root->items, root->nitems);
    free_sections_rec(root->subs, root->nsubs);
    free(root->subs);
    free(root->name);
    free(root);
}

Section *tt_find_section(Section *root, const char *name)
{
    if (root && strcmp(root->name, name) == 0) return root;
    if (!root) return 0;
    for (int i = 0; i < root->nsubs; i++)
    {
        Section *f = tt_find_section(root->subs[i], name);
        if (f) return f;
    }
    return 0;
}

Item *tt_find_item(Section *s, const char *key)
{
    if (!s) return 0;
    for (int i = 0; i < s->nitems; i++)
        if (strcmp(s->items[i].key, key) == 0) return &s->items[i];
    return 0;
}

const char *tt_item_str(const Section *s, const char *key, const char *def)
{
    Item *it = tt_find_item((Section *)s, key);
    if (!it || it->nvals < 1) return def;
    if (it->vals[0].type == 0) return it->vals[0].str;
    return def;
}

float tt_item_float(const Section *s, const char *key, float def)
{
    Item *it = tt_find_item((Section *)s, key);
    if (!it || it->nvals < 1) return def;
    if (it->vals[0].type == 1) return it->vals[0].flt;
    return (float)atof(it->vals[0].str);
}

int tt_item_csv(const Section *s, const char *key, char *out, size_t n)
{
    out[0] = 0;
    Item *it = tt_find_item((Section *)s, key);
    if (!it) return 0;
    size_t used = 0;
    for (int j = 0; j < it->nvals; j++)
    {
        const char *part;
        char *tmp = 0;
        if (it->vals[j].type == 0)
            part = it->vals[j].str;
        else
        {
            tmp = tt_fmt_float(it->vals[j].flt);
            part = tmp;
        }
        size_t need = strlen(part) + 2;
        if (used + need < n)
        {
            if (j > 0) { out[used++] = ','; }
            strcpy(out + used, part);
            used += strlen(part);
        }
        free(tmp);
    }
    out[used] = 0;
    return 1;
}

int tt_set_item_csv(Section *s, const char *key, const char *csv)
{
    char work[2048];
    strncpy(work, csv ? csv : "", sizeof(work) - 1);
    work[sizeof(work) - 1] = 0;
    char *toks[64];
    int ntok = 0;
    char *p = work;
    while (*p && ntok < 64)
    {
        while (*p == ' ' || *p == '\t') p++;
        if (!*p) break;
        char *start = p;
        while (*p && *p != ',') p++;
        if (*p) *p++ = 0;
        while (*(p) == ' ' || *p == '\t') p++;
        toks[ntok++] = start;
    }
    if (ntok == 0) return 0;

    Item *it = tt_find_item(s, key);
    if (!it)
    {
        s->items = xrealloc(s->items, (s->nitems + 1) * sizeof(Item));
        it = &s->items[s->nitems];
        s->nitems++;
        it->key = xstrdup(key);
        it->vals = 0;
        it->nvals = 0;
    }
    else
    {
        for (int j = 0; j < it->nvals; j++)
            if (it->vals[j].type == 0) free(it->vals[j].str);
        free(it->vals);
    }
    it->nvals = ntok;
    it->vals = xmalloc(ntok * sizeof(Value));
    for (int j = 0; j < ntok; j++)
    {
        char *end;
        float f = strtof(toks[j], &end);
        if (*end == 0 && strpbrk(toks[j], "0123456789") != 0)
        {
            it->vals[j].type = 1;
            it->vals[j].str = 0;
            it->vals[j].flt = f;
        }
        else
        {
            it->vals[j].type = 0;
            it->vals[j].str = xstrdup(toks[j]);
            it->vals[j].flt = 0;
        }
    }
    return 1;
}

int tt_set_item(Section *s, const char *key, int type, const char *str, float flt)
{
    if (type == 0)
        return tt_set_item_csv(s, key, str);
    char buf[64];
    char *f = tt_fmt_float(flt);
    snprintf(buf, sizeof(buf), "%s", f);
    free(f);
    return tt_set_item_csv(s, key, buf);
}

Section *tt_add_section(Section *parent, const char *name)
{
    parent->subs = xrealloc(parent->subs, (parent->nsubs + 1) * sizeof(Section *));
    Section *s = xmalloc(sizeof(Section));
    s->name = xstrdup(name);
    s->items = 0;
    s->nitems = 0;
    s->subs = 0;
    s->nsubs = 0;
    s->parent = parent;
    parent->subs[parent->nsubs++] = s;
    return s;
}

void tt_remove_section(Section *parent, Section *child)
{
    int idx = -1;
    for (int i = 0; i < parent->nsubs; i++)
        if (parent->subs[i] == child) { idx = i; break; }
    if (idx < 0) return;
    free_sections_rec(&child, 1);
    for (int i = idx; i < parent->nsubs - 1; i++)
        parent->subs[i] = parent->subs[i + 1];
    parent->nsubs--;
}

Section *tt_clone_section(const Section *src)
{
    Section *s = xmalloc(sizeof(Section));
    s->name = xstrdup(src->name);
    s->parent = 0;
    s->nitems = src->nitems;
    s->items = xmalloc((s->nitems ? s->nitems : 1) * sizeof(Item));
    for (int i = 0; i < s->nitems; i++)
    {
        s->items[i].key = xstrdup(src->items[i].key);
        s->items[i].nvals = src->items[i].nvals;
        s->items[i].vals = xmalloc((s->items[i].nvals ? s->items[i].nvals : 1) * sizeof(Value));
        for (int j = 0; j < s->items[i].nvals; j++)
        {
            s->items[i].vals[j].type = src->items[i].vals[j].type;
            s->items[i].vals[j].flt = src->items[i].vals[j].flt;
            s->items[i].vals[j].str =
                src->items[i].vals[j].type == 0 ? xstrdup(src->items[i].vals[j].str) : 0;
        }
    }
    s->nsubs = 0;
    s->subs = 0;
    return s;
}

int tt_rename_section(Section *s, const char *name)
{
    if (!name || !*name) return 0;
    free(s->name);
    s->name = xstrdup(name);
    return 1;
}
