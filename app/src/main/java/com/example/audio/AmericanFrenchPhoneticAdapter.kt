package com.example.audio

import java.util.Locale

/**
 * Phonetic adapter designed specifically for American learners of Beginning French.
 * It analyzes speech recognition candidates and normalizes common American accent
 * phonetic transcriptions into accurate French.
 */
object AmericanFrenchPhoneticAdapter {

    // Common phonetic substitutions made by speech recognizers when listening to American-accented French
    private val phoneticReplacements = listOf(
        // Greetings & Basics
        Regex("(?i)\\b(bone\\s*jour|bon\\s*jure|banjo|bone\\s*jewel)\\b") to "bonjour",
        Regex("(?i)\\b(bone\\s*soir|bon\\s*swar)\\b") to "bonsoir",
        Regex("(?i)\\b(sa\\s*loo|saloot|sa\\s*lut)\\b") to "salut",
        Regex("(?i)\\b(comment\\s*tally\\s*voo|commen\\s*tallez\\s*vous|comon\\s*tallez\\s*vous)\\b") to "comment allez-vous",
        Regex("(?i)\\b(comment\\s*va\\s*two|comment\\s*vas\\s*two|comon\\s*va\\s*tu)\\b") to "comment vas-tu",
        Regex("(?i)\\b(sah\\s*vah|sa\\s*va\\s*\\?)\\b") to "ça va",
        Regex("(?i)\\b(oh\\s*revoir|o\\s*revoir|or\\s*voir|all\\s*revoir|orevoir)\\b") to "au revoir",
        Regex("(?i)\\b(ah\\s*biento|a\\s*bientoe|ah\\s*bee\\s*en\\s*toe)\\b") to "à bientôt",
        Regex("(?i)\\b(mare\\s*see|mercy|mair\\s*see)\\b") to "merci",
        Regex("(?i)\\b(mare\\s*see\\s*boh\\s*coo|merci\\s*bo\\s*coo|merci\\s*beau\\s*coup)\\b") to "merci beaucoup",
        Regex("(?i)\\b(sil\\s*voo\\s*play|s'il\\s*vous\\s*plate|see\\s*voo\\s*play|s'il\\s*voo\\s*play)\\b") to "s'il vous plaît",
        Regex("(?i)\\b(sil\\s*tuh\\s*play|seel\\s*tuh\\s*play)\\b") to "s'il te plaît",
        Regex("(?i)\\b(de\\s*ree\\s*en|duh\\s*ree\\s*en)\\b") to "de rien",
        Regex("(?i)\\b(wee)\\b") to "oui",
        Regex("(?i)\\b(noh|nohng)\\b") to "non",

        // Pronouns & Verb Être / Avoir
        Regex("(?i)\\b(juh\\s*swee|je\\s*sweet|je\\s*sue|zhuh\\s*swee)\\b") to "je suis",
        Regex("(?i)\\b(two\\s*ay|tu\\s*eh|to\\s*ay)\\b") to "tu es",
        Regex("(?i)\\b(eel\\s*ay|il\\s*eh)\\b") to "il est",
        Regex("(?i)\\b(el\\s*ay|elle\\s*eh)\\b") to "elle est",
        Regex("(?i)\\b(noo\\s*som|noo\\s*sommes|nous\\s*some)\\b") to "nous sommes",
        Regex("(?i)\\b(voo\\s*zet|vooz\\s*et|voo\\s*zette)\\b") to "vous êtes",
        Regex("(?i)\\b(eel\\s*son|ils\\s*song|il\\s*son)\\b") to "ils sont",
        Regex("(?i)\\b(el\\s*son|elles\\s*song|elle\\s*son)\\b") to "elles sont",
        Regex("(?i)\\b(zhay|j'eh|jay)\\b") to "j'ai",
        Regex("(?i)\\b(two\\s*ah|tu\\s*ah)\\b") to "tu as",
        Regex("(?i)\\b(eel\\s*ah|il\\s*ah)\\b") to "il a",
        Regex("(?i)\\b(el\\s*ah|elle\\s*ah)\\b") to "elle a",

        // Polite requests & Café
        Regex("(?i)\\b(zhuh\\s*voodray|je\\s*voudre|je\\s*voudra|je\\s*voodray)\\b") to "je voudrais",
        Regex("(?i)\\b(oon\\s*caff\\s*ay|un\\s*cafee|un\\s*kafe|un\\s*caff\\s*ay)\\b") to "un café",
        Regex("(?i)\\b(oon\\s*caff\\s*ay\\s*olay|un\\s*café\\s*au\\s*lay|un\\s*cafe\\s*o\\s*lay)\\b") to "un café au lait",
        Regex("(?i)\\b(oon\\s*crois\\s*sant|un\\s*croy\\s*sant|un\\s*kwa\\s*son)\\b") to "un croissant",
        Regex("(?i)\\b(duh\\s*lo|de\\s*lo|do\\s*low)\\b") to "de l'eau",
        Regex("(?i)\\b(l'\\s*adiseen|la\\s*diseen|l'\\s*addition)\\b") to "l'addition",

        // Question Words & Structures
        Regex("(?i)\\b(keskuh\\s*say|kess\\s*kuh\\s*say|kes\\s*kuh\\s*say|qu'\\s*est\\s*ce\\s*que\\s*c'\\s*est)\\b") to "qu'est-ce que c'est",
        Regex("(?i)\\b(keskuh\\s*two|kess\\s*kuh\\s*tu|qu'\\s*est\\s*ce\\s*que\\s*tu)\\b") to "qu'est-ce que tu",
        Regex("(?i)\\b(pour\\s*kwa|poor\\s*kwa|pour\\s*quoi)\\b") to "pourquoi",
        Regex("(?i)\\b(par\\s*skuh|parce\\s*kuh|parce\\s*que)\\b") to "parce que",
        Regex("(?i)\\b(ooh\\s*ay|oo\\s*eh|oo\\s*ay)\\b") to "où est",
        Regex("(?i)\\b(ooh\\s*sont|oo\\s*son)\\b") to "où sont",
        Regex("(?i)\\b(kan\\s*ay|kahn\\s*eh)\\b") to "quand est",
        Regex("(?i)\\b(kee\\s*ay|key\\s*ay)\\b") to "qui est",
        Regex("(?i)\\b(kombien|comb\\s*ee\\s*en)\\b") to "combien",

        // Common Verbs: Aller, Faire, Parler, Aimer
        Regex("(?i)\\b(zhuh\\s*vay|je\\s*vay|je\\s*veh)\\b") to "je vais",
        Regex("(?i)\\b(two\\s*vah|tu\\s*vah)\\b") to "tu vas",
        Regex("(?i)\\b(eel\\s*vah|il\\s*vah)\\b") to "il va",
        Regex("(?i)\\b(el\\s*vah|elle\\s*vah)\\b") to "elle va",
        Regex("(?i)\\b(noo\\s*zal\\s*on|nous\\s*allon)\\b") to "nous allons",
        Regex("(?i)\\b(voo\\s*zal\\s*ay|vous\\s*allay)\\b") to "vous allez",
        Regex("(?i)\\b(eel\\s*vont|ils\\s*von)\\b") to "ils vont",
        Regex("(?i)\\b(zhuh\\s*fay|je\\s*fay|je\\s*feh)\\b") to "je fais",
        Regex("(?i)\\b(two\\s*fay|tu\\s*fay)\\b") to "tu fais",
        Regex("(?i)\\b(eel\\s*fay|il\\s*fay)\\b") to "il fait",
        Regex("(?i)\\b(zhaym|j'\\s*aim|zhem)\\b") to "j'aime",
        Regex("(?i)\\b(zhuh\\s*parl|je\\s*parle)\\b") to "je parle",
        Regex("(?i)\\b(two\\s*parl|tu\\s*parle)\\b") to "tu parles",

        // Numbers 1-10
        Regex("(?i)\\b(uhn|ahn)\\b") to "un",
        Regex("(?i)\\b(duh)\\b") to "deux",
        Regex("(?i)\\b(twah|trwah)\\b") to "trois",
        Regex("(?i)\\b(kat|kahtr|kahtruh)\\b") to "quatre",
        Regex("(?i)\\b(sank|sahnk)\\b") to "cinq",
        Regex("(?i)\\b(sees)\\b") to "six",
        Regex("(?i)\\b(set)\\b") to "sept",
        Regex("(?i)\\b(weet)\\b") to "huit",
        Regex("(?i)\\b(nuf|nerf)\\b") to "neuf",
        Regex("(?i)\\b(dees)\\b") to "dix",

        // Family & Descriptions
        Regex("(?i)\\b(mon\\s*pair|mon\\s*pare)\\b") to "mon père",
        Regex("(?i)\\b(mah\\s*mair|ma\\s*mare)\\b") to "ma mère",
        Regex("(?i)\\b(mon\\s*frair|mon\\s*fraire)\\b") to "mon frère",
        Regex("(?i)\\b(mah\\s*ser|ma\\s*soor)\\b") to "ma sœur",
        Regex("(?i)\\b(poteet|p'\\s*teet)\\b") to "petit",
        Regex("(?i)\\b(gron|grahn)\\b") to "grand",

        // Campus & Places (from Textbook pp. 28-33)
        Regex("(?i)\\b(la\\s*biblio\\s*tek|la\\s*bibliotech|la\\s*bibliotheque)\\b") to "la bibliothèque",
        Regex("(?i)\\b(la\\s*leeb\\s*rare\\s*ee|la\\s*librairie)\\b") to "la librairie",
        Regex("(?i)\\b(la\\s*salle\\s*duh\\s*class|la\\s*sal\\s*de\\s*class)\\b") to "la salle de classe",
        Regex("(?i)\\b(la\\s*say\\s*tay\\s*oo|la\\s*cite\\s*u|la\\s*cité\\s*u)\\b") to "la cité-U",
        Regex("(?i)\\b(luh\\s*resto\\s*oo|le\\s*resto\\s*u)\\b") to "le resto-U",
        Regex("(?i)\\b(l'\\s*amphi|lamphi)\\b") to "l'amphithéâtre",
        Regex("(?i)\\b(l'\\s*universitey|l'\\s*universite|luniversite)\\b") to "l'université",
        Regex("(?i)\\b(l'\\s*informateek|l'\\s*informatique)\\b") to "l'informatique",

        // Introductions
        Regex("(?i)\\b(zhuh\\s*mah\\s*pel|je\\s*mapel|je\\s*m'apel)\\b") to "je m'appelle",
        Regex("(?i)\\b(comment\\s*vooz\\s*ah\\s*play\\s*voo|comment\\s*vous\\s*appelez\\s*vous)\\b") to "comment vous appelez-vous",
        Regex("(?i)\\b(comment\\s*two\\s*tah\\s*pel|comment\\s*tu\\s*t'apelle)\\b") to "comment tu t'appelles",
        Regex("(?i)\\b(j'\\s*ah\\s*beet|j'\\s*habeet|jabite|j'\\s*abite)\\b") to "j'habite",
        Regex("(?i)\\b(ah\\s*paris)\\b") to "à Paris",
        Regex("(?i)\\b(ah\\s*montreal)\\b") to "à Montréal",
        Regex("(?i)\\b(ah\\s*geneve)\\b") to "à Genève"
    )

