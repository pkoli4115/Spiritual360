package com.hindu.pooja.feature.quiz

/**
 * Aranya Kanda — Quiz module
 * Uses the same QuizQuestion / QuizModule models.
 *
 * Questions are based on the Telugu Wikipedia “సంక్షిప్త కథ”
 * content that you loaded into aranya_kanda_te_wiki_simple.json.
 */
object AranyaKandaQuizRepo {

    fun default(): QuizModule = QuizModule(
        title = "Aranya Kanda — Quiz",
        passPercent = 80,
        questions = listOf(

            // Q1: Viradha's original identity
            QuizQuestion(
                id = "Q1",
                text = "విరాధుడుగా శపించబడి రాక్షసుడైనవాడు అసలు ఎవరు?",
                options = listOf(
                    "నహుషుడు అనే రాజు",
                    "తుంబురుడనే గంధర్వుడు",
                    "జటాయువుని అన్న",
                    "ఇంద్రుని దూత"
                ),
                correct = setOf(1)
            ),

            // Q2: Who freed Viradha from his curse
            QuizQuestion(
                id = "Q2",
                text = "విరాధుని శాప విమోచనం ఎవరి చేత జరిగింది?",
                options = listOf(
                    "అగస్త్య మహర్షి చేత",
                    "జటాయువు చేత",
                    "రామ–లక్ష్మణుల చేత",
                    "శబరి చేత"
                ),
                correct = setOf(2)
            ),

            // Q3: Sage waiting for Rama before going to Brahmaloka
            QuizQuestion(
                id = "Q3",
                text = "బ్రహ్మలోకానికి వెళ్ళకుండా రాముని దర్శనం కోసం వేచిచూసిన మహర్షి ఎవరు?",
                options = listOf(
                    "సుతీక్ష్ణ మహర్షి",
                    "శరభంగ మహర్షి",
                    "మతంగ మహర్షి",
                    "విశ్వామిత్ర మహర్షి"
                ),
                correct = setOf(1)
            ),

            // Q4: Who directed Rama to Sutikshna
            QuizQuestion(
                id = "Q4",
                text = "శరభంగ మహర్షి రాములను ఎవరిని దర్శించమని పంపాడు?",
                options = listOf(
                    "సుతీక్ష్ణ మహర్షి ని",
                    "వశిష్ఠ మహర్షి ని",
                    "వాల్మీకి మహర్షి ని",
                    "మతంగ మహర్షి ని"
                ),
                correct = setOf(0)
            ),

            // Q5: Panchapsaras lake creator
            QuizQuestion(
                id = "Q5",
                text = "పంచాప్సరసం అనే తటాకాన్ని తన తపస్సుతో ఎవరు సృష్టించారు?",
                options = listOf(
                    "అత్రి మహర్షి",
                    "మాండకర్ణి మహర్షి",
                    "అగస్త్య మహర్షి",
                    "భరద్వాజ మహర్షి"
                ),
                correct = setOf(1)
            ),

            // Q6: Feats of Agastya
            QuizQuestion(
                id = "Q6",
                text = "కిందివాటిలో అగస్త్య మహర్షితో సంబంధం లేని విషయం ఏది?",
                options = listOf(
                    "వాతాపి–ఇల్వలులను సంహరించాడు",
                    "వింధ్యపర్వత పెరుగుదలను అదుపుచేశాడు",
                    "సప్తసాగరాలను ఒకచోటికి రప్పించాడు",
                    "దక్షిణ దిక్కును మునులకు ఆవాస యోగ్యంగా చేశాడు"
                ),
                correct = setOf(2) // this is Matanga/శబరి భాగం, not Agastya
            ),

            // Q7: Place where Rama stays per Agastya's advice
            QuizQuestion(
                id = "Q7",
                text = "అగస్త్య మహర్షి సూచన ప్రకారం రామ–సీత–లక్ష్మణులు ఎక్కడ ఆశ్రమం వేసుకున్నారు?",
                options = listOf(
                    "చిత్రకూటం",
                    "నందిగ్రామం",
                    "పంపా సరస్సు తీరంలో",
                    "గోదావరీ తీరంలోని పంచవటి లో"
                ),
                correct = setOf(3)
            ),

            // Q8: Who is Jatayu?
            QuizQuestion(
                id = "Q8",
                text = "జటాయువు ఎవరు?",
                options = listOf(
                    "దశరథుని మిత్రుడైన గరుత్మంతుడు",
                    "దశరథుని మిత్రుడైన గ్రద్దరాజు",
                    "కైకయి తమ్ముడు",
                    "సుగ్రీవుని తమ్ముడు"
                ),
                correct = setOf(1)
            ),

            // Q9: Disfiguring of Surpanakha
            QuizQuestion(
                id = "Q9",
                text = "శూర్పణఖ సీతను హింసించబోయినప్పుడు లక్ష్మణుడు ఏమి చేశాడు?",
                options = listOf(
                    "శూర్పణఖను చంపేశాడు",
                    "శూర్పణఖను బంధించాడు",
                    "శూర్పణఖ ముక్కు, చెవులను కోసివేశాడు",
                    "ఆమెను లంకకు పంపేశాడు"
                ),
                correct = setOf(2)
            ),

            // Q10: Size of Khara-Dushana army
            QuizQuestion(
                id = "Q10",
                text = "ఖర–దూషణుల సేనలో ఎంతమంది రాక్షసులను రాముడు సంహరించాడు?",
                options = listOf(
                    "ఏడు వందలు",
                    "వెయ్యి మంది",
                    "పది వేల మంది",
                    "పద్నాలుగు వేల మంది"
                ),
                correct = setOf(3)
            ),

            // Q11: Who warned Ravana about Rama
            QuizQuestion(
                id = "Q11",
                text = "రామునితో వైరం పెట్టుకోవొద్దని, అది రాక్షస జాతి నాశనానికి దారితీస్తుందని రావణుని హెచ్చరించినవాడు ఎవరు?",
                options = listOf(
                    "విభీషణుడు",
                    "మారీచుడు",
                    "శూర్పణఖ",
                    "కబంధుడు"
                ),
                correct = setOf(1)
            ),

            // Q12: Form of Maricha
            QuizQuestion(
                id = "Q12",
                text = "రాముణ్ని పర్ణశాల నుండి దూరం తీసుకెళ్ళడానికి మారీచుడు ఏ రూపం ధరించాడు?",
                options = listOf(
                    "వానర రూపం",
                    "బ్రాహ్మణ రూపం",
                    "బంగారు లేడిగా (మాయలేడి)",
                    "సన్యాసి రూపం"
                ),
                correct = setOf(2)
            ),

            // Q13: What trick did Maricha use when dying
            QuizQuestion(
                id = "Q13",
                text = "మారీచుడు రామబాణానికి గురై మరణించే సమయానికి ఏ దుష్ట పన్నాగం చేశాడు?",
                options = listOf(
                    "సీతను నేరుగా లంకకు తీసుకెళ్లాడు",
                    "రావణుని పేరు మీద సహాయం కోరాడు",
                    "రాముని స్వరంలా \"అయ్యో సీతా, అయ్యో లక్ష్మణా\" అని అరచాడు",
                    "ఆశ్రమాన్ని అగ్నితో దహనం చేశాడు"
                ),
                correct = setOf(2)
            ),

            // Q14: Who told Rama about Sugriva
            QuizQuestion(
                id = "Q14",
                text = "కబంధుని శాపవిమోచన తర్వాత రామునికి సుగ్రీవునితో స్నేహం చేయాలని సూచించినవాడు ఎవరు?",
                options = listOf(
                    "కబంధుడే",
                    "శబరి",
                    "మతంగ మహర్షి",
                    "జటాయువు"
                ),
                correct = setOf(0)
            ),

            // Q15: Sabari's devotion
            QuizQuestion(
                id = "Q15",
                text = "శబరి రామునికి ఎలా సేవ చేసింది?",
                options = listOf(
                    "యుద్ధంలో రథసారథిగా నిలిచింది",
                    "తన తపస్సుతో కేతకీ వనాన్ని సృష్టించింది",
                    "మధురమైన ఫలాలతో అతిథి పూజ చేసి భక్తితో పాద సేవ చేసింది",
                    "లంక వరకు రామునికి దారి చూపింది"
                ),
                correct = setOf(2)
            )
        )
    )
}
