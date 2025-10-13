package com.hindu.pooja.feature.ramakoti.util

object LocaleText {
    data class Copy(
        val title: String,
        val subtitle: String,
        val completedLine: String,
        val issuedOn: String,
        val footer: String
    )

    fun certificateCopy(lang: String): Copy = when (lang.uppercase()) {
        "TE" -> Copy(
            title = "రామకోటీ పూర్తి ధ్రువీకరణ పత్రం",
            subtitle = "భక్తి పూర్వకంగా శ్రీరామ నామ స్మరణ",
            completedLine = "క్రింద పేర్కొన్న భక్తుడు/భక్తురాలు",
            issuedOn = "జారీ చేసిన తేది",
            footer = "జయ శ్రీరాం"
        )
        "HI" -> Copy(
            title = "रामकोटी पूर्णता प्रमाणपत्र",
            subtitle = "भक्ति से श्री राम नाम लेखन",
            completedLine = "नीचे उल्लेखित भक्त",
            issuedOn = "जारी दिनांक",
            footer = "जय श्री राम"
        )
        else -> Copy(
            title = "Ramakoti Completion Certificate",
            subtitle = "Devotional Sri Rama Namam Writing",
            completedLine = "The devotee named below has",
            issuedOn = "Issued On",
            footer = "Jai Sri Ram"
        )
    }
}
