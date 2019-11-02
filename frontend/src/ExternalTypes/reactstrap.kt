@file:JsModule("reactstrap")

import org.w3c.dom.events.Event
import kotlin.js.*
import react.*

@JsName("Form")
external val Form: RClass<dynamic>

@JsName("FormGroup")
external val FormGroup: RClass<dynamic>

@JsName("Label")
external val Label: RClass<dynamic>

@JsName("Input")
external val Input: RClass<InputProps>

external interface InputProps: RProps {
    var onChange: ((Event) -> dynamic)?
    var type: String
}

@JsName("Button")
external val Button: RClass<ButtonProps>

external interface ButtonProps: RProps {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var disabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var onClick: (Event) -> Unit
}

@JsName("Spinner")
external val Spinner: RClass<SpinnerProps>

external interface SpinnerProps: RProps {
    var children: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var size: String? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
}

