package Components

import ExternalTypes.*
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.*

interface LocationSelectProps : RProps {
    var location: String
    var onSelect: (value: String) -> Unit
}

fun RBuilder.LocationSelect(handler: LocationSelectProps.() -> Unit): ReactElement {
    return child(LocationSelect::class) {
        this.attrs(handler)
    }
}

class LocationSelect(props: LocationSelectProps) : RComponent<LocationSelectProps, RState>() {
    val locations: List<String> = listOf("Zentralbibliothek", "Alstertal", "Altona", "Barmbek", "Bergedorf", "Billstedt", "Bramfeld", "Dehnhaide", "Eidelstedt", "Eimsbüttel", "Elbvororte", "Farmsen", "Finkenwerder", "Fuhlsbüttel", "Harburg", "Hohenhorst", "Holstenstraße", "Horn", "Kirchdorf", "Langenhorn", "Lokstedt", "Mümmelmannsberg", "Neuallermöhe", "Neugraben", "Niendorf", "Osdorfer Born", "Rahlstedt", "Schnelsen", "Steilshoop", "Volksdorf", "Wandsbek", "Wilhelmsburg", "Winterhude")

    override fun RBuilder.render() {
        Label {
            +"Standort:"
        }
        Input {
            attrs.type = "select"
            attrs.value = props.location
            attrs.onChange = {
                val target = it.target as HTMLSelectElement
                props.onSelect(target.value)
            }
            for (location in locations) {
                option { +"$location" }
            }
        }
    }
}
