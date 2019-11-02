import Components.MediaList
import Models.Media
import react.dom.*
import react.*
import org.w3c.dom.HTMLInputElement
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface AppState : RState {
    var user: String
    var pass: String
    var availableItems: List<Media>
    var isLoading: Boolean
}

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        user = ""
        pass = ""
        availableItems = listOf()
        isLoading = false
    }

    override fun RBuilder.render() {
        h1 {
            +"Merkliste 2.0"
        }
        Form {
            FormGroup {
                Label {
                    +"Username:"
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
                    +"Password:"
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
            availableItems = listOf()
        }
        val mainScope = MainScope()
        mainScope.launch {
            try {
                val medias = fetchAvailableMedias(state.user, state.pass)
                setState {
                    isLoading = false
                    availableItems = medias.toList()
                }
            } catch (ex: Exception) {
                console.log(ex)
            } finally {
                setState {
                    isLoading = false
                }
            }
        }
    }
}
