package Components

import ExternalTypes.*
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.*

interface MediatypeSelectProps : RProps {
    var mediatype: String
    var onSelect: (value: String) -> Unit
}

fun RBuilder.MediatypeSelect(handler: MediatypeSelectProps.() -> Unit): ReactElement {
    return child(MediatypeSelect::class) {
        this.attrs(handler)
    }
}

class MediatypeSelect(props: MediatypeSelectProps) : RComponent<MediatypeSelectProps, RState>() {
    val mediatypes: Map<String, String> = mapOf("Alle" to "all", "Bücher" to "books", "Filme/Serien" to "movies", "Musik" to "music")

    override fun RBuilder.render() {
        Label {
            +"Medienart:"
        }
        Input {
            attrs.type = "select"
            attrs.value = props.mediatype
            attrs.onChange = {
                val target = it.target as HTMLSelectElement
                props.onSelect(target.value)
            }
            for ((mediatypeDisplay, mediatype) in mediatypes) {
                option {
                    attrs.value = mediatype
                    +"$mediatypeDisplay"
                }
            }
        }
    }
}