    /**
     * Inspects a list of SpeechRecognizer hypothesis candidates and selects or adapts
     * the best French match for an American speaker.
     */
    fun resolveBestFrenchMatch(candidates: List<String>): String {
        if (candidates.isEmpty()) return ""

        // 1. Check if any candidate directly matches standard French words
        val bestScored = candidates.maxByOrNull { scoreFrenchCharacteristics(it) }
        val primary = bestScored ?: candidates.first()

        // 2. Apply American phonetic normalization to the best candidate
        return normalizeAmericanFrench(primary)
    }

    /**
     * Normalizes American learner speech patterns and recognition phonetic misinterpretations.
     */
    fun normalizeAmericanFrench(text: String): String {
        var normalized = text.trim()

        for ((regex, replacement) in phoneticReplacements) {
            normalized = regex.replace(normalized, replacement)
        }

        // Capitalize first letter cleanly
        return normalized.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
    }

    /**
     * Scores how "French" a candidate string is based on French functional words,
     * accents, and vocabulary common in French I.
     */
    private fun scoreFrenchCharacteristics(text: String): Int {
        val lower = text.lowercase(Locale.ROOT)
        var score = 0

        // Common French functional markers
        val frenchTokens = setOf(
            "je", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles",
            "le", "la", "les", "un", "une", "des", "du", "de", "d'", "l'",
            "est", "suis", "es", "sommes", "êtes", "sont", "ai", "as", "a", "avons", "avez", "ont",
            "bonjour", "salut", "merci", "oui", "non", "s'il", "plaît", "vous",
            "très", "bien", "avec", "dans", "sur", "pour", "bibliothèque", "café",
            "étudiant", "américain", "français", "habite", "appelle", "matière"
        )

        for (token in lower.split("\\s+".toRegex())) {
            val cleaned = token.trim('.', ',', '?', '!')
            if (frenchTokens.contains(cleaned)) {
                score += 3
            }
        }

        // French accent presence bonus
        val frenchAccents = setOf('é', 'è', 'ê', 'à', 'â', 'ç', 'î', 'ï', 'ô', 'ù', 'û', 'œ', 'æ')
        for (ch in lower) {
            if (frenchAccents.contains(ch)) {
                score += 2
            }
        }

        return score
    }
}
