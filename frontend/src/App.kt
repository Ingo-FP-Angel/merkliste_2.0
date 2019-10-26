import Components.MediaList
import Models.Media
import react.dom.*
import react.*

interface AppState: RState {
    var availableItems: List<Media>
}

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        availableItems = listOf(
                Media("Heavy Trip", "DVD", "Spielfilm Komödie HEAV", "https://www.buecherhallen.de/suchergebnis-detail/medium/T019895215.html", 1),
                Media("Pathfinder Rollenspiel Ausrüstungskompendium", "Buch", "GAMES Rollenspiele PATH", "https://www.buecherhallen.de/suchergebnis-detail/medium/T019263328.html", 1),
                Media("Die Weltverbesserer", "Buch", "Gal 1 WELT", "https://www.buecherhallen.de/suchergebnis-detail/medium/T017596504.html", 1)
        )
    }

    override fun RBuilder.render() {
        // typesafe HTML goes here!
        h1 {
            +"Merkliste 2.0"
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
}
