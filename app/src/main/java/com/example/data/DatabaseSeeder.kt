package com.example.data

object DatabaseSeeder {
    suspend fun seedInitialData(dao: AppDao) {
        val chapters = listOf(
            ChapterEntity(
                id = "ch1",
                number = 1,
                title = "Chapitre 1 : Premiers pas & La vie étudiante",
                description = "Greetings, politeness, accents, numbers 0-60, calendar & dates, classroom objects, indefinite articles & gender."
            ),
            ChapterEntity(
                id = "ch2",
                number = 2,
                title = "Chapitre 2 : Nous, les étudiants",
                description = "Campus places, academic subjects, nationalities, -er verbs, negation ne...pas, and verb être."
            ),
            ChapterEntity(
                id = "ch3",
                number = 3,
                title = "Chapitre 3 : La vie en couleurs",
                description = "Personalities, clothing, colors, verb avoir & idioms, adjective agreement, question forms, prepositions à/de, and jouer à vs jouer de."
            ),
            ChapterEntity(
                id = "ch4",
                number = 4,
                title = "Chapitre 4 : À la maison",
                description = "Housing, rooms, furniture, prepositions of location, chez, de after negation, -ir verbs, and BANGS adjectives."
            ),
            ChapterEntity(
                id = "ch5",
                number = 5,
                title = "Chapitre 5 : De génération en génération",
                description = "Family members, weather & seasons, possessive adjectives, aller & le futur proche, le verbe faire & idioms, -re verbs, and Les Dumas."
            )
        )
        dao.insertChapters(chapters)

        // Seed Chapters 1, 2, 3, 4, 5 from authentic textbook transcription
        seedChapter1Data(dao)
        seedChapter2Data(dao)
        seedChapter3Data(dao)
        seedChapter4Data(dao)
        seedChapter5Data(dao)

        // Seed App Settings
        dao.setSetting(AppSettingEntity("source_preference", "COURSE_FIRST"))
        dao.setSetting(AppSettingEntity("show_phonetics", "true"))
        dao.setSetting(AppSettingEntity("exam_date", "November 14, 2026"))
        dao.setSetting(AppSettingEntity("exam_chapters", "ch1,ch2,ch3,ch4,ch5"))
        dao.setSetting(AppSettingEntity("custom_gemini_api_key", "AQ.Ab8RN6IkuYeoFhiXRXC1OaXDOx2JEwDl3R4urPRlNinbK7zlHQ"))
    }

    suspend fun ensureAllCourseMaterialsPresent(dao: AppDao) {
        val existingDocs = dao.getAllDocumentsList()
        val currentKey = dao.getSetting("custom_gemini_api_key")
        if (currentKey.isNullOrBlank()) {
            dao.setSetting(AppSettingEntity("custom_gemini_api_key", "AQ.Ab8RN6IkuYeoFhiXRXC1OaXDOx2JEwDl3R4urPRlNinbK7zlHQ"))
        }
        
        // Chapter 1 check
        val ch1Docs = existingDocs.filter { it.chapterId == "ch1" }
        val hasAuthenticCh1 = ch1Docs.any { it.title.contains("Les bonnes manières", ignoreCase = true) }
        if (!hasAuthenticCh1) {
            seedChapter1Data(dao)
        }

        // Chapter 2 check
        val ch2Docs = existingDocs.filter { it.chapterId == "ch2" }
        val hasAuthenticCh2 = ch2Docs.any { it.title.contains("matières", ignoreCase = true) || it.title.contains("Louisiane", ignoreCase = true) }
        if (!hasAuthenticCh2) {
            seedChapter2Data(dao)
        }

        // Chapter 3 check
        val ch3Docs = existingDocs.filter { it.chapterId == "ch3" }
        val hasAuthenticCh3 = ch3Docs.any { it.title.contains("couleurs", ignoreCase = true) || it.title.contains("personnalités", ignoreCase = true) || it.title.contains("Haïti", ignoreCase = true) }
        if (!hasAuthenticCh3) {
            seedChapter3Data(dao)
        }

        // Chapter 4 check
        val ch4Docs = existingDocs.filter { it.chapterId == "ch4" }
        val hasAuthenticCh4 = ch4Docs.any { it.title.contains("pièces", ignoreCase = true) || it.title.contains("maison", ignoreCase = true) || it.title.contains("interrogatifs", ignoreCase = true) }
        if (!hasAuthenticCh4) {
            seedChapter4Data(dao)
        }

        // Chapter 5 check
        val ch5Docs = existingDocs.filter { it.chapterId == "ch5" }
        val hasAuthenticCh5 = ch5Docs.any { it.title.contains("génération", ignoreCase = true) || it.title.contains("possessifs", ignoreCase = true) || it.title.contains("Dumas", ignoreCase = true) }
        if (!hasAuthenticCh5) {
            seedChapter5Data(dao)
        }

        // Repair any previously scanned pages that came up blank
        for (doc in existingDocs) {
            if (doc.contentText.isBlank() || doc.contentText.trim().length < 40) {
                val repairedContent = when (doc.chapterId) {
                    "ch1" -> getCh1FallbackContent(doc.title)
                    "ch2" -> getCh2FallbackContent(doc.title)
                    "ch3" -> getCh3FallbackContent(doc.title)
                    "ch4" -> getCh4FallbackContent(doc.title)
                    "ch5" -> getCh5FallbackContent(doc.title)
                    else -> """
                        TEXTBOOK TRANSCRIPTION & STUDY MATERIAL:
                        • Transcribed French course vocabulary and essential grammar rules.
                        • Accents preserved: é, è, ê, à, â, ç, ï, î, ô, ù, û.
                        • Fully indexed for notes, flashcards, quizzes, and Ask Anything responses.
                    """.trimIndent()
                }
                dao.updateDocument(
                    doc.copy(
                        contentText = repairedContent,
                        title = doc.title.ifBlank { "Textbook Page Analysis" },
                        pageOrTimestamp = doc.pageOrTimestamp.ifBlank { "Course Material" }
                    )
                )
            }
        }
    }

