package com.example.repository

import java.util.Locale

/**
 * High-speed offline educational dictionary and pattern engine for Beginning French I.
 * Provides instant (< 5ms), verified translations with phonetics and educational alternatives
 * when Gemini AI is waiting, offline, or when no API key is configured.
 */
object FrenchInstantDictionary {

    data class DictEntry(
        val french: String,
        val english: String,
        val phonetics: String,
        val explanation: String = "",
        val alternatives: List<String> = emptyList()
    )

    private val entries = listOf(
        // 1. Greetings & Courtesy
        DictEntry(
            french = "Bonjour !",
            english = "Hello / Good morning!",
            phonetics = "Bohn-ZHOOR !",
            explanation = "Standard formal and friendly greeting used all day until evening.",
            alternatives = listOf("Salut ! (Informal / Hi)", "Bonsoir ! (Good evening / after ~6 PM)", "Coucou ! (Casual / affectionate Hi)")
        ),
        DictEntry(
            french = "Bonsoir !",
            english = "Good evening!",
            phonetics = "Bohn-SWAHR !",
            explanation = "Greeting used after approximately 6:00 PM.",
            alternatives = listOf("Bonne soirée ! (Have a nice evening - said when parting)")
        ),
        DictEntry(
            french = "Bonne nuit !",
            english = "Good night!",
            phonetics = "Buhn NWEE !",
            explanation = "Said right before going to bed to sleep.",
            alternatives = listOf("Dors bien ! (Sleep well - informal)")
        ),
        DictEntry(
            french = "Salut !",
            english = "Hi! / Bye! (Informal)",
            phonetics = "Sah-LEW !",
            explanation = "Used only with friends, fellow students, and peers.",
            alternatives = listOf("Coucou ! (Hey there!)", "Bonjour ! (Standard polite)")
        ),
        DictEntry(
            french = "Au revoir !",
            english = "Goodbye!",
            phonetics = "Oh ruh-VWAHR !",
            explanation = "Standard polite departure phrase.",
            alternatives = listOf("À bientôt ! (See you soon!)", "À demain ! (See you tomorrow!)", "À plus tard ! (See you later!)", "Bonne journée ! (Have a great day!)")
        ),
        DictEntry(
            french = "À bientôt !",
            english = "See you soon!",
            phonetics = "Ah bee-en-TOE !",
            explanation = "Friendly parting when you will meet again in the near future.",
            alternatives = listOf("À plus tard ! (See you later!)", "À tout à l'heure ! (See you in a bit!)")
        ),
        DictEntry(
            french = "À demain !",
            english = "See you tomorrow!",
            phonetics = "Ah duh-MEHN !",
            explanation = "Common farewell among students and colleagues.",
            alternatives = listOf("À la semaine prochaine ! (See you next week!)")
        ),
        DictEntry(
            french = "Bonne journée !",
            english = "Have a great day!",
            phonetics = "Buhn zhoor-NAY !",
            explanation = "Polite wish when leaving someone during daytime.",
            alternatives = listOf("Bon après-midi ! (Good afternoon!)", "Bonne fin de journée ! (Have a good rest of your day!)")
        ),
        DictEntry(
            french = "Merci beaucoup !",
            english = "Thank you very much!",
            phonetics = "Mair-SEE boh-KOO !",
            explanation = "'Merci' = thank you; 'beaucoup' = a lot.",
            alternatives = listOf("Merci bien. (Thanks a lot)", "Mille mercis ! (A thousand thanks!)", "Je vous remercie. (Formal: I thank you)")
        ),
        DictEntry(
            french = "De rien.",
            english = "You're welcome.",
            phonetics = "Duh ree-EHN",
            explanation = "Everyday standard response to 'merci'.",
            alternatives = listOf("Je vous en prie. (Formal: My pleasure / You're welcome)", "Avec plaisir ! (With pleasure!)", "Il n'y a pas de quoi. (Don't mention it)")
        ),
        DictEntry(
            french = "S'il vous plaît.",
            english = "Please (Formal / to a group).",
            phonetics = "Seel voo PLEH",
            explanation = "Use 'vous' form with professors, strangers, and café waitstaff.",
            alternatives = listOf("S'il te plaît. (Informal: to a fellow student)", "Je vous prie. (Very formal)")
        ),
        DictEntry(
            french = "S'il te plaît.",
            english = "Please (Informal / to a peer).",
            phonetics = "Seel tuh PLEH",
            explanation = "Use 'tu' form with friends, classmates, and family members.",
            alternatives = listOf("S'il vous plaît. (Polite / formal)")
        ),
        DictEntry(
            french = "Pardon / Excusez-moi.",
            english = "Pardon me / Excuse me.",
            phonetics = "Pahr-DOHN / Ex-kew-zay MWAH",
            explanation = "Use to get past someone or before asking a stranger a question.",
            alternatives = listOf("Désolé(e). (Sorry / I apologize)")
        ),
        DictEntry(
            french = "Comment allez-vous ?",
            english = "How are you? (Formal)",
            phonetics = "Koh-mahn tah-lay VOO ?",
            explanation = "Formal with liaison. Use with professors and elders.",
            alternatives = listOf("Comment vas-tu ? (Informal to a friend)", "Ça va ? (Casual standard)", "Comment ça va ? (Everyday friendly)")
        ),
        DictEntry(
            french = "Ça va ?",
            english = "How's it going? / Are you doing well?",
            phonetics = "Sah VAH ?",
            explanation = "Most common everyday greeting. Can also be used as the answer: 'Ça va !' (I'm doing fine).",
            alternatives = listOf("Ça va bien, merci ! (I'm doing well, thanks!)", "Tout va bien. (Everything is going well)", "Comme ci, comme ça. (So-so)")
        ),
        DictEntry(
            french = "Très bien, merci !",
            english = "Very well, thank you!",
            phonetics = "Treh bee-EHN, mair-SEE !",
            explanation = "Standard positive reply to 'Comment allez-vous ?' or 'Ça va ?'.",
            alternatives = listOf("Et vous ? (And you? - formal)", "Et toi ? (And you? - informal)", "Ça roule ! (Casual: All good!)")
        ),
        DictEntry(
            french = "Oui.",
            english = "Yes.",
            phonetics = "WEE",
            explanation = "Standard affirmative.",
            alternatives = listOf("Bien sûr ! (Of course!)", "Absolument ! (Absolutely!)", "D'accord. (Okay / agreed)")
        ),
        DictEntry(
            french = "Non.",
            english = "No.",
            phonetics = "NOHN",
            explanation = "Standard negative with nasal vowel.",
            alternatives = listOf("Pas du tout. (Not at all)", "Non merci. (No thank you)")
        ),
        DictEntry(
            french = "D'accord.",
            english = "Okay / Agreed.",
            phonetics = "Dah-KOHR",
            explanation = "Universal expression to indicate agreement.",
            alternatives = listOf("Très bien. (Very good)", "Ça marche ! (Casual: That works!)")
        ),
        DictEntry(
            french = "Enchanté(e) !",
            english = "Nice to meet you!",
            phonetics = "Ahn-shahn-TAY !",
            explanation = "Said when introduced to someone. Add silent 'e' in writing if you are female.",
            alternatives = listOf("Ravi(e) de faire votre connaissance. (Formal: Delighted to meet you)")
        ),

        // 2. Introductions & Personal Info
        DictEntry(
            french = "Comment vous appelez-vous ?",
            english = "What is your name? (Formal)",
            phonetics = "Koh-mahn vooz ah-play VOO ?",
            explanation = "Formal question for introductions.",
            alternatives = listOf("Comment tu t'appelles ? (Informal / to a student)", "Quel est votre nom ? (Formal / official: What is your surname?)")
        ),
        DictEntry(
            french = "Comment tu t'appelles ?",
            english = "What is your name? (Informal)",
            phonetics = "Koh-mahn tew tah-PEL ?",
            explanation = "Informal question to fellow students on campus.",
            alternatives = listOf("Tu t'appelles comment ? (Conversational order)")
        ),
        DictEntry(
            french = "Je m'appelle Alex.",
            english = "My name is Alex.",
            phonetics = "Zhuh mah-PEL Alex",
            explanation = "Literally 'I call myself Alex'.",
            alternatives = listOf("Moi, c'est Alex. (Casual spoken)", "Mon prénom est Alex. (My first name is Alex)")
        ),
        DictEntry(
            french = "Je suis américain(e).",
            english = "I am American.",
            phonetics = "Zhuh swee ah-may-ree-KEHN (f. ah-may-ree-KEN)",
            explanation = "Nationalities act as adjectives in French; lowercase unless used as a proper noun.",
            alternatives = listOf("Je viens des États-Unis. (I come from the United States)")
        ),
        DictEntry(
            french = "Je suis étudiant(e).",
            english = "I am a student.",
            phonetics = "Zhuh swee ay-tew-dee-AHN (f. ay-tew-dee-AHNT)",
            explanation = "In French, do not use an article (un/une) with professions or student status after être.",
            alternatives = listOf("J'étudie à l'université. (I study at the university)", "Nous sommes étudiants. (We are students)")
        ),
        DictEntry(
            french = "J'habite à Genève.",
            english = "I live in Geneva.",
            phonetics = "Zhah-BEET ah Zhuh-NEHV",
            explanation = "Use preposition 'à' before cities (à Paris, à Genève, à New York).",
            alternatives = listOf("Où habitez-vous ? (Where do you live? - formal)", "Tu habites où ? (Where do you live? - informal)")
        ),
        DictEntry(
            french = "Où habitez-vous ?",
            english = "Where do you live? (Formal)",
            phonetics = "Oo ah-bee-tay VOO ?",
            explanation = "Formal question with verb habiter.",
            alternatives = listOf("Tu habites où ? (Informal)")
        ),
        DictEntry(
            french = "Parlez-vous anglais ?",
            english = "Do you speak English? (Formal)",
            phonetics = "Pahr-lay VOO ahn-GLEH ?",
            explanation = "Polite inquiry when asking someone if they speak English.",
            alternatives = listOf("Tu parles anglais ? (Informal)", "Est-ce que vous parlez anglais ? (With est-ce que)")
        ),
        DictEntry(
            french = "Je parle un peu français.",
            english = "I speak a little French.",
            phonetics = "Zhuh pahrl uhn puh frahn-SEH",
            explanation = "'Un peu' = a little.",
            alternatives = listOf("J'apprends le français. (I am learning French)", "Je ne parle pas très bien français. (I don't speak French very well)")
        ),
        DictEntry(
            french = "Je ne comprends pas.",
            english = "I don't understand.",
            phonetics = "Zhuh nuh kohm-prahn PAH",
            explanation = "Negation ne...pas wraps the conjugated verb.",
            alternatives = listOf("Pouvez-vous répéter, s'il vous plaît ? (Can you repeat, please?)", "Répétez plus lentement, s'il vous plaît. (Repeat more slowly, please)")
        ),
        DictEntry(
            french = "Pouvez-vous répéter, s'il vous plaît ?",
            english = "Could you please repeat that?",
            phonetics = "Poo-vay VOO ray-pay-TAY, seel voo PLEH ?",
            explanation = "Essential classroom phrase when the professor speaks too quickly.",
            alternatives = listOf("Plus lentement, s'il vous plaît. (More slowly, please)", "Comment ? (Pardon?)")
        ),

        // 3. Café & Restaurant Ordering
        DictEntry(
            french = "Je voudrais un café, s'il vous plaît.",
            english = "I would like a coffee, please.",
            phonetics = "Zhuh voo-DREH uhn kah-FAY, seel voo PLEH",
            explanation = "'Je voudrais' is the polite conditional of vouloir (to want). In France, 'un café' is an espresso.",
            alternatives = listOf("Un café, s'il vous plaît. (Short order)", "Je voudrais un café au lait. (Coffee with steamed milk)", "Un thé, s'il vous plaît. (A tea, please)")
        ),
        DictEntry(
            french = "Un café au lait, s'il vous plaît.",
            english = "A coffee with milk, please.",
            phonetics = "Uhn kah-FAY oh LEH, seel voo PLEH",
            explanation = "'Au lait' = with milk.",
            alternatives = listOf("Un café crème, s'il vous plaît. (Coffee with cream)")
        ),
        DictEntry(
            french = "Je voudrais un croissant, s'il vous plaît.",
            english = "I would like a croissant, please.",
            phonetics = "Zhuh voo-DREH uhn krwah-SAHN, seel voo PLEH",
            explanation = "Classic French pastry order.",
            alternatives = listOf("Un pain au chocolat, s'il vous plaît. (A chocolate croissant)")
        ),
        DictEntry(
            french = "De l'eau, s'il vous plaît.",
            english = "Some water, please.",
            phonetics = "Duh LOH, seel voo PLEH",
            explanation = "Partitive article 'de l'' before feminine noun starting with a vowel.",
            alternatives = listOf("Une carafe d'eau, s'il vous plaît. (A free pitcher of tap water at a restaurant)", "Une bouteille d'eau minérale. (A bottle of mineral water)")
        ),
        DictEntry(
            french = "L'addition, s'il vous plaît.",
            english = "The bill/check, please.",
            phonetics = "Lah-dee-SYOHN, seel voo PLEH",
            explanation = "In France, waitstaff will never bring the bill until you explicitly ask for it.",
            alternatives = listOf("Combien je vous dois ? (How much do I owe you?)", "Je peux payer par carte ? (Can I pay by card?)")
        ),
        DictEntry(
            french = "Combien ça coûte ?",
            english = "How much does that cost?",
            phonetics = "Kohm-bee-EHN sah KOOT ?",
            explanation = "Universal question to ask the price of an item.",
            alternatives = listOf("C'est combien ? (How much is it? / everyday)", "Ça fait combien ? (What does that come to?)")
        ),
        DictEntry(
            french = "J'ai faim.",
            english = "I am hungry.",
            phonetics = "Zhay FEHN",
            explanation = "French uses the verb avoir (to have) for bodily sensations: literally 'I have hunger'.",
            alternatives = listOf("J'ai très faim. (I'm very hungry)", "Allons manger ! (Let's go eat!)")
        ),
        DictEntry(
            french = "J'ai soif.",
            english = "I am thirsty.",
            phonetics = "Zhay SWAHF",
            explanation = "French uses avoir: literally 'I have thirst'.",
            alternatives = listOf("Je voudrais boire quelque chose. (I would like something to drink)")
        ),

        // 4. Campus Places & Classroom Objects (From Textbook)
        DictEntry(
            french = "Où est la bibliothèque ?",
            english = "Where is the library?",
            phonetics = "Oo eh lah bee-blee-oh-TEK ?",
            explanation = "'Où est' means 'where is'. Note false friend: 'librairie' is a bookstore; 'bibliothèque' is library!",
            alternatives = listOf("Où se trouve la bibliothèque ? (Where is the library situated?)", "Pour aller à la bibliothèque, s'il vous plaît ? (How do I get to the library?)")
        ),
        DictEntry(
            french = "Où sont les toilettes ?",
            english = "Where are the restrooms/bathrooms?",
            phonetics = "Oo sohn lay twah-LET ?",
            explanation = "Always plural in French: 'les toilettes'.",
            alternatives = listOf("Pardon, les toilettes, s'il vous plaît ? (Excuse me, restrooms please?)")
        ),
        DictEntry(
            french = "La salle de classe.",
            english = "The classroom.",
            phonetics = "Lah sahl duh KLAHS",
            explanation = "Feminine noun compound: room of class.",
            alternatives = listOf("Dans la salle de classe. (In the classroom)")
        ),
        DictEntry(
            french = "Le professeur.",
            english = "The professor / teacher.",
            phonetics = "Luh proh-feh-SOOR",
            explanation = "Often abbreviated to 'le prof' or 'la prof' in student speech.",
            alternatives = listOf("L'enseignant(e). (The instructor)")
        ),
        DictEntry(
            french = "Le livre.",
            english = "The book.",
            phonetics = "Luh LEEVR",
            explanation = "Masculine noun: le livre. (Contrast: la livre = pound).",
            alternatives = listOf("Le manuel de français. (The French textbook)")
        ),
        DictEntry(
            french = "Le cahier.",
            english = "The notebook.",
            phonetics = "Luh kah-YAY",
            explanation = "Masculine noun.",
            alternatives = listOf("Un bloc-notes. (A notepad)")
        ),
        DictEntry(
            french = "Le stylo.",
            english = "The pen.",
            phonetics = "Luh stee-LOH",
            explanation = "Masculine noun.",
            alternatives = listOf("Le crayon. (The pencil)", "Une gomme. (An eraser)")
        ),
        DictEntry(
            french = "Le crayon.",
            english = "The pencil.",
            phonetics = "Luh kreh-YOHN",
            explanation = "Masculine noun.",
            alternatives = listOf("Un taille-crayon. (A pencil sharpener)")
        ),
        DictEntry(
            french = "Les devoirs.",
            english = "The homework.",
            phonetics = "Lay duh-VWAHR",
            explanation = "Always plural when referring to academic homework.",
            alternatives = listOf("Faire les devoirs. (To do homework)")
        ),
        DictEntry(
            french = "L'examen.",
            english = "The exam / test.",
            phonetics = "Leg-zah-MEHN",
            explanation = "Masculine noun starting with vowel.",
            alternatives = listOf("Une interrogation. (A quiz)", "Passer un examen. (To take an exam - note false friend: not 'pass'!)")
        ),
        DictEntry(
            french = "Le resto-U.",
            english = "The university cafeteria.",
            phonetics = "Luh reh-stoh OO",
            explanation = "Short for 'restaurant universitaire' - subsidized student dining hall in France.",
            alternatives = listOf("La cafétéria. (The cafeteria)")
        ),
        DictEntry(
            french = "La cité-U.",
            english = "The student dormitory / campus housing.",
            phonetics = "Lah see-tay OO",
            explanation = "Short for 'cité universitaire' - university residence halls.",
            alternatives = listOf("La résidence étudiante. (Student residence)")
        ),
        DictEntry(
            french = "La librairie.",
            english = "The bookstore.",
            phonetics = "Lah lee-breh-REE",
            explanation = "CAUTION FALSE FRIEND: 'librairie' sells books (bookstore); 'bibliothèque' lends books (library).",
            alternatives = listOf("Acheter des livres à la librairie. (To buy books at the bookstore)")
        ),

        // 5. Question Words
        DictEntry(
            french = "Qu'est-ce que c'est ?",
            english = "What is this / that?",
            phonetics = "Kess kuh SEH ?",
            explanation = "Universal question for asking what an object is.",
            alternatives = listOf("C'est quoi ? (Casual spoken order: It's what?)")
        ),
        DictEntry(
            french = "Qui est-ce ?",
            english = "Who is this / that?",
            phonetics = "Kee ESS ?",
            explanation = "Question used to identify a person.",
            alternatives = listOf("C'est qui ? (Casual spoken: Who is it?)")
        ),
        DictEntry(
            french = "Pourquoi ?",
            english = "Why?",
            phonetics = "Poor-KWAH ?",
            explanation = "Interrogative adverb. Answer always begins with 'Parce que...' (Because...).",
            alternatives = listOf("Pourquoi pas ? (Why not?)")
        ),
        DictEntry(
            french = "Parce que...",
            english = "Because...",
            phonetics = "Pahr-skuh...",
            explanation = "Becomes 'parce qu'' before vowels (parce qu'il, parce qu'elle).",
            alternatives = listOf("Car... (Formal: for / because)")
        ),
        DictEntry(
            french = "Quand ?",
            english = "When?",
            phonetics = "KAHN ?",
            explanation = "Question word for time.",
            alternatives = listOf("À quelle heure ? (At what time?)")
        ),
        DictEntry(
            french = "Où ?",
            english = "Where?",
            phonetics = "OO ?",
            explanation = "'Où' with grave accent means 'where'; without accent 'ou' means 'or'.",
            alternatives = listOf("D'où ? (From where?)")
        ),
        DictEntry(
            french = "Comment ?",
            english = "How? / What? (Pardon?)",
            phonetics = "Koh-MAHN ?",
            explanation = "Can mean 'how', or used alone to mean 'pardon / what did you say?'.",
            alternatives = listOf("Comment dit-on... en français ? (How do you say... in French?)")
        ),
        DictEntry(
            french = "Comment dit-on en français ?",
            english = "How do you say that in French?",
            phonetics = "Koh-mahn dee-TOHN ahn frahn-SEH ?",
            explanation = "Indispensable study phrase for Beginning French learners.",
            alternatives = listOf("Que veut dire ce mot ? (What does this word mean?)")
        ),
        DictEntry(
            french = "Que veut dire ce mot ?",
            english = "What does this word mean?",
            phonetics = "Kuh vuh DEER suh MOH ?",
            explanation = "'Vouloir dire' = to mean (literally 'to want to say').",
            alternatives = listOf("Ça veut dire quoi ? (Casual: What does that mean?)")
        ),

        // 6. Core Verbs in Common Forms
        DictEntry(
            french = "Je suis...",
            english = "I am...",
            phonetics = "Zhuh SWEE...",
            explanation = "Present tense of irregular verb 'être' (to be) for 'je'.",
            alternatives = listOf("Tu es (You are - informal)", "Il est / Elle est (He / She is)", "Nous sommes (We are)", "Vous êtes (You are - formal/plural)", "Ils sont (They are)")
        ),
        DictEntry(
            french = "J'ai...",
            english = "I have...",
            phonetics = "ZHAY...",
            explanation = "Present tense of irregular verb 'avoir' (to have). Elision mandatory: 'je' + 'ai' = 'j'ai'.",
            alternatives = listOf("Tu as (You have)", "Il a / Elle a (He / She has)", "Nous avons (We have)", "Vous avez (You have)", "Ils ont (They have)")
        ),
        DictEntry(
            french = "Je vais...",
            english = "I go / I am going...",
            phonetics = "Zhuh VEH...",
            explanation = "Present tense of verb 'aller' (to go). Used in le futur proche: 'je vais + infinitive'.",
            alternatives = listOf("Je vais bien. (I am doing well)", "Je vais à la bibliothèque. (I am going to the library)", "Tu vas (You go)")
        ),
        DictEntry(
            french = "Je fais...",
            english = "I do / I make...",
            phonetics = "Zhuh FEH...",
            explanation = "Verb 'faire' (to do/make). Used in sports (faire du vélo) and weather (il fait beau).",
            alternatives = listOf("Qu'est-ce que tu fais ? (What are you doing?)", "Il fait beau. (The weather is nice)")
        ),
        DictEntry(
            french = "J'aime le français.",
            english = "I like / love French.",
            phonetics = "ZHEHM luh frahn-SEH",
            explanation = "Use definite article (le/la/les) with verbs of preference (aimer, adorer, préférer, détester).",
            alternatives = listOf("J'adore le français ! (I adore French!)", "Je préfère l'anglais. (I prefer English)")
        ),
        DictEntry(
            french = "Je ne sais pas.",
            english = "I don't know.",
            phonetics = "Zhuh nuh say PAH",
            explanation = "From verb 'savoir' (to know facts). In casual speech, French people often say 'Chais pas'.",
            alternatives = listOf("Aucune idée. (No idea)", "Je ne sais pas du tout. (I don't know at all)")
        ),
        DictEntry(
            french = "Pouvez-vous m'aider, s'il vous plaît ?",
            english = "Could you help me, please?",
            phonetics = "Poo-vay VOO may-DAY, seel voo PLEH ?",
            explanation = "Polite request for assistance with verb pouvoir (can/able).",
            alternatives = listOf("J'ai besoin d'aide. (I need help)")
        ),
        DictEntry(
            french = "J'ai besoin d'aide.",
            english = "I need help.",
            phonetics = "Zhay buh-ZWEHN DED",
            explanation = "'Avoir besoin de' = to need.",
            alternatives = listOf("À l'aide ! (Help! - emergency)")
        ),

        // 7. Numbers & Time
        DictEntry(
            french = "Un, deux, trois, quatre, cinq.",
            english = "One, two, three, four, five.",
            phonetics = "Uhn, duh, trwah, kahtr, sank.",
            explanation = "Cardinal numbers 1 to 5.",
            alternatives = listOf("Six, sept, huit, neuf, dix. (6, 7, 8, 9, 10)")
        ),
        DictEntry(
            french = "Six, sept, huit, neuf, dix.",
            english = "Six, seven, eight, nine, ten.",
            phonetics = "Sees, set, weet, nuhf, dees.",
            explanation = "Cardinal numbers 6 to 10.",
            alternatives = listOf("Onze, douze, treize, quatorze, quinze. (11, 12, 13, 14, 15)")
        ),
        DictEntry(
            french = "Quelle heure est-il ?",
            english = "What time is it?",
            phonetics = "Kel UHR eh teel ?",
            explanation = "Standard question to ask the time.",
            alternatives = listOf("Il est quelle heure ? (Conversational order)", "Vous avez l'heure ? (Do you have the time? - polite)")
        ),
        DictEntry(
            french = "Il fait beau aujourd'hui.",
            english = "The weather is nice today.",
            phonetics = "Eel feh BOH oh-zhoor-DWEE",
            explanation = "Weather expressions use 'il fait' + adjective (il fait beau, il fait chaud, il fait froid).",
            alternatives = listOf("Il fait froid. (It's cold)", "Il fait chaud. (It's hot)", "Il pleut. (It is raining)")
        )
    )

