package com.sagrd.codereviewerapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destinations {
    @Serializable
    data object Selection : Destinations()

    @Serializable
    data object Review : Destinations()

    @Serializable
    data object Summary : Destinations()

    @Serializable
    data object History : Destinations()
}