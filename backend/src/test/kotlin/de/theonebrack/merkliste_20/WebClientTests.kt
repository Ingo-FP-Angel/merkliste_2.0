package de.theonebrack.merkliste_20

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import de.theonebrack.merkliste_20.Config.MerklisteProperties
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebClientTests {
    private var wiremock: WireMockServer? = null

    @BeforeAll
    fun setupWireMock() {
        wiremock = WireMockServer(32140)
        configureFor(32140)
        wiremock?.start()
        stubFor(get(urlEqualTo("/suchergebnis-detail/medium/T018915385.html")).willReturn(aResponse()
                .withBodyFile("Anhalter.html")
        ))
        stubFor(get(urlEqualTo("/suchergebnis-detail/medium/T019497569.html")).willReturn(aResponse()
                .withBodyFile("Potter.html")
        ))
        stubFor(get(urlEqualTo("/suchergebnis-detail/medium/T008488736.html")).willReturn(aResponse()
                .withBodyFile("HdR.html")
        ))
    }

    @AfterAll
    fun tearDown() {
        wiremock?.stop()
    }

    @Test
    fun whenGettingDetails_FindAvailable() {
        val props = MerklisteProperties("http://localhost:32140")

        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html")

        Assertions.assertEquals(1, availability)
    }

    @Test
    fun whenGettingDetails_FindUnavailable() {
        val props = MerklisteProperties("http://localhost:32140")

        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T019497569.html")

        Assertions.assertEquals(0, availability)
    }

    @Test
    fun whenGettingDetails_NotFound() {
        val props = MerklisteProperties("http://localhost:32140")

        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T008488736.html")

        Assertions.assertEquals(-1, availability)
    }
}