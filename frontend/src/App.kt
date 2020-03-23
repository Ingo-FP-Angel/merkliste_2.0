import Components.LocationSelect
import Components.MediaList
import Components.MediatypeSelect
import ExternalTypes.*
import Models.Media
import react.dom.*
import react.*
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.*
import kotlin.browser.window
import kotlin.js.json
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface AppState : RState {
    var user: String
    var pass: String
    var location: String
    var mediatype: String
    var availableItems: List<Media>
    var isLoading: Boolean
    var errorMessage: String
}

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        user = ""
        pass = ""
        location = "Zentralbibliothek"
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
        val useStream = true
        val mainScope = MainScope()
        if (useStream) {
            mainScope.launch {
                try {
                    val backendBaseUrl = if (process.env.NODE_ENV == "production") "/api" else "http://localhost:8080/api"
                    val responsePromise = window.fetch("$backendBaseUrl/flux/media?location=${state.location}&mediatype=${state.mediatype}",
                            object: RequestInit {
                                override var method: String? = "GET"
                                override var headers: dynamic = json("username" to state.user, "password" to state.pass)
                            }
                    )

                    responsePromise.then { response ->
                        ndjsonStream(response.body)
                    }.then { stream ->
                        val reader = stream.getReader()
                        var readRecurse: ((result: Result) -> Unit)? = null
                        readRecurse = { result ->
                            console.log("result = ", result)
                            if (!result.done) {
                                val media = convertToMedia(result?.value)
                                console.log("media = ", media)
                                media?.let {
                                    setState {
                                        availableItems = availableItems + listOf(media)
                                    }
                                }
                                reader.read().then(readRecurse)
                            } else {
                                setState {
                                    isLoading = false
                                }
                            }
                        }
                        reader.read().then(readRecurse)
                    }
                } catch (err: Error) {
                    setState {
                        errorMessage = err.message ?: "Fehler beim Abrufen"
                        isLoading = false
                    }
                } finally {
                }
            }
        } else {
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

    private fun convertToMedia(input: Any?): Media? {
        return input?.let {
            val foo = it.asDynamic()
            Media(foo.name, foo.author, foo.type, foo.signature, foo.url, foo.availability)
        }
    }
}
