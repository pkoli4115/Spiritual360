package com.hindu.pooja.feature.quiz

/**
 * Bala Kanda — Quiz (English)
 * Uses the shared QuizQuestion / QuizModule models.
 */
object BalaKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Bala Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "Who composed the Ramayana after hearing the story from Sage Narada?",
                options = listOf(
                    "Sage Valmiki",
                    "Sage Vashishta",
                    "Sage Vishwamitra",
                    "Sage Bharadwaja"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q2",
                text = "Why did King Dasharatha perform the Putrakameshti Yaga (sacrifice)?",
                options = listOf(
                    "To win a war against enemies",
                    "To obtain prosperity for Ayodhya",
                    "To obtain worthy sons",
                    "To cure a disease"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Who was the sage that took Rama and Lakshmana to protect his Yaga from demons?",
                options = listOf(
                    "Sage Vishwamitra",
                    "Sage Vashishta",
                    "Sage Bharadwaja",
                    "Sage Agastya"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q4",
                text = "Which demoness was slain by Rama in the forest at the request of Vishwamitra?",
                options = listOf(
                    "Tataka",
                    "Shoorpanakha",
                    "Kaikeyi",
                    "Manthara"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q5",
                text = "What miracle did Rama perform at Sage Gautama’s ashram involving Ahalya?",
                options = listOf(
                    "He made a river flow again",
                    "He restored Ahalya from a curse and made her visible",
                    "He created a palace for the sage",
                    "He brought a dead bird back to life"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q6",
                text = "In Mithila, what condition did King Janaka set for Sita’s marriage?",
                options = listOf(
                    "Winning a chariot race",
                    "Lifting and stringing Shiva’s great bow",
                    "Killing a demon in battle",
                    "Performing 100 yajnas"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Who successfully lifted and broke the great bow of Shiva (Pinaka)?",
                options = listOf(
                    "Lakshmana",
                    "Bharata",
                    "Rama",
                    "Parashurama"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q8",
                text = "Whose incarnation or aspect is Hanuman traditionally considered to be?",
                options = listOf(
                    "Indra",
                    "Vayu (Wind God)",
                    "Agni (Fire God)",
                    "Varuna (Water God)"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "Which sage became angry seeing a hunter kill one of a pair of birds, inspiring the first shloka?",
                options = listOf(
                    "Vashishta",
                    "Valmiki",
                    "Vishwamitra",
                    "Narada"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q10",
                text = "How many sons did King Dasharatha finally receive after the sacred Yaga?",
                options = listOf(
                    "One",
                    "Two",
                    "Three",
                    "Four"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Who among the following is not a son of Dasharatha?",
                options = listOf(
                    "Rama",
                    "Lakshmana",
                    "Bharata",
                    "Vibhishana"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q12",
                text = "Which city is ruled by King Janaka, father of Sita?",
                options = listOf(
                    "Ayodhya",
                    "Mithila",
                    "Lanka",
                    "Kishkindha"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q13",
                text = "Which divine weapon training did Rama and Lakshmana receive from Vishwamitra?",
                options = listOf(
                    "Knowledge of celestial missiles (Astras)",
                    "Only archery with normal arrows",
                    "Only sword fighting",
                    "Only wrestling techniques"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q14",
                text = "Whom did Rama marry?",
                options = listOf(
                    "Urmila",
                    "Mandavi",
                    "Shrutakirti",
                    "Sita"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q15",
                text = "How many royal weddings took place in Mithila according to the Bala Kanda story?",
                options = listOf(
                    "One – only Rama and Sita",
                    "Two – Rama–Sita and Lakshmana–Urmila",
                    "Four – marriages of all four brothers",
                    "None – only an engagement"
                ),
                correct = setOf(2)
            )
        )
    )
}
