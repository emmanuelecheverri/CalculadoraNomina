package com.emmanuelecheverri.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emmanuelecheverri.calculadoranomina.ui.theme.CalculadoraNominaTheme
import java.text.NumberFormat
import java.util.Locale


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CalculadoraNominaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    CalculadoraNominaScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun CalculadoraNominaScreen(
    modifier: Modifier = Modifier
) {

    // ---------------------------------------------------------
    // ESTADO DE LOS CAMPOS
    // ---------------------------------------------------------

    var salarioInput by remember {
        mutableStateOf("")
    }

    var horasDiurnasInput by remember {
        mutableStateOf("")
    }

    var horasNocturnasInput by remember {
        mutableStateOf("")
    }


    // ---------------------------------------------------------
    // ESTADO DE LOS SWITCHES
    // ---------------------------------------------------------

    var esDomingoFestivo by remember {
        mutableStateOf(false)
    }

    var transporteEmpresa by remember {
        mutableStateOf(false)
    }


    // ---------------------------------------------------------
    // ESTADO DEL RESULTADO
    // ---------------------------------------------------------

    var resultado by remember {
        mutableStateOf<ResultadoNomina?>(null)
    }


    // ---------------------------------------------------------
    // ESTADO DE VALIDACIONES
    // ---------------------------------------------------------

    var errorSalario by remember {
        mutableStateOf(false)
    }

    var errorHorasDiurnas by remember {
        mutableStateOf(false)
    }

    var errorHorasNocturnas by remember {
        mutableStateOf(false)
    }

    var errorMaximoHoras by remember {
        mutableStateOf(false)
    }


    // ---------------------------------------------------------
    // FUNCIÓN PARA CALCULAR Y VALIDAR
    // ---------------------------------------------------------

    fun realizarCalculo() {

        val salario = salarioInput.toDoubleOrNull()

        val horasDiurnas =
            if (horasDiurnasInput.isBlank()) {
                0.0
            } else {
                horasDiurnasInput.toDoubleOrNull()
                    ?: -1.0
            }

        val horasNocturnas =
            if (horasNocturnasInput.isBlank()) {
                0.0
            } else {
                horasNocturnasInput.toDoubleOrNull()
                    ?: -1.0
            }


        // -----------------------------------------------------
        // VALIDACIÓN DEL SALARIO
        // -----------------------------------------------------

        errorSalario =
            salario == null ||
                    salario < SMMLV_2026


        // -----------------------------------------------------
        // VALIDACIÓN DE HORAS DIURNAS
        // -----------------------------------------------------

        errorHorasDiurnas =
            horasDiurnas < 0


        // -----------------------------------------------------
        // VALIDACIÓN DE HORAS NOCTURNAS
        // -----------------------------------------------------

        errorHorasNocturnas =
            horasNocturnas < 0


        // -----------------------------------------------------
        // VALIDACIÓN DEL MÁXIMO DE HORAS EXTRA
        // -----------------------------------------------------

        errorMaximoHoras =
            !errorHorasDiurnas &&
                    !errorHorasNocturnas &&
                    horasDiurnas + horasNocturnas >
                    MAXIMO_HORAS_EXTRA


        // -----------------------------------------------------
        // COMPROBAR SI HAY ALGÚN ERROR
        // -----------------------------------------------------

        val hayErrores =
            errorSalario ||
                    errorHorasDiurnas ||
                    errorHorasNocturnas ||
                    errorMaximoHoras


        // -----------------------------------------------------
        // CALCULAR SOLO SI TODO ES VÁLIDO
        // -----------------------------------------------------

        if (!hayErrores && salario != null) {

            resultado = calcularNomina(
                salarioBasico = salario,
                horasDiurnas = horasDiurnas,
                horasNocturnas = horasNocturnas,
                esDominical = esDomingoFestivo,
                transporteEmpresa = transporteEmpresa
            )
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -----------------------------------------------------
        // TÍTULO
        // -----------------------------------------------------

        Text(
            text = stringResource(R.string.titulo_nomina),
            fontSize = 24.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -----------------------------------------------------
        // F1 — SALARIO BÁSICO
        // -----------------------------------------------------

        CampoNumerico(
            etiqueta = R.string.salario_basico,
            valor = salarioInput,

            onValueChange = {
                salarioInput = it
                errorSalario = false
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),

            isError = errorSalario,

            modifier = Modifier.fillMaxWidth()
        )

        if (errorSalario) {

            Text(
                text = if (salarioInput.isBlank()) {
                    stringResource(
                        R.string.error_salario_requerido
                    )
                } else {
                    stringResource(
                        R.string.error_salario_minimo
                    )
                }
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // F2 — HORAS EXTRA DIURNAS
        // -----------------------------------------------------

        CampoNumerico(
            etiqueta = R.string.horas_extra_diurnas,
            valor = horasDiurnasInput,

            onValueChange = {
                horasDiurnasInput = it
                errorHorasDiurnas = false
                errorMaximoHoras = false
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),

            isError =
                errorHorasDiurnas ||
                        errorMaximoHoras,

            modifier = Modifier.fillMaxWidth()
        )

        if (errorHorasDiurnas) {

            Text(
                text = stringResource(
                    R.string.error_horas_negativas
                )
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // F3 — HORAS EXTRA NOCTURNAS
        // -----------------------------------------------------

        CampoNumerico(
            etiqueta = R.string.horas_extra_nocturnas,
            valor = horasNocturnasInput,

            onValueChange = {
                horasNocturnasInput = it
                errorHorasNocturnas = false
                errorMaximoHoras = false
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),

            isError =
                errorHorasNocturnas ||
                        errorMaximoHoras,

            modifier = Modifier.fillMaxWidth()
        )

        if (errorHorasNocturnas) {

            Text(
                text = stringResource(
                    R.string.error_horas_negativas
                )
            )
        }

        if (errorMaximoHoras) {

            Text(
                text = stringResource(
                    R.string.error_maximo_horas_extra
                )
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // F4 — DOMINGO O FESTIVO
        // -----------------------------------------------------

        FilaInterruptor(
            etiqueta = stringResource(
                R.string.horas_domingo_festivo
            ),

            checked = esDomingoFestivo,

            onCheckedChange = {

                esDomingoFestivo = it

                // Si ya existe un resultado,
                // recalculamos automáticamente.
                if (resultado != null) {
                    realizarCalculo()
                }
            }
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // -----------------------------------------------------
        // F5 — TRANSPORTE DE LA EMPRESA
        // -----------------------------------------------------

        FilaInterruptor(
            etiqueta = stringResource(
                R.string.transporte_empresa
            ),

            checked = transporteEmpresa,

            onCheckedChange = {

                transporteEmpresa = it

                // Si ya existe un resultado,
                // recalculamos automáticamente.
                if (resultado != null) {
                    realizarCalculo()
                }
            }
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -----------------------------------------------------
        // F6 — BOTÓN CALCULAR
        // -----------------------------------------------------

        Button(
            onClick = {
                realizarCalculo()
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = stringResource(
                    R.string.calcular
                )
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // -----------------------------------------------------
        // F11 — BOTÓN LIMPIAR
        // -----------------------------------------------------

        Button(
            onClick = {

                // Vaciar campos.
                salarioInput = ""
                horasDiurnasInput = ""
                horasNocturnasInput = ""

                // Apagar switches.
                esDomingoFestivo = false
                transporteEmpresa = false

                // Quitar errores.
                errorSalario = false
                errorHorasDiurnas = false
                errorHorasNocturnas = false
                errorMaximoHoras = false

                // Ocultar resultado.
                resultado = null
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = stringResource(
                    R.string.limpiar
                )
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -----------------------------------------------------
        // RESULTADO
        // -----------------------------------------------------

        resultado?.let { nomina ->

            ResultadoNominaView(
                resultado = nomina
            )
        }
    }
}


/*
 * ------------------------------------------------------------
 * CAMPO NUMÉRICO REUTILIZABLE
 * ------------------------------------------------------------
 *
 * No tiene estado propio.
 * Recibe el valor y el callback desde el padre.
 */
@Composable
fun CampoNumerico(
    etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = valor,

        onValueChange = onValueChange,

        label = {
            Text(
                text = stringResource(etiqueta)
            )
        },

        keyboardOptions = keyboardOptions,

        isError = isError,

        modifier = modifier
    )
}


/*
 * ------------------------------------------------------------
 * SWITCH REUTILIZABLE
 * ------------------------------------------------------------
 *
 * No tiene estado propio.
 * Recibe el valor actual y el callback.
 */
@Composable
fun FilaInterruptor(
    etiqueta: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = etiqueta
        )

        Switch(
            checked = checked,

            onCheckedChange =
                onCheckedChange
        )
    }
}


/*
 * ------------------------------------------------------------
 * RESULTADOS
 * ------------------------------------------------------------
 */
@Composable
fun ResultadoNominaView(
    resultado: ResultadoNomina
) {

    // El salario básico se recupera a partir
    // del valor de la hora ordinaria.
    val salarioBasico =
        resultado.valorHora *
                HORAS_ORDINARIAS_MENSUALES


    // ---------------------------------------------------------
    // F9 — CLASIFICACIÓN DEL RANGO
    // ---------------------------------------------------------

    val rango =
        clasificarRango(salarioBasico)


    val imagenRango: Int
    val tituloRango: Int
    val descripcionRango: Int
    val descripcionImagen: Int


    when (rango) {

        RangoSalarial.RANGO_1 -> {

            imagenRango =
                R.drawable.rango_1

            tituloRango =
                R.string.rango_1

            descripcionRango =
                R.string.descripcion_rango_1

            descripcionImagen =
                R.string.imagen_rango_1
        }


        RangoSalarial.RANGO_2 -> {

            imagenRango =
                R.drawable.rango_2

            tituloRango =
                R.string.rango_2

            descripcionRango =
                R.string.descripcion_rango_2

            descripcionImagen =
                R.string.imagen_rango_2
        }


        RangoSalarial.RANGO_3 -> {

            imagenRango =
                R.drawable.rango_3

            tituloRango =
                R.string.rango_3

            descripcionRango =
                R.string.descripcion_rango_3

            descripcionImagen =
                R.string.imagen_rango_3
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // -----------------------------------------------------
        // TÍTULO DEL RESULTADO
        // -----------------------------------------------------

        Text(
            text = stringResource(
                R.string.resultado
            ),

            fontSize = 20.sp
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // F9 — IMAGEN DEL RANGO
        // -----------------------------------------------------

        Image(
            painter =
                painterResource(
                    id = imagenRango
                ),

            contentDescription =
                stringResource(
                    descripcionImagen
                ),

            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = stringResource(
                tituloRango
            ),

            fontSize = 18.sp
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = stringResource(
                descripcionRango
            )
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // -----------------------------------------------------
        // F7 — DESGLOSE
        // -----------------------------------------------------

        Text(
            text =
                stringResource(
                    R.string.valor_hora
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.valorHora
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.salario_basico_resultado
                ) +
                        ": " +
                        formatearMoneda(
                            salarioBasico
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.horas_extra_resultado
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.totalHorasExtra
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.auxilio_transporte
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.auxilioTransporte
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.total_devengado
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.totalDevengado
                        )
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Text(
            text =
                stringResource(
                    R.string.salud
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.aporteSalud
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.pension
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.aportePension
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.solidaridad
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.fondoSolidaridad
                        )
        )


        Text(
            text =
                stringResource(
                    R.string.total_deducciones
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.totalDeducciones
                        )
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------------------------------
        // SALARIO NETO
        // -----------------------------------------------------

        Text(
            text =
                stringResource(
                    R.string.salario_neto
                ) +
                        ": " +
                        formatearMoneda(
                            resultado.salarioNeto
                        ),

            fontSize = 22.sp
        )
    }
}


/*
 * ------------------------------------------------------------
 * F8 — FORMATO DE MONEDA
 * ------------------------------------------------------------
 */
fun formatearMoneda(
    valor: Double
): String {

    val formato =
        NumberFormat.getCurrencyInstance(
            Locale("es", "CO")
        )

    formato.maximumFractionDigits = 0

    return formato.format(valor)
}


/*
 * ------------------------------------------------------------
 * F12 — PREVIEW
 * ------------------------------------------------------------
 */
@Preview(
    showBackground = true
)
@Composable
fun CalculadoraNominaPreview() {

    CalculadoraNominaTheme {

        CalculadoraNominaScreen()
    }
}