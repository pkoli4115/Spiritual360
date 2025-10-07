package com.hindu.pooja.feature.quiz

/**
 * Ayodhya Kanda — Quiz module
 * Uses the exact same data model and screen (BalaKandaQuizScreen).
 *
 * Source of story content (for crafting questions):
 * Telugu Wikipedia-based lessons you loaded into the app JSON.
 */
object AyodhyaKandaQuizRepo {

    fun default(): QuizModule = QuizModule(
        title = "Ayodhya Kanda — Quiz",
        passPercent = 80,
        questions = listOf(
            // Q1: Kaikeyi’s two boons
            QuizQuestion(
                id = "Q1",
                text = "కైకయి దశరథుని వద్ద కోరిన రెండు వరాలు ఏమిటి?",
                options = listOf(
                    "రాముని పట్టాభిషేకం, భరతుని వనవాసం",
                    "భరతుని పట్టాభిషేకం, రాముని 14 ఏళ్ల వనవాసం",
                    "శత్రుఘ్నుని పట్టాభిషేకం, లక్ష్మణుని వనవాసం",
                    "రాముని 7 ఏళ్ల వనవాసం, భరతుని మంత్రిపదవి"
                ),
                correct = setOf(1)
            ),

            // Q2: Who instigated Kaikeyi
            QuizQuestion(
                id = "Q2",
                text = "కైకయిని భరతుని కోసం వరాలు కోరమని ప్రేరేపించినది ఎవరు?",
                options = listOf("వశిష్ఠుడు", "మంధర", "సుమంత్రుడు", "కౌసల్య"),
                correct = setOf(1)
            ),

            // Q3: Exile duration
            QuizQuestion(
                id = "Q3",
                text = "రాముని వనవాసం ఎంత కాలం?",
                options = listOf("12 సంవత్సరాలు", "7 సంవత్సరాలు", "14 సంవత్సరాలు", "10 సంవత్సరాలు"),
                correct = setOf(2)
            ),

            // Q4: Who accompanies Rama
            QuizQuestion(
                id = "Q4",
                text = "వనవాసానికి రామునితో పాటు వెళ్లినవారు ఎవరు?",
                options = listOf(
                    "సీత మాత్రమే",
                    "లక్ష్మణుడు మాత్రమే",
                    "సీత మరియు లక్ష్మణుడు",
                    "భరతుడు మరియు శత్రుఘ్నుడు"
                ),
                correct = setOf(2)
            ),

            // Q5: Guha’s hospitality
            QuizQuestion(
                id = "Q5",
                text = "గుహుడు ఎవరికి ఆతిథ్యం ఇచ్చాడు?",
                options = listOf(
                    "భరతుడికి",
                    "సీత రామ లక్ష్మణులకు",
                    "వశిష్ఠ మహర్షికి",
                    "సుమంత్రునికి మాత్రమే"
                ),
                correct = setOf(1)
            ),

            // Q6: First night halt
            QuizQuestion(
                id = "Q6",
                text = "రాములు మొదటి రాత్రి ఎక్కడ విశ్రమించారు?",
                options = listOf("శృంగిబేరపురం", "తమసా నది ఒడ్డు", "ప్రయాగం", "చిత్రకూటం"),
                correct = setOf(1)
            ),

            // Q7: River crossed with Guha’s help
            QuizQuestion(
                id = "Q7",
                text = "గుహుడు ఏర్పాటు చేసిన నావలో ఎవరు ఏ నదిని దాటారు?",
                options = listOf(
                    "భరతుడు — సరయూ",
                    "సీతారామలక్ష్మణులు — గంగా",
                    "వశిష్ఠుడు — యమునా",
                    "సుమంత్రుడు — గోదావరి"
                ),
                correct = setOf(1)
            ),

            // Q8: Hermitage near confluence
            QuizQuestion(
                id = "Q8",
                text = "గంగా-యమునల సంగమానికి సమీపంలో ఉన్న ఆశ్రమం ఏది?",
                options = listOf("అత్రీ ఆశ్రమం", "భరద్వాజ ఆశ్రమం", "వాల్మీకి ఆశ్రమం", "వశిష్ఠ ఆశ్రమం"),
                correct = setOf(1)
            ),

            // Q9: Place of early forest stay
            QuizQuestion(
                id = "Q9",
                text = "రాముడు ప్రారంభంగా ఏ ప్రదేశంలో పర్ణశాల నిర్మించి నివసించాడు?",
                options = listOf("చిత్రకూటం", "పంపా సరస్సు", "దండకారణ్యం లోతు", "నందిగ్రామం"),
                correct = setOf(0)
            ),

            // Q10: Dasaratha’s death cause (curse context)
            QuizQuestion(
                id = "Q10",
                text = "దశరథుని మరణానికి సంబంధించి యౌవనంలో జరిగిన తప్పిదం ఏమిటి?",
                options = listOf(
                    "అశ్వమేధంలో వైఫల్యం",
                    "శబ్దవేధంతో మునికుమారుని పొరపాటు బాణంతో హత్య",
                    "వనవాస ఆజ్ఞను వెనక్కి తీసుకోవడం",
                    "భరతుని మీద కోపం"
                ),
                correct = setOf(1)
            ),

            // Q11: Bharata’s resolve
            QuizQuestion(
                id = "Q11",
                text = "భరతుడు అయోధ్య చేరిన తరువాత ఏ నిశ్చయానికి వచ్చాడు?",
                options = listOf(
                    "తక్షణే పట్టాభిషేకం స్వీకరించాలి",
                    "రాముడిని తిరిగి రాజ్యానికి ఆహ్వానించాలి",
                    "లక్ష్మణునే రాజుగా చేయాలి",
                    "శత్రుఘ్నుడిని పంపాలి"
                ),
                correct = setOf(1)
            ),

            // Q12: Where did Bharata stay later?
            QuizQuestion(
                id = "Q12",
                text = "పాదుకల రాజ్యపాలన సమయంలో భరతుడు ఎక్కడ నివసించాడు?",
                options = listOf("అయోధ్యలో రాజభవనం", "నందిగ్రామం", "చిత్రకూటం", "మిథిలా"),
                correct = setOf(1)
            ),

            // Q13: Paduka pattabhishekam
            QuizQuestion(
                id = "Q13",
                text = "భరతుడు ఎవరిని ప్రతీకగా ఉంచి రాజ్యపాలన చేశాడు?",
                options = listOf("వశిష్ఠుని ఆజ్ఞ", "శివధనుస్సు", "శ్రీరాముని పాదుకలు", "గంగామాత ఫలకం"),
                correct = setOf(2)
            ),

            // Q14: Anasuya’s gifts/instruction
            QuizQuestion(
                id = "Q14",
                text = "అనసూయ సీతకు ఏమి బోధించి, ఏమి ప్రసాదించింది?",
                options = listOf(
                    "వనవాస నియమాలు మాత్రమే",
                    "పతివ్రతా ధర్మాలు; పూలదండ, చందనం, వస్త్రాభరణాలు",
                    "యుద్ధ విద్యలు; ఖడ్గం",
                    "వేదాధ్యయనం; అక్షమాల"
                ),
                correct = setOf(1)
            ),

            // Q15: Who tried to stop exile (but Rama insisted on dharma)?
            QuizQuestion(
                id = "Q15",
                text = "రాముని వనవాసాన్ని ఆపాలని ప్రయత్నించిన వారు ఎవరు (రాముడు ధర్మ నిశ్చయంతో ఒప్పించేడు)?",
                options = listOf(
                    "కౌసల్య మరియు లక్ష్మణుడు",
                    "వశిష్ఠుడు మరియు సుమంత్రుడు",
                    "భరతుడు మరియు శత్రుఘ్నుడు",
                    "అనసూయ మరియు అత్రి"
                ),
                correct = setOf(0)
            )
        )
    )
}
