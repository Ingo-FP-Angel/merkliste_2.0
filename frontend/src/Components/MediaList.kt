package Components

import Models.Media
import react.*
import react.dom.*

interface MediaListProps : RProps {
    var Medias: List<Media>
    var isLoading: Boolean
}

fun RBuilder.MediaList(handler: MediaListProps.() -> Unit): ReactElement {
    return child(MediaList::class) {
        this.attrs(handler)
    }
}

class MediaList(props: MediaListProps) : RComponent<MediaListProps, RState>() {
    override fun RBuilder.render() {
        if (props.isLoading) {
            p { +"Merkliste wird abgerufen. Dies dauert etwa eine Sekunde pro Merklisteneintrag." }
        } else if (props.Medias.size == 0) {
            p { +"Merkliste leer oder noch nicht abgerufen" }
        } else {
            ul {
                for (item in props.Medias.filter { it.availability > 0 }.sortedBy { it.name }) {
                    li {
                        div {
                            h3 { +"${item.name}" }
                            p { +"${item.type}" }
                            p { +"${item.signature}" }
                            p { +"Anzahl: ${item.availability}" }
                        }
                    }
                }
            }
        }
    }
}
