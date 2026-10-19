#ifndef TTCORE_H
#define TTCORE_H

#include <stdint.h>
#include <wchar.h>
#include <stddef.h>

typedef struct Value { unsigned char type; char *str; float flt; } Value;
typedef struct Item { char *key; Value *vals; int nvals; } Item;

typedef struct Section Section;
struct Section {
    char *name;
    Item *items;
    int nitems;
    Section **subs;
    int nsubs;
    Section *parent;
};

/* load a .inf (auto-detects binary magic 0xFFFFA5AD vs text) */
Section *tt_load_file(const wchar_t *path, char *err, size_t errsz);
int tt_save_text(const wchar_t *path, Section *root, char *err, size_t errsz);
int tt_save_binary(const wchar_t *path, Section *root, char *err, size_t errsz);
void tt_free(Section *root);

Section *tt_find_section(Section *root, const char *name);
Item *tt_find_item(Section *s, const char *key);
int tt_set_item(Section *s, const char *key, int type, const char *str, float flt);
Section *tt_add_section(Section *parent, const char *name);
void tt_remove_section(Section *parent, Section *child);
Section *tt_clone_section(const Section *src);
int tt_rename_section(Section *s, const char *name);

const char *tt_item_str(const Section *s, const char *key, const char *def);
float tt_item_float(const Section *s, const char *key, float def);
int tt_item_csv(const Section *s, const char *key, char *out, size_t n);
int tt_set_item_csv(Section *s, const char *key, const char *csv);
char *tt_fmt_float(float f);

#endif
