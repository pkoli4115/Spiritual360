package com.hindu.pooja.feature.quiz

/**
 * Ayodhya Kanda — Quiz (English)
 */
object AyodhyaKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Ayodhya Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "What did Kaikeyi ask from Dasharatha as her two boons?",
                options = listOf(
                    "Rama’s coronation and Lakshmana’s exile",
                    "Bharata’s coronation and Rama’s 14-year exile",
                    "Shatrughna’s coronation and Bharata’s exile",
                    "Rama’s 7-year exile and Kaikeyi’s kingdom"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q2",
                text = "Who instigated Kaikeyi to claim her boons and send Rama to the forest?",
                options = listOf(
                    "Manthara, the maid",
                    "Vashishta",
                    "Sumitra",
                    "Kausalya"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q3",
                text = "How many years of exile did Rama agree to undergo?",
                options = listOf(
                    "7 years",
                    "10 years",
                    "12 years",
                    "14 years"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q4",
                text = "Who accompanied Rama into the forest out of brotherly devotion?",
                options = listOf(
                    "Bharata",
                    "Lakshmana",
                    "Shatrughna",
                    "None; he went alone"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q5",
                text = "Which of the queens insisted on going to the forest with Rama?",
                options = listOf(
                    "Kausalya",
                    "Sumitra",
                    "Kaikeyi",
                    "Sita"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q6",
                text = "How did the citizens of Ayodhya react when Rama left for the forest?",
                options = listOf(
                    "They rejoiced for Bharata’s coronation",
                    "They indifferently continued daily work",
                    "They followed Rama, weeping and lamenting",
                    "They protested against Rama"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Where did Rama, Sita and Lakshmana finally settle after leaving Ayodhya and staying at several hermitages?",
                options = listOf(
                    "Chitrakoota",
                    "Naimisharanya",
                    "Lanka",
                    "Dandakaranya"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q8",
                text = "What happened to King Dasharatha after Rama left for the forest?",
                options = listOf(
                    "He went to bring Rama back",
                    "He fell sick and died in grief",
                    "He ruled Ayodhya for many more years",
                    "He left for a pilgrimage"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "On hearing of Rama’s exile, what was Bharata’s reaction?",
                options = listOf(
                    "He happily accepted the throne",
                    "He also asked for a separate kingdom",
                    "He became furious and rejected his mother’s plan",
                    "He left Ayodhya forever"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q10",
                text = "What did Bharata finally take from Rama as a symbol of his rule?",
                options = listOf(
                    "Rama’s bow",
                    "Rama’s sandals (padukas)",
                    "Rama’s ring",
                    "Rama’s sword"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Where did Bharata stay and rule from, waiting for Rama’s return?",
                options = listOf(
                    "Inside Ayodhya palace",
                    "In Nandigrama outside Ayodhya",
                    "In Mithila",
                    "In Kishkindha"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q12",
                text = "What did Rama insist upon when Bharata begged him to return immediately?",
                options = listOf(
                    "That Bharata must also go to the forest",
                    "That Kaikeyi must apologise first",
                    "That the promise to Dasharatha must be honored fully",
                    "That Sita should return to Ayodhya"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q13",
                text = "Which sage’s hermitage did Rama stay in, where he was advised further about forest life?",
                options = listOf(
                    "Sage Bharadwaja",
                    "Sage Vashishta",
                    "Sage Valmiki",
                    "Sage Shringi"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q14",
                text = "What virtue of Rama is most highlighted in Ayodhya Kanda?",
                options = listOf(
                    "Bravery in war",
                    "Skill in music",
                    "Unwavering obedience to Dharma and his father’s word",
                    "Desire for kingdom"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q15",
                text = "Whose love and loyalty towards Rama is especially emphasized in Ayodhya Kanda?",
                options = listOf(
                    "Bharata’s devotion",
                    "Vibhishana’s devotion",
                    "Sugriva’s devotion",
                    "Hanuman’s devotion"
                ),
                correct = setOf(0)
            )
        )
    )
}

