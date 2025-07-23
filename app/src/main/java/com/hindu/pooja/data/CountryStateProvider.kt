package com.hindu.pooja.data

object CountryStateProvider {

    private val countryStateMap: Map<String, List<String>> = mapOf(
        "India" to listOf(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
            "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
            "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Puducherry"
        ),
        "United States" to listOf(
            "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado",
            "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho",
            "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana",
            "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota",
            "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada",
            "New Hampshire", "New Jersey", "New Mexico", "New York",
            "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon",
            "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota",
            "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington",
            "West Virginia", "Wisconsin", "Wyoming"
        ),
        "Canada" to listOf(
            "Alberta", "British Columbia", "Manitoba", "New Brunswick", "Newfoundland and Labrador",
            "Nova Scotia", "Ontario", "Prince Edward Island", "Quebec", "Saskatchewan"
        ),
        "Australia" to listOf(
            "New South Wales", "Queensland", "South Australia", "Tasmania",
            "Victoria", "Western Australia", "Australian Capital Territory",
            "Northern Territory"
        ),
        "United Kingdom" to listOf(
            "England", "Scotland", "Wales", "Northern Ireland"
        ),
        "Other" to emptyList()
    )

    fun getAllCountries(): List<String> {
        return countryStateMap.keys.sorted()
    }

    fun getStatesForCountry(country: String): List<String> {
        return countryStateMap[country] ?: emptyList()
    }
}
