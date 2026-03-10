package com.hindu.pooja.feature.quiz

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correct: Set<Int>,             // indexes of correct options
    val explanation: String? = null
)

data class QuizModule(
    val title: String,
    val passPercent: Int = 80,
    val questions: List<QuizQuestion>
)

object BalaKandaQuizRepo {
    fun default(): QuizModule = QuizModule(
        title = "Bala Kanda — Quiz",
        passPercent = 80,
        questions = listOf(
            QuizQuestion("Q1","వాల్మీకి మొదట చెప్పిన శ్లోకం ఏ సందర్భంలో పుట్టింది?",
                listOf("గంగా స్నానం","తమసా నదిలో క్రౌంచపక్షి వధను చూసినప్పుడు","అశ్వమేధ యాగం","రాజసూయ యాగం"), setOf(1)),
            QuizQuestion("Q2","పుత్రకామేష్టి యాగం ఎవరు నిర్వహించారు?",
                listOf("వశిష్ఠుడు","విశ్వామిత్రుడు","ఋష్యశృంగుడు","వాల్మీకి"), setOf(2)),
            QuizQuestion("Q3","తాటక వనంలో తాటక వధ ఎవరుచేశారు?",
                listOf("లక్ష్మణుడు","రాముడు","భరతుడు","శత్రుఘ్నుడు"), setOf(1)),
            QuizQuestion("Q4","శివధనుస్సును ఎవరు విరిచారు?",
                listOf("లక్ష్మణుడు","భరతుడు","రాముడు","పరశురాముడు"), setOf(2)),
            QuizQuestion("Q5","అహల్య శాప విమోచనం ఎప్పుడు జరిగింది?",
                listOf("రాముడు ఆశ్రమంలోకి ప్రవేశించినప్పుడు","విశ్వామిత్రుడు హోమం పూర్తి చేసినప్పుడు","శివధనుస్సు విరిచిన తర్వాత","పుత్రకామేష్టి యాగం అనంతరం"), setOf(0)),
            QuizQuestion("Q6","భగీరధుని తపస్సుతో ఎవరు గంగను ధరించారు?",
                listOf("బ్రహ్మ","విష్ణు","శివుడు","ఇంద్రుడు"), setOf(2)),
            QuizQuestion("Q7","మారీచుడు మొదట ఎక్కడికి ఎగిరిపోయాడు?",
                listOf("లంక","సముద్రంలో దూరంగా","మిథిలా","అయోధ్య"), setOf(1)),
            QuizQuestion("Q8","సీత స్వయంవరానికి నిబంధన?",
                listOf("వేద పారాయణం","శివధనుస్సు ఎక్కుపెట్టి తీర్చి దిద్దడం","రథపందెం గెలవడం","యుద్ధంలో విజయం"), setOf(1)),
            QuizQuestion("Q9","పరశురాముని ధనస్సు మీద బాణం తొడిగింది ఎవరు?",
                listOf("లక్ష్మణుడు","రాముడు","భరతుడు","వశిష్ఠుడు"), setOf(1)),
            QuizQuestion("Q10","విశ్వామిత్రుడు చివరికి ఎలాంటి బిరుదు పొందారు?",
                listOf("రాజర్షి","మహర్షి","బ్రహ్మర్షి","దేవర్షి"), setOf(2)),
            QuizQuestion("Q11","సాగరపుత్రుల విమోచనానికి అవసరం ఏమిటి?",
                listOf("గంగా ప్రవాహం భస్మంపై","అశ్వమేధ మరలా","రాజసూయ","సోమ యాగం"), setOf(0)),
            QuizQuestion("Q12","ఆంజనేయుడు ఎవరి అంసుడు?",
                listOf("శివుడు","వాయుదేవుడు","ఇంద్రుడు","బ్రహ్మ"), setOf(1)),
            QuizQuestion("Q13","విశ్వామిత్రుని సోదరి నదిగా అవతరించింది?",
                listOf("సరయూ","కౌశికీ","గోదావరి","నేర్వ"), setOf(1)),
            QuizQuestion("Q14","జనకుడు సీతకు ఏ నామం ఇచ్చారు?",
                listOf("జానకి","వైదేహి","మిథిలేశ్వరి","అన్నీ సరైనవే"), setOf(3)),
            QuizQuestion("Q15","రామ–సీత వివాహం ఎప్పుడు జరిగింది?",
                listOf("ఉత్తర ఫల్గుని నక్షత్రంలో","పునర్వసు నక్షత్రంలో","పుష్యమి నక్షత్రంలో","ఆశ్లేషా నక్షత్రంలో"), setOf(0)),
        )
    )
}
