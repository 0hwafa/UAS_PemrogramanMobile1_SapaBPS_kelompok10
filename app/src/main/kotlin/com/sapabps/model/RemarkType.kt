package com.sapabps.model

enum class RemarkType(val label: String) {
    KUNJUNGAN_AKADEMIK("Kunjungan Akademik"),
    KONSULTASI_DATA("Konsultasi Data Statistik"),
    PERMINTAAN_DATA("Permintaan Data"),
    PENELITIAN_RISET("Penelitian / Riset"),
    KUNJUNGAN_DINAS("Kunjungan Dinas");

    companion object {
        fun labels(): List<String> = values().map { it.label }
    }
}
