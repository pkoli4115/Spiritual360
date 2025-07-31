package com.hindu.pooja.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val spouseName: String = "",
    val maritalStatus: String = "",
    val hasChildren: Boolean = false,
    val numberOfChildren: String = "",
    val childNames: List<String> = emptyList(),
    val gothram: String = "",
    val nakshatram: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val birthPlace: String = "",
    val email: String = "",
    val phone: String = "",
    val countryCode: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val addressLine3: String = "",
    val selectedCountry: String = "",
    val selectedState: String = "",
    val city: String = "",
    val pincode: String = "",
    val photoUrl: String = "",              // 🔼 New for profile image
    val isPremium: Boolean = false          // 🔼 New for premium badge/upgrade logic
)
