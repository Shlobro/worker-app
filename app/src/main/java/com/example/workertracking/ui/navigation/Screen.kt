package com.example.workertracking.ui.navigation

sealed class Screen(val route: String, val titleRes: Int, val iconRes: Int? = null) {
    object Dashboard : Screen("dashboard", com.example.workertracking.R.string.nav_dashboard)
    object Projects : Screen("projects", com.example.workertracking.R.string.nav_projects)
    object Workers : Screen("workers", com.example.workertracking.R.string.nav_workers)
    object Events : Screen("events", com.example.workertracking.R.string.nav_events)
    
    // Detail screens
    object ProjectDetail : Screen("project_detail/{projectId}", com.example.workertracking.R.string.projects_title) {
        fun createRoute(projectId: Long) = "project_detail/$projectId"
    }
    object WorkerDetail : Screen("worker_detail/{workerId}", com.example.workertracking.R.string.workers_title) {
        fun createRoute(workerId: Long) = "worker_detail/$workerId"
    }
    object EventDetail : Screen("event_detail/{eventId}", com.example.workertracking.R.string.events_title) {
        fun createRoute(eventId: Long) = "event_detail/$eventId"
    }
    
    // Add/Edit screens
    object AddProject : Screen("add_project", com.example.workertracking.R.string.add_project)
    object EditProject : Screen("edit_project/{projectId}", com.example.workertracking.R.string.edit_project) {
        fun createRoute(projectId: Long) = "edit_project/$projectId"
    }
    object AddWorker : Screen("add_worker", com.example.workertracking.R.string.add_worker)
    object EditWorker : Screen("edit_worker/{workerId}", com.example.workertracking.R.string.edit_worker) {
        fun createRoute(workerId: Long) = "edit_worker/$workerId"
    }
    object AddEvent : Screen("add_event", com.example.workertracking.R.string.add_event)
    object EditEvent : Screen("edit_event/{eventId}", com.example.workertracking.R.string.edit_event) {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
    object AddShift : Screen("add_shift/{projectId}", com.example.workertracking.R.string.add_shift) {
        fun createRoute(projectId: Long) = "add_shift/$projectId"
    }
    object EditShift : Screen("edit_shift/{shiftId}", com.example.workertracking.R.string.edit_shift) {
        fun createRoute(shiftId: Long) = "edit_shift/$shiftId"
    }
    object ShiftDetail : Screen("shift_detail/{shiftId}", com.example.workertracking.R.string.shift_detail) {
        fun createRoute(shiftId: Long) = "shift_detail/$shiftId"
    }
    object AddIncome : Screen("add_income/{projectId}", com.example.workertracking.R.string.add_income) {
        fun createRoute(projectId: Long) = "add_income/$projectId"
    }
}