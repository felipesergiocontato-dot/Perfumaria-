package com.example.data.model

enum class FuelType(val displayName: String) {
    GASOLINA_COMUM("Gasolina Comum"),
    GASOLINA_ADITIVADA("Gasolina Aditivada"),
    ETANOL("Etanol"),
    DIESEL_S10("Diesel S10"),
    DIESEL_COMUM("Diesel"),
    GNV("GNV"),
    ELETRICO("Elétrico");

    companion object {
        fun fromDisplayName(name: String): FuelType {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: GASOLINA_COMUM
        }
    }
}

enum class ExpenseCategory(val displayName: String, val iconName: String) {
    ESTACIONAMENTO("Estacionamento", "local_parking"),
    PEDAGIO("Pedágio", "toll"),
    LAVAGEM("Lavagem", "local_car_wash"),
    TROCA_OLEO("Troca de Óleo", "oil_barrel"),
    MANUTENCAO("Manutenção", "build"),
    SEGURO("Seguro", "shield"),
    IPVA("IPVA / Taxas", "receipt_long"),
    MULTA("Multa", "warning"),
    OUTROS("Outros Gastos", "more_horiz");

    companion object {
        fun fromDisplayName(name: String): ExpenseCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: OUTROS
        }
    }
}
