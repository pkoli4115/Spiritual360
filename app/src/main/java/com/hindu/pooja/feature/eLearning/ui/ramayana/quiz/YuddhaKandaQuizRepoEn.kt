package com.hindu.pooja.feature.quiz

/**
 * Yuddha Kanda — Quiz (English)
 */
object YuddhaKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Yuddha Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "Which great construction allowed Rama’s army to cross the ocean to Lanka?",
                options = listOf(
                    "A flying chariot",
                    "A bridge built across the ocean (Rama Setu)",
                    "A tunnel under the sea",
                    "A floating palace"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q2",
                text = "Who supervised the building of the bridge across the sea?",
                options = listOf(
                    "Sugriva",
                    "Nala, son of Vishwakarma",
                    "Hanuman",
                    "Jambavan"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Who came from Lanka and sought refuge (sharanagati) at Rama’s feet before the war?",
                options = listOf(
                    "Kumbhakarna",
                    "Vibhishana",
                    "Indrajit",
                    "Prahasta"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q4",
                text = "Which gigantic Rakshasa brother of Ravana was awakened from deep sleep to fight Rama’s army?",
                options = listOf(
                    "Indrajit",
                    "Kumbhakarna",
                    "Makaraksha",
                    "Mahodara"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q5",
                text = "Who was Ravana’s mighty son who fought with great magic and was slain by Lakshmana?",
                options = listOf(
                    "Kumbhakarna",
                    "Indrajit (Meghanada)",
                    "Akampana",
                    "Trishiras"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q6",
                text = "Which weapon of Indrajit temporarily bound Rama and Lakshmana with serpent-like bonds?",
                options = listOf(
                    "Agneya Astra",
                    "Naga Astra",
                    "Vajra Astra",
                    "Varuna Astra"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Who brought the Sanjeevani-bearing mountain to revive Lakshmana and the Vanaras?",
                options = listOf(
                    "Sugriva",
                    "Jambavan",
                    "Hanuman",
                    "Vibhishana"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q8",
                text = "Who instructed Rama in the sacred hymn Aditya Hridaya during the final battle?",
                options = listOf(
                    "Vashishta",
                    "Agastya",
                    "Bharadwaja",
                    "Narada"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "Which ultimate divine weapon did Rama use to finally slay Ravana?",
                options = listOf(
                    "Agneya Astra",
                    "Vajra Astra",
                    "Brahma Astra",
                    "Naga Astra"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q10",
                text = "Who was crowned king of Lanka after Ravana’s death?",
                options = listOf(
                    "Indrajit",
                    "Vibhishana",
                    "Sugriva",
                    "Angada"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q11",
                text = "What did Sita undergo after being brought from Lanka to prove her purity?",
                options = listOf(
                    "A long fast",
                    "An oath before the people",
                    "A trial by fire (Agni Pariksha)",
                    "Another exile"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q12",
                text = "Who brought Sita back safely from the fire to Rama?",
                options = listOf(
                    "Brahma",
                    "Agni Deva (Fire God)",
                    "Indra",
                    "Hanuman"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q13",
                text = "Which vehicle carried Rama, Sita and Lakshmana back to Ayodhya after the war?",
                options = listOf(
                    "Pushpaka Vimana",
                    "Garuda’s back",
                    "A giant boat",
                    "Ravana’s chariot"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q14",
                text = "Before returning to Ayodhya, whose hermitage did Rama visit and receive blessings from?",
                options = listOf(
                    "Valmiki’s hermitage",
                    "Bharadwaja’s hermitage",
                    "Vashishta’s hermitage",
                    "Agastya’s hermitage"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q15",
                text = "What major theme does Yuddha Kanda represent in the Ramayana?",
                options = listOf(
                    "Childhood play",
                    "Forest wanderings",
                    "War between Dharma and Adharma leading to Rama’s victory",
                    "Court politics in Ayodhya"
                ),
                correct = setOf(2)
            )
        )
    )
}
