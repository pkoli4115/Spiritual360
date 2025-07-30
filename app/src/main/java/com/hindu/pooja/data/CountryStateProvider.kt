package com.hindu.pooja.data

object CountryStateProvider {
    private val countryStateMap = mapOf(
        "India" to listOf("Andhra Pradesh", "Telangana", "Tamil Nadu", "Karnataka", "Maharashtra"),
        "United States" to listOf("California", "Texas", "New York"),
        "Canada" to listOf("Ontario", "Quebec", "British Columbia"),
        "Australia" to listOf("New South Wales", "Victoria", "Queensland")
    )

    fun getAllCountries(): List<String> {
        return countryStateMap.keys.sorted()
    }

    fun getStatesForCountry(country: String): List<String> {
        return countryStateMap[country] ?: emptyList()
    }

    // ✅ Add these aliases for compatibility
    fun getCountries(): List<String> = getAllCountries()
    fun getStates(country: String): List<String> = getStatesForCountry(country)
}