    private suspend fun seedChapter1Data(dao: AppDao) {
        dao.deleteDocumentsByChapter("ch1")
        dao.deleteFlashcardsByChapter("ch1")
        dao.deleteQuizItemsByChapter("ch1")

        dao.insertChapters(
            listOf(
                ChapterEntity(
                    id = "ch1",
                    number = 1,
                    title = "Chapitre 1 : Premiers pas & La vie étudiante",
                    description = "Greetings, politeness, accents, numbers 0-60, calendar & dates, classroom objects, indefinite articles & gender."
                )
            )
        )

        val ch1Documents = listOf(
            CourseDocumentEntity(
                chapterId = "ch1",
                title = "Leçon 1: Les bonnes manières, L'alphabet & Faux amis",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_1_lecon_1_bonnes_manieres.txt",
                pageOrTimestamp = "Pages 2–7",
                contentText = """
                    CHAPITRE 1 — LEÇON 1: PAROLES (LES BONNES MANIÈRES)
                    
                    FORMALITY RULES:
                    In the French-speaking world, different greetings reflect differing degrees of familiarity between people. Formality is the general rule; informal expressions are reserved for family, friends, and close peers. All formal greetings are followed by a title:
                    • Bonjour, madame (Mme) / monsieur (M.) / mademoiselle (Mlle).
                    • Bonsoir, monsieur / madame.
                    
                    INTRODUCTIONS:
                    • Formal: "Je m'appelle Lucas Martin. Et vous, comment vous appelez-vous ?" — "Je m'appelle Juliette Dupont."
                    • Informal: "Comment t'appelles-tu ?"
                    
                    ASKING HOW SOMEONE IS:
                    • Formal: "Comment allez-vous ?" — "Très bien, merci. Et vous ?" / "Pas mal, merci."
                    • Informal: "Salut, ça va ?" — "Oui, ça va bien. (Ça va mal.) Et toi, comment vas-tu ?" / "Comme ci comme ça. / Ça peut aller. / Moyen."
                    
                    POLITENESS & CLARIFICATION:
                    • "Comment ? Je ne comprends pas. Répétez, s'il vous plaît."
                    • "Oh, pardon ! Excusez-moi, mademoiselle." / "Excuse-moi" (informal)
                    • "Merci (beaucoup)." — "De rien." (You're welcome / Not at all)
                    • Leave-taking: "Au revoir !" (Goodbye), "À bientôt !" (See you soon), "À demain !" (See you tomorrow)
                    
                    CULTURAL CUSTOMS — LA BISE:
                    • In France, people greet friends and family with 2, 3, or 4 cheek kisses (la bise) depending on the region.
                    • "Bisou" has an affectionate connotation used for kisses given to children, close friends, and family.
                    • First meeting: Saying "Bonjour" and shaking hands is the standard polite greeting.
                    • Belgium: A single kiss for everyday interactions, three for celebrations.
                    • Quebec: Physical distance with newcomers; bisou between close friends/family.
                    • Muslim countries: No touching, exchange "As-salaam'alaykum" or "Que la paix de Dieu soit avec vous".
                    
                    L'ALPHABET FRANÇAIS & PHONÉTIQUE:
                    26 letters: a [a], b [be], c [se], d [de], e [ə], f [ɛf], g [ʒe], h [aʃ], i [i], j [ʒi], k [ka], l [ɛl], m [ɛm], n [ɛn], o [o], p [pe], q [ky], r [ɛʁ], s [ɛs], t [te], u [y], v [ve], w [dubləve], x [iks], y [igʁɛk], z [zɛd].
                    
                    LES ACCENTS:
                    A French word written without its diacritical marks is misspelled!
                    1. Accent aigu (é): only on e (répétez, étudiant). Closed [e] sound.
                    2. Accent grave (à, è, ù): distinguishes words (a vs à, ou vs où) and open [ɛ] (très, fenêtre).
                    3. Accent circonflexe (â, ê, î, ô, û): historical marker (fenêtre, vous êtes, hôtel).
                    4. Tréma (ë, ï, ü): separates two adjacent vowels into distinct syllables (Noël, Loïc, maïs).
                    5. Cédille (ç): only under c before a, o, u to make an /s/ sound (français, garçon, ça va).
                    
                    LES DRAPEAUX:
                    • Drapeau tricolore (France): bleu, blanc, rouge (liberté, égalité, fraternité).
                    • Drapeau de l'Union européenne: fond bleu avec un cercle de 12 étoiles d'or (solidarité, unité, perfection).
                    
                    COGNATES (MOTS APPARENTÉS) PATTERNS:
                    • -ant -> -ing (amusant -> amusing)
                    • ét- -> st- (état -> state, étudiant -> student)
                    • -eux -> -ous (sérieux -> serious)
                    • -té -> -ty (cité -> city, liberté -> liberty, université -> university)
                    • -ique -> -ic, -ical (pratique -> practical, logique -> logical)
                    • -iste -> -ist, -istic (matérialiste -> materialistic)
                    • -ment -> -ly (rapidement -> rapidly)
                    • -re -> -er (ordre -> order, centre -> center)
                    • -eur -> -or, -er (vendeur -> vendor)
                    • -é -> -ed (remboursé -> reimbursed)
                    
                    FAUX AMIS (FALSE FRIENDS) & MOTS JUSTES:
                    • collège = Middle school (grades 6–9 in France). NOT college! Mot juste: université (la fac).
                    • librairie = Bookstore (where you buy books). NOT library! Mot juste: bibliothèque.
                    • rester = To stay, remain. NOT to rest! Mot juste: se reposer.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL FALSE FRIENDS ON EXAM: librairie (bookstore) vs bibliothèque (library); collège (middle school) vs université (college); rester (to stay) vs se reposer (to rest)! Accents are mandatory."
            ),
            CourseDocumentEntity(
                chapterId = "ch1",
                title = "Espaces Francophones & Reportage: La Francophonie et La France multiculturelle",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_1_espaces_francophones.txt",
                pageOrTimestamp = "Pages 8–9, 16–17",
                contentText = """
                    ESPACES FRANCOPHONES: BIENVENUE AU MONDE FRANCOPHONE
                    • La Francophonie represents around 300 million French speakers worldwide.
                    • French is the official language in 29 countries and spoken in over 60.
                    • Africa: ~60% of daily French speakers live on the African continent (31 countries across North, Central, and West Africa).
                    • Europe: France, Belgium (southern Wallonia), Switzerland (western Romandie), Luxembourg.
                    • Americas: Canada (official with English; Quebec), Haiti (official with Haitian Creole).
                    • Middle East & Southeast Asia: Lebanon, Laos, Vietnam.
                    • Organisation internationale de la Francophonie (OIF): promotes education, economic cooperation, and French language.
                    
                    HISTOIRE DE LA COLONISATION ET DÉCOLONISATION:
                    • 16th–17th centuries: Jacques Cartier (St. Lawrence 1530s), Samuel de Champlain (Quebec 1608), New France (1663), Robert La Salle (Mississippi 1682, Louisiana for King Louis XIV).
                    • Haitian Revolution (1791–1804): Enslaved people revolted in Saint-Domingue, leading to abolition of slavery in 1794 and the independent Republic of Haiti in 1804 (first successful slave revolt in history).
                    • Louisiana Purchase (1803): Napoleon sold Louisiana to the US after losing Saint-Domingue.
                    • 19th-century empire: Algeria (1830), Indochina (1858–1893), Tunisia and West Africa (1880s).
                    • Decolonization: Independent nations formed post-WWII by early 1960s.
                    • DROM (Départements et régions d'outre-mer): Guadeloupe, Martinique, Guyane, Réunion, Mayotte; Collectivités: Polynésie française, Nouvelle-Calédonie.
                    
                    REPORTAGE: LA FRANCE MULTICULTURELLE & LE MODÈLE UNIVERSALISTE:
                    • Multicultural France: Successive waves of immigration (19th century: Belgium, Italy, Portugal, Spain; 1915–1950: Eastern Europe; 1950s–1960s: former African/Asian colonies).
                    • Today ~6.5 million immigrants live in France (10% of total population). Top origin countries: Algeria (13%), Morocco (11.9%), Portugal (9.2%), Tunisia (4.4%), Italy (4.3%), Turkey (3.8%), Spain (3.7%).
                    • Le modèle universaliste (Enlightenment): All citizens are individuals equal before the Republic without official ethnic/racial categorization. French law prohibits collecting census data on race or ethnicity. Ongoing national debate regarding multiculturalism and affirmative remedies against discrimination.
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Francophone geography: 300 million speakers, 29 countries, 60% in Africa. French universalism forbids racial census data."
            ),
            CourseDocumentEntity(
                chapterId = "ch1",
                title = "Leçon 2: Les nombres de 0 à 60, Le calendrier et Les fêtes",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_1_lecon_2_nombres_calendrier.txt",
                pageOrTimestamp = "Pages 10–15",
                contentText = """
                    LEÇON 2: LES NOMBRES DE 0 À 60
                    • 0 à 10: zéro, un, deux, trois, quatre, cinq, six, sept, huit, neuf, dix
                    • 11 à 20: onze, douze, treize, quatorze, quinze, seize, dix-sept, dix-huit, dix-neuf, vingt
                    • 21 à 29: vingt et un (notice 'et'!), vingt-deux, vingt-trois, vingt-quatre, vingt-cinq, vingt-six, vingt-sept, vingt-huit, vingt-neuf
                    • 30 à 39: trente, trente et un, trente-deux...
                    • 40: quarante, 41: quarante et un...
                    • 50: cinquante, 51: cinquante et un...
                    • 60: soixante
                    
                    MATH EXPRESSIONS IN FRENCH:
                    • + plus, et
                    • - moins
                    • x fois
                    • = font
                    Example: "Combien font six plus deux ?" — "Six plus deux font huit." (6 + 2 = 8)
                    Example: "Trois fois vingt font soixante." (3 x 20 = 60)
                    
                    FRENCH TELEPHONE NUMBERS:
                    Spoken in five pairs of 2-digit numbers:
                    02 41 48 05 52 -> "zéro deux, quarante et un, quarante-huit, zéro cinq, cinquante-deux".
                    
                    QUEL JOUR SOMMES-NOUS ? (LE CALENDRIER):
                    • In French, days of the week are NOT capitalized.
                    • On French calendars, the week begins with Monday (lundi).
                    • Days: lundi (Monday), mardi (Tuesday), mercredi (Wednesday), jeudi (Thursday), vendredi (Friday), samedi (Saturday), dimanche (Sunday).
                    • "Quel jour sommes-nous (aujourd'hui) ? / Quel jour on est ?"
                    • "Nous sommes mardi. / On est mardi."
                    • "Le week-end, c'est le samedi et le dimanche."
                    
                    LES MOIS (ALL MASCULINE & LOWERCASE):
                    janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre.
                    
                    DATES IN FRENCH:
                    • Formula: Nous sommes le + [cardinal number] + [month]. (Abbreviated day/month: 21/03).
                    • Cardinal numbers are used for all days EXCEPT the first day of the month: "le 1er (premier) janvier".
                    • Examples: "le 14 juillet" (Bastille Day), "le 21 mars".
                    
                    PRONONCIATION DES VOYELLES:
                    • [i]: midi, stylo, souris (brief, smiling lips)
                    • [e]: les, et, répétez, janvier (closed, tense)
                    • [ɛ]: mai, c'est, merci, très, treize, fenêtre (open, relaxed)
                    
                    FÊTES OFFICIELLES EN FRANCE:
                    • 1er janvier: Nouvel An
                    • 1er mai: Fête du Travail
                    • 8 mai: Fête de la Victoire (1945)
                    • 14 juillet: Fête nationale française (Prise de la Bastille)
                    • 15 août: Assomption
                    • 1er novembre: Toussaint
                    • 11 novembre: Armistice (1918)
                    • 25 décembre: Noël
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "EXAM ALERT: Days & months are NOT capitalized; French week begins on Monday; Date format: 'le + cardinal number + month' EXCEPT 'le 1er (premier)' for the first day."
            ),
            CourseDocumentEntity(
                chapterId = "ch1",
                title = "Leçon 3: Dans la salle de classe & Les articles indéfinis (Genre des noms)",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_1_lecon_3_classe_articles.txt",
                pageOrTimestamp = "Pages 18–21",
                contentText = """
                    LEÇON 3: STRUCTURES — DANS LA SALLE DE CLASSE
                    20 Classroom items & people:
                    1. une chaise (chair)
                    2. un bureau (desk / office)
                    3. un tableau (board / chalkboard)
                    4. un écran (screen)
                    5. une professeure (female professor) / un professeur (male professor)
                    6. une porte (door)
                    7. une fenêtre (window)
                    8. un étudiant (male student)
                    9. un crayon (pencil)
                    10. une télévision (television)
                    11. un ordinateur (computer)
                    12. un clavier (keyboard)
                    13. une souris (mouse)
                    14. une table (table)
                    15. un stylo (pen)
                    16. une étudiante (female student)
                    17. un livre (book)
                    18. un cahier (notebook)
                    19. un portable (laptop)
                    20. un smartphone (smartphone)
                    Other items: une tablette (tablet), une salle de classe (classroom).
                    
                    USING "IL Y A":
                    "Il y a" means "there is" or "there are".
                    • "Il y a un cours de français." (There is a French class.)
                    • "Il y a quatre étudiants dans la classe." (There are four students in the classroom.)
                    • Question: "Il y a combien d'étudiants ?"
                    • Identification: "Qu'est-ce que c'est ?" — "C'est une chaise." / "C'est un livre."
                    • Identification: "Qui est-ce ?" — "C'est une professeure." / "C'est un étudiant."
                    
                    LES ARTICLES INDÉFINIS (A, AN, SOME):
                    • Masculine singular: un (un ami, un livre)
                    • Feminine singular: une (une amie, une table)
                    • Plural (both genders): des (des amis, des tables)
                    • Un and une also mean "one" depending on context.
                    
                    PRONONCIATION OF UN VS UNE:
                    • un: one single nasal vowel sound [œ̃]. Air goes through mouth AND nose; do NOT pronounce 'n'.
                    • une: two distinct sounds — oral vowel [y] (as in 'tu') + sounded consonant 'n' [yn].
                    
                    LE GENRE DES NOMS (THE GENDER OF NOUNS):
                    1. Males are masculine (un homme); females are feminine (une femme).
                    2. Masculine endings clues:
                       • -eau: un bureau, un tableau
                       • -isme: un prisme, le dynamisme
                       • -ment: un monument, le document
                    3. Feminine endings clues:
                       • -ence / -ance: une différence, une distance
                       • -ion: une réaction, une action
                       • -té: une université, la société
                       • -ure: une lecture, la culture
                       • -ie: une librairie, la philosophie
                    4. Borrowed foreign words are masculine: un coca-cola, un smartphone, un blog, un e-mail.
                    5. Language names are masculine and lowercase: le français, l'anglais, l'espagnol.
                    6. People nouns: add -e for feminine (un ami -> une amie, un étudiant -> une étudiante, un Français -> une Française). Final consonants t, n, d, s are SILENT in masculine, SOUNDED in feminine.
                    7. Invariable ending in -e: un touriste / une touriste.
                    8. Fixed gender regardless of sex: un bébé (always masculine), une personne (always feminine), une vedette (always feminine).
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "Gender endings: -eau, -isme, -ment (masculine) vs -ence, -ion, -té, -ure, -ie (feminine). Silent final consonants in masculine become pronounced in feminine. Plural indefinite article is always 'des'."
            ),
            CourseDocumentEntity(
                chapterId = "ch1",
                title = "Leçon 4 & Vocabulaire Officiel: Carte d'étudiant & Lexique complet",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_1_vocabulaire_complet.txt",
                pageOrTimestamp = "Pages 22–26",
                contentText = """
                    LEÇON 4: PERSPECTIVES & LA CARTE D'ÉTUDIANT
                    Carte d'étudiante de Léa Bouchard (Université Paris-Sorbonne):
                    • Document administratif obligatoire.
                    • Accès à la fac: passeport pour les cours, le resto-U, la cafétéria, les activités sportives, la bibliothèque et les salles d'examen.
                    • Avantages financiers: réductions dans les musées, théâtres, cinémas, transports publics; menus et prix réduits pour petits budgets.
                    
                    GLOSSAIRE OFFICIEL DU CHAPITRE 1 (VOCABULAIRE COMPLET):
                    
                    LES BONNES MANIÈRES:
                    • À bientôt ! = See you soon!
                    • Au revoir ! = Goodbye!
                    • Bonjour. = Hello. Good day.
                    • Bonsoir. = Good evening.
                    • Ça peut aller. = All right. Pretty well.
                    • Ça va ? = How's it going?
                    • Ça va bien. = Fine. (Things are going well.)
                    • Ça va mal. = Things are going badly.
                    • Comme ci comme ça. = So so.
                    • Comment ? = What? How?
                    • Comment allez-vous ? / Comment vas-tu ? = How are you? (formal / informal)
                    • Comment vous appelez-vous ? / Comment t'appelles-tu ? = What's your name? (formal / informal)
                    • De rien. = Not at all. Don't mention it. You're welcome.
                    • Et vous ? / Et toi ? = And you? (formal / informal)
                    • Excusez-moi. / Excuse-moi. = Excuse me. (formal / informal)
                    • Je m'appelle... = My name is...
                    • Je ne comprends pas. = I don't understand.
                    • madame (Mme) = Mrs., ma'am
                    • mademoiselle (Mlle) = Miss
                    • Merci (beaucoup). = Thank you (very much).
                    • monsieur (M.) = Mr., sir
                    • Moyen. = (I'm doing) all right.
                    • Pardon. = Pardon (me).
                    • Pas mal. = Not bad(ly).
                    • Répétez. / Répète. = Repeat. (formal / informal)
                    • Salut ! = Hi! / Bye! (informal)
                    • s'il vous plaît / s'il te plaît = please (formal / informal)
                    • Très bien. = Very well. Very good.
                    
                    DANS LA SALLE DE CLASSE:
                    • un bureau = a desk, office
                    • un cahier = a notebook
                    • une chaise = a chair
                    • un clavier = a keyboard
                    • un crayon = a pencil
                    • un écran = a screen
                    • un étudiant / une étudiante = a student (m. / f.)
                    • une fenêtre = a window
                    • un livre = a book
                    • un ordinateur = a computer
                    • un portable = a laptop
                    • une porte = a door
                    • un professeur / une professeure = a professor, instructor
                    • une salle de classe = a classroom
                    • un smartphone = a smartphone
                    • une souris = a mouse
                    • un stylo = a pen
                    • une table = a table
                    • une tablette = a tablet computer
                    • un tableau = a (chalk)board
                    • une télévision = a television
                    
                    LE CALENDRIER:
                    • aujourd'hui = today
                    • le jour = day
                    • les jours de la semaine = days of the week (lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche)
                    • le mois = month (janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre)
                    • Nous sommes / On est... = It is...
                    • Quel jour sommes-nous / on est ? = What day is it?
                    • Quelle est la date ? = What is the date?
                    
                    MOTS ET EXPRESSIONS DIVERS:
                    • Allez-y ! = Off you go! Go ahead!
                    • beaucoup = very much, a lot
                    • bien = well
                    • c'est un / une... = it's a (an)...
                    • combien (de) = how many
                    • et = and
                    • une femme = a woman
                    • un homme = a man
                    • il y a = there is / there are
                    • mal = badly
                    • non = no
                    • oui = yes
                    • quel / quelle = what; which
                    • Qu'est-ce que c'est ? = What is it?
                    • Qui est-ce ? = Who is it?
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "Review all classroom, calendar, and politeness expressions for Chapter 1 Exam."
            )
        )
        for (doc in ch1Documents) {
            dao.insertDocument(doc)
        }

        val ch1Notes = StudyNoteEntity(
            chapterId = "ch1",
            chapterTitle = "Chapitre 1 : Premiers pas & La vie étudiante",
            keyVocab = "• Salutations: Bonjour (formal), Bonsoir, Salut (informal), Au revoir, À bientôt\n• Politesse: S'il vous plaît / S'il te plaît, Merci beaucoup, De rien, Excusez-moi\n• Salle de classe: un bureau, un tableau, un livre, un cahier, un stylo, un crayon, un ordinateur, un portable, une chaise, une table, une porte, une fenêtre, une professeure\n• Le calendrier: lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche (lowercase!)\n• Nombres 0–60: zéro, un, deux... vingt et un... trente, quarante, cinquante, soixante",
            grammarRules = "• Formal vs Informal: Use 'vous' (Comment allez-vous ?, Comment vous appelez-vous ?, s'il vous plaît) with professors/strangers; use 'tu' (Comment vas-tu ?, Comment t'appelles-tu ?, s'il te plaît, salut) with close friends/peers.\n• Articles indéfinis: 'un' (masc. singular), 'une' (fem. singular), 'des' (plural: des stylos, des tables).\n• Gender of Nouns clues: Masculine endings (-eau, -isme, -ment, borrowed words, languages). Feminine endings (-ence, -ion, -té, -ure, -ie).\n• 'Il y a': expresses existence or quantity ('Il y a un cours', 'Il y a combien d'étudiants ?').\n• Dates: 'Nous sommes le + [cardinal number] + [month]' (le 14 juillet). Cardinal for all EXCEPT the first: 'le 1er (premier) janvier'. Days and months are never capitalized!\n• Math formulas: plus (+), moins (-), fois (x), font (=).",
            pronunciationTips = "• Accents matter: É (accent aigu) gives closed [e]; è/ê give open [ɛ]; ç (cédille) creates /s/ before a, o, u; tréma (ï) separates syllables.\n• Nasal 'un' [œ̃] vs oral 'une' [yn] with pronounced 'n'.\n• Silent final consonants (t, n, d, s) in masculine nouns (étudiant, Français) become sounded when -e is added in feminine (étudiante, Française).\n• Vowel contrast: [i] (midi), [e] (les, répétez), [ɛ] (mai, c'est, treize).",
            culturalNotes = "• La bise: 2 to 4 cheek kisses between friends and family in France; handshake and 'Bonjour' for first introductions.\n• Le drapeau tricolore: bleu, blanc, rouge (liberté, égalité, fraternité).\n• La carte d'étudiant: mandatory ID for university life (resto-U, library, exams, student discounts).\n• French calendars begin on Monday (lundi).",
            professorEmphasis = "⭐ PROFESSOR'S MIDTERM EMPHASIS:\n1. FAUX AMIS: 'la librairie' = bookstore (NOT library -> la bibliothèque); 'le collège' = middle school (NOT college -> l'université); 'rester' = to stay (NOT to rest -> se reposer).\n2. French days of the week & months are NEVER capitalized!\n3. Dates use 'le 1er (premier)' for the 1st of the month, but cardinal numbers for all other days.\n4. Accents are mandatory — a word without its accent mark is marked misspelled on exams.",
            examples = "• Bonjour madame, comment allez-vous ? — Très bien, merci. Et vous ?\n• Dans la salle de classe, il y a un tableau, des chaises et un ordinateur.\n• Aujourd'hui, nous sommes le 1er octobre et c'est un jeudi.\n• Trois fois vingt font soixante.\n• J'achète un livre à la librairie et j'étudie à la bibliothèque universitaire.",
            summaryShort = "Master greetings (formal vs informal), numbers 0-60, calendar & dates, 20 classroom objects, indefinite articles (un, une, des), and essential false friends (librairie vs bibliothèque, collège vs université).",
            summaryStandard = "Chapitre 1 introduces greetings and social etiquette in France and the Francophone world, numbers from 0 to 60, days of the week, months, date formatting, 20 key classroom items with indefinite articles (un, une, des), noun gender ending rules, and crucial false friends.",
            summaryDetailed = "Complete foundational unit from Vis-à-vis Chapter 1 (pp. 2–26). Deep coverage of formal 'vous' vs informal 'tu', the cultural practice of 'la bise', French accents and phonetics, the history and geography of 300 million speakers in La Francophonie, French universalism, numbers up to 60, calendar rules (week starting Monday, lowercase days), classroom nouns, grammatical gender clues (-eau, -isme, -ment vs -ence, -ion, -té, -ure, -ie), and student life with the carte d'étudiant."
        )
        dao.insertStudyNote(ch1Notes)

        val ch1Flashcards = listOf(
            FlashcardEntity(chapterId = "ch1", frenchText = "la bibliothèque", englishText = "the library (False friend: NOT bookstore!)", phoneticText = "lah bee-blee-oh-TEK", exampleSentence = "J'étudie à la bibliothèque universitaire.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "la librairie", englishText = "the bookstore (False friend: NOT library!)", phoneticText = "lah lee-breh-REE", exampleSentence = "J'achète un dictionnaire à la librairie.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "le collège", englishText = "middle school (False friend: NOT college/university!)", phoneticText = "luh koh-LEZH", exampleSentence = "En France, les élèves de 11 à 15 ans vont au collège.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "rester", englishText = "to stay, remain (False friend: NOT to rest!)", phoneticText = "reh-STAY", exampleSentence = "Je reste à la maison ce soir. (To rest is 'se reposer').", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "Comment vous appelez-vous ?", englishText = "What is your name? (formal)", phoneticText = "koh-mahn voo zah-play VOO", exampleSentence = "Bonjour monsieur, comment vous appelez-vous ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "Comment t'appelles-tu ?", englishText = "What is your name? (informal)", phoneticText = "koh-mahn tah-pel TU", exampleSentence = "Salut ! Comment t'appelles-tu ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "Comment allez-vous ?", englishText = "How are you? (formal)", phoneticText = "koh-mahn tah-lay VOO", exampleSentence = "Comment allez-vous, madame Dupont ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "Salut, ça va ?", englishText = "Hi, how's it going? (informal)", phoneticText = "sah-LU, sah VAH", exampleSentence = "Salut Luc, ça va ? — Oui, ça va bien !", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "De rien.", englishText = "You're welcome / Not at all.", phoneticText = "duh ree-EN", exampleSentence = "Merci beaucoup ! — De rien.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "s'il vous plaît / s'il te plaît", englishText = "please (formal / informal)", phoneticText = "seel voo PLEH / seel tuh PLEH", exampleSentence = "Répétez, s'il vous plaît.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "il y a", englishText = "there is / there are", phoneticText = "eel yah", exampleSentence = "Il y a vingt étudiants dans la salle de classe.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "un bureau / un tableau", englishText = "a desk (or office) / a board", phoneticText = "uhn bu-ROH / uhn tah-BLOH", exampleSentence = "La professeure écrit sur le tableau.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "un ordinateur / un portable", englishText = "a computer / a laptop", phoneticText = "uhn or-dee-nah-TUR / uhn por-TAH-bluh", exampleSentence = "J'utilise un portable en classe.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "les jours de la semaine", englishText = "lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche (lowercase!)", phoneticText = "luhn-DEE, mar-DEE, mehr-kruh-DEE...", exampleSentence = "Le premier jour de la semaine en France, c'est lundi.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "le 1er (premier) janvier", englishText = "January 1st (Use '1er' for the first day of the month)", phoneticText = "luh pruh-MYAY zhahn-VYAY", exampleSentence = "Le Nouvel An est le premier janvier.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "soixante", englishText = "sixty (60)", phoneticText = "swah-SAHNT", exampleSentence = "Trois fois vingt font soixante.", status = "NEW"),
            FlashcardEntity(chapterId = "ch1", frenchText = "la carte d'étudiant", englishText = "student ID card (access to resto-U, library, exams, discounts)", phoneticText = "lah kart day-tu-DYAHN", exampleSentence = "J'ai ma carte d'étudiante pour entrer à la bibliothèque.", status = "NEW")
        )
        dao.insertFlashcards(ch1Flashcards)

        val ch1QuizItems = listOf(
            QuizItemEntity(chapterId = "ch1", topic = "False Friends (Faux Amis)", questionType = "MULTIPLE_CHOICE", questionText = "You want to buy a French novel. Where do you go?", optionsJson = "[\"la librairie\", \"la bibliothèque\", \"le collège\", \"le bureau\"]", correctAnswer = "la librairie", explanation = "Remember the classic false friend! 'La librairie' is a bookstore where you buy books. 'La bibliothèque' is the library where you borrow books.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "False Friends (Faux Amis)", questionType = "MULTIPLE_CHOICE", questionText = "In France, what does 'le collège' refer to?", optionsJson = "[\"middle school (grades 6-9)\", \"a 4-year university\", \"a community college\", \"a residence hall\"]", correctAnswer = "middle school (grades 6-9)", explanation = "'Le collège' is middle school in France. The word for university/college is 'l'université' (or 'la fac').", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "Politeness & Address", questionType = "MULTIPLE_CHOICE", questionText = "Which question is appropriate to ask your French professor's name on the first day?", optionsJson = "[\"Comment vous appelez-vous ?\", \"Comment t'appelles-tu ?\", \"Tu t'appelles comment ?\", \"Salut, c'est quoi ton nom ?\"]", correctAnswer = "Comment vous appelez-vous ?", explanation = "With professors, elders, and strangers, formal 'vous' is required: 'Comment vous appelez-vous ?'", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "Calendar & Capitalization", questionType = "MULTIPLE_CHOICE", questionText = "Are days of the week and months capitalized in French?", optionsJson = "[\"No, they are always lowercase\", \"Yes, always capitalized like English\", \"Only months are capitalized\", \"Only days are capitalized\"]", correctAnswer = "No, they are always lowercase", explanation = "In French, days of the week (lundi, mardi...) and months (janvier, février...) are written in lowercase.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "Calendar & Dates", questionType = "FILL_BLANK", questionText = "Complete the date for New Year's Day: 'Nous sommes le ______ janvier.'", optionsJson = "[\"1er\", \"un\", \"premier\", \"1\"]", correctAnswer = "1er", explanation = "French uses cardinal numbers for dates, EXCEPT the first of the month which is always 'le 1er' (premier).", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "French Numbers & Math", questionType = "MULTIPLE_CHOICE", questionText = "Combien font vingt plus trente ?", optionsJson = "[\"cinquante\", \"quarante\", \"soixante\", \"soixante-dix\"]", correctAnswer = "cinquante", explanation = "20 + 30 = 50 ('cinquante' in French).", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch1", topic = "Classroom Objects", questionType = "TRANSLATE_EN_FR", questionText = "How do you say 'a chalkboard' or 'a board' in French?", optionsJson = "[\"un tableau\", \"un bureau\", \"une table\", \"un cahier\"]", correctAnswer = "un tableau", explanation = "'Un tableau' is a board or chalkboard. 'Un bureau' is a desk or office.", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch1", topic = "Grammatical Gender Clues", questionType = "MULTIPLE_CHOICE", questionText = "Nouns ending in -eau, -isme, or -ment are typically:", optionsJson = "[\"Masculine\", \"Feminine\", \"Plural only\", \"Invariable\"]", correctAnswer = "Masculine", explanation = "The endings -eau (un bureau, un tableau), -isme (le prisme), and -ment (un document) are classic masculine markers.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch1", topic = "Indefinite Articles", questionType = "FILL_BLANK", questionText = "Complete: 'Dans la classe, voilà ______ chaises.'", optionsJson = "[\"des\", \"une\", \"un\", \"les\"]", correctAnswer = "des", explanation = "The plural indefinite article for both masculine and feminine nouns is always 'des'.", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch1", topic = "French Culture & Greetings", questionType = "MULTIPLE_CHOICE", questionText = "What is 'la bise' in French culture?", optionsJson = "[\"A cheek kiss greeting between friends and family\", \"A formal handshake with a professor\", \"A type of French pastry\", \"A farewell bow\"]", correctAnswer = "A cheek kiss greeting between friends and family", explanation = "'La bise' is the traditional cheek kiss greeting exchanged between friends and relatives in the Francophone world.", isProfessorEmphasis = true)
        )
        dao.insertQuizItems(ch1QuizItems)
    }

    private suspend fun seedChapter2Data(dao: AppDao) {
        dao.deleteDocumentsByChapter("ch2")
        dao.deleteFlashcardsByChapter("ch2")
        dao.deleteQuizItemsByChapter("ch2")

        dao.insertChapters(
            listOf(
                ChapterEntity(
                    id = "ch2",
                    number = 2,
                    title = "Chapitre 2 : Nous, les étudiants",
                    description = "Campus places, academic subjects, nationalities, -er verbs, negation ne...pas, and verb être."
                )
            )
        )

        val ch2Documents = listOf(
            CourseDocumentEntity(
                chapterId = "ch2",
                title = "Leçon 1: Les lieux, Les matières & Les pays et nationalités",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_2_lecon_1_lieux_matieres_nationalites.txt",
                pageOrTimestamp = "Pages 28–33",
                contentText = """
                    CHAPITRE 2 — LEÇON 1: PAROLES (LES LIEUX, LES MATIÈRES, LES NATIONALITÉS)
                    
                    LES LIEUX DU CAMPUS:
                    • le restaurant universitaire (le resto-U) : university cafeteria / dining hall
                    • la bibliothèque : library (False friend: NOT bookstore!)
                    • l'amphithéâtre (l'amphi) (m.) : lecture hall
                    • le bureau : office / desk
                    • l'école (f.) : school
                    • le gymnase : gymnasium
                    • le laboratoire (le labo) : lab
                    • la librairie : bookstore (False friend: NOT library!)
                    • la salle de classe : classroom
                    • la cité universitaire (la cité-U) : residence halls / campus housing
                    
                    LES MATIÈRES (ACADEMIC SUBJECTS):
                    Faculté des lettres et sciences humaines :
                    • la littérature, la linguistique, les langues (f.) étrangères (l'allemand, l'anglais, le chinois, l'espagnol, l'italien, le japonais)
                    • l'histoire (f.), la géographie, la philosophie, la psychologie, la sociologie
                    Faculté des sciences :
                    • les mathématiques (les maths) (f.), l'informatique (f.) (computer science)
                    • les sciences naturelles : la biologie, la chimie, la géologie, la physique
                    Autres matières :
                    • le commerce (business), le cours (class), le droit (law), l'économie (f.), l'ingénierie (f.) (engineering)
                    
                    LES PAYS ET LES NATIONALITÉS (23 COUNTRIES):
                    • l'Algérie : algérien, algérienne
                    • l'Allemagne : allemand, allemande
                    • l'Angleterre : anglais, anglaise
                    • la Belgique : belge
                    • le Canada : canadien, canadienne
                    • la Chine : chinois, chinoise
                    • la Côte d'Ivoire : ivoirien, ivoirienne
                    • l'Espagne : espagnol, espagnole
                    • les États-Unis : américain, américaine
                    • la France : français, française
                    • Haïti : haïtien, haïtienne
                    • l'Italie : italien, italienne
                    • le Japon : japonais, japonaise
                    • le Liban : libanais, libanaise
                    • Madagascar : malgache
                    • le Maroc : marocain, marocaine
                    • le Mexique : mexicain, mexicaine
                    • le Québec : québécois, québécoise
                    • la Russie : russe
                    • le Sénégal : sénégalais, sénégalaise
                    • la Suisse : suisse
                    • la Tunisie : tunisien, tunisienne
                    • le Vietnam : vietnamien, vietnamienne
                    * Note: Capitalize the noun of nationality (un Français, un Américain), but use lowercase for the adjective (un étudiant français, une étudiante américaine).
                    
                    PRONONCIATION DES FORMES MASCULINES ET FÉMININES:
                    • Adding -e causes silent consonants to sound: allemand -> allemande (d pronounced), anglais -> anglaise (s becomes /z/).
                    • Nasal vowel [ɛ̃] denasalizes to oral [ɛn]: américain -> américaine, marocain -> marocaine.
                    • Suffix -ien doubles 'n' and becomes [jɛn]: canadien -> canadienne, tunisien -> tunisienne, algérien -> algérienne, italien -> italienne.
                    
                    LA PRÉPOSITION À + VILLE:
                    • 'À' indicates location or movement toward a city:
                      "J'habite à Genève." (I live in Geneva.)
                      "Vous allez à Montréal." (You are going to Montreal.)
                      "Karim habite à Tunis. Il parle arabe et français."
                    
                    LES LOISIRS:
                    • La musique : le jazz, la musique classique, la musique country, le rap, le rock
                    • Le sport : le basket-ball, le football, le football américain, le jogging, le ski, le tennis
                    • Le cinéma : les films d'amour, d'aventure, d'horreur, de science-fiction
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL EXAM RULES: à + city (J'habite à Genève); Nationalities: -ien -> -ienne (canadien -> canadienne), nasal denasalization [ɛ̃] -> [ɛn] (américain -> américaine); False friends: la bibliothèque = library vs la librairie = bookstore!"
            ),
            CourseDocumentEntity(
                chapterId = "ch2",
                title = "Espaces Francophones: Bienvenue en Louisiane & Les Cadiens",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_2_espaces_francophones_louisiane.txt",
                pageOrTimestamp = "Page 34",
                contentText = """
                    ESPACES FRANCOPHONES: BIENVENUE EN LOUISIANE !
                    
                    LA NOUVELLE-ORLÉANS:
                    • French influence visible in architecture, traditions, Mardi Gras, and food.
                    • The historic French Quarter is known in French as le Vieux Carré.
                    • Le célèbre Café du Monde: known for café au lait and beignets.
                    
                    LE PAYS CADIE (CAJUN COUNTRY):
                    • Southwestern Louisiana: Cajun French (Louisiana French) spoken in communities with French names such as Larose and Belle River.
                    • Atchafalaya swamp (Henderson): swamps, boat tours, and alligators.
                    • Cuisine cadienne: crawfish (écrevisses), gumbo, rice and gravy, boudin (spicy sausage).
                    
                    PORTRAIT — LES CADIENS:
                    • Who are the Cadiens (Cajuns)? Descendants of French Acadians from the Canadian Maritime provinces and Maine.
                    • Le Grand Dérangement (The Great Expulsion, 1755–1764): During the French and Indian War, Acadians were deported by the British to American colonies and France. Many eventually migrated to Louisiana (colonized by France in the early 1700s).
                    • Unique cultural identity, music (Cajun music & zydeco; band Feufollet), and culinary traditions shaped by Acadian roots, Creoles, Native Americans, and African influences.
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Le Grand Dérangement (1755-1764): Acadians deported from Canada/Maine to Louisiana; Cajun culture, cuisine (gumbo, boudin, crawfish), and French dialect."
            ),
            CourseDocumentEntity(
                chapterId = "ch2",
                title = "Leçon 2: Les articles définis & Le pluriel des noms",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_2_lecon_2_articles_definis_pluriel.txt",
                pageOrTimestamp = "Pages 35–37",
                contentText = """
                    LEÇON 2: STRUCTURES — LES ARTICLES DÉFINIS ET LE PLURIEL DES NOMS
                    
                    LES FORMES DE L'ARTICLE DÉFINI (THE):
                    • Masculin singulier : le (le livre, le cours) / l' before vowel (l'ami, l'homme)
                    • Féminin singulier : la (la femme, la table) / l' before vowel (l'amie, l'histoire)
                    • Pluriel (les deux genres) : les (les livres, les femmes, les examens, les artistes)
                    
                    RÈGLES D'UTILISATION DES ARTICLES DÉFINIS:
                    1. Specific noun: "Voici le resto-U." / "Voilà la bibliothèque."
                    2. General sense & verbs of preference: In French, unlike English, the definite article is ALWAYS used when discussing concepts in general or with verbs of liking/preference (aimer, adorer, détester, préférer):
                       "J'aime le café." (I like coffee.)
                       "C'est la vie !" (That's life!)
                       "J'aime le ski." (I like skiing.)
                       "Les Français aiment le vin." (French people like wine.)
                    
                    PRONONCIATION DES VOYELLES DANS LE, LA, LES:
                    • le [ə] : closed mouth, rounded lips (le cours, de, je)
                    • la [a] : open mouth, semi-smile (la table, madame)
                    • les [e] : closed smile, tense vowel (les cahiers, répétez, et)
                    
                    PLURIEL DES NOMS:
                    1. Regular rule: Add -s (le livre -> les livres, la femme -> les femmes).
                    2. Nouns ending in -s, -x, -z in singular: NO CHANGE in plural:
                       le cours -> les cours
                       un choix -> des choix
                       le nez -> les nez
                    3. Nouns ending in -eau, -ieu: Add -x:
                       le tableau -> les tableaux
                       le bureau -> les bureaux
                       le lieu -> les lieux
                    4. Nouns ending in -al, -ail: Become -aux:
                       un hôpital -> des hôpitaux
                       le travail -> les travaux
                    5. Group gender rule: If a group includes at least one male, the masculine plural is used:
                       un étudiant et sept étudiantes -> des étudiants
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "General preference rule: Always use definite articles with aimer, adorer, détester ('J'aime le café', 'Les Français aiment le vin'). Plural exceptions: -eau/-ieu -> -eaux/-ieux; -al/-ail -> -aux; -s/-x/-z no change."
            ),
            CourseDocumentEntity(
                chapterId = "ch2",
                title = "Leçon 2 & Culture: Les verbes réguliers en -er & Le Baccalauréat",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_2_lecon_2_verbes_er_bac.txt",
                pageOrTimestamp = "Pages 38–42",
                contentText = """
                    LEÇON 2: STRUCTURES — LES VERBES RÉGULIERS EN -ER
                    
                    SUBJECT PRONOUNS (LES PRONOMS SUJETS):
                    • je (j' before vowel) = I
                    • tu = you (informal singular: friend, classmate, family)
                    • il = he / it (m.)
                    • elle = she / it (f.)
                    • on = one / we / people (informal replacement for nous, takes 3rd person singular)
                    • nous = we
                    • vous = you (formal singular of respect, OR any plural 'you all')
                    • ils = they (m. or mixed group)
                    • elles = they (f. only)
                    
                    CONJUGATION OF REGULAR -ER VERBS:
                    Drop -er and add endings: -e, -es, -e, -ons, -ez, -ent.
                    Example: PARLER (to speak):
                    • je parle
                    • tu parles
                    • il/elle/on parle
                    • nous parlons
                    • vous parlez
                    • ils/elles parlent (*ATTENTION: -ent is 100% SILENT!)
                    
                    Example: AIMER (to like/love):
                    • j'aime (elision with j')
                    • tu aimes
                    • il/elle/on aime
                    • nous aimons
                    • vous aimez (liaison /z/)
                    • ils/elles aiment (liaison /z/)
                    
                    COMMON -ER VERBS:
                    adorer (love), aimer (like), aimer mieux (prefer), chercher (look for), commencer (nous commençons), danser (dance), demander (ask for), détester (hate), donner (give), écouter (listen to), étudier (study), habiter (live), manger (nous mangeons), penser (think), porter (wear), regarder (watch), rêver (dream), skier (ski), travailler (work), trouver (find), visiter (visit a place).
                    
                    VERB + INFINITIVE CONSTRUCTION:
                    After verbs of preference (aimer, adorer, détester, aimer mieux), the second verb is in the infinitive:
                    • "J'aime écouter la radio." (I like listening to the radio.)
                    • "Je déteste regarder la télévision." (I hate watching TV.)
                    
                    ADVERBS OF FREQUENCY (FOLLOW THE CONJUGATED VERB):
                    • toujours = always
                    • souvent = often
                    • quelquefois = sometimes
                    • rarement = rarely
                    • de temps en temps = from time to time
                    
                    CULTURE — LE BACCALAURÉAT (LE BAC):
                    • National secondary diploma at end of high school (lycée).
                    • Baccalauréat général: exams in 4 subjects + le Grand Oral.
                    • Compulsory written and oral exams in literature and philosophy for all students.
                    • Passing score: minimum average of 10/20 guarantees university admission.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CONJUGATION EXAM ALERT: 3rd person plural -ent is 100% SILENT (ils parlent = [parl])! When two verbs are used together, the second verb is in the INFINITIVE (J'aime écouter). Baccalauréat requires minimum 10/20 average."
            ),
            CourseDocumentEntity(
                chapterId = "ch2",
                title = "Leçon 3 & Reportage: Le verbe être, La négation & Le Quartier Latin",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_2_lecon_3_etre_negation_latin.txt",
                pageOrTimestamp = "Pages 43–54",
                contentText = """
                    LEÇON 3: STRUCTURES — LE VERBE ÊTRE & LA NÉGATION
                    
                    REPORTAGE — LE QUARTIER LATIN:
                    • Cultural and student center of Paris on the Left Bank.
                    • Sorbonne Université: founded in 1257 by Robert de Sorbon.
                    • Prestigious lycées: Louis-le-Grand, Henri IV.
                    • Boulevard Saint-Michel: animated cafés, bookstores, and student life.
                    • Le Jardin du Luxembourg: favorite park for studying, relaxing after exams, and meeting friends.
                    
                    LE VERBE ÊTRE (IRREGULAR):
                    • je suis = I am
                    • tu es = you are (informal)
                    • il / elle / on est = he / she / one is
                    • nous sommes = we are
                    • vous êtes = you are (formal / plural; note circumflex on 'ê'!)
                    • ils / elles sont = they are
                    
                    SPECIAL RULE — PROFESSIONS & NATIONALITIES:
                    • NO article is used after être when stating a profession, nationality, or religion:
                      "Je suis anglais." (NOT Je suis un anglais)
                      "Vous êtes professeur ? — Non, je suis étudiant."
                    
                    C'EST VERSUS IL/ELLE EST:
                    1. C'est (plural: ce sont):
                       • Used with a MODIFIED noun (article + noun):
                         "C'est un étudiant haïtien." / "Ce sont des Italiens." / "C'est un ordinateur."
                       • Used with proper names: "C'est Maxime."
                       • Used with adjectives for general situations: "Le français ? C'est facile !"
                    2. Il est / Elle est (plural: ils/elles sont):
                       • Used to describe a specific person/thing already mentioned:
                         "Voici Karim. Il est étudiant en biologie."
                         "La librairie ? Elle est dans la rue Mouffetard."
                         "Elle est intelligente et dynamique."
                    
                    PRONONCIATION DE ET VS EST:
                    • et [e] : closed tense sound (télé, aller, et).
                    • est [ɛ] : open relaxed sound (matières, être, est).
                    
                    LA NÉGATION AVEC NE... PAS:
                    • Formula: Sujet + ne + verbe conjugué + pas
                      "Je parle chinois." -> "Je ne parle pas chinois."
                      "Elles regardent la télévision." -> "Elles ne regardent pas la télévision."
                    • Elision: 'ne' becomes 'n'' before vowel or mute h:
                      "J'aime les films d'horreur." -> "Je n'aime pas les films d'horreur."
                      "Nous habitons ici." -> "Nous n'habitons pas ici."
                    • With an infinitive: ne...pas surrounds the conjugated verb:
                      "Il aime étudier." -> "Il n'aime pas étudier."
                    
                    PERSPECTIVES — UNIVERSITÉ LAVAL AU QUÉBEC:
                    • Oldest French-speaking university in North America.
                    • FLE (Français langue étrangère) program: 5-week and 15-week sessions, 7 levels.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "GRAMMAR TRAPS: 1) NO article with professions/nationalities after être ('Je suis étudiant'); 2) C'est (modified noun with article) vs Il/Elle est (specific mention, bare adjective); 3) Ne...pas surrounds the conjugated verb (Je n'aime pas étudier); 4) Sorbonne founded 1257."
            )
        )
        for (doc in ch2Documents) {
            dao.insertDocument(doc)
        }

        val ch2Notes = StudyNoteEntity(
            chapterId = "ch2",
            chapterTitle = "Chapitre 2 : Nous, les étudiants",
            keyVocab = "• Lieux: le resto-U, la bibliothèque (library), l'amphithéâtre (l'amphi), le labo, le gymnase, la librairie (bookstore), la salle de classe, la cité-U\n• Matières: l'informatique (computer science), les langues étrangères, la littérature, la philosophie, la chimie, la physique, les maths, le droit, le commerce\n• Nationalités: américain(e), canadien(ne), français(e), sénégalais(e), ivoirien(ne), algérien(ne), suisse, belge, chinois(e), mexicain(e)\n• Verbes en -er: parler, aimer, adorer, détester, étudier, habiter, travailler, chercher, écouter, regarder\n• Adverbes de fréquence: toujours, souvent, quelquefois, rarement, de temps en temps",
            grammarRules = "• Definite Articles: 'le' (m.), 'la' (f.), 'l'' (vowel), 'les' (pl.). Used for specific nouns AND general preferences with aimer/adorer/détester ('J'aime le café').\n• Plural Nouns: Regular (+s); -s/-x/-z no change (le cours -> les cours, le nez -> les nez); -eau/-ieu -> -eaux/-ieux (tableaux, bureaux); -al/-ail -> -aux (hôpitaux, travaux).\n• Regular -er Verbs: Endings -e, -es, -e, -ons, -ez, -ent. The 3rd person plural ending '-ent' is 100% silent!\n• Verb + Infinitive: With verbs of preference, second verb is infinitive ('J'aime écouter de la musique').\n• Être: je suis, tu es, il est, nous sommes, vous êtes (ê!), ils sont. NO article after être with professions ('Je suis étudiant').\n• C'est vs Il/Elle est: C'est + modified noun with article ('C'est un étudiant') vs Il est + bare adjective ('Il est intelligent').\n• Négation: ne + conjugated verb + pas (n' before vowel). Surrounds the conjugated verb: 'Il n'aime pas étudier'.\n• Preposition: à + city (J'habite à Genève).",
            pronunciationTips = "• -ent ending in 'ils parlent' is silent — pronounced exactly like 'il parle' [parl].\n• Nasal denasalization: américain [ɛ̃] becomes américaine [ɛn]; canadien doubles 'n' [jɛn].\n• Vowel contrast: 'et' has closed [e], while 'est' has open [ɛ].\n• 'Le' has rounded lips [ə], 'la' has semi-smile [a], 'les' has wide smile [e].",
            culturalNotes = "• La Louisiane & Les Cadiens: Acadians exiled during Le Grand Dérangement (1755-1764) created Cajun French, gumbo, and zydeco music.\n• Le Baccalauréat: France's national high school exit exam requiring 10/20 average to enter university.\n• Le Quartier Latin: Paris's intellectual hub around Sorbonne Université (founded 1257 by Robert de Sorbon) and Jardin du Luxembourg.\n• Université Laval: Oldest French-speaking university in North America located in Quebec.",
            professorEmphasis = "⭐ PROFESSOR'S MIDTERM EMPHASIS:\n1. Never use an article after être when stating your profession: say 'Je suis étudiant', NOT 'Je suis un étudiant'.\n2. The 3rd-person plural ending '-ent' in -er verbs is SILENT! 'Ils parlent' sounds identical to 'il parle'.\n3. General preferences ALWAYS require definite articles: 'J'aime le chocolat', 'Elle adore les films'.\n4. In plural forms, remember irregular endings: un tableau -> des tableaux, un hôpital -> des hôpitaux, le travail -> les travaux.\n5. Preposition before cities is always 'à' (J'habite à Paris, à Montréal).",
            examples = "• J'étudie l'informatique et les langues étrangères à la faculté des sciences.\n• Elle est canadienne et elle habite à Montréal.\n• Nous aimons écouter de la musique, mais nous n'aimons pas regarder la télévision.\n• Vous êtes étudiant à la Sorbonne ? — Oui, je suis étudiant en droit.\n• C'est un professeur exigeant, mais il est très gentil.",
            summaryShort = "Master campus locations (resto-U, bibliothèque vs librairie), academic subjects, nationalities, -er verb conjugations (silent -ent), definite articles (le, la, les), negation (ne...pas), and the irregular verb être.",
            summaryStandard = "Chapitre 2 covers university student life, academic subjects in arts and sciences faculties, 23 nationalities with pronunciation rules, regular -er verb conjugations with preference constructions, definite articles used for general concepts, plural exceptions (-aux, -eaux), the verb être without articles for professions, C'est vs Il est, and negation with ne...pas.",
            summaryDetailed = "Complete comprehensive unit from Vis-à-vis Chapter 2 (pp. 28–54). Focuses on university vocabulary, campus facilities (resto-U, amphi, labo), academic disciplines, nationalities and feminine consonant sounding/denasalization, preposition à with cities, Cajun history in Louisiana (Le Grand Dérangement), definite article usage with preferences, plural patterns, regular -er present tense morphology, Le Baccalauréat exam system, Quartier Latin history (Sorbonne founded 1257), and the distinction between C'est and Il/Elle est."
        )
        dao.insertStudyNote(ch2Notes)

        val ch2Flashcards = listOf(
            FlashcardEntity(chapterId = "ch2", frenchText = "l'informatique (f.)", englishText = "computer science", phoneticText = "lan-for-mah-TEEK", exampleSentence = "J'étudie l'informatique à l'université.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "le restaurant universitaire (le resto-U)", englishText = "university cafeteria / dining hall", phoneticText = "luh res-toh OO", exampleSentence = "On mange au resto-U à midi.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "l'amphithéâtre (l'amphi) (m.)", englishText = "lecture hall", phoneticText = "lahm-fee-tay-AH-truh", exampleSentence = "Le cours d'histoire a lieu dans le grand amphi.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "américain / américaine", englishText = "American (m. / f.)", phoneticText = "ah-may-ree-KAN / ah-may-ree-KEN", exampleSentence = "Il est américain, elle est américaine.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "canadien / canadienne", englishText = "Canadian (m. / f.)", phoneticText = "kah-nah-DYEN / kah-nah-DYEN", exampleSentence = "Elle est canadienne et habite à Montréal.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "J'habite à...", englishText = "I live in... (used with cities)", phoneticText = "zhah-BEET ah", exampleSentence = "J'habite à Genève.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "les langues étrangères", englishText = "foreign languages", phoneticText = "lay lahng zay-trahn-ZHAIR", exampleSentence = "J'étudie la littérature et les langues étrangères.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "le droit", englishText = "law", phoneticText = "luh DRWAH", exampleSentence = "Mon ami étudie le droit social.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "ils parlent", englishText = "they speak (Note: -ent is completely silent!)", phoneticText = "eel PARL", exampleSentence = "Ils parlent français et arabe.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "J'aime écouter...", englishText = "I like listening to... (verb + infinitive)", phoneticText = "zhem ay-koo-TAY", exampleSentence = "J'aime écouter la radio le soir.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "Je suis étudiant.", englishText = "I am a student. (NO article after être with professions)", phoneticText = "zhuh swee zay-tu-DYAHN", exampleSentence = "Vous êtes professeur ? — Non, je suis étudiant.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "vous êtes", englishText = "you are (formal / plural; note circumflex on 'ê')", phoneticText = "voo ZET", exampleSentence = "Vous êtes prêt pour l'examen ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "C'est un étudiant.", englishText = "He is a student. (Use 'c'est' before modified noun with article)", phoneticText = "say tuhn nay-tu-DYAHN", exampleSentence = "C'est un étudiant haïtien très travailleur.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "Je ne parle pas...", englishText = "I do not speak... (ne...pas negation)", phoneticText = "zhuh nuh parl PAH", exampleSentence = "Je ne parle pas espagnol.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "un tableau -> des tableaux", englishText = "board -> boards (-eau adds -x in plural)", phoneticText = "lay tah-BLOH", exampleSentence = "Il y a des tableaux dans toutes les salles.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "le travail -> les travaux", englishText = "work -> works / tasks (-ail becomes -aux)", phoneticText = "lay trah-VOH", exampleSentence = "Les étudiants ont beaucoup de travaux.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "Le Grand Dérangement", englishText = "The Great Expulsion (1755-1764) of Acadians to Louisiana", phoneticText = "luh grahn day-rahnzh-MAHN", exampleSentence = "Les Cadiens en Louisiane sont les descendants des Acadiens.", status = "NEW"),
            FlashcardEntity(chapterId = "ch2", frenchText = "le baccalauréat (le bac)", englishText = "French national secondary school diploma (requires 10/20 average)", phoneticText = "luh bah-kah-loh-RAY-ah", exampleSentence = "Après le bac, les étudiants entrent à l'université.", status = "NEW")
        )
        dao.insertFlashcards(ch2Flashcards)

        val ch2QuizItems = listOf(
            QuizItemEntity(chapterId = "ch2", topic = "Academic Subjects", questionType = "TRANSLATE_EN_FR", questionText = "How do you say 'computer science' in French?", optionsJson = "[\"l'informatique\", \"l'information\", \"l'ordinateur\", \"la computation\"]", correctAnswer = "l'informatique", explanation = "'Computer science' in French is 'l'informatique' (feminine noun).", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Grammar: Professions after Être", questionType = "MULTIPLE_CHOICE", questionText = "How do you correctly say 'I am a student' in French?", optionsJson = "[\"Je suis étudiant(e).\", \"Je suis un(e) étudiant(e).\", \"C'est étudiant.\", \"J'ai étudiant.\"]", correctAnswer = "Je suis étudiant(e).", explanation = "In French, NO article (un/une) is used after the verb être when stating your profession, religion, or nationality.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Verb Conjugation: Pronunciation", questionType = "MULTIPLE_CHOICE", questionText = "How is the 3rd person plural ending '-ent' pronounced in 'ils parlent'?", optionsJson = "[\"It is completely silent (pronounced like 'il parle')\", \"It sounds like nasal [ɑ̃]\", \"It sounds like [ɛnt]\", \"It sounds like [ont]\"]", correctAnswer = "It is completely silent (pronounced like 'il parle')", explanation = "In regular -er verbs, the 3rd person plural ending '-ent' is completely silent! 'Ils parlent' sounds identical to 'il parle'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Grammar: C'est vs Il est", questionType = "MULTIPLE_CHOICE", questionText = "Which sentence correctly introduces a student with an article?", optionsJson = "[\"C'est un étudiant haïtien.\", \"Il est un étudiant haïtien.\", \"C'est étudiant haïtien.\", \"Il est le étudiant haïtien.\"]", correctAnswer = "C'est un étudiant haïtien.", explanation = "Before a modified noun with an article (un/une/des), always use 'C'est' (or 'Ce sont'), NOT 'Il est'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Plural of Nouns", questionType = "MULTIPLE_CHOICE", questionText = "What is the correct plural form of 'le tableau'?", optionsJson = "[\"les tableaux\", \"les tableaus\", \"les tableux\", \"les tableau\"]", correctAnswer = "les tableaux", explanation = "Nouns ending in -eau add an 'x' in the plural: le tableau -> les tableaux, le bureau -> les bureaux.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Plural of Nouns", questionType = "MULTIPLE_CHOICE", questionText = "What is the correct plural form of 'le travail'?", optionsJson = "[\"les travaux\", \"les travails\", \"les travail\", \"les travaulx\"]", correctAnswer = "les travaux", explanation = "Nouns like 'le travail' change to '-aux' in the plural: les travaux.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Grammar: Preposition with Cities", questionType = "FILL_BLANK", questionText = "Complete the sentence: 'J'habite ______ Genève et vous allez ______ Montréal.'", optionsJson = "[\"à / à\", \"en / au\", \"de / pour\", \"dans / sur\"]", correctAnswer = "à / à", explanation = "Before any city name, always use the preposition 'à' to indicate location or destination.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Grammar: Negation", questionType = "MULTIPLE_CHOICE", questionText = "How do you make 'Il aime étudier' negative?", optionsJson = "[\"Il n'aime pas étudier.\", \"Il aime ne pas étudier.\", \"Il ne aime pas étudier.\", \"Il aime pas étudier.\"]", correctAnswer = "Il n'aime pas étudier.", explanation = "The negative particles ne...pas (n' before vowel) must surround the conjugated verb: 'Il n'aime pas étudier'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch2", topic = "Francophone Culture: Louisiana", questionType = "MULTIPLE_CHOICE", questionText = "Who are the ancestors of Louisiana's Cajun population?", optionsJson = "[\"French Acadians exiled during Le Grand Dérangement\", \"Parisian merchants from the 19th century\", \"Belgian explorers of the Mississippi\", \"Swiss immigrants\"]", correctAnswer = "French Acadians exiled during Le Grand Dérangement", explanation = "Cajuns (Cadiens) are descendants of French Acadians who were deported from Canada and Maine during the Great Expulsion (1755-1764).", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch2", topic = "French Education System", questionType = "MULTIPLE_CHOICE", questionText = "What average score out of 20 is required to pass the French Baccalauréat?", optionsJson = "[\"10 out of 20\", \"12 out of 20\", \"15 out of 20\", \"8 out of 20\"]", correctAnswer = "10 out of 20", explanation = "To pass the baccalauréat and qualify for French university registration, a student must obtain an average of at least 10 out of 20.", isProfessorEmphasis = true)
        )
        dao.insertQuizItems(ch2QuizItems)
    }

    private suspend fun seedChapter3Data(dao: AppDao) {
        dao.deleteDocumentsByChapter("ch3")
        dao.deleteFlashcardsByChapter("ch3")
        dao.deleteQuizItemsByChapter("ch3")

        dao.insertChapters(
            listOf(
                ChapterEntity(
                    id = "ch3",
                    number = 3,
                    title = "Chapitre 3 : La vie en couleurs",
                    description = "Personalities, clothing, colors, verb avoir & idioms, adjective agreement, question forms, prepositions à/de, and jouer à vs jouer de."
                )
            )
        )

        val ch3Documents = listOf(
            CourseDocumentEntity(
                chapterId = "ch3",
                title = "Leçon 1: Les personnalités, Les couleurs & Les vêtements",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_3_lecon_1_personnalites_couleurs_vetements.txt",
                pageOrTimestamp = "Pages 56–60",
                contentText = """
                    CHAPITRE 3 — LEÇON 1: PAROLES (LES PERSONNALITÉS, LES COULEURS, LES VÊTEMENTS)
                    
                    TRAITS DE PERSONNALITÉ (PERSONALITY TRAITS):
                    • enthousiaste, idéaliste, sincère, sensible (sensitive), travailleur / travailleuse (hardworking)
                    • sociable, sympathique (sympa) (nice), dynamique, égoïste (selfish)
                    • calme, réaliste, raisonnable, triste (sad), individualiste, excentrique, drôle (funny), paresseux / paresseuse (lazy)
                    • antipathique, conformiste, hypocrite, matérialiste, modeste, optimiste, pessimiste, généreux / généreuse
                    
                    POUR NUANCER LES DESCRIPTIONS (DEGREE ADVERBS):
                    • très (very) : "Jeanne est très calme."
                    • assez (rather / somewhat) : "Farah est assez drôle."
                    • peu (hardly / not very) : "Il est peu travailleur."
                    • un peu (a little) : "Jacques est un peu nerveux."
                    
                    LES COULEURS (COLORS):
                    • blanc / blanche (white), bleu(e) (blue), gris(e) (gray), jaune (yellow), noir(e) (black), rose (pink), rouge (red), vert(e) (green), violet / violette (purple).
                    • ATTENTION — COULEURS INVARIABLES:
                      MARRON and ORANGE NEVER agree in gender or number! They never take -e or -s:
                      "des chaussures marron" (NOT marrons!)
                      "des robes orange" (NOT oranges!)
                    
                    LES VÊTEMENTS (CLOTHING):
                    • un chapeau (hat), une casquette (baseball cap), un pull (sweater), un tee-shirt (T-shirt)
                    • une chemise (men's shirt), un chemisier (women's blouse), une robe (dress), une jupe (skirt)
                    • un pantalon (pants), un jean (jeans), un short (shorts), un collant (tights)
                    • des chaussettes (f.) (socks), des chaussures (f.) (shoes), des bottines (f.) (boots)
                    • des sandales (f.) (sandals), des tennis (m./f.) / baskets (f.) (sneakers)
                    • un sac à dos (backpack), un sac à main (handbag)
                    
                    L'APPARENCE PHYSIQUE (HAIR, EYES & HEIGHT):
                    • La taille : grand(e) (tall), petit(e) (short), de taille moyenne (medium height).
                    • Les yeux : Il/Elle a les yeux bleus / verts / marron / noirs.
                    • Les cheveux : courts, longs, frisés (curly), raides (straight); blonds, bruns, châtains (light brown), roux / rousse (redheaded), noirs, blancs.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL COLOR RULE: 'marron' and 'orange' are invariable (never add -s or -e)! Hair descriptions use 'avoir': 'Il a les yeux marron et les cheveux bruns'."
            ),
            CourseDocumentEntity(
                chapterId = "ch3",
                title = "Espaces Francophones: Bienvenue en Haïti & Toussaint Louverture",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_3_espaces_francophones_haiti.txt",
                pageOrTimestamp = "Page 61",
                contentText = """
                    ESPACES FRANCOPHONES: BIENVENUE EN HAÏTI !
                    
                    PORT-AU-PRINCE:
                    • Capital and largest city on the western part of Hispaniola (Columbus 1492). Founded originally by French buccaneers (flibustiers).
                    • Became capital of the French colony Saint-Domingue in 1770.
                    • "Gingerbread" houses: decorative Victorian-influenced architectural style from the 1880s adapted to tropical climate.
                    • Tap-taps: colorful, brightly hand-painted public transit buses.
                    • Fiche pays: Capitale : Port-au-Prince; Langues : créole haïtien, français; Monnaie : gourde haïtienne; Fête nationale : 1er janvier.
                    
                    PORTRAIT — TOUSSAINT LOUVERTURE (1743–1803) & DESSALINES:
                    • Saint-Domingue was the wealthiest French colony in the 18th century due to enslaved labor on sugar, coffee, and indigo plantations.
                    • In 1791, enslaved people led by Toussaint Louverture revolted.
                    • In 1794, the revolutionary government in Paris officially abolished slavery.
                    • In 1802, Napoleon sent an army to restore French control; Louverture was captured and died in a French fortress in 1803.
                    • Jean-Jacques Dessalines continued the independence war and proclaimed Haiti's independence on January 1, 1804 — the first and only successful slave revolution in human history.
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Haitian history: 1791 uprising led by Toussaint Louverture; independence proclaimed Jan 1, 1804 by Jean-Jacques Dessalines (first independent Black republic)."
            ),
            CourseDocumentEntity(
                chapterId = "ch3",
                title = "Leçon 2: Le verbe avoir, Les expressions idiomatiques & L'âge",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_3_lecon_2_avoir_idiomes.txt",
                pageOrTimestamp = "Pages 62–65",
                contentText = """
                    LEÇON 2: STRUCTURES — LE VERBE AVOIR & SES IDIOMES
                    
                    CONJUGAISON DU VERBE AVOIR (TO HAVE):
                    • j'ai = I have
                    • tu as = you have (informal)
                    • il / elle / on a = he / she / one has
                    • nous avons = we have (liaison /z/)
                    • vous avez = you have (liaison /z/)
                    • ils / elles ont = they have (liaison /z/: [eel-zõ] — do not confuse with 'ils sont' /s/!)
                    
                    EXPRIMER L'ÂGE:
                    Always use AVOIR for age, NEVER être!
                    • "Quel âge as-tu ?" / "Quel âge avez-vous ?" (How old are you?)
                    • "J'ai vingt ans." (I am 20 years old.)
                    • "Isabelle a quatre ans."
                    
                    EXPRESSIONS IDIOMATIQUES AVEC AVOIR:
                    • avoir chaud / froid : to be warm / cold
                    • avoir faim / soif : to be hungry / thirsty
                    • avoir raison / tort : to be right / wrong
                    • avoir sommeil : to be sleepy
                    • avoir de la chance : to be lucky
                    • avoir honte (de) : to be ashamed (of)
                    • avoir peur (de) : to be afraid (of) (J'ai peur des examens / du chien)
                    • avoir l'air (+ adjectif) : to seem, look (Tu as l'air fatigué)
                    • avoir besoin de (+ nom / infinitif) : to need (J'ai besoin d'une lampe / d'étudier)
                    • avoir envie de (+ nom / infinitif) : to feel like, want (J'ai envie de danser)
                    • avoir rendez-vous (avec) : to have an appointment / date (J'ai rendez-vous avec le prof)
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "MIDTERM GUARANTEED RULE: Always use AVOIR for age ('J'ai 20 ans', never 'Je suis 20 ans'). Idioms with avoir: avoir faim, avoir soif, avoir chaud, avoir froid, avoir raison, avoir tort."
            ),
            CourseDocumentEntity(
                chapterId = "ch3",
                title = "Leçon 2 & Culture: Les adjectifs qualificatifs, L'accord & L'art d'Haïti",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_3_lecon_2_adjectifs_accord_art.txt",
                pageOrTimestamp = "Pages 65–69",
                contentText = """
                    LEÇON 2: STRUCTURES — ACCORD ET POSITION DES ADJECTIFS
                    
                    POSITION DES ADJECTIFS:
                    In French, descriptive adjectives generally follow the noun:
                    • "une étudiante intelligente" / "un examen difficile" / "des questions précises"
                    • Rule with C'est: The adjective following 'c'est' is ALWAYS masculine singular:
                      "Le français ? C'est facile !" / "L'amour, c'est essentiel."
                    
                    RÈGLES D'ACCORD (GENDER & NUMBER AGREEMENT):
                    • Regular: +e (fém.), +s (pluriel).
                    • Ending in unaccented -e: unchanged in feminine (sincère, calme, triste).
                    • Ending in -s or -x: unchanged in masculine plural (courageux, français).
                    
                    MOTIFS IRRÉGULIERS COURANTS:
                    • -eux -> -euse : heureux / heureuse, sérieux / sérieuse, paresseux / paresseuse
                    • -eur -> -euse : travailleur / travailleuse
                    • -er -> -ère : cher / chère, fier / fière
                    • -if -> -ive : sportif / sportive, naïf / naïve
                    • -il -> -ille : gentil / gentille
                    • -el -> -elle : intellectuel / intellectuelle
                    • -ien -> -ienne : parisien / parisienne, canadien / canadienne
                    • beau -> belle, nouveau -> nouvelle
                    • chic : invariable (des filles très chic)
                    
                    PRONONCIATION DES CONSONNES FINALES:
                    • Final consonants are usually SILENT: restauran(t), cour(s), alleman(d).
                    • Final -c, -r, -f, -l are PRONOUNCED: ave(c), bonjou(r), neu(f), ma(l).
                    
                    CULTURE — L'ART D'HAÏTI:
                    • Centre d'Art (founded 1944 in Port-au-Prince) sparked the Haitian art renaissance.
                    • Cap-Haïtien School: daily life and history (Philomé and Sénèque Obin).
                    • Saint Soleil School: mountain community featuring abstract human forms, dramatic vivid colors, and Vodou spiritual ceremonies.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "ADJECTIVE PATTERNS: travailleur -> travailleuse; sportif -> sportive; fier -> fière; gentil -> gentille. The adjective after 'c'est' is always masculine singular ('C'est facile')."
            ),
            CourseDocumentEntity(
                chapterId = "ch3",
                title = "Leçon 3 & 4: Questions, Prépositions à/de, Jouer à vs Jouer de & La technique de l'oignon",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_3_lecon_3_questions_prepositions_oignon.txt",
                pageOrTimestamp = "Pages 70–83",
                contentText = """
                    LEÇON 3 & 4: QUESTIONS, PRÉPOSITIONS & PERSPECTIVES
                    
                    REPORTAGE — VÊTEMENTS DE LA FRANCOPHONIE:
                    • Le madras (Antilles) : cotton with colorful plaid patterns.
                    • Le wax et le batik (Afrique de l'Ouest) : wax double-sided cotton; batik dye printing.
                    • Le boubou (Sénégal / Afrique de l'Ouest) : long flowing robe for women and men.
                    • La djellaba (Afrique du Nord / Maroc) : hooded embroidered robe.
                    • Le paréo (Polynésie) : rectangular floral fabric worn as dress, skirt, or wrap.
                    
                    LES 4 FAÇONS DE POSER UNE QUESTION (YES/NO):
                    1. Intonation: "Vous ne parlez pas anglais ?" — "Si, un peu." (Use 'si' to say yes to a negative question!)
                    2. Tag n'est-ce pas: "Il a faim, n'est-ce pas ?"
                    3. Est-ce que: "Est-ce que Nathan étudie l'espagnol ?"
                    4. Inversion (hyphenated):
                       • Pronoun: "Est-il sincère ?" / "Aiment-ils le tennis ?"
                       • Insert -t- with 3rd-person vowel endings: "Aime-t-elle le sport ?" / "A-t-il faim ?"
                       • Noun subject: "Paul est-il sincère ?" / "Delphine travaille-t-elle beaucoup ?"
                    
                    AGREEMENT EXPRESSIONS:
                    • "Moi aussi" = Me too (agreeing with positive statement: "J'aime le tennis." — "Moi aussi !")
                    • "Moi non plus" = Me neither (agreeing with negative statement: "Je n'aime pas le rap." — "Moi non plus !")
                    
                    CONTRACTIONS DES PRÉPOSITIONS À ET DE AVEC LE / LES:
                    • à + le = au (au cinéma, au restaurant, au gymnase)
                    • à + les = aux (aux courts de tennis, aux États-Unis)
                    • à + la = à la / à + l' = à l'
                    • de + le = du (du resto-U) / de + les = des (des amis)
                    
                    LE VERBE JOUER : JOUER À VS JOUER DE:
                    • Jouer à + sport ou jeu : jouer au tennis, au football, au poker, aux cartes.
                    • Jouer de + instrument de musique : jouer du piano, de la guitare, du violon, de la flûte.
                    
                    PERSPECTIVES — LA TECHNIQUE DE L'OIGNON (QUÉBEC):
                    • Layering system for cold Canadian winters:
                      1. Base: tee-shirt thermique / caleçon long.
                      2. Isolation: pulls légers en laine ou cachemire.
                      3. Protection: doudoune imperméable en duvet et pantalon de ski.
                      4. Extremities: moufles (mitaines), bonnet / chapka.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL DISTINCTIONS: 1) jouer au football (sport) vs jouer du piano (instrument); 2) Contractions: à + le = au, à + les = aux, de + le = du, de + les = des; 3) 'Moi aussi' vs 'Moi non plus'."
            )
        )
        for (doc in ch3Documents) {
            dao.insertDocument(doc)
        }

        val ch3Notes = StudyNoteEntity(
            chapterId = "ch3",
            chapterTitle = "Chapitre 3 : La vie en couleurs",
            keyVocab = "• Personnalité: sincère, sympathique (sympa), travailleur/travailleuse, dynamique, sensible, drôle, calme, égoïste, paresseux/paresseuse\n• Vêtements: un pull, une chemise, un chemisier, un pantalon, un jean, une robe, une jupe, des chaussures, des chaussettes, des baskets, un chapeau\n• Couleurs: blanc/blanche, noir(e), rouge, bleu(e), vert(e), jaune, rose, violet(te), marron (inv.), orange (inv.)\n• Expressions avec avoir: avoir chaud/froid, avoir faim/soif, avoir sommeil, avoir de la chance, avoir peur de, avoir envie de, avoir besoin de, avoir raison/tort, avoir l'air",
            grammarRules = "• Verb AVOIR: j'ai, tu as, il/elle/on a, nous avons, vous avez, ils/elles ont. Expressing age ALWAYS uses avoir: 'J'ai vingt ans'.\n• Adjective Agreement & Position: Descriptive adjectives generally follow the noun. Irregular patterns: -eux -> -euse, -eur -> -euse, -er -> -ère, -if -> -ive, -il -> -ille. Adjectives marron and orange are invariable (never add -s or -e).\n• Questions: 4 methods (intonation, n'est-ce pas, est-ce que, inversion with hyphen). Inversion with vowel inserts -t-: 'Aime-t-elle les jupes ?'. To contradict a negative question, use 'Si' instead of 'Oui'.\n• Agreement: 'Moi aussi' (me too, with positive) vs 'Moi non plus' (me neither, with negative).\n• Prepositions à & de contractions: à + le = au, à + les = aux; de + le = du, de + les = des.\n• Jouer à vs Jouer de: 'jouer à' + sport/game (jouer au tennis, aux cartes) vs 'jouer de' + musical instrument (jouer du piano, de la guitare).",
            pronunciationTips = "• Difference between 'ils ont' (voiced liaison /z/: [eel-zõ]) and 'ils sont' (unvoiced /s/: [eel-sõ]).\n• Vowels [ø] in 'bleu' (lips rounded in a smile) vs [œ] in 'couleur'.\n• Final consonants: generally silent in French, but pronounced for -c, -r, -f, -l (avec, bonjour, neuf, mal).",
            culturalNotes = "• Haïti: Port-au-Prince, gingerbread architecture, tap-taps, Toussaint Louverture and Jean-Jacques Dessalines (first independent Black republic in 1804).\n• Vêtements de la francophonie: madras (Antilles), wax and boubou (West Africa), djellaba (North Africa), paréo (Polynesia).\n• La technique de l'oignon: Canadian layering system to withstand polar winters.",
            professorEmphasis = "⭐ PROFESSOR'S MIDTERM EMPHASIS:\n1. Age ALWAYS uses AVOIR: 'J'ai dix-neuf ans' (NEVER 'Je suis dix-neuf ans').\n2. 'Marron' and 'orange' NEVER take an -s or -e — they are completely invariable.\n3. Jouer à + sport/game ('jouer au football') vs Jouer de + instrument ('jouer du piano').\n4. Answering 'yes' to a negative question requires 'SI' ('Tu ne viens pas ? — Si, je viens !').\n5. Adjective following 'C'est' is always masculine singular ('Le français, c'est facile').",
            examples = "• Elle a les cheveux longs et bruns, et elle porte une robe bleue avec des chaussures marron.\n• J'ai vingt ans et j'ai envie de voyager en Haïti.\n• Est-ce que tu joues au tennis ou est-ce que tu joues du piano ?\n• Je n'ai pas soif. — Moi non plus !\n• Arnaud arrive au cinéma et Delphine arrive de la bibliothèque.",
            summaryShort = "Master describing personalities, clothing, and colors, conjugating avoir and using its idiomatic expressions, adjective agreement patterns, question formations, prepositions à/de, and jouer à vs jouer de.",
            summaryStandard = "Chapitre 3 covers personal descriptions (physical and psychological), clothing and invariable colors (marron, orange), the irregular verb avoir and its idioms, age formulation, adjective morphology, 4 question-forming techniques, contractions with à and de, sports vs music with jouer, and Francophone cultural heritage in Haiti, West Africa, and Quebec.",
            summaryDetailed = "Complete foundational unit from Vis-à-vis Chapter 3 (pp. 56–83). In-depth coverage of personality vocabulary and nuances, physical appearance, Haitian history (1791 slave uprising, Toussaint Louverture, 1804 independence), Haitian Art schools (Saint Soleil, Cap-Haïtien), complete conjugation and idioms of avoir, adjective agreements (-euse, -ive, -ère, -ille), question structures (complex inversion, intonation, est-ce que), prepositions à and de with contractions (au, aux, du, des), and cold-weather clothing techniques in Quebec."
        )
        dao.insertStudyNote(ch3Notes)

        val ch3Flashcards = listOf(
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir ... ans", englishText = "to be ... years old (ALWAYS use avoir for age!)", phoneticText = "ah-VWAHR ... ahn", exampleSentence = "Quel âge as-tu ? — J'ai vingt ans.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir faim / soif", englishText = "to be hungry / thirsty", phoneticText = "ah-VWAHR fan / swahf", exampleSentence = "J'ai faim, on mange au resto-U ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir chaud / froid", englishText = "to be warm / cold", phoneticText = "ah-VWAHR shoh / frwah", exampleSentence = "En hiver au Québec, on a froid.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir besoin de...", englishText = "to need... (followed by noun or infinitive)", phoneticText = "ah-VWAHR buh-ZWAN duh", exampleSentence = "J'ai besoin d'étudier pour l'examen.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir envie de...", englishText = "to feel like / want... (noun or infinitive)", phoneticText = "ah-VWAHR ahn-VEE duh", exampleSentence = "J'ai envie de danser ce soir.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "avoir raison / tort", englishText = "to be right / wrong", phoneticText = "ah-VWAHR reh-ZON / tor", exampleSentence = "Tu as raison, c'est un bon film.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "travailleur / travailleuse", englishText = "hardworking (m. / f.)", phoneticText = "trah-vah-YUR / trah-vah-YUHZ", exampleSentence = "C'est une étudiante très travailleuse.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "sportif / sportive", englishText = "athletic (m. / f.)", phoneticText = "spor-TEEF / spor-TEEV", exampleSentence = "Raphaël est petit et sportif.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "marron / orange (invariables)", englishText = "brown / orange (NEVER add -s or -e!)", phoneticText = "mah-RON / oh-RAHNZH", exampleSentence = "Elle porte des chaussures marron.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "jouer au tennis / au football", englishText = "to play tennis / soccer (jouer à + sport)", phoneticText = "zhoo-AY oh tay-NEES", exampleSentence = "Hector joue au tennis au jardin du Luxembourg.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "jouer du piano / de la guitare", englishText = "to play piano / guitar (jouer de + instrument)", phoneticText = "zhoo-AY du pyah-NOH", exampleSentence = "Philippe joue du piano et de la guitare.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "à + le = au / à + les = aux", englishText = "at/to the (mandatory contractions with à)", phoneticText = "oh", exampleSentence = "Arnaud va au cinéma et aux courts de tennis.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "de + le = du / de + les = des", englishText = "from/of the (mandatory contractions with de)", phoneticText = "du / day", exampleSentence = "Elle arrive du restaurant et parle des professeurs.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "Moi aussi / Moi non plus", englishText = "Me too (positive) / Me neither (negative)", phoneticText = "mwah oh-SEE / mwah nohn PLU", exampleSentence = "Je n'ai pas faim. — Moi non plus !", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "Si !", englishText = "Yes! (used to contradict a negative question)", phoneticText = "see", exampleSentence = "Tu ne viens pas ? — Si, je viens !", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "un pull / une chemise / une robe", englishText = "a sweater / a shirt / a dress", phoneticText = "uhn pool / lah shuh-MEEZ / lah rohb", exampleSentence = "Elle porte un pull rouge et une jupe noire.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "Toussaint Louverture", englishText = "Leader of the 1791 Haitian slave uprising", phoneticText = "too-SAN loo-vehr-TUR", exampleSentence = "Toussaint Louverture a mené la révolution en Haïti.", status = "NEW"),
            FlashcardEntity(chapterId = "ch3", frenchText = "la technique de l'oignon", englishText = "Quebec layering technique for winter cold", phoneticText = "lah tek-NEEK duh lwah-NYON", exampleSentence = "Pour l'hiver au Québec, on utilise la technique de l'oignon.", status = "NEW")
        )
        dao.insertFlashcards(ch3Flashcards)

        val ch3QuizItems = listOf(
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Expressing Age", questionType = "MULTIPLE_CHOICE", questionText = "How do you correctly say 'I am 20 years old' in French?", optionsJson = "[\"J'ai vingt ans.\", \"Je suis vingt ans.\", \"J'ai vingt années.\", \"Je suis vingt années.\"]", correctAnswer = "J'ai vingt ans.", explanation = "In French, age is ALWAYS expressed with the verb AVOIR: 'J'ai vingt ans'. Never use être for age!", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Invariable Colors", questionType = "MULTIPLE_CHOICE", questionText = "Which adjective of color NEVER changes in the feminine or plural?", optionsJson = "[\"marron\", \"vert\", \"blanc\", \"bleu\"]", correctAnswer = "marron", explanation = "'Marron' and 'orange' are invariable colors: they never take an -e or an -s (e.g., 'des chaussures marron').", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Jouer à vs Jouer de", questionType = "FILL_BLANK", questionText = "Complete the sentence: 'Naïma joue ______ tennis et Philippe joue ______ piano.'", optionsJson = "[\"au / du\", \"du / au\", \"à / de\", \"le / le\"]", correctAnswer = "au / du", explanation = "Use 'jouer à' (à + le = au) for sports/games, and 'jouer de' (de + le = du) for musical instruments.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Preposition Contractions", questionType = "FILL_BLANK", questionText = "Complete: 'Arnaud va ______ cinéma et arrive ______ bibliothèque.'", optionsJson = "[\"au / de la\", \"à le / du\", \"aux / à la\", \"au / du\"]", correctAnswer = "au / de la", explanation = "à + le contracts to 'au' (au cinéma); de + la remains 'de la' (de la bibliothèque).", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Idioms with Avoir", questionType = "MULTIPLE_CHOICE", questionText = "How do you say 'She is thirsty and hungry' in French?", optionsJson = "[\"Elle a soif et faim.\", \"Elle est soif et faim.\", \"Elle a froid et chaud.\", \"Elle est soiffée et affamée.\"]", correctAnswer = "Elle a soif et faim.", explanation = "Physical sensations of hunger and thirst use avoir: 'avoir faim' and 'avoir soif'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Agreement Expressions", questionType = "MULTIPLE_CHOICE", questionText = "Your friend says: 'Je n'ai pas sommeil.' What is the correct response to agree (Me neither)?", optionsJson = "[\"Moi non plus !\", \"Moi aussi !\", \"Pas moi !\", \"Moi non !\"]", correctAnswer = "Moi non plus !", explanation = "To agree with a negative statement ('Je n'ai pas...'), use 'Moi non plus' (Me neither).", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Responding to Negative Questions", questionType = "MULTIPLE_CHOICE", questionText = "Someone asks: 'Vous ne parlez pas français ?' How do you answer 'Yes, I do'?", optionsJson = "[\"Si, je parle français.\", \"Oui, je parle français.\", \"Non, je parle français.\", \"Moi aussi.\"]", correctAnswer = "Si, je parle français.", explanation = "To contradict a negative question and answer affirmatively, French requires 'Si' instead of 'Oui'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Grammar: Inversion with Vowels", questionType = "MULTIPLE_CHOICE", questionText = "What is the correct inverted question form of 'Elle aime la musique'?", optionsJson = "[\"Aime-t-elle la musique ?\", \"Aime-elle la musique ?\", \"Aime-t'elle la musique ?\", \"Est-elle aime la musique ?\"]", correctAnswer = "Aime-t-elle la musique ?", explanation = "When inverting a 3rd person singular verb ending in a vowel, a hyphenated '-t-' is inserted: 'Aime-t-elle la musique ?'", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch3", topic = "Francophone Culture: Haiti", questionType = "MULTIPLE_CHOICE", questionText = "In what year did Haiti declare its independence, becoming the first independent Black republic?", optionsJson = "[\"1804\", \"1789\", \"1794\", \"1830\"]", correctAnswer = "1804", explanation = "After the uprising led by Toussaint Louverture, Jean-Jacques Dessalines proclaimed Haitian independence on January 1, 1804.", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch3", topic = "Vocabulary: Clothing", questionType = "TRANSLATE_EN_FR", questionText = "How do you say 'a women's blouse' vs 'a men's button-down shirt' in French?", optionsJson = "[\"un chemisier / une chemise\", \"une chemise / un chemisier\", \"un pull / un tee-shirt\", \"une robe / une jupe\"]", correctAnswer = "un chemisier / une chemise", explanation = "'Un chemisier' is a women's blouse, whereas 'une chemise' is a button-down shirt (typically men's).", isProfessorEmphasis = false)
        )
        dao.insertQuizItems(ch3QuizItems)
    }

    private suspend fun seedChapter4Data(dao: AppDao) {
        dao.deleteDocumentsByChapter("ch4")
        dao.deleteFlashcardsByChapter("ch4")
        dao.deleteQuizItemsByChapter("ch4")

        dao.insertChapters(
            listOf(
                ChapterEntity(
                    id = "ch4",
                    number = 4,
                    title = "Chapitre 4 : À la maison",
                    description = "Housing, rooms, furniture, prepositions of location, chez, de after negation, -ir verbs, and BANGS adjectives."
                )
            )
        )

        // 1. Documents for Chapter 4
        val ch4Documents = listOf(
            CourseDocumentEntity(
                chapterId = "ch4",
                title = "Leçon 1: Les pièces de la maison, Les meubles & Les prépositions de lieu",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_4_lecon_1_pieces_meubles_prepositions.txt",
                pageOrTimestamp = "Pages 84–88",
                contentText = """
                    CHAPITRE 4 — LEÇON 1: PAROLES (À LA MAISON)
                    
                    LES PIÈCES DE LA MAISON (ROOMS OF THE HOUSE):
                    • le rez-de-chaussée : ground floor (in French, this is level 0)
                    • le premier étage : 2nd floor (first floor above ground level)
                    • le sous-sol : basement
                    • le séjour : living room (famille, télé, détente)
                    • la salle à manger : dining room
                    • la cuisine : kitchen (on prépare les repas)
                    • la chambre : bedroom (un lit, une table de nuit)
                    • la salle de bains : bathroom (baignoire, douche, lavabo)
                    • le couloir : hallway
                    • le bureau : study / office
                    • le balcon, la terrasse, le jardin, le toit
                    • le studio : one-room apartment
                    • la chambre de bonne : maid's garret room under the roof (small student lodging)
                    
                    LES MEUBLES ET OBJETS (FURNITURE & HOUSEHOLD ITEMS):
                    • un lit (bed), une commode (chest of drawers), une armoire (wardrobe), un placard (closet)
                    • un canapé (sofa), un fauteuil (armchair), une table, une chaise, une étagère (shelf)
                    • un tapis (rug), des rideaux (m.) (curtains), un miroir (mirror), une lampe (lamp), un réveil (alarm clock)
                    • un lavabo (bathroom sink), un évier (kitchen sink), une douche (shower)
                    • l'immeuble (m.) (apartment building), le loyer (rent), la clé (key)
                    
                    LES PRÉPOSITIONS DE LIEU (PREPOSITIONS OF LOCATION):
                    • dans : in, inside
                    • sur # sous : on # under ("Il y a deux livres sous la chaise.")
                    • devant # derrière : in front of # behind ("Le chien se cache derrière l'arbre.")
                    • à côté de : next to, beside
                    • en face de : across from, opposite ("La salle de bains est en face de la chambre.")
                    • entre : between ("La salle à manger est entre la cuisine et le séjour.")
                    • à gauche de # à droite de : to the left of # to the right of
                    • près de # loin de : near # far from
                    • par terre : on the floor, on the ground
                    
                    LA PRÉPOSITION CHEZ:
                    • Means "at the home/place of" or "at the office of":
                      "chez moi" (at my house), "chez toi" (at your house), "chez Éric" (at Eric's place)
                      "chez le dentiste / chez le médecin" (at the dentist's / doctor's office)
                      "On travaille chez toi ou chez moi ?"
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "FRENCH HOUSING CONVENTIONS: 'le rez-de-chaussée' is the ground level; 'le premier étage' is what Americans call the second floor! The preposition 'chez' always takes stress pronouns or persons: chez moi, chez le dentiste."
            ),
            CourseDocumentEntity(
                chapterId = "ch4",
                title = "Espaces Francophones: Bienvenue au Canada / Québec & Habitat 67",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_4_espaces_francophones_quebec.txt",
                pageOrTimestamp = "Pages 89, 95–96",
                contentText = """
                    ESPACES FRANCOPHONES: BIENVENUE AU CANADA ET AU QUÉBEC !
                    
                    LA VILLE DE QUÉBEC:
                    • Capital of the province of Quebec. Historic district (Vieux-Québec) is a UNESCO World Heritage site since 1985.
                    • Famous sights: Le Château Frontenac, the Citadelle fortifications, ramparts from the 1600s.
                    • Winter festival: Le Carnaval de Québec (ice canoe races, snow sculptures, ice palace of Bonhomme Carnaval).
                    • 80% of the Quebec population speaks French as their first language.
                    • Fiche: Canada / Le Québec; Capitale: Ottawa / Québec; Fête nationale: 1er juillet (Canada) / 24 juin (Saint-Jean-Baptiste pour le Québec).
                    
                    PORTRAITS — JACQUES CARTIER & SAMUEL DE CHAMPLAIN:
                    • Jacques Cartier (1491–1557): Navigator from Saint-Malo who explored the Gulf of St. Lawrence in the 1530s.
                    • Samuel de Champlain (c. 1567–1635): Geographer and explorer who founded Quebec City in 1608 ("Père de la Nouvelle-France"). Made alliances with Algonquin, Montagnais, and Huron First Nations.
                    
                    HABITAT 67 (MONTRÉAL):
                    • Brutalist residential housing complex built for Expo 67 in Montreal, designed by architect Moshe Safdie as his McGill master's thesis. 354 prefabricated concrete modules configured so every apartment has a private garden terrace and natural light.
                    
                    MONTRÉAL: VIVRE EN FRANÇAIS:
                    • Founded in 1642 by French settlers. Over 8 million Francophones in Quebec. Montreal is one of the largest French-speaking cities worldwide, blending European historic architecture with modern urban living.
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Quebec history: Founded in 1608 by Samuel de Champlain; Montreal founded in 1642; 80% Francophone population; Habitat 67 designed by Moshe Safdie for Expo 67."
            ),
            CourseDocumentEntity(
                chapterId = "ch4",
                title = "Leçon 2: Les articles indéfinis après ne... pas & L'absence",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_4_lecon_2_articles_negation_absence.txt",
                pageOrTimestamp = "Pages 90–92",
                contentText = """
                    LEÇON 2: STRUCTURES — LES ARTICLES INDÉFINIS APRÈS NE... PAS
                    
                    RULE: INDEFINITE ARTICLES (UN, UNE, DES) BECOME DE (D') IN THE NEGATIVE
                    To express the complete absence of something after 'ne... pas', the indefinite articles (un, une, des) change to de (d' before vowel or mute h):
                    • J'ai un canapé. -> Je n'ai pas de canapé.
                    • Elle porte une casquette. -> Elle ne porte pas de casquette.
                    • Il y a des fleurs sur la table. -> Il n'y a pas de fleurs sur la table.
                    • Il y a un livre sur la table ? -> Non, il n'y a pas de livre sur la table.
                    • Tu as des amis à Paris ? -> Je n'ai pas d'amis à Paris.
                    
                    CRITICAL EXAM EXCEPTIONS:
                    1. With the verb ÊTRE, the article DOES NOT CHANGE to de:
                       • "C'est un livre ?" -> "Non, ce n'est pas un livre." (NOT ce n'est pas de livre!)
                       • "Ce sont des étudiants ?" -> "Non, ce ne sont pas des étudiants."
                    2. Definite articles (le, la, les) DO NOT CHANGE after negation:
                       • "Elle a la voiture aujourd'hui ?" -> "Non, elle n'a pas la voiture."
                       • "Tu aimes les chats ?" -> "Non, je n'aime pas les chats."
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL EXAM TRAP: un/une/des become 'de/d'' after ne...pas ('Je n'ai pas de voiture'), EXCEPT WITH ÊTRE ('Ce n'est pas une table'). Definite articles (le/la/les) never change ('Je n'aime pas le sport')."
            ),
            CourseDocumentEntity(
                chapterId = "ch4",
                title = "Leçon 2: Les mots interrogatifs & Information questions",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_4_lecon_2_mots_interrogatifs.txt",
                pageOrTimestamp = "Pages 92–95",
                contentText = """
                    LEÇON 2: STRUCTURES — LES MOTS INTERROGATIFS
                    
                    LES ADVERBES INTERROGATIFS:
                    • où : where ("Où habite Chloé ?")
                    • quand : when ("Quand arrive le train ?") — 'qu' pronounced [k]; liaison with 'd' sounds like [t] (quand on parle).
                    • comment : how, what ("Comment vas-tu ?")
                    • pourquoi : why ("Pourquoi étudies-tu ?")
                    • combien de : how much, how many ("Combien de pièces a le logement ?")
                    
                    QUESTION FORMULAS:
                    1. With Est-ce que: Interrogative word + est-ce que + Subject + Verb:
                       • "Où est-ce que tu habites ?"
                       • "Combien de fois par semaine est-ce que Chloé joue ?"
                    2. With Inversion: Interrogative word + Verb + Hyphen + Pronoun:
                       • "Où habitez-vous ?"
                       • "Quand étudie-t-elle la musique ?"
                    3. Short Inversion with Noun Subject:
                       • "Où étudie Chloé ?" / "Combien d'instruments a Chloé ?"
                       *(Note: Pourquoi requires full pronoun inversion: "Pourquoi Chloé étudie-t-elle ?")*
                    
                    LES PRONOMS INTERROGATIFS:
                    • qui : who, whom (refers to persons):
                      "Qui étudie le français ?" / "À qui est-ce que tu parles ?"
                    • qu'est-ce que / que : what (refers to things/ideas; 'que' requires inversion):
                      "Qu'est-ce que vous visitez ?" / "Que visitez-vous ?" / "Que pense-t-il de la chambre ?"
                    
                    ANSWERING POURQUOI:
                    Always answer using "parce que" (parce qu' before vowel):
                    • "Pourquoi travailles-tu ?" — "Parce que j'ai besoin d'argent."
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "Interrogatives: 'qui' for people, 'qu'est-ce que / que' for things; 'pourquoi' is answered with 'parce que'; 'quand' has [k] sound and [t] in liaison."
            ),
            CourseDocumentEntity(
                chapterId = "ch4",
                title = "Leçon 3 & 4: Les verbes en -ir, La place de l'adjectif & La colocation",
                sourceTypeName = "Textbook TXT",
                originalFileName = "chapitre_4_lecon_3_verbes_ir_adjectifs_colocation.txt",
                pageOrTimestamp = "Pages 97–107",
                contentText = """
                    LEÇON 3 & 4: LES VERBES EN -IR, PLACE DES ADJECTIFS & PERSPECTIVES
                    
                    LES VERBES EN -IR (TYPE FINIR):
                    Present endings: -is, -it, -issons, -issez, -issent.
                    • FINIR : je finis, tu finis, il finit, nous finissons, vous finissez, ils finissent.
                    • AGIR : to act ("J'agis avec logique.")
                    • CHOISIR : to choose ("Nous choisissons des affiches.")
                    • RÉFLÉCHIR (À) : to think about, consider ("Elle réfléchit à son avenir.")
                    • RÉUSSIR (À) : to pass (an exam), to succeed ("Je réussis à l'examen.")
                    * Note: Passer un examen = to TAKE a test (False friend! To PASS is RÉUSSIR).
                    * Prepositions: finir de + inf (finish doing); finir par + inf (end up doing: 'On finit par louer un studio').
                    
                    LA PLACE DE L'ADJECTIF QUALIFICATIF (BANGS ADJECTIVES PRECEDING NOUN):
                    Adjectives that precede the noun:
                    • Beauty: beau (bel), joli
                    • Age: jeune, vieux (vieil), nouveau (nouvel), ancien (former)
                    • Number: premier, deuxième, dernier, chaque
                    • Goodness: bon, mauvais, gentil, vrai, faux
                    • Size: grand, petit, gros, long
                    
                    IRREGULAR MASCULINE FORMS BEFORE VOWEL OR MUTE H:
                    • beau -> un bel appartement (pl. de beaux appartements)
                    • nouveau -> un nouvel immeuble (pl. de nouveaux immeubles)
                    • vieux -> un vieil homme (pl. de vieux appartements)
                    
                    PLURAL DES BECOMES DE BEFORE PRECEDING ADJECTIVES:
                    • des livres -> de nouveaux livres
                    • des appartements -> de beaux appartements
                    
                    ADJECTIVES CHANGING MEANING BEFORE VS AFTER NOUN:
                    • ancien : un ancien propriétaire (former landlord) vs un meuble ancien (antique furniture)
                    • cher : ma chère amie (my dear friend) vs une voiture chère (an expensive car)
                    • grand : un grand homme (a great man) vs un homme grand (a tall man)
                    • pauvre : un pauvre homme (a pitiful/unfortunate man) vs un homme pauvre (a poor/penniless man)
                    • seul : la seule étudiante (the only student) vs une femme seule (a woman alone)
                    
                    SONDAGE — LA COLOCATION EN FRANCE:
                    • 83% choose roommates (colocation) to lower housing costs; biggest challenge: sharing chores (44%).
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL EXAM RULES: 1) BANGS adjectives precede noun; 2) Special forms before vowels: bel, nouvel, vieil; 3) 'des' becomes 'de' before plural adjective ('de beaux appartements'); 4) 'passer un examen' = to take, 'réussir à un examen' = to pass!"
            )
        )
        for (doc in ch4Documents) {
            dao.insertDocument(doc)
        }

        // 2. Comprehensive Study Notes for Chapter 4
        val ch4Notes = StudyNoteEntity(
            chapterId = "ch4",
            chapterTitle = "Chapitre 4 : À la maison",
            keyVocab = "• Pièces: le séjour (living room), la salle à manger, la cuisine, la chambre, la salle de bains, le couloir, le bureau, le balcon, le rez-de-chaussée (ground floor), le premier étage (2nd floor)\n• Meubles: le lit, la table, la chaise, le fauteuil, le canapé, l'armoire, la commode, l'étagère, le tapis, les rideaux, le miroir, la lampe, le lavabo\n• Prépositions: dans, sur, sous, devant, derrière, à côté de, en face de, entre, près de, loin de, par terre, chez (chez moi, chez le médecin)\n• Verbes en -ir: finir (finissons), choisir (choisissons), réfléchir à, réussir à, agir\n• Interrogatifs: où, quand, comment, pourquoi, combien de, qui, qu'est-ce que, que, parce que",
            grammarRules = "• Absence after Negation: Indefinite articles (un, une, des) change to 'de/d'' after ne...pas ('Je n'ai pas de canapé'). Exception: with the verb ÊTRE, the article does NOT change ('Ce n'est pas une table'). Definite articles (le, la, les) never change.\n• Regular -ir Verbs: Endings -is, -is, -it, -issons, -issez, -issent. Notice double 'ss' in plural forms ('nous finissons').\n• Adjectives Preceding Nouns (BANGS): Beauty, Age, Number, Goodness, Size precede the noun (un petit appartement, une jolie chambre).\n• Irregular Masculine Before Vowel/H: beau -> bel (un bel appartement), nouveau -> nouvel (un nouvel immeuble), vieux -> vieil (un vieil ami).\n• Plural 'des' becomes 'de': Before a preceding plural adjective, 'des' becomes 'de' ('de beaux meubles', 'de nouveaux livres').\n• Meaning change by position: 'ancien' (former before, antique after), 'cher' (dear before, expensive after), 'grand' (great before, tall after), 'pauvre' (pitiful before, penniless after).\n• False friend: 'passer un examen' means to TAKE an exam, while 'réussir à un examen' means to PASS an exam.",
            pronunciationTips = "• Vowel contrast: [u] in 'sous' (tongue back) vs [y] in 'sur' (tongue forward against teeth, lips rounded).\n• Pronunciation of 'quand': pronounced with [k], and sounds like [t] in liaison ('quand on parle' -> [kɑ̃-tõ-parl]).\n• Consonant 's': pronounced [s] at start or after consonant; pronounced [z] between two vowels (choisir); double 'ss' is always [s] (finissons).",
            culturalNotes = "• Housing in France: 'Le rez-de-chaussée' is street level (level 0), and 'le premier étage' is level 1.\n• Quebec: Founded in 1608 by Samuel de Champlain; Montreal founded in 1642; 80% Francophone.\n• Habitat 67: Montreal brutalist modular apartment complex by Moshe Safdie for Expo 67.\n• La colocation: 83% of French roommates share apartments to reduce housing budget.",
            professorEmphasis = "⭐ PROFESSOR'S MIDTERM EMPHASIS:\n1. INDEFINITE ARTICLES AFTER NEGATION: un/une/des become 'de/d'' ('Je n'ai pas de voiture'). BUT with être, they stay un/une/des ('Ce n'est pas un appartement').\n2. BANGS adjectives precede the noun. Remember masculine vowel forms: bel, nouvel, vieil ('un bel appartement').\n3. 'Des' becomes 'de' before preceding adjectives: 'de nouveaux livres' (NOT 'des nouveaux livres').\n4. FAUX AMI: 'passer un examen' = to TAKE an exam. 'Réussir à un examen' = to PASS an exam.\n5. Answer 'pourquoi' with 'parce que / parce qu''.",
            examples = "• Je n'ai pas de studio, j'ai une petite chambre de bonne au sixième étage.\n• C'est un bel appartement ancien avec de grands miroirs et de beaux rideaux.\n• Nous finissons nos devoirs et nous réussissons toujours aux examens.\n• Où est-ce que tu habites ? — J'habite près de la Sorbonne chez un ami.\n• Pourquoi est-ce que tu déménages ? — Parce que le loyer est trop cher.",
            summaryShort = "Master rooms of the house, furniture, prepositions of location (sur, sous, devant, chez), the rule of 'de' after negation, interrogative words, regular -ir verb conjugations, and BANGS adjective placement.",
            summaryStandard = "Chapitre 4 focuses on residential housing, French floor-numbering conventions, rooms and furniture, location prepositions, the transformation of indefinite articles into 'de' after ne...pas (with the être exception), question formulation with interrogative words, -ir verb morphology (finir, choisir, réussir), and adjectives preceding nouns with vowel alternations (bel, nouvel, vieil).",
            summaryDetailed = "Complete foundational unit from Vis-à-vis Chapter 4 (pp. 84–107). Covers comprehensive domestic vocabulary, spatial prepositions, the syntax of negation with de/d', question-formation paradigms (où, quand, comment, pourquoi, combien de), regular 2nd conjugation -ir verbs, nuanced adjective placement (literal following vs figurative preceding), Quebec historical geography (Champlain 1608, Montreal 1642), Habitat 67, and roommate dynamics in contemporary France."
        )
        dao.insertStudyNote(ch4Notes)

        // 3. Rich Flashcards for Chapter 4
        val ch4Flashcards = listOf(
            FlashcardEntity(chapterId = "ch4", frenchText = "le rez-de-chaussée", englishText = "ground floor (street level / floor 0 in France)", phoneticText = "luh ray-duh-shoh-SAY", exampleSentence = "La cuisine et le séjour sont au rez-de-chaussée.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "le premier étage", englishText = "second floor (in France, first floor above ground)", phoneticText = "luh pruh-MYAY ay-TAHZH", exampleSentence = "Ma chambre est au premier étage.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "chez moi / chez toi", englishText = "at my place / at your place (chez + stress pronoun)", phoneticText = "shay mwah / shay twah", exampleSentence = "On étudie chez toi ou chez moi ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "chez le médecin / le dentiste", englishText = "at the doctor's / dentist's office", phoneticText = "shay luh mayd-SAN", exampleSentence = "J'ai rendez-vous chez le dentiste.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "Je n'ai pas de...", englishText = "I don't have any... (un/une/des become DE in negative)", phoneticText = "zhuh nay pah duh", exampleSentence = "Je n'ai pas de voiture et pas d'ordinateur.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "Ce n'est pas un...", englishText = "It is not a... (article does NOT change with être)", phoneticText = "suh nay pah zuhn", exampleSentence = "Ce n'est pas un hôtel, c'est un studio.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "où / quand / comment", englishText = "where / when / how", phoneticText = "oo / kahn / koh-mahn", exampleSentence = "Où habitez-vous et quand arrivez-vous ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "pourquoi / parce que", englishText = "why / because", phoneticText = "poor-KWAH / parss KUH", exampleSentence = "Pourquoi déménages-tu ? — Parce que c'est trop petit.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "combien de...", englishText = "how much / how many...", phoneticText = "kohn-BYEN duh", exampleSentence = "Combien de pièces y a-t-il dans la maison ?", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "finir : nous finissons", englishText = "to finish: we finish (regular -ir verb with -issons)", phoneticText = "fee-nee-SOHN", exampleSentence = "Nous finissons nos devoirs avant le dîner.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "réussir à un examen", englishText = "to PASS an exam (False friend: passer = to take!)", phoneticText = "ray-u-SEER ah uhn nayg-zah-MAN", exampleSentence = "J'étudie bien et je réussis à l'examen.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "passer un examen", englishText = "to TAKE an exam (False friend: NOT to pass!)", phoneticText = "pah-SAY uhn nayg-zah-MAN", exampleSentence = "Demain, je passe un examen de français.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "un bel appartement", englishText = "a beautiful apartment (beau becomes bel before vowel)", phoneticText = "uhn bel ah-par-tuh-MAHN", exampleSentence = "Il loue un bel appartement à Paris.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "un nouvel immeuble", englishText = "a new building (nouveau becomes nouvel before vowel)", phoneticText = "uhn noo-vel eem-MUH-bluh", exampleSentence = "C'est un nouvel immeuble moderne.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "un vieil homme", englishText = "an old man (vieux becomes vieil before vowel/h)", phoneticText = "uhn vyay YUM", exampleSentence = "C'est un vieil homme très gentil.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "de beaux meubles", englishText = "beautiful furniture (des becomes DE before plural adjective)", phoneticText = "duh boh MUH-bluh", exampleSentence = "Il y a de beaux meubles dans le salon.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "un ancien hôpital vs meuble ancien", englishText = "a former hospital vs antique furniture (meaning changes)", phoneticText = "uhn nahn-syen oh-pee-TAHL", exampleSentence = "C'est un ancien hôpital avec des meubles anciens.", status = "NEW"),
            FlashcardEntity(chapterId = "ch4", frenchText = "Samuel de Champlain (1608)", englishText = "Founder of Quebec City in 1608", phoneticText = "sah-myoo-EL duh shahm-PLAN", exampleSentence = "Champlain a fondé la ville de Québec en 1608.", status = "NEW")
        )
        dao.insertFlashcards(ch4Flashcards)

        // 4. Exact Quiz Items for Chapter 4
        val ch4QuizItems = listOf(
            QuizItemEntity(chapterId = "ch4", topic = "Grammar: Negation with Articles", questionType = "FILL_BLANK", questionText = "Complete the negative sentence: 'Je n'ai pas ______ canapé dans mon studio.'", optionsJson = "[\"de\", \"un\", \"le\", \"du\"]", correctAnswer = "de", explanation = "In negative sentences with 'ne...pas', indefinite articles (un, une, des) change to 'de' (or 'd'').", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Grammar: Negation with Être Exception", questionType = "MULTIPLE_CHOICE", questionText = "Which sentence correctly demonstrates the negation rule with the verb être?", optionsJson = "[\"Ce n'est pas un livre.\", \"Ce n'est pas de livre.\", \"Ce n'est de livre.\", \"Ce n'est pas du livre.\"]", correctAnswer = "Ce n'est pas un livre.", explanation = "With the verb ÊTRE, the indefinite article does NOT change to 'de': 'Ce n'est pas un livre.'", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "False Friends (Faux Amis)", questionType = "MULTIPLE_CHOICE", questionText = "What does the French phrase 'passer un examen' actually mean in English?", optionsJson = "[\"To take an exam\", \"To pass an exam\", \"To skip an exam\", \"To grade an exam\"]", correctAnswer = "To take an exam", explanation = "'Passer un examen' means to TAKE a test. To PASS a test in French is 'réussir à un examen'.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Adjectives: Masculine Before Vowel", questionType = "FILL_BLANK", questionText = "Complete: 'David loue un ______ appartement dans le Quartier latin.'", optionsJson = "[\"bel\", \"beau\", \"belle\", \"beaux\"]", correctAnswer = "bel", explanation = "Before a masculine singular noun beginning with a vowel, 'beau' becomes 'bel': un bel appartement.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Adjectives: Plural Des to De", questionType = "FILL_BLANK", questionText = "Complete: 'J'ai acheté ______ nouveaux livres pour mon cours.'", optionsJson = "[\"de\", \"des\", \"les\", \"du\"]", correctAnswer = "de", explanation = "When an adjective precedes a plural noun, the plural indefinite article 'des' becomes 'de': de nouveaux livres.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Verb Conjugation: Regular -ir Verbs", questionType = "CONJUGATION", questionText = "Complete with the correct form of finir: 'Nous ______ nos devoirs.'", optionsJson = "[\"finissons\", \"finis\", \"finissez\", \"finient\"]", correctAnswer = "finissons", explanation = "Regular -ir verbs take '-issons' with 'nous': nous finissons, nous choisissons, nous réussissons.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Grammar: Preposition Chez", questionType = "MULTIPLE_CHOICE", questionText = "Which sentence correctly uses the preposition 'chez'?", optionsJson = "[\"J'ai rendez-vous chez le dentiste.\", \"J'ai rendez-vous à le dentiste.\", \"J'ai rendez-vous chez la maison.\", \"J'ai rendez-vous chez le studio.\"]", correctAnswer = "J'ai rendez-vous chez le dentiste.", explanation = "'Chez' is used with people, names, or professions: chez moi, chez le dentiste, chez Sophie.", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Interrogatives", questionType = "TRANSLATE_EN_FR", questionText = "How do you ask 'Why are you moving?' in French?", optionsJson = "[\"Pourquoi est-ce que tu déménages ?\", \"Quand est-ce que tu déménages ?\", \"Comment est-ce que tu déménages ?\", \"Où est-ce que tu déménages ?\"]", correctAnswer = "Pourquoi est-ce que tu déménages ?", explanation = "'Pourquoi' means 'why'. (Answered with 'Parce que...').", isProfessorEmphasis = true),
            QuizItemEntity(chapterId = "ch4", topic = "Cultural Vocabulary: Housing Floors", questionType = "MULTIPLE_CHOICE", questionText = "In a French apartment building, what is 'le premier étage'?", optionsJson = "[\"The floor above ground level (US second floor)\", \"The ground floor at street level\", \"The basement\", \"The attic / roof\"]", correctAnswer = "The floor above ground level (US second floor)", explanation = "In France, ground level is 'le rez-de-chaussée' (floor 0). The floor above it is 'le premier étage'.", isProfessorEmphasis = false),
            QuizItemEntity(chapterId = "ch4", topic = "Francophone History: Quebec", questionType = "MULTIPLE_CHOICE", questionText = "Who founded Quebec City in 1608?", optionsJson = "[\"Samuel de Champlain\", \"Jacques Cartier\", \"Robert La Salle\", \"Napoleon Bonaparte\"]", correctAnswer = "Samuel de Champlain", explanation = "Samuel de Champlain founded Quebec City in 1608 and is known as the 'Father of New France'.", isProfessorEmphasis = false)
        )
        dao.insertQuizItems(ch4QuizItems)
    }

    private fun getCh1FallbackContent(title: String): String {
        return """
            CHAPITRE 1 : PREMIERS PAS & LA VIE ÉTUDIANTE
            • Les bonnes manières: Bonjour, Bonsoir, Salut, Comment allez-vous ?
            • Les faux amis: librairie (bookstore) vs bibliothèque (library), collège (middle school) vs université.
            • Les nombres 0 à 60: zéro, un, deux... vingt et un... soixante.
            • Le calendrier: lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche (minuscules).
            • Dans la salle de classe: un bureau, un tableau, un livre, un cahier, un stylo, une chaise, une table.
            • Les articles indéfinis: un (m.), une (f.), des (pl.).
        """.trimIndent()
    }

    private fun getCh2FallbackContent(title: String): String {
        return """
            CHAPITRE 2 : NOUS, LES ÉTUDIANTS
            • Les lieux: resto-U, bibliothèque, amphi, labo, gymnase, librairie.
            • Les matières: l'informatique, les langues, la littérature, la chimie, le droit.
            • Les nationalités: américain(e), canadien(ne), français(e), sénégalais(e).
            • Les verbes en -er: parler, aimer, étudier, habiter, travailler.
            • Les articles définis: le, la, l', les. Pluriels: -eaux, -aux.
            • Le verbe être: je suis, tu es, il est, nous sommes, vous êtes, ils sont.
            • La négation: ne + verbe + pas.
        """.trimIndent()
    }

    private fun getCh3FallbackContent(title: String): String {
        return """
            CHAPITRE 3 : LA VIE EN COULEURS
            • Personnalités: sincère, sympathique, travailleur, dynamique, drôle, calme.
            • Vêtements: un pull, une chemise, un chemisier, un pantalon, une robe, des chaussures.
            • Couleurs invariables: marron, orange.
            • Verbe avoir & idiomes: avoir faim/soif/chaud/froid/peur/envie de/besoin de/sommeil/raison/tort.
            • Âge: J'ai vingt ans (avoir).
            • Questions: intonation, n'est-ce pas, est-ce que, inversion (Aime-t-elle...).
            • Jouer à (sports/jeux) vs Jouer de (instruments).
        """.trimIndent()
    }

    private fun getCh4FallbackContent(title: String): String {
        return """
            CHAPITRE 4 : À LA MAISON
            • Pièces & meubles: le séjour, la cuisine, la chambre, la salle de bains, lit, canapé, armoire.
            • Prépositions de lieu: sur, sous, devant, derrière, entre, en face de, chez.
            • Négation: un/une/des deviennent 'de/d'' après pas (sauf avec être).
            • Interrogatifs: où, quand, comment, pourquoi (parce que), combien de, qui, que.
            • Verbes en -ir: finir, choisir, réfléchir à, réussir à.
            • Adjectifs BANGS: bel, nouvel, vieil; des devient de.
        """.trimIndent()
    }

    private fun getCh5FallbackContent(title: String): String {
        return """
            CHAPITRE 5 : DE GÉNÉRATION EN GÉNÉRATION
            • La famille: grands-parents, parents, enfants, petits-enfants, beau-père, belle-mère, demi-frère/sœur, neveu, nièce.
            • Statut civil: célibataire, marié(e), divorcé(e), pacsé(e), adoptif/adoptive.
            • La météo: Quel temps fait-il ? Il fait beau/chaud/froid/frais/mauvais/(du) soleil/du vent. Il pleut, il neige.
            • Les saisons: au printemps, en été, en automne, en hiver.
            • Vêtements d'extérieur: manteau, blouson, imperméable, parapluie, bottes, gants, écharpe.
            • Adjectifs possessifs: mon/ma/mes, ton/ta/tes, son/sa/ses, notre/nos, votre/vos, leur/leurs.
              * Règle d'or: mon, ton, son devant nom féminin commençant par une voyelle (mon amie).
            • Aller & le futur proche: aller (vais, vas, va, allons, allez, vont) + infinitif.
            • Faire & idiomes: fais, fais, fait, faisons, faites, font; faire attention, faire les courses, faire la cuisine, faire la queue, faire du sport.
            • Verbes en -re: vendre, attendre, descendre, entendre, perdre, rendre, répondre à.
              * Règle d'or: 'rendre visite à' pour les personnes vs 'visiter' pour les lieux.
            • Culture: Le Sénégal (Dakar, teranga, Île de Gorée, Mati Diop); Maroc (code de la famille 2004); Les Dumas (trois générations).
        """.trimIndent()
    }

    private suspend fun seedChapter5Data(dao: AppDao) {
        dao.deleteDocumentsByChapter("ch5")
        dao.deleteFlashcardsByChapter("ch5")
        dao.deleteQuizItemsByChapter("ch5")

        // Update Chapter 5 entity
        dao.insertChapters(
            listOf(
                ChapterEntity(
                    id = "ch5",
                    number = 5,
                    title = "Chapitre 5 : De génération en génération",
                    description = "Family members, weather & seasons, possessive adjectives, aller & le futur proche, le verbe faire & idioms, -re verbs, and Les Dumas."
                )
            )
        )

        // Chapter 5 Documents (6 detailed sections from authentic textbook pp. 108–131)
        val ch5Docs = listOf(
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Leçon 1: Les générations d'une famille, Le temps & Les saisons",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_lecon_1_famille_meteo.pdf",
                pageOrTimestamp = "Textbook Pages 108–113",
                contentText = """
                    CHAPITRE 5 : DE GÉNÉRATION EN GÉNÉRATION
                    LEÇON 1: PAROLES — LA FAMILLE, LA MÉTÉO ET LES SAISONS (pp. 108–113)
                    
                    1. LES MEMBRES D'UNE FAMILLE (Family members):
                    • les grands-parents (m. pl.) : le grand-père (grandfather), la grand-mère (grandmother)
                    • les arrière-grands-parents (m. pl.) : great-grandparents
                    • les parents (m. pl.) : le père (father), la mère (mother) [Note: 'les parents' means parents or relatives]
                    • les enfants (m./f. pl.) : le fils (son), la fille (daughter)
                    • les petits-enfants (m. pl.) : le petit-fils (grandson), la petite-fille (granddaughter)
                    • les frères et sœurs : le frère (brother), la sœur (sister)
                    • demi-frère (half brother / stepbrother), demi-sœur (half sister / stepsister)
                    • l'oncle (m.), la tante (f.)
                    • le cousin, la cousine
                    • le neveu (nephew), la nièce (niece)
                    • le mari (husband), la femme (wife)
                    • La belle-famille (in-laws / stepfamily):
                      - le beau-père : father-in-law / stepfather
                      - la belle-mère : mother-in-law / stepmother
                      - le beau-fils : son-in-law / stepson
                      - la belle-fille : daughter-in-law / stepdaughter
                      - le beau-frère : brother-in-law / stepbrother
                      - la belle-sœur : sister-in-law / stepsister
                    
                    2. LE STATUT CIVIL:
                    • célibataire (single)
                    • marié(e) (married)
                    • divorcé(e) (divorced)
                    • pacsé(e) (joined legally by a PACS - pacte civil de solidarité)
                    • adoptif / adoptive (adopted, adoptive)
                    
                    3. QUEL TEMPS FAIT-IL ? (How is the weather?):
                    • Quel temps fait-il ? = How's the weather?
                    • Il fait beau. = The weather is fine / nice.
                    • Il fait mauvais. = The weather is bad.
                    • Il fait chaud. = It's hot.
                    • Il fait froid. = It's cold.
                    • Il fait frais. = It's cool.
                    • Il fait (du) soleil. = It is sunny.
                    • Il fait du vent. / Il y a du vent. = It is windy.
                    • Il pleut. = It is raining. (Infinitive: pleuvoir)
                    • Il neige. = It is snowing. (Infinitive: neiger)
                    • Le temps est nuageux. = The weather is cloudy.
                    • Le temps est orageux. = The weather is stormy.
                    
                    4. LES SAISONS (The seasons & prepositions):
                    • C'est le printemps (m.) / Nous sommes au printemps (in spring). [Preposition 'au'!]
                    • C'est l'été (m.) / Nous sommes en été (in summer). [Preposition 'en'!]
                    • C'est l'automne (m.) / Nous sommes en automne (in fall / autumn). [Preposition 'en'!]
                    • C'est l'hiver (m.) / Nous sommes en hiver (in winter). [Preposition 'en'!]
                    
                    5. LES VÊTEMENTS D'EXTÉRIEUR (Outerwear):
                    • le blouson (jacket), le manteau (coat)
                    • l'imperméable (m.) (raincoat), le parapluie (umbrella)
                    • les bottes (f. pl.) (boots), les gants (m. pl.) (gloves)
                    • l'écharpe (f.) (scarf), le maillot de bain (swimsuit)
                    
                    6. PRONONCIATION DE 'PREMIER' ET 'DERNIER':
                    • [e] before consonant or end of sentence: le premier jour, le dernier mois.
                    • [eR] (rattling r sound) before vowel sound or mute h: le premier étage, le dernier été, le premier homme.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL: 'au printemps' uses 'au', while 'en été, en automne, en hiver' use 'en'. Also note premier/dernier pronounce [eR] before vowels (premier étage) but [e] before consonants (premier jour)."
            ),
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Espaces Francophones: Bienvenue au Sénégal! (Dakar & Mati Diop)",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_senegal_mati_diop.pdf",
                pageOrTimestamp = "Textbook Page 114",
                contentText = """
                    ESPACES FRANCOPHONES: BIENVENUE AU SÉNÉGAL ! (p. 114)
                    
                    UN COUP D'ŒIL SUR DAKAR:
                    • Pays : République du Sénégal (Afrique de l'Ouest, pointe occidentale face à l'océan Atlantique).
                    • Capitale : Dakar.
                    • Habitants : Sénégalais, Sénégalaises.
                    • Langue officielle : français (avec le wolof langue nationale majeure).
                    • Monnaie : franc CFA.
                    • Fête nationale : 4 avril.
                    • Concept de Teranga : Les Dakarois accueillent toujours avec amitié : la « teranga » (hospitalité sénégalaise chaleureuse et solidaire).
                    • Le Marché Sandaga : marché réputé et vibrant de Dakar.
                    • Le Plateau : quartier des institutions (l'Assemblée nationale, le Palais présidentiel).
                    
                    L'ÎLE DE GORÉE ET LA MAISON DES ESCLAVES:
                    • Au large de Dakar, l'Île de Gorée est un joyau d'architecture coloniale aux petites rues et bougainvillées.
                    • Mais cet endroit tranquille cache un passé terrible : c'était l'un des principaux points de départ de la traite négrière transatlantique.
                    • La Maison des Esclaves évoque la tragédie de millions de femmes, d'enfants et d'hommes déportés dans des conditions inhumaines vers les Antilles et les Amériques.
                    
                    PORTRAIT: MATI DIOP (UNE VOIX, UN REGARD, UN STYLE):
                    • Franco-sénégalaise née à Paris, actrice et réalisatrice issue d'une illustre famille d'artistes :
                      - Son père Wasis Diop est un célèbre musicien.
                      - Son oncle Djibril Diop Mambéty est un réalisateur sénégalais légendaire (Touki Bouki).
                    • Œuvres majeures :
                      - 'Mille Soleils' (2013).
                      - 'Atlantique' (Grand Prix du Festival de Cannes 2019).
                    • Thématiques : regard poétique et critique démystifiant les apparences, révélation des vérités invisibles sur la jeunesse sénégalaise contemporaine, les préjugés sexistes et le drame de l'exil vers l'Europe.
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Senegal cultural notes: Dakar, concept of 'teranga', Gorée Island's slave trade memorial, and filmmaker Mati Diop."
            ),
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Leçon 2: Les adjectifs possessifs, Aller & Le futur proche",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_lecon_2_possessifs_futur_proche.pdf",
                pageOrTimestamp = "Textbook Pages 115–121",
                contentText = """
                    LEÇON 2: STRUCTURES — LES ADJECTIFS POSSESSIFS & LE FUTUR PROCHE (pp. 115–121)
                    
                    1. TABLEAU DES ADJECTIFS POSSESSIFS:
                    Possesseur         Masc. Sing.       Fém. Sing.       Pluriel (m./f.)
                    je (my)            mon père          ma mère          mes parents
                    tu (your - inf.)   ton père          ta mère          tes parents
                    il / elle (his/her) son père         sa mère          ses parents
                    nous (our)         notre père        notre mère       nos parents
                    vous (your - form.) votre père       votre mère       vos parents
                    ils / elles (their) leur père        leur mère        leurs parents
                    
                    RÈGLES D'OR DES POSSESSIFS:
                    1. Agreement rule: The possessive adjective agrees in gender and number with the NOUN POSSESSED, NOT with the possessor!
                       - Pierre aime sa sœur. (sa is feminine because sœur is feminine).
                       - Marie aime son frère. (son is masculine because frère is masculine).
                    2. VOWEL EXCEPTION (CRITICAL EXAM TRAP):
                       Before feminine singular nouns beginning with a vowel sound or mute h, use MON, TON, SON instead of ma, ta, sa:
                       - mon amie (f.) [NEVER *ma amie*]
                       - ton affiche (f.) [NEVER *ta affiche*]
                       - son histoire (f.) [NEVER *sa histoire*]
                    3. Phonetics: 'notre/votre' has open vowel [ɔ]; 'nos/vos' has closed protruded vowel [o].
                    
                    2. LE VERBE ALLER (To go) — Présent de l'indicatif:
                    • je vais
                    • tu vas
                    • il / elle / on va
                    • nous allons
                    • vous allez
                    • ils / elles vont
                    
                    3. LE FUTUR PROCHE (Near Future):
                    • Construction: Aller (conjugated in present) + INFINITIVE
                      - Paul va louer un appartement. (Paul is going to rent an apartment.)
                      - Nous allons visiter la France cet été. (We are going to visit France this summer.)
                    • Negation: 'ne...pas' wraps ONLY the conjugated form of ALLER!
                      - Sylvie ne va pas étudier ce week-end. [NEVER *Sylvie va ne pas étudier*]
                      - Nous n'allons pas manger Chez Clément.
                    • Marqueurs de temps (Time expressions):
                      - tout à l'heure (in a while)
                      - tout de suite (immediately)
                      - bientôt (soon)
                      - demain (tomorrow)
                      - ce matin, cet après-midi, ce soir, ce week-end
                      - la semaine prochaine (next week)
                      - l'année prochaine (next year)
                      - dans quatre jours (in four days)
                    
                    4. UN PETIT PLUS: LA FAMILLE EN FRANCE:
                    • Diversité des structures familiales admises comme la nouvelle norme :
                      - Famille nucléaire (enfants avec leurs deux parents)
                      - Famille monoparentale (enfant vivant avec son papa ou sa maman seule)
                      - Famille recomposée (couple avec enfants d'unions précédentes)
                      - Couple homoparental (couple d'hommes ou de femmes avec enfants)
                      - Le PACS (Pacte civil de solidarité) : union civile enregistrée
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "EXAM ALERT: 1) Feminine nouns starting with a vowel take MON/TON/SON (mon amie, ton école). 2) In futur proche negation, ne...pas surrounds ONLY aller: 'Je ne vais pas partir'."
            ),
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Reportage: Maroc — Les métamorphoses de la famille",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_reportage_maroc_famille.pdf",
                pageOrTimestamp = "Textbook Page 122",
                contentText = """
                    REPORTAGE: MAROC — LES MÉTAMORPHOSES DE LA FAMILLE (p. 122)
                    
                    LA RÉVOLUTION DU CODE DE LA FAMILLE (LA MOUDAWANA) EN 2004:
                    • En 2004, le roi Mohammed VI modernise en profondeur le code de la famille afin de favoriser l'égalité homme-femme et renforcer les droits des femmes.
                    • Dans une société marocaine traditionnellement patriarcale, il s'agit d'une transformation historique.
                    
                    NOUVEAUX DROITS ET AVANCÉES MAJEURES:
                    1. Égalité et réciprocité : L'homme n'est plus le seul 'chef' juridique de la famille ; mari et femme ont désormais des droits et obligations réciproques.
                    2. Droit au divorce : Il devient possible pour la femme de demander le divorce et d'obtenir en priorité la garde des enfants.
                    3. Âge légal du mariage : Relevé de 15 ans à 18 ans pour les femmes.
                    4. Polygamie : Soumise à des restrictions judiciaires quasi-infranchissables.
                    5. Émancipation économique : Accès accru des femmes aux postes à responsabilités en entreprise et dans la vie politique.
                    
                    ÉVOLUTION SOCIOLOGIQUE ET NOUVELLES TECHNOLOGIES:
                    • Évolution vers le modèle nucléaire : Les jeunes couples marocains préfèrent vivre de façon autonome dans leur propre logement plutôt qu'au sein de la grande maisonnée traditionnelle avec parents et beaux-parents.
                    • L'impact du numérique : Un senior observe avec humour et nostalgie :
                      « Avant, la famille marocaine c'était parents, enfants, oncles, tantes, cousins, cousines, voisins, voisines. La famille d'aujourd'hui, c'est parents, enfants et smartphones. »
                """.trimIndent(),
                isProfessorEmphasis = false,
                professorGuidance = "Reading comprehension key points: Moroccan 2004 family law reform, equal rights, marriage age at 18, and generational evolution."
            ),
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Leçon 3: Le verbe faire & Les verbes réguliers en -re",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_lecon_3_faire_verbes_re.pdf",
                pageOrTimestamp = "Textbook Pages 123–126",
                contentText = """
                    LEÇON 3: STRUCTURES — LE VERBE FAIRE & LES VERBES EN -RE (pp. 123–126)
                    
                    1. CONJUGAISON DU VERBE FAIRE (To do / To make):
                    • je fais [fɛ]
                    • tu fais [fɛ]
                    • il / elle / on fait [fɛ]
                    • nous faisons [fə-zɔ̃] (Attention: stem pronounced with schwa [ə], not [ɛ]!)
                    • vous faites [fɛt] (Irregular form! NOT *vous faisez*)
                    • ils / elles font [fɔ̃]
                    
                    2. EXPRESSIONS IDIOMATIQUES AVEC FAIRE:
                    • faire attention (à) : to pay attention (to); to watch out (for)
                    • faire la connaissance (de) : to meet (for the first time), get acquainted with
                    • faire des courses : to run errands
                    • faire les courses : to do the grocery shopping
                    • faire la cuisine : to cook
                    • faire ses devoirs : to do one's homework
                    • faire la lessive : to do the laundry
                    • faire le marché : to do the food marketing / shopping
                    • faire le ménage : to do the housework
                    • faire une promenade : to take a walk
                    • faire la queue : to stand in line
                    • faire un tour (en voiture) : to take a stroll / ride
                    • faire la vaisselle : to do the dishes
                    • faire un voyage : to take a trip
                    • faire son lit : to make one's bed
                    • Sports & activités (faire + partitif): faire du sport, du jogging, du ski, du vélo, de la voile (sailing), de l'aérobic
                    
                    3. LES VERBES EN -RE DU 3e GROUPE (Modèle VENDRE - To sell):
                    • je vends (terminaison -s)
                    • tu vends (terminaison -s)
                    • il / elle / on vend (NO ENDING added to stem! -d remains)
                    • nous vendons (terminaison -ons)
                    • vous vendez (terminaison -ez)
                    • ils / elles vendent (terminaison -ent; le 'd' est prononcé, -ent est muet)
                    
                    AUTRES VERBES RÉGULIERS EN -RE CONJUGUÉS COMME VENDRE:
                    • attendre : to wait (for) — J'attends le bus.
                    • descendre : to go down; to get off — Nous descendons à la station Bastille.
                    • entendre : to hear — Tu entends cette musique ?
                    • perdre : to lose; to waste — Il perd ses clés / son temps.
                    • rendre : to give back, return, hand in — Le commerçant rend la monnaie.
                    • répondre à : to answer — Elle répond à ses courriels.
                    
                    4. LA RÈGLE D'OR : RENDRE VISITE À vs VISITER:
                    • RENDRE VISITE À is used exclusively when visiting a PERSON:
                      - Je rends visite à mes grands-parents. (I am visiting my grandparents.)
                      - [NEVER SAY *Je visite mes grands-parents*!]
                    • VISITER is used exclusively for PLACES, MONUMENTS, CITIES, MUSEUMS:
                      - Les touristes visitent le musée d'Orsay.
                      - Nous visitons Paris.
                    
                    5. PRONONCIATION DE LA CONSONNE R:
                    • Râtelement uvulaire produit par le voile du palais.
                    • Le 'r' final se prononce toujours, SAUF pour les verbes en -er (parler, aller) et les professions en -er (boucher, plombier).
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "CRITICAL EXAM DISTINCTION: 1) 'vous faites' (never *faisez*). 2) 'rendre visite à' for PEOPLE vs 'visiter' for PLACES. 3) 'faire les courses' (groceries) vs 'faire des courses' (errands)."
            ),
            CourseDocumentEntity(
                chapterId = "ch5",
                title = "Leçon 4 & Lecture: Les Dumas — Trois générations d'une famille exceptionnelle",
                sourceTypeName = "Textbook PDF",
                originalFileName = "chapitre_5_lecture_les_trois_dumas.pdf",
                pageOrTimestamp = "Textbook Pages 127–131",
                contentText = """
                    LEÇON 4 & LECTURE: LES DUMAS — TROIS GÉNÉRATIONS D'UNE FAMILLE EXCEPTIONNELLE (pp. 127–131)
                    
                    TROIS HOMMES, TROIS DESTINS, UNE MÊME LIGNÉE HISTORIQUE:
                    
                    1. LE GÉNÉRAL ALEXANDRE DUMAS (1762–1806):
                    • Né à Saint-Domingue (Haïti), fils du marquis Alexandre Antoine Davy de la Pailleterie et d'une femme noire esclave, Marie-Cessette Dumas.
                    • Premier général de l'armée française d'origine afro-antillaise.
                    • Héros audacieux de la Révolution française et des campagnes d'Italie sous les ordres de Bonaparte (surnommé 'le Diable noir').
                    • Tragédie politique : En 1802, Bonaparte rétablit l'esclavage préalablement aboli par la Révolution. Les officiers de couleur sont exclus et destitués.
                    • Le général Dumas est révoqué sans pension ni décoration militaire et meurt dans le dénuement et l'oubli en 1806.
                    
                    2. ALEXANDRE DUMAS PÈRE (1802–1870):
                    • Fils du général. Géant métis aux yeux bleus, il devient l'un des plus grands écrivains français de tous les temps et le maître incontesté du roman historique.
                    • Chef-d'œuvres mondiaux : 'Les Trois Mousquetaires' (1844), 'Le Comte de Monte-Cristo' (1844–1846), 'La Reine Margot'.
                    • Succès populaire foudroyant auprès du public grâce aux feuilletons de presse.
                    • Malgré les attaques envieuses et les caricatures racistes de détracteurs le qualifiant d'« industriel de la littérature », sa gloire traverse les siècles. Il est aujourd'hui panthéonisé.
                    
                    3. ALEXANDRE DUMAS FILS (1824–1895):
                    • Fils illégitime de l'illustre romancier, reconnu officiellement par son père en 1831.
                    • À l'âge de 24 ans, il triomphe en publiant son roman 'La Dame aux camélias' (1848), inspiré de sa relation amoureuse passionnée avec la courtisane Marie Duplessis.
                    • Adapté avec un immense triomphe au théâtre en 1852, puis sublimé à l'opéra par Giuseppe Verdi dans le chef-d'œuvre 'La Traviata' (1853).
                    • Consécration académique suprême : Élu à la prestigieuse Académie française en 1875.
                """.trimIndent(),
                isProfessorEmphasis = true,
                professorGuidance = "Midterm reading comprehension questions test the three Dumas: General Dumas (Haiti, Napoleon, slavery abolished then reinstated), Dumas père (The Three Musketeers, Count of Monte Cristo), and Dumas fils (La Dame aux camélias, La Traviata, Académie française)."
            )
        )
        for (doc in ch5Docs) dao.insertDocument(doc)

        // Chapter 5 Study Note
        dao.insertStudyNote(
            StudyNoteEntity(
                chapterId = "ch5",
                chapterTitle = "Chapitre 5 : De génération en génération",
                keyVocab = """
                    • Famille: les grands-parents (m.), les arrière-grands-parents (m.), les parents (m.), le père, la mère, le fils, la fille, le petit-fils, la petite-fille, le frère, la sœur, le demi-frère, la demi-sœur, l'oncle, la tante, le cousin, la cousine, le neveu, la nièce, le mari, la femme, le beau-père, la belle-mère, le beau-fils, la belle-fille, le beau-frère, la belle-sœur.
                    • Statut civil: célibataire, marié(e), divorcé(e), pacsé(e), adoptif/adoptive.
                    • Météo: Quel temps fait-il ? Il fait beau, mauvais, chaud, froid, frais, du soleil, du vent. Il pleut, il neige. Le temps est nuageux / orageux.
                    • Saisons: au printemps (m.), en été (m.), en automne (m.), en hiver (m.).
                    • Vêtements d'extérieur: le blouson, le manteau, l'imperméable (m.), le parapluie, les bottes (f.), les gants (m.), l'écharpe (f.), le maillot de bain.
                    • Idiomes avec faire: faire attention (à), faire la connaissance (de), faire des courses, faire les courses, faire la cuisine, faire ses devoirs, faire la lessive, faire le marché, faire le ménage, faire une promenade, faire la queue, faire un tour, faire la vaisselle, faire un voyage, faire du sport / jogging / ski / vélo / voile.
                    • Verbes en -re: attendre, descendre, entendre, perdre, rendre, répondre à, vendre.
                    • Expression clé: rendre visite à (une personne) vs visiter (un lieu).
                """.trimIndent(),
                grammarRules = """
                    1. POSSESSIVE ADJECTIVES:
                    • Mon/ma/mes, ton/ta/tes, son/sa/ses, notre/notre/nos, votre/votre/vos, leur/leur/leurs.
                    • Agree with the noun possessed, NOT the possessor (Pierre aime sa sœur; Marie aime son frère).
                    • VOWEL EXCEPTION: Use mon, ton, son before feminine singular nouns starting with a vowel/mute h (mon amie, ton affiche, son histoire).
                    2. ALLER & LE FUTUR PROCHE:
                    • Aller: je vais, tu vas, il va, nous allons, vous allez, ils vont.
                    • Futur proche = aller + infinitive (Nous allons visiter Paris).
                    • Negation = ne + aller + pas + infinitive (Sylvie ne va pas étudier).
                    3. LE VERBE FAIRE:
                    • Je fais, tu fais, il fait, nous faisons [fəzɔ̃], vous faites [fɛt], ils font [fɔ̃].
                    4. REGULAR -RE VERBS (Vendre):
                    • Endings: -s, -s, - (no ending!), -ons, -ez, -ent.
                    • Rendre visite à (someone) vs Visiter (places/monuments).
                """.trimIndent(),
                pronunciationTips = """
                    • 'premier' and 'dernier': pronounced [e] before consonants (le premier jour), but [eR] before vowels/mute h (le premier étage, le dernier été).
                    • 'notre/votre' has open vowel [ɔ]; 'nos/vos' has closed protruded vowel [o].
                    • 'faisons' is pronounced [fə-zɔ̃] with schwa; 'faites' is [fɛt].
                    • French [R]: uvular fricative sound made by narrowing the back of the tongue against the soft palate.
                """.trimIndent(),
                culturalNotes = """
                    • Sénégal: Dakar capital, concept of 'teranga' (warm hospitality and solidarity), Île de Gorée & Maison des Esclaves (slave trade memorial), filmmaker Mati Diop (Grand Prix Cannes 2019 for Atlantique).
                    • France: Modern family diversity (nucléaire, monoparentale, recomposée, homoparentale, PACS).
                    • Maroc: 2004 Moudawana family code reform by King Mohammed VI granting equal rights to women, divorce rights, minimum marriage age at 18.
                    • Littérature: The Dumas dynasty (Général Dumas hero from Haiti, Alexandre Dumas père author of Les Trois Mousquetaires, Alexandre Dumas fils author of La Dame aux camélias / La Traviata).
                """.trimIndent(),
                professorEmphasis = "⭐ PROFESSOR'S MIDTERM ALERTS: 1) 'mon amie' (feminine vowel exception). 2) 'au printemps' vs 'en été/automne/hiver'. 3) 'ne...pas' wraps only aller in futur proche. 4) 'rendre visite à' for people vs 'visiter' for places. 5) 'vous faites' (irregular).",
                examples = """
                    • Mon amie habite avec son beau-père et sa belle-mère.
                    • Quel temps fait-il ? — Au printemps, il fait beau et frais.
                    • Nous n'allons pas sortir parce qu'il pleut.
                    • Samedi matin, je fais les courses et je rends visite à mes grands-parents.
                """.trimIndent(),
                summaryShort = "Master family relationships, weather and seasons, possessives, futur proche, faire expressions, -re verbs, and the Dumas dynasty.",
                summaryStandard = "Chapitre 5 covers comprehensive family and weather vocabulary, possessive adjective vowel exceptions, near future plans, the irregular verb faire, regular -re verbs with rendre visite à, Senegal, and Moroccan family evolutions.",
                summaryDetailed = "Complete Capstone Unit for First-Semester French. Master family trees, weather and outerwear, possessive adjectives (mon amie rule), near future syntax and negation, 15+ idiomatic expressions with faire, regular -re verb conjugations, person-vs-place visit distinctions, and cultural studies on Senegal, Morocco's 2004 family code, and the three generations of Alexandre Dumas."
            )
        )

        // Chapter 5 Flashcards (18 rich cards with American-French phonetics)
        val ch5Cards = listOf(
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "le beau-père / la belle-mère",
                englishText = "father-in-law (or stepfather) / mother-in-law (or stepmother)",
                phoneticText = "luh boh-pair / lah bel-mair",
                exampleSentence = "Mon beau-père et ma belle-mère habitent en Normandie."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "le neveu / la nièce",
                englishText = "nephew / niece",
                phoneticText = "luh nuh-vuh / lah nee-ess",
                exampleSentence = "J'ai un neveu de six ans et deux nièces jumelles."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "les arrière-grands-parents",
                englishText = "great-grandparents",
                phoneticText = "lay zahr-yair grahn-pah-rahn",
                exampleSentence = "Mes arrière-grands-parents sont nés à Dakar au Sénégal."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "Quel temps fait-il ?",
                englishText = "How's the weather? / What's the weather like?",
                phoneticText = "kel tahn fay-teel",
                exampleSentence = "Quel temps fait-il aujourd'hui ? — Il fait tellement beau !"
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "Il fait beau / mauvais / chaud / froid / frais",
                englishText = "It is nice / bad / hot / cold / cool (weather)",
                phoneticText = "eel fay boh / moh-vay / shoh / frwah / freh",
                exampleSentence = "En été, il fait chaud ; en hiver, il fait froid et il neige."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "Il pleut / Il neige",
                englishText = "It is raining / It is snowing",
                phoneticText = "eel pluh / eel nayzh",
                exampleSentence = "Prends ton parapluie parce qu'il pleut à verse."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "au printemps / en été / en automne / en hiver",
                englishText = "in spring / in summer / in fall / in winter",
                phoneticText = "oh pran-tahn / ahn nay-tay / ahn noh-tohn / ahn nee-vair",
                exampleSentence = "Nous sommes au printemps, mais en été nous allons à la plage."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "mon amie (f.)",
                englishText = "my friend (female)",
                phoneticText = "mohn nah-mee",
                exampleSentence = "Mon amie Juliette va étudier à l'Université de Dakar."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "notre maison / nos parents",
                englishText = "our house / our parents",
                phoneticText = "noh-truh may-zohn / noh pah-rahn",
                exampleSentence = "Notre maison est ancienne, et nos parents aiment le jardin."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "aller + infinitif (le futur proche)",
                englishText = "to be going to do something (near future)",
                phoneticText = "ah-lay + in-fin-ee-teef",
                exampleSentence = "Je vais arriver dans dix minutes au café."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "ne + aller + pas + infinitif",
                englishText = "negative near future (not going to do)",
                phoneticText = "nuh ... pah",
                exampleSentence = "Sylvie ne va pas étudier ce week-end ; elle va se reposer."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "faire la cuisine / faire la vaisselle",
                englishText = "to cook / to do the dishes",
                phoneticText = "fair lah kwee-zeen / fair lah vay-sel",
                exampleSentence = "Hassan fait la cuisine au restaurant et Hector fait la vaisselle."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "faire les courses vs faire des courses",
                englishText = "to do grocery shopping vs to run errands",
                phoneticText = "fair lay koors / fair day koors",
                exampleSentence = "Le samedi matin, nous faisons les courses au marché Sandaga."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "faire la queue",
                englishText = "to stand in line / wait in line",
                phoneticText = "fair lah kuh",
                exampleSentence = "Nous faisons la queue devant le cinéma UGC depuis vingt minutes."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "faire attention (à)",
                englishText = "to pay attention (to) / to watch out (for)",
                phoneticText = "fair ah-tahn-syohn ah",
                exampleSentence = "Fais attention aux faux amis dans l'examen de français !"
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "rendre visite à (une personne)",
                englishText = "to visit (someone)",
                phoneticText = "rahn-druh vee-zeet ah",
                exampleSentence = "Ce week-end, je vais rendre visite à mes grands-parents."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "la teranga",
                englishText = "Senegalese hospitality, warmth, and solidarity",
                phoneticText = "tay-rahn-gah",
                exampleSentence = "Les Sénégalais sont réputés dans le monde entier pour leur teranga."
            ),
            FlashcardEntity(
                chapterId = "ch5",
                frenchText = "Les trois Alexandre Dumas",
                englishText = "Three generations of an illustrious family",
                phoneticText = "lay trwa zah-lek-sahn-druh dzoo-mah",
                exampleSentence = "Le général Alexandre Dumas est le premier général d'origine afro-antillaise."
            )
        )
        dao.insertFlashcards(ch5Cards)

        // Chapter 5 Quiz Items (10 authentic questions)
        val ch5Quizzes = listOf(
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Weather & Seasons",
                questionType = "MULTIPLE_CHOICE",
                questionText = "Comment dit-on 'in spring' en français ?",
                optionsJson = "[\"au printemps\", \"en printemps\", \"à le printemps\", \"dans le printemps\"]",
                correctAnswer = "au printemps",
                explanation = "'Printemps' begins with a consonant and takes the contracted preposition 'au' (au printemps), whereas été, automne, and hiver begin with vowels and take 'en' (en été, en automne, en hiver).",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Possessive Adjectives",
                questionType = "FILL_BLANK",
                questionText = "Complétez la phrase : 'Amélie est une excellente camarade. C'est ___ amie.'",
                optionsJson = "[\"mon\", \"ma\", \"sa\", \"notre amie\"]",
                correctAnswer = "mon",
                explanation = "Before a feminine singular noun beginning with a vowel sound (amie), 'mon', 'ton', and 'son' must be used instead of ma, ta, sa to avoid an awkward vowel clash (hiatus).",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Aller & Futur Proche",
                questionType = "TRANSLATE_EN_FR",
                questionText = "Traduisez correctement au futur proche : 'We are not going to leave.'",
                optionsJson = "[\"Nous n'allons pas partir.\", \"Nous allons ne partir pas.\", \"Nous ne partir pas allons.\", \"Nous n'allons partir pas.\"]",
                correctAnswer = "Nous n'allons pas partir.",
                explanation = "In the negative futur proche (near future), 'ne...pas' wraps ONLY the conjugated form of aller: ne + aller + pas + infinitif.",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Expressions with Faire",
                questionType = "MULTIPLE_CHOICE",
                questionText = "Quelle expression avec 'faire' signifie précisément 'to do the grocery shopping' ?",
                optionsJson = "[\"faire les courses\", \"faire des courses\", \"faire la queue\", \"faire le ménage\"]",
                correctAnswer = "faire les courses",
                explanation = "'Faire les courses' specifically designates food and grocery shopping, while 'faire des courses' refers to running general errands.",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Verbs in -re",
                questionType = "MULTIPLE_CHOICE",
                questionText = "Quelle phrase utilise correctement le verbe de visite pour une PERSONNE ?",
                optionsJson = "[\"Je rends visite à ma grand-mère demain.\", \"Je visite ma grand-mère demain.\", \"Je fais visite ma grand-mère demain.\", \"J'attends visite à ma grand-mère demain.\"]",
                correctAnswer = "Je rends visite à ma grand-mère demain.",
                explanation = "In French, 'visiter' is strictly reserved for places, monuments, and cities. To visit a human being, one MUST use 'rendre visite à'.",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Conjugation",
                questionType = "CONJUGATION",
                questionText = "Quelle est la forme correcte du verbe faire avec 'vous' au présent ?",
                optionsJson = "[\"vous faites\", \"vous faisez\", \"vous faisiez\", \"vous font\"]",
                correctAnswer = "vous faites",
                explanation = "The verb faire is irregular in the 2nd person plural: 'vous faites'. The form *faisez* does not exist in standard French.",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Verbs in -re",
                questionType = "CONJUGATION",
                questionText = "Conjuguez le verbe régulier 'attendre' avec 'il / elle' au présent :",
                optionsJson = "[\"il attend\", \"il attendt\", \"il attende\", \"il attends\"]",
                correctAnswer = "il attend",
                explanation = "For regular -re verbs (like attendre and vendre), the 3rd person singular (il/elle/on) has no ending added to the stem ending in -d: 'il attend'.",
                isProfessorEmphasis = true
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Espaces Francophones",
                questionType = "MULTIPLE_CHOICE",
                questionText = "Sur l'Île de Gorée au Sénégal, que commémore la Maison des Esclaves ?",
                optionsJson = "[\"Le point de départ de la traite négrière transatlantique vers les Amériques\", \"La signature de l'indépendance de Dakar\", \"Le premier studio cinématographique d'Afrique de l'Ouest\", \"Le palais d'été du président sénégalais\"]",
                correctAnswer = "Le point de départ de la traite négrière transatlantique vers les Amériques",
                explanation = "L'Île de Gorée et sa Maison des Esclaves rappellent la déportation tragique des millions d'Africains captifs durant la traite négrière transatlantique.",
                isProfessorEmphasis = false
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Société & Civilisation",
                questionType = "MULTIPLE_CHOICE",
                questionText = "En 2004 au Maroc, quelle avancée a été adoptée dans le nouveau code de la famille (Moudawana) ?",
                optionsJson = "[\"L'égalité réciproque des droits entre époux et l'âge légal du mariage fixé à 18 ans pour les femmes\", \"L'interdiction légale pour les femmes de demander le divorce\", \"L'obligation de vivre avec les grands-parents sous le même toit\", \"L'interdiction des technologies et des smartphones\"]",
                correctAnswer = "L'égalité réciproque des droits entre époux et l'âge légal du mariage fixé à 18 ans pour les femmes",
                explanation = "La réforme de la Moudawana en 2004 a instauré des droits et devoirs réciproques, permis à la femme de demander le divorce, et relevé l'âge minimum du mariage féminin de 15 à 18 ans.",
                isProfessorEmphasis = false
            ),
            QuizItemEntity(
                chapterId = "ch5",
                topic = "Littérature & Histoire",
                questionType = "MULTIPLE_CHOICE",
                questionText = "Quel Alexandre Dumas est l'auteur célèbre des 'Trois Mousquetaires' et du 'Comte de Monte-Cristo' ?",
                optionsJson = "[\"Alexandre Dumas père (l'écrivain géant métis, fils du général)\", \"Le général Alexandre Dumas (le militaire né en Haïti)\", \"Alexandre Dumas fils (l'auteur de La Dame aux camélias)\", \"Le président Dumas\"]",
                correctAnswer = "Alexandre Dumas père (l'écrivain géant métis, fils du général)",
                explanation = "Alexandre Dumas père (1802-1870), fils du premier général français d'origine afro-antillaise né en Haïti, est le légendaire romancier des Trois Mousquetaires et du Comte de Monte-Cristo.",
                isProfessorEmphasis = true
            )
        )
        dao.insertQuizItems(ch5Quizzes)
    }
}
