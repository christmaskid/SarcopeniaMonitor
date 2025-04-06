package com.example.sarcopeniamonitor

object MyConstants {
    val gender_options = listOf(
        "Male", "Female", "Not applicable"
    )
    const val gender_default = 2
    val gender_map = mapOf(
        "Male" to 1, "Female" to 0, "Not applicable" to null,
    )
    val questionKeysList = listOf(
        "birthDate", "gender",
        "smoking",
        "a2_1", "a2_2", "b1_2", "b2_2", "b9",
        "c15","d1", "d2", "d3", "e1", "meal_status"
//        "a2", "a3_1",
//        "b2", "b2_1", "b2_2", "b4_2",
//        "c2", "c11_1", "c11_2", "c11_3",
//        "e2", "e4", "e11", "g10",
//        "ad8_a3"
    )
    val questionDescriptions = MyConstants.questionKeysList.associateWith { key ->
        try {
            val resId = R.string::class.java.getField("${key}_description").getInt(null)
            resId
        } catch (e: Exception) {
            null // If the resource is missing, default to null
        }
    }.filterValues { it != null } // Remove null values

    val optionResources = MyConstants.questionKeysList.associateWith { key ->
        try {
            val resId = R.array::class.java.getField("${key}_options").getInt(null)
            resId
        } catch (e: Exception) {
            null // If the resource is missing, default to null
        }
    }.filterValues { it != null } // Remove null values
}