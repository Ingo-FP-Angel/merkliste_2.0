package Components

import Models.Media
import react.*
import react.dom.*

interface MediaListProps: RProps {
    var Medias: List<Media>
}

fun RBuilder.MediaList(handler: MediaListProps.() -> Unit): ReactElement {
    return child(MediaList::class) {
        this.attrs(handler)
    }
}

class MediaList(props: MediaListProps): RComponent<MediaListProps, RState>() {
    override fun RBuilder.render() {
        ul {
            for (item in props.Medias.sortedBy { it.name }) {
                li {
                    div {
                        h3 {+"${item.name}"}
                        p {+"${item.type}"}
                        p {+"${item.signature}"}
                        p {+"Anzahl: ${item.availability}"}
                    }
                }
            }
        }
    }
}
