package com.example.sarcopeniamonitor

data class Prediction (
    val asmiPrediction: Double? = null,
    val asmiPredictionTScore: Double? = null,
    val handGripPrediction: Double? = null,
    val sarcopeniaStatus: Int? = null,
    val gripStrengthData: Double? = null,
    val gaitSpeedData: Double? = null,
    val standUpData: Double? = null,
)