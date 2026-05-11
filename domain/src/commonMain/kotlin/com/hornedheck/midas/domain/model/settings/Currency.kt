package com.hornedheck.midas.domain.model.settings

enum class Currency {
    USD, EUR, PLN, GBP;

    val code: String get() = name
}
