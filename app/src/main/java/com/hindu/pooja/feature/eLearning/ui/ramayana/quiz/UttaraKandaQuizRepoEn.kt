package com.hindu.pooja.feature.quiz

/**
 * Uttara Kanda — Quiz (English)
 */
object UttaraKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Uttara Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "What is the main setting at the beginning of Uttara Kanda?",
                options = listOf(
                    "Rama’s childhood in Ayodhya",
                    "Rama’s reign (Ram Rajya) after returning from Lanka",
                    "The forest exile period",
                    "The war in Lanka"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q2",
                text = "What painful decision does Rama make due to public gossip about Sita?",
                options = listOf(
                    "To send her back to Lanka",
                    "To make her live separately in the palace",
                    "To banish Sita to the forest despite knowing her purity",
                    "To remarry another queen"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Who is asked to take Sita to the forest and leave her near the hermitages on the Ganga’s bank?",
                options = listOf(
                    "Bharata",
                    "Shatrughna",
                    "Lakshmana",
                    "Hanuman"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q4",
                text = "Which great sage gives shelter to Sita in his hermitage?",
                options = listOf(
                    "Vashishta",
                    "Valmiki",
                    "Bharadwaja",
                    "Agastya"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q5",
                text = "What are the names of Sita’s twin sons born in Valmiki’s ashram?",
                options = listOf(
                    "Kusha and Shatrughna",
                    "Lava and Bharata",
                    "Lava and Kusha",
                    "Rama and Lakshmana"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q6",
                text = "What great Yaga does Rama decide to perform in Uttara Kanda?",
                options = listOf(
                    "Rajasuya Yaga",
                    "Ashwamedha Yaga (horse sacrifice)",
                    "Putrakameshti Yaga",
                    "Vajapeya Yaga"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q7",
                text = "Who teaches the full Ramayana to Lava and Kusha to sing during the Yaga?",
                options = listOf(
                    "Vashishta",
                    "Valmiki",
                    "Narada",
                    "Bharadwaja"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q8",
                text = "How does Rama first encounter Lava and Kusha’s singing?",
                options = listOf(
                    "On the battlefield",
                    "On the streets and later in the Yaga assembly",
                    "Inside the palace",
                    "At the Ganga river bank"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "What does Rama request of Valmiki regarding Sita?",
                options = listOf(
                    "To keep Sita hidden forever",
                    "To send Sita back to Lanka",
                    "To bring Sita to the public assembly to declare her innocence",
                    "To marry Sita to another king"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q10",
                text = "How does Sita finally prove her absolute purity and devotion?",
                options = listOf(
                    "By a second fire ordeal",
                    "By entering the earth, praying to Mother Earth to receive her",
                    "By swearing an oath before the sages",
                    "By staying in the forest alone for 12 more years"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Who appears and takes Sita into the earth?",
                options = listOf(
                    "Agni Deva",
                    "Brahma",
                    "Mother Earth (Bhudevi)",
                    "Indra"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q12",
                text = "Which god later comes to Rama and reminds him that his earthly mission is complete?",
                options = listOf(
                    "Yama (God of Death)",
                    "Varuna",
                    "Kubera",
                    "Vayu"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q13",
                text = "What happens to Lakshmana towards the end of Uttara Kanda?",
                options = listOf(
                    "He becomes king of Ayodhya",
                    "He is cursed and turned to stone",
                    "He enters Yoga Samadhi at the Sarayu river and departs his body",
                    "He goes to live in the forest"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q14",
                text = "What final step does Rama take regarding his kingdom before leaving the world?",
                options = listOf(
                    "He crowns Lava and Kusha to rule different parts of Kosala",
                    "He destroys Ayodhya",
                    "He gives the kingdom to Hanuman",
                    "He leaves the throne empty"
                ),
                correct = setOf(0)
            ),
            QuizQuestion(
                id = "Q15",
                text = "How does Rama’s earthly life conclude according to Uttara Kanda?",
                options = listOf(
                    "He disappears without a trace",
                    "He ascends into the Sarayu river and assumes his divine Vishnu form",
                    "He dies on the battlefield",
                    "He goes back into exile"
                ),
                correct = setOf(1)
            )
        )
    )
}
