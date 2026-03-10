package com.hindu.pooja.feature.quiz

/**
 * Kishkindha Kanda — Quiz (English)
 */
object KishkindhaKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Kishkindha Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "Who was the first to meet Rama and Lakshmana in Kishkindha Kanda?",
                options = listOf(
                    "Sugriva",
                    "Hanuman",
                    "Jambavan",
                    "Angada"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q2",
                text = "In what disguise did Hanuman initially approach Rama and Lakshmana?",
                options = listOf(
                    "As a king",
                    "As a sage (brahmachari)",
                    "As a soldier",
                    "As a merchant"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Who was Sugriva’s powerful brother and rival king of Kishkindha?",
                options = listOf(
                    "Ravana",
                    "Vali (Bali)",
                    "Jatayu",
                    "Vibhishana"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q4",
                text = "What promise did Sugriva make to Rama in return for help against Vali?",
                options = listOf(
                    "To give Rama a kingdom",
                    "To send his Vanara army to search for Sita",
                    "To fight against Ayodhya",
                    "To perform a Yaga for Rama"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q5",
                text = "How did Rama kill Vali?",
                options = listOf(
                    "In open duel face-to-face",
                    "By arrow from behind a tree during Vali’s fight with Sugriva",
                    "Using a divine curse",
                    "By sending Hanuman to fight him"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q6",
                text = "Who became the crowned king of Kishkindha after Vali’s death?",
                options = listOf(
                    "Hanuman",
                    "Angada",
                    "Sugriva",
                    "Jambavan"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Who was crowned as the Yuvaraja (crown prince) of Kishkindha?",
                options = listOf(
                    "Lakshmana",
                    "Angada",
                    "Hanuman",
                    "Sugriva"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q8",
                text = "What did Sugriva initially forget to do after becoming king, angering Rama?",
                options = listOf(
                    "Perform Rama’s coronation",
                    "Send out search parties for Sita immediately",
                    "Invite Bharata to Kishkindha",
                    "Free all prisoners in the kingdom"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "Who reminded Sugriva of his promise and urged him to act quickly?",
                options = listOf(
                    "Lakshmana",
                    "Hanuman",
                    "Jambavan",
                    "Trijata"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q10",
                text = "Which group did Hanuman lead at the end of Kishkindha Kanda?",
                options = listOf(
                    "The northern search party",
                    "The eastern search party",
                    "The southern search party searching for Sita",
                    "The western search party"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Which mountain and cave episode delayed one of the search groups and led to despair?",
                options = listOf(
                    "Sanjeevani mountain",
                    "Suryaparvata",
                    "Rikshaparvata and a deep cave",
                    "Meru mountain"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q12",
                text = "Who encourages the southern party not to give up hope when the search time limit is ending?",
                options = listOf(
                    "Sugriva",
                    "Jambavan",
                    "Rama",
                    "Lakshmana"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q13",
                text = "What key discovery at the end of Kishkindha Kanda leads directly into Sundara Kanda?",
                options = listOf(
                    "Finding Sita’s ornaments on the ground",
                    "Learning about the city of Lanka across the ocean",
                    "Hearing Vibhishana’s name",
                    "Meeting Trijata"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q14",
                text = "Whose strength and ability to leap the ocean is highlighted at the close of Kishkindha Kanda?",
                options = listOf(
                    "Angada’s",
                    "Jambavan’s",
                    "Hanuman’s",
                    "Sugriva’s"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q15",
                text = "Which main theme of Kishkindha Kanda prepares the way for the war in later Kandas?",
                options = listOf(
                    "Rama’s childhood games",
                    "Building alliances with the Vanaras",
                    "Stories of sages only",
                    "Life in Ayodhya’s palace"
                ),
                correct = setOf(1)
            )
        )
    )
}
