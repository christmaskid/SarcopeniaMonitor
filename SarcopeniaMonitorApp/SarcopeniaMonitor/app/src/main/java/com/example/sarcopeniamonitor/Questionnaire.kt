package com.example.sarcopeniamonitor

import android.util.Log

// Questionnaire Data Class
data class QuestionnaireData(
    val answers: Map<String, Any?>
)
data class QuestionnaireResponse(
    val answers: Map<String, Any?>
//    val birthDate: String?,
//    val gender: Int?,
//    val smoking: Int?,
//    val a2_1: Int?,
//    val a2_2: Int?,
//    val b1_2: Int?,
//    val b2_2: Int?,
//    val b9: Int?,
//    val c15: Int?,
//    val d1: Int?,
//    val d2: Int?,
//    val d3: Int?,
//    val e1: Int?,
//    val meal_status: Int?,

//    val a2: Int?,
//    val a3_1: Int?,
//    val b2: Int?,
//    val b2_1: Int?,
//    val b4_2: Int?,
//    val c2: Int?,
//    val c11_1: Int?,
//    val c11_2: Int?,
//    val c11_3: Int?,
//    val e2: Int?,
//    val e4: Int?,
//    val g10: Int?,
//    val ad8_a3: Int?,
//    val numericAnswers: Map<String, Int>? // for questions that need numeric values
)
fun QuestionnaireResponse.toQuestionnaireData(): QuestionnaireData {
    Log.d("ToQuestionnaireData", "Mapped answers: $answers")
//    return QuestionnaireData(answers = answers.filterValues { it != null })
    val filteredAnswers = answers.filterValues { it != null }
    Log.d("FilteredAnswers", filteredAnswers.toString())
    return QuestionnaireData(answers = filteredAnswers)
}

class Questionnaire(
    private val fields: MutableMap<String, Any?> = mutableMapOf()
) {
    fun getField(fieldName: String): Any? {
        return fields[fieldName] ?: ""  // Avoid returning null
    }

    fun setField(fieldName: String, value: Any?) {
        fields[fieldName] = value ?: "" // Avoid storing null values
    }

    fun getAllFields(): Map<String, Any?> {
        return fields.ifEmpty { mutableMapOf("placeholder" to "") } // Ensure it's not empty
    }
}
fun QuestionnaireData.toQuestionnaire(): Questionnaire {
    return Questionnaire(fields = answers.toMutableMap()) // Copy answers into the mutable fields map
}
fun Questionnaire.isAllRequiredFieldsFilled(requiredFields: List<String>): Boolean {
    requiredFields.forEach { field ->
        Log.d("require", field)
        Log.d("require", getField(field)?.toString()?.isNotEmpty().toString())
    }
    return requiredFields.all { field ->
        getField(field)?.toString()?.isNotEmpty() == true
    }
}
