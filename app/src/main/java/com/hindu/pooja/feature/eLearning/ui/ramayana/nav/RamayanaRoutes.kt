package com.hindu.pooja.ui.ramayana

object RamayanaRoutes {
    const val HUB = "ramayana/hub"

    const val RAMAKOTI_WRITER = "ramakoti/writer"

    const val BALA_WIKI = "ramayana/bala/wiki"
    const val BALA_QUIZ = "ramayana/bala/quiz"

    const val AYODHYA_WIKI = "ramayana/ayodhya/wiki"
    const val AYODHYA_QUIZ = "ramayana/ayodhya/quiz"

    const val ARANYA_WIKI = "ramayana/aranya/wiki"
    const val ARANYA_QUIZ = "ramayana/aranya/quiz"

    const val KISHKINDHA_WIKI = "ramayana/kishkindha/wiki"
    const val KISHKINDHA_QUIZ = "ramayana/kishkindha/quiz"

    const val SUNDARA_WIKI = "ramayana/sundara/wiki"
    const val SUNDARA_QUIZ = "ramayana/sundara/quiz"

    const val YUDDHA_WIKI = "ramayana/yuddha/wiki"
    const val YUDDHA_QUIZ = "ramayana/yuddha/quiz"

    const val UTTARA_WIKI = "ramayana/uttara/wiki"
    const val UTTARA_QUIZ = "ramayana/uttara/quiz"

    fun kandas(lang: String): String = "ramayana/kandas/$lang"

    fun reader(lang: String, kandaId: String): String =
        "ramayana/reader/$lang/$kandaId"
}