package com.hindu.pooja.feature.quiz

/**
 * Aranya Kanda — Quiz (English)
 */
object AranyaKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Aranya Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "Which demon did Rama and Lakshmana slay early in Aranya Kanda while protecting Sita?",
                options = listOf(
                    "Viradha",
                    "Maricha",
                    "Khara",
                    "Kabandha"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q2",
                text = "Which forest region is mainly associated with Aranya Kanda?",
                options = listOf(
                    "Chitrakoota",
                    "Dandakaranya",
                    "Naimisharanya",
                    "Kishkindha"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Who was the sister of Ravana who approached Rama and Lakshmana in the forest?",
                options = listOf(
                    "Shoorpanakha",
                    "Mandodari",
                    "Trijata",
                    "Sarama"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q4",
                text = "What did Shoorpanakha initially ask of Rama in the forest?",
                options = listOf(
                    "To give her food",
                    "To bless her with a boon",
                    "To marry her",
                    "To leave the forest"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q5",
                text = "Which demon took the form of a golden deer to enchant Sita?",
                options = listOf(
                    "Viradha",
                    "Maricha",
                    "Khara",
                    "Trishira"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q6",
                text = "What did Sita request Rama to do when she saw the golden deer?",
                options = listOf(
                    "Ignore it and continue prayers",
                    "Catch it alive for her",
                    "Shoot it immediately",
                    "Ask Lakshmana to chase it"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Who abducted Sita while Rama and Lakshmana were away from the hut?",
                options = listOf(
                    "Khara",
                    "Maricha",
                    "Ravana",
                    "Kabandha"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q8",
                text = "Which great vulture hero tried to stop Ravana and sacrificed his life?",
                options = listOf(
                    "Garuda",
                    "Jatayu",
                    "Sampati",
                    "Taksha"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "Which demon with a huge misshapen body did Rama and Lakshmana liberate by cutting off his arms?",
                options = listOf(
                    "Khara",
                    "Kabandha",
                    "Dushana",
                    "Subahu"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q10",
                text = "Which devoted woman did Rama meet who offered him berries, tasting them first out of love?",
                options = listOf(
                    "Shabari",
                    "Ahalya",
                    "Trijata",
                    "Tara"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Why did Lakshmana leave Sita alone in the hut, even though he was reluctant?",
                options = listOf(
                    "To fetch water",
                    "To follow Rama after hearing a cry that sounded like Rama",
                    "To meet a sage",
                    "To gather fruits"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q12",
                text = "What line did Lakshmana draw around the hut, according to popular tradition, to protect Sita?",
                options = listOf(
                    "Lakshmana Chakra",
                    "Lakshmana Rekha",
                    "Rama Rekha",
                    "Dharmic Line"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q13",
                text = "In what form did Ravana come to Sita’s hut to deceive her?",
                options = listOf(
                    "As a sage (mendicant)",
                    "As a Vanara",
                    "As a king",
                    "As a soldier"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q14",
                text = "What main emotion fills Rama’s heart at the end of Aranya Kanda?",
                options = listOf(
                    "Joy of triumph",
                    "Anger and grief at Sita’s abduction",
                    "Pride in his power",
                    "Indifference"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q15",
                text = "Which meeting at the end of Aranya Kanda leads towards Kishkindha Kanda?",
                options = listOf(
                    "Meeting Vibhishana",
                    "Meeting Hanuman and Sugriva’s world indirectly (through Kabandha’s guidance)",
                    "Meeting Bharata again",
                    "Meeting the sages of Naimisharanya"
                ),
                correct = setOf(1)
            )
        )
    )
}
