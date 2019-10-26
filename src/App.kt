import Models.Media
import react.dom.*
import react.*

interface AppState: RState {
    var availableItems: List<Media>
}

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        availableItems = listOf(
                Media("Heavy Trip", "DVD", "Spielfilm Komödie HEAV", 1),
                Media("Pathfinder Rollenspiel Ausrüstungskompendium", "Buch", "GAMES Rollenspiele PATH", 1),
                Media("Die Weltverbesserer", "Buch", "Gal 1 WELT", 1)
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
            ul {
                for (item in state.availableItems) {
                    li {
                        key = item.name
                        +"${item.name}, ${item.type}, ${item.signature}, ${item.availability}"
                    }
                }
            }
        }
    }
}
