import Components.MediaList
import Models.Media
import react.dom.*
import react.*
import org.w3c.dom.HTMLInputElement
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

interface AppState : RState {
    var user: String
    var pass: String
    var availableItems: List<Media>
}

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        user = ""
        pass = ""
        availableItems = listOf()
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
                    attrs.onClick = {
                        getMediaList()
                    }
                    +"Medien abrufen"
                }
            }
        }
        div {
            h3 {
                +"Verfügbare Medien"
            }
            MediaList {
                Medias = state.availableItems
            }
        }
    }

    fun getMediaList() {
        val mainScope = MainScope()
        mainScope.launch {
            val medias = fetchAvailableMedias(state.user, state.pass)
            setState {
                availableItems = medias.toList()
            }
        }
    }
}