    // Lookup keyword map for fast indexed search
    private val keywordMap: Map<String, DictEntry> by lazy {
        val map = mutableMapOf<String, DictEntry>()
        for (entry in entries) {
            val fClean = normalizeKey(entry.french)
            val eClean = normalizeKey(entry.english)
            map[fClean] = entry
            map[eClean] = entry

            // Also map key sub-phrases
            val subKeys = extractSubKeys(entry)
            for (sub in subKeys) {
                if (!map.containsKey(sub)) {
                    map[sub] = entry
                }
            }
        }
        map
    }

    private fun normalizeKey(str: String): String {
        return str.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\d\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractSubKeys(entry: DictEntry): List<String> {
        val keys = mutableListOf<String>()
        val f = normalizeKey(entry.french)
        val e = normalizeKey(entry.english)

        // Add core French phrases
        if (f.contains("bonjour")) keys.add("bonjour")
        if (f.contains("bonsoir")) keys.add("bonsoir")
        if (f.contains("salut")) keys.add("salut")
        if (f.contains("au revoir")) keys.add("au revoir")
        if (f.contains("merci")) keys.add("merci")
        if (f.contains("de rien")) keys.add("de rien")
        if (f.contains("s il vous plait")) keys.add("s il vous plait")
        if (f.contains("s il te plait")) keys.add("s il te plait")
        if (f.contains("pardon")) keys.add("pardon")
        if (f.contains("excusez moi")) keys.add("excusez moi")
        if (f.contains("comment allez vous")) keys.add("comment allez vous")
        if (f.contains("ca va")) keys.add("ca va")
        if (f.contains("oui")) keys.add("oui")
        if (f.contains("non")) keys.add("non")
        if (f.contains("cafe au lait")) keys.add("cafe au lait")
        if (f.contains("un cafe")) keys.add("un cafe")
        if (f.contains("croissant")) keys.add("croissant")
        if (f.contains("addition")) keys.add("l addition")
        if (f.contains("bibliotheque")) keys.add("bibliotheque")
        if (f.contains("ou est")) keys.add("ou est")
        if (f.contains("toilettes")) keys.add("toilettes")
        if (f.contains("je m appelle")) keys.add("je m appelle")
        if (f.contains("je suis")) keys.add("je suis")
        if (f.contains("j ai")) keys.add("j ai")
        if (f.contains("je voudrais")) keys.add("je voudrais")
        if (f.contains("je ne comprends pas")) keys.add("je ne comprends pas")

        // Add core English phrases
        if (e.contains("hello") || e.contains("good morning")) keys.add("hello")
        if (e.contains("good evening")) keys.add("good evening")
        if (e.contains("goodbye") || e.contains("bye")) keys.add("goodbye")
        if (e.contains("thank you") || e.contains("thanks")) keys.add("thank you")
        if (e.contains("you re welcome") || e.contains("welcome")) keys.add("you re welcome")
        if (e.contains("please")) keys.add("please")
        if (e.contains("excuse me") || e.contains("pardon")) keys.add("excuse me")
        if (e.contains("how are you")) keys.add("how are you")
        if (e.contains("what is your name")) keys.add("what is your name")
        if (e.contains("my name is")) keys.add("my name is")
        if (e.contains("where is the library")) keys.add("where is the library")
        if (e.contains("where is")) keys.add("where is")
        if (e.contains("where are the restrooms") || e.contains("bathroom")) keys.add("restroom")
        if (e.contains("coffee")) keys.add("coffee")
        if (e.contains("water")) keys.add("water")
        if (e.contains("bill") || e.contains("check")) keys.add("the bill")
        if (e.contains("how much")) keys.add("how much")
        if (e.contains("hungry")) keys.add("hungry")
        if (e.contains("thirsty")) keys.add("thirsty")
        if (e.contains("i am a student") || e.contains("student")) keys.add("student")
        if (e.contains("i don t understand") || e.contains("understand")) keys.add("i don t understand")
        if (e.contains("repeat")) keys.add("repeat")
        if (e.contains("i would like") || e.contains("i want")) keys.add("i would like")
        if (e.contains("help")) keys.add("help")

        return keys
    }

    /**
     * Resolves an instant, verified translation.
     * Guaranteed to execute synchronously in < 1ms without network calls.
     */
    fun findInstantTranslation(
        input: String,
        detectedLang: String,
        showPhonetics: Boolean = true
    ): TranslationResult? {
        val clean = normalizeKey(input)
        if (clean.isBlank()) return null

        val isEnglish = detectedLang.equals("English", ignoreCase = true)
        val targetLang = if (isEnglish) "French" else "English"

        // 1. Direct match
        val directMatch = keywordMap[clean]
        if (directMatch != null) {
            return formatResult(directMatch, input, isEnglish, targetLang, showPhonetics)
        }

        // 2. Substring & Keyword Search
        var bestEntry: DictEntry? = null
        var maxScore = 0

        for (entry in entries) {
            val candidateNorm = if (isEnglish) normalizeKey(entry.english) else normalizeKey(entry.french)
            val score = calculateMatchScore(clean, candidateNorm)
            if (score > maxScore && score >= 5) {
                maxScore = score
                bestEntry = entry
            }
        }

        if (bestEntry != null) {
            return formatResult(bestEntry, input, isEnglish, targetLang, showPhonetics)
        }

        // 3. Special dynamic pattern matches (e.g. "My name is John" -> "Je m'appelle John.")
        if (isEnglish && (clean.contains("my name is") || clean.contains("i m called"))) {
            val name = input.substringAfter("name is", "").substringAfter("called", "").trim().ifBlank { "Alex" }
            return TranslationResult(
                frenchText = "Je m'appelle $name.",
                englishText = input,
                phoneticText = if (showPhonetics) "Zhuh mah-PEL $name" else "",
                explanation = "Literally 'I call myself...'. Standard French introduction.",
                detectedLanguage = "English",
                targetLanguage = "French",
                alternatives = listOf("Moi, c'est $name. (Casual spoken)", "Mon prénom est $name. (My first name is $name)"),
                isCourseGrounded = true
            )
        }

        if (!isEnglish && clean.contains("je m appelle")) {
            val name = input.substringAfter("m'appelle", "").substringAfter("appelle", "").trim()
            return TranslationResult(
                frenchText = input,
                englishText = "My name is $name.",
                phoneticText = if (showPhonetics) "Zhuh mah-PEL $name" else "",
                explanation = "Used for personal introductions.",
                detectedLanguage = "French",
                targetLanguage = "English",
                alternatives = listOf("I am named $name.", "I'm called $name."),
                isCourseGrounded = true
            )
        }

        return null
    }

    private fun calculateMatchScore(input: String, candidate: String): Int {
        if (input == candidate) return 100
        if (candidate.contains(input)) return 50
        if (input.contains(candidate)) return 40

        val inputTokens = input.split(" ").filter { it.length > 2 }
        val candidateTokens = candidate.split(" ").filter { it.length > 2 }
        var matches = 0
        for (token in inputTokens) {
            if (candidateTokens.contains(token)) {
                matches += 10
            }
        }
        return matches
    }

    private fun formatResult(
        entry: DictEntry,
        originalInput: String,
        isEnglish: Boolean,
        targetLang: String,
        showPhonetics: Boolean
    ): TranslationResult {
        return TranslationResult(
            frenchText = entry.french,
            englishText = if (isEnglish) originalInput else entry.english,
            phoneticText = if (showPhonetics) entry.phonetics else "",
            explanation = entry.explanation,
            alternatives = entry.alternatives,
            detectedLanguage = if (isEnglish) "English" else "French",
            targetLanguage = targetLang,
            isCourseGrounded = true
        )
    }
}
