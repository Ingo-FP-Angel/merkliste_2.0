package de.theonebrack.merkliste_20

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import de.theonebrack.merkliste_20.Config.MerklisteProperties
import org.junit.jupiter.api.*
import org.springframework.web.server.ResponseStatusException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebClientTests {
    private var wiremock: WireMockServer? = null
    private val props: MerklisteProperties = MerklisteProperties("http://localhost:32140")

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
        stubFor(get(urlEqualTo("/suchergebnis-detail/medium/AvailabilityInfoUnavailable.html")).willReturn(aResponse()
                .withBodyFile("AvailabilityInfoUnavailable.html")
        ))
    }

    @AfterAll
    fun tearDown() {
        wiremock?.stop()
    }

    @Test
    fun whenGettingDetails_FindAvailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html")

        Assertions.assertEquals(1, availability)
    }

    @Test
    fun whenGettingDetails_FindUnavailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T019497569.html")

        Assertions.assertEquals(0, availability)
    }

    @Test
    fun whenGettingDetails_NotFound() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T008488736.html")

        Assertions.assertEquals(-1, availability)
    }

    @Test
    fun whenGettingDetailsWithoutLocation_UseZentralbibliothek() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html")

        Assertions.assertEquals(1, availability)
    }

    @Test
    fun whenGettingDetailsForNiendorf_FindUnavailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html", "Niendorf")

        Assertions.assertEquals(0, availability)
    }

    @Test
    fun whenGettingDetailsAndAvailabilityInfoUnavailable_Throw() {
        val cut = WebClient(props)

        val ex = Assertions.assertThrows(ResponseStatusException::class.java) {
            cut.getMediaDetails("suchergebnis-detail/medium/AvailabilityInfoUnavailable.html")
        }
        Assertions.assertTrue(ex.message.contains("Es sind zur Zeit keine Daten zur Verfügbarkeit abrufbar. Bitte wenden Sie sich an das Bibliothekspersonal."))
    }
}