import Components.LocationSelect
import Components.MediaList
import Components.MediatypeSelect
import ExternalTypes.*
import Models.Media
import react.dom.*
import react.*
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.localStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface AppState : RState {
    var user: String
    var pass: String
    var location: String
    var defaultLocation: String?
    var mediatype: String
    var availableItems: List<Media>
    var isLoading: Boolean
    var errorMessage: String
}

val localStorageDefaultLocation = "merkliste:defaultLocation"

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        user = ""
        pass = ""
        location = localStorage.getItem(localStorageDefaultLocation) ?: "Zentralbibliothek"
        defaultLocation = localStorage.getItem(localStorageDefaultLocation)
        mediatype = "all"
        availableItems = listOf()
        isLoading = false
        errorMessage = ""
    }

    override fun RBuilder.render() {
        h1 {
            +"Merkliste 2.0"
        }
        Form {
            FormGroup {
                Label {
                    +"Nummer der Kundenkarte:"
                }
                Input {
                    attrs.type = "text"
                    attrs.onChange = {
                        val target = it.target as HTMLInputElement
                        setState {
                            user = target.value
                        }
                    }
                }
                Label {
                    +"Passwort:"
                }
                Input {
                    attrs.type = "password"
                    attrs.onChange = {
                        val target = it.target as HTMLInputElement
                        setState {
                            pass = target.value
                        }
                    }
                }
                LocationSelect {
                    location = state.location
                    onSelect = { value ->
                        localStorage.setItem(localStorageDefaultLocation, value)
                        setState {
                            location = value
                        }
                    }
                }
                MediatypeSelect {
                    mediatype = state.mediatype
                    onSelect = { value ->
                        setState {
                            mediatype = value
                        }
                    }
                }
                Button {
                    attrs.color = "primary"
                    attrs.disabled = state.isLoading || state.user.isNullOrEmpty() || state.pass.isNullOrEmpty()
                    attrs.onClick = {
                        getMediaList()
                    }
                    if (state.isLoading) {
                        Spinner {
                            attrs.color = "dark"
                            attrs.size = "sm"
                            attrs.type = "border"
                        }
                        +"Lade..."
                    } else {
                        +"Medien abrufen"
                    }
                }
            }
        }
        if (!state.errorMessage.isNullOrEmpty()) {
            p { +"${state.errorMessage}" }
        }
        div {
            h3 {
                +"Verfügbare Medien"
            }
            MediaList {
                Medias = state.availableItems
                isLoading = state.isLoading
            }
        }
    }

    fun getMediaList() {
        setState {
            isLoading = true
            errorMessage = ""
            availableItems = listOf()
        }
        val mainScope = MainScope()
        mainScope.launch {
            try {
                val medias = fetchAvailableMedias(state.user, state.pass, state.location, state.mediatype)
                setState {
                    isLoading = false
                    availableItems = medias.toList()
                }
            } catch (err: Error) {
                setState {
                    errorMessage = err.message ?: "Fehler beim Abrufen"
                }
            } finally {
                setState {
                    isLoading = false
                }
            }
        }
    }
}
