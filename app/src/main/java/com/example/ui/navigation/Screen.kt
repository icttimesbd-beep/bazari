package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Lists : Screen("lists")
    object ListDetail : Screen("list_detail/{listId}") {
        fun createRoute(listId: String) = "list_detail/$listId"
    }
    object ProductPicker : Screen("product_picker/{listId}") {
        fun createRoute(listId: String) = "product_picker/$listId"
    }
    object Templates : Screen("templates")
    object TemplateDetail : Screen("template_detail/{templateId}") {
        fun createRoute(templateId: String) = "template_detail/$templateId"
    }
    object History : Screen("history")
    object Events : Screen("events")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Settings : Screen("settings")
}
