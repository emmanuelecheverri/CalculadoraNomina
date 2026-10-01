package com.emmanuelecheverri.calculadoranomina
const val SMMLV_2026 = 1_750_905.0
const val AUXILIO_TRANSPORTE_2026 = 249_095.0
const val HORAS_ORDINARIAS_MENSUALES = 210.0
const val PORCENTAJE_SALUD = 0.04
const val PORCENTAJE_PENSION = 0.04
const val PORCENTAJE_SOLIDARIDAD = 0.01
const val FACTOR_EXTRA_DIURNA = 1.25
const val FACTOR_EXTRA_NOCTURNA = 1.75
const val FACTOR_EXTRA_DIURNA_DOMINICAL = 2.15
const val FACTOR_EXTRA_NOCTURNA_DOMINICAL = 2.65

const val MAXIMO_HORAS_EXTRA = 48.0
enum class RangoSalarial {
    RANGO_1,
    RANGO_2,
    RANGO_3
}
data class ResultadoNomina(
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double
)

fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {

    val valorHora = salarioBasico / HORAS_ORDINARIAS_MENSUALES

    val factorDiurno = if (esDominical) {
        FACTOR_EXTRA_DIURNA_DOMINICAL
    } else {
        FACTOR_EXTRA_DIURNA
    }

    val factorNocturno = if (esDominical) {
        FACTOR_EXTRA_NOCTURNA_DOMINICAL
    } else {
        FACTOR_EXTRA_NOCTURNA
    }

    val pagoExtrasDiurnas =
        horasDiurnas * valorHora * factorDiurno

    val pagoExtrasNocturnas =
        horasNocturnas * valorHora * factorNocturno

    val totalHorasExtra =
        pagoExtrasDiurnas + pagoExtrasNocturnas

    val ibc = salarioBasico + totalHorasExtra

    val auxilioTransporte = if (
        salarioBasico <= SMMLV_2026 * 2 &&
        !transporteEmpresa
    ) {
        AUXILIO_TRANSPORTE_2026
    } else {
        0.0
    }

    val totalDevengado =
        ibc + auxilioTransporte

    val aporteSalud =
        ibc * PORCENTAJE_SALUD

    val aportePension =
        ibc * PORCENTAJE_PENSION

    val fondoSolidaridad = if (
        ibc >= SMMLV_2026 * 4
    ) {
        ibc * PORCENTAJE_SOLIDARIDAD
    } else {
        0.0
    }

    val totalDeducciones =
        aporteSalud +
                aportePension +
                fondoSolidaridad

    val salarioNeto =
        totalDevengado - totalDeducciones

    return ResultadoNomina(
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto
    )
}

fun clasificarRango(
    salarioBasico: Double
): RangoSalarial {

    return when {
        salarioBasico <= SMMLV_2026 * 2 -> {
            RangoSalarial.RANGO_1
        }

        salarioBasico < SMMLV_2026 * 4 -> {
            RangoSalarial.RANGO_2
        }

        else -> {
            RangoSalarial.RANGO_3
        }
    }
}