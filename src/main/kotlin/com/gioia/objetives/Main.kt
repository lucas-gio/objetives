package com.gioia.objetives

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import java.math.BigDecimal
import java.math.RoundingMode

data class Model(
    var salaryInARS: BigDecimal = BigDecimal.ZERO,
    var objetive: BigDecimal = BigDecimal.ZERO,
    var resultInYears: String = "0"
    //var savingEuros: BigDecimal = BigDecimal.ZERO,
    //var savingDollars: BigDecimal = BigDecimal.ZERO,
)

private val _model = mutableStateOf(Model())
val model: State<Model> = _model
private val state by model // para evitar hacer model.value siempre

private inline fun changeState(reducer: Model.() -> Model): Model {
    val newModel = state.reducer()
    _model.value = newModel
    return newModel
}

@Composable
@Preview
fun MainWindow(dolar: BigDecimal, euro: BigDecimal) {
    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = state.salaryInARS.toString(),
            onValueChange = {
                changeState {
                    state.copy(salaryInARS = (it.ifEmpty { "0" }).toBigDecimal(), resultInYears = calculateYears(it.ifEmpty { "0" }.toBigDecimal(), objetive)) }
            },
            label = {
                Text(text = "Salario neto (AR$)")
            },
            trailingIcon = {
                if (state.salaryInARS.toString().isNotBlank())
                    IconButton(onClick = {
                        changeState {
                            state.copy(
                                salaryInARS = BigDecimal.ZERO,
                                resultInYears = BigDecimal.ZERO.toString()
                            )
                        }}
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = "Borrar"
                        )
                    }
            }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = state.objetive.toString(),
            onValueChange = {
                changeState { state.copy(objetive = (it.ifEmpty { "0" }).toBigDecimal(), resultInYears = calculateYears(state.salaryInARS, it.ifEmpty { "0" }.toBigDecimal())) }
            },
            label = {
                Text(text = "Cantidad de dinero objetivo")
            },
            trailingIcon = {
                if (state.objetive.toString().isNotBlank())
                    IconButton(onClick = {
                        changeState {
                            state.copy(
                                objetive = BigDecimal.ZERO,
                                resultInYears = BigDecimal.ZERO.toString()
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = "Borrar"
                        )
                    }
            }
        )
        /*OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            value = state.savingEuros.toString(),
            onValueChange = {
                changeState { state.copy(savingEuros = (it.ifEmpty { "0" }).toBigDecimal(), resultInYears = calculateYears(state.salaryInARS, state.objetive, (it.ifEmpty { "0" }).toBigDecimal())) }
            },
            label = {
                Text(text = "Cantidad de euros de ahorro a sumar")
            },
            trailingIcon = {
                if (state.savingEuros.toString().isNotBlank())
                    IconButton(onClick = {
                        changeState {
                            state.copy(
                                savingEuros = BigDecimal.ZERO,
                                resultInYears = state.resultInYears
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = "Borrar"
                        )
                    }
            }
        )*/
        Text(
            text = "${state.resultInYears} Años"
        )
    }
}

fun calculateYears(salaryInArs: BigDecimal, objetive: BigDecimal/*, savingEuros: BigDecimal*/): String {
    if(salaryInArs == BigDecimal.ZERO || objetive == BigDecimal.ZERO){
        return "0"
    }

    return try {
        val convertedSalary = salaryInArs
            .divide(Main.dolarValue, 2, RoundingMode.FLOOR)

        val remainingInMonths = objetive
            .divide(convertedSalary, 2, RoundingMode.FLOOR)
            .setScale(0, RoundingMode.FLOOR)

        remainingInMonths
            .divide("12".toBigDecimal(), 2, RoundingMode.FLOOR)
            .toString()
    }
    catch (e: ArithmeticException){
        "0"
    }
}

class Main {
    companion object {
        lateinit var dolarValue: BigDecimal
        lateinit var euroValue: BigDecimal
    }
}

fun main() {
    val client = JettyClient()
    val request = Request(Method.GET, "https://api.bluelytics.com.ar/v2/latest")
    val response = Gson().fromJson(client(request).bodyString(), JsonObject::class.java)

    Main.dolarValue = (response["blue"] as JsonObject)["value_sell"].toString().toBigDecimal()
    Main.euroValue = (response["blue_euro"] as JsonObject)["value_sell"].toString().toBigDecimal()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                size = DpSize(300.dp, 400.dp)
            )
        ) {
            MaterialTheme {
                MainWindow(Main.dolarValue, Main.euroValue)
            }
        }
    }
}