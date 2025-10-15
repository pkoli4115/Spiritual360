package com.hindu.pooja.feature.ramakoti.reminders

/** Repeat pattern for Ramakoti reminders. */
enum class ReminderType(val code: Int) {
    ONE_TIME(0),
    DAILY(1),
    WEEKLY(2),
    INTERVAL(3);

    companion object {
        /** Safe decode from an int stored in DataStore/Firestore. Defaults to DAILY. */
        fun fromCode(code: Int): ReminderType =
            values().firstOrNull { it.code == code } ?: DAILY
    }
}
