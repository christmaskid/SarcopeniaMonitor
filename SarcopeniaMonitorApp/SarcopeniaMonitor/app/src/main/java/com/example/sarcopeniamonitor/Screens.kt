package com.example.sarcopeniamonitor

sealed class Screens (val screen: String) {
    data object Home: Screens("home")
    data object RecordList: Screens("recordList")
    data object Profile: Screens("profile")
    data object MyRecordIOInterface: Screens("myRecordIOInterface")
    data object Questionnaire: Screens("questionnaire")
    data object MyLLMChatScreen: Screens("myLLMChatScreen")
}