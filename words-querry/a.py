from xml.sax.saxutils import escape

from wordfreq import top_n_list

languages = [
    "en",
    "cs",
    "da",
    "de",
    "el",
    "es",
    "fi",
    "fr",
    "hr",
    "hu",
    "id",
    "it",
    "nb",
    "nl",
    "pl",
    "pt",
    "ro",
    "ru",
    "sk",
    "sv",
    "tr",
    "uk",
]

res = {}
levels_num = 5
words_num = 30000

for l in languages:
    lang_words = {}

    ws = top_n_list(l, words_num)

    curr_lvl = 0
    lang_words[curr_lvl] = []
    c = 0
    for w in ws:
        if c > ((curr_lvl + 1) * (words_num / levels_num)):
            curr_lvl += 1
            lang_words[curr_lvl] = []
        lang_words[curr_lvl].append(w)
        c += 1
    res[l] = lang_words


def toXML(ws: list[str], name: str) -> str:
    return escape(
        f'\t<string-array name="{name}" tools:ignore="InconsistentArrays">\n\t\t<item>{"</item>\n\t\t<item>".join(ws)}</item>\n\t</string-array>\n'
    )


for l in res:
    ks = list(res[l].keys())
    ks.sort()
    with open(f"lang-{l}.xml", "w", encoding="utf-8") as f:
        f.writelines(toXML(res[l][diff], f"words{diff + 1}") for diff in ks)
