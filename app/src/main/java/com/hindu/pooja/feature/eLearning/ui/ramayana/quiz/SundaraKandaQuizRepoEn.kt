package com.hindu.pooja.feature.quiz

/**
 * Sundara Kanda — Quiz (English)
 */
object SundaraKandaQuizRepoEn {

    fun default(): QuizModule = QuizModule(
        title = "Sundara Kanda — Quiz (English)",
        passPercent = 80,
        questions = listOf(
            QuizQuestion(
                id = "Q1",
                text = "Who is the central hero of Sundara Kanda?",
                options = listOf(
                    "Rama",
                    "Lakshmana",
                    "Hanuman",
                    "Sugriva"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q2",
                text = "What great feat begins Sundara Kanda?",
                options = listOf(
                    "Building a bridge across the ocean",
                    "Hanuman’s leap across the ocean to Lanka",
                    "Rama’s coronation",
                    "Ravana’s coronation"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q3",
                text = "Whom does Hanuman have to find in Lanka?",
                options = listOf(
                    "Kaikeyi",
                    "Trijata",
                    "Sita",
                    "Mandodari"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q4",
                text = "What form does Hanuman generally use to move around Lanka unnoticed?",
                options = listOf(
                    "His full gigantic form",
                    "A tiny form like a cat or small being",
                    "A royal prince",
                    "A glowing ball of light"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q5",
                text = "Where in Lanka does Hanuman finally find Sita?",
                options = listOf(
                    "In Ravana’s palace on a throne",
                    "In a garden called Ashoka Vatika",
                    "In a prison cell",
                    "On the city walls"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q6",
                text = "What does Hanuman give Sita as proof that he is truly Rama’s messenger?",
                options = listOf(
                    "Rama’s crown",
                    "Rama’s sandals",
                    "Rama’s ring",
                    "Rama’s bow"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q7",
                text = "What does Sita give Hanuman in return to show Rama as proof of her meeting?",
                options = listOf(
                    "Her hair ornament",
                    "Her anklet",
                    "Her upper cloth",
                    "Her Chudamani (jewel)"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q8",
                text = "What did Hanuman do in Lanka that caused great destruction?",
                options = listOf(
                    "Flooded the city",
                    "Set large parts of Lanka on fire with his burning tail",
                    "Turned soldiers into stone",
                    "Collapsed the palace with an earthquake"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q9",
                text = "How did Hanuman’s tail catch fire in Lanka?",
                options = listOf(
                    "He dipped it in oil himself",
                    "It was scorched by lightning",
                    "The Rakshasas wrapped it in cloth and set it on fire as punishment",
                    "Ravana cursed his tail"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q10",
                text = "What message does Hanuman bring back to Rama from Sita?",
                options = listOf(
                    "To forgive Ravana",
                    "To delay the war",
                    "To come quickly and rescue her, as she can no longer bear Ravana’s harassment",
                    "To send Lakshmana instead of Rama"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q11",
                text = "Which quality of Hanuman is most celebrated in Sundara Kanda?",
                options = listOf(
                    "His greed for wealth",
                    "His laziness",
                    "His devotion, courage and intelligence in service of Rama",
                    "His desire to rule Lanka"
                ),
                correct = setOf(2)
            ),
            QuizQuestion(
                id = "Q12",
                text = "Why is this Kanda often called 'Sundara' (beautiful)?",
                options = listOf(
                    "Because Lanka is beautifully described",
                    "Because it shows the beauty of Hanuman’s devotion and Sita’s purity",
                    "Because it has no battles",
                    "Because Ravana decorates his palace"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q13",
                text = "Who in Ravana’s court speaks gentle words and gives some hope to Sita?",
                options = listOf(
                    "Mandodari",
                    "Trijata",
                    "Kaikesi",
                    "Sarama"
                ),
                correct = setOf(1)
            ),
            QuizQuestion(
                id = "Q14",
                text = "What does Hanuman do before leaving Lanka to show his fearlessness?",
                options = listOf(
                    "Challenges Ravana directly and kills him",
                    "Destroys Ravana’s throne",
                    "Uproots a pillar and escapes laughing",
                    "Allows himself to be captured, then breaks free and burns Lanka"
                ),
                correct = setOf(3)
            ),
            QuizQuestion(
                id = "Q15",
                text = "What is the main outcome of Sundara Kanda for the overall story?",
                options = listOf(
                    "Rama returns to Ayodhya",
                    "The war ends",
                    "Rama gains clear proof of Sita’s location and condition, and resolves to attack Lanka",
                    "Vali is killed"
                ),
                correct = setOf(2)
            )
        )
    )
}
