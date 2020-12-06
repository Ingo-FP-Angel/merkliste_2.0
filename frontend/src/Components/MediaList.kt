package Components

import ExternalTypes.Input
import ExternalTypes.Label
import Models.Media
import react.*
import react.dom.*

interface MediaListProps: RProps {
    var Medias: List<Media>
    var isLoading: Boolean
}

interface MediaListState: RState {
    var showUnavailable: Boolean
}

fun RBuilder.MediaList(handler: MediaListProps.() -> Unit): ReactElement {
    return child(MediaList::class) {
        this.attrs(handler)
    }
}

val baseUrl= "https://www.buecherhallen.de/"

class MediaList(props: MediaListProps) : RComponent<MediaListProps, MediaListState>() {
    override fun MediaListState.init() {
        showUnavailable = false
    }

    override fun RBuilder.render() {
        if (props.isLoading) {
            p { +"Merkliste wird abgerufen. Dies dauert etwa eine Sekunde pro Merklisteneintrag." }
        } else if (props.Medias.size == 0) {
            p { +"Merkliste leer oder noch nicht abgerufen" }
        } else {
            Input {
                attrs.type = "checkbox"
                attrs.id = "showUnavailable"
                attrs.className = ""
                attrs.checked = state.showUnavailable
                attrs.onChange = {
                    setState {
                        showUnavailable = !showUnavailable
                    }
                }
            }
            Label {
                +"Nicht verfügbare Medien anzeigen"
            }
            ul {
                for (item in getMediaList(state.showUnavailable)) {
                    li {
                        div {
                            p(if (item.availability > 0) "available" else "unavailable")
                                { +"${item.name} ${if (!item.author.isNullOrEmpty()) "- " + item.author else ""}" }
                            p { +"${item.type} - ${item.signature}" }
                            p {
                                +"${getAvailabilityDisplay(item.availability)} - "
                                a {
                                    attrs.href = baseUrl + item.url
                                    attrs.target = "_blank"
                                    +"Details anzeigen"
                                    img {
                                        attrs.src = "external-link-ltr-icon.svg"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMediaList(showUnavailable: Boolean): List<Media> = if (showUnavailable) props.Medias.sortedBy { it.name } else props.Medias.filter { it.availability > 0 }.sortedBy { it.name }

    private fun getAvailabilityDisplay(availability: Int): String = when (availability) {
        -3 -> "Abrufen fehlgeschlagen"
        -2 -> "Keine Verfügbarkeitsinfos"
        -1 -> "Nicht am Standort"
        0 -> "Aktuell nicht verfügbar"
        else -> "${availability} verfügbar"
    }
}
