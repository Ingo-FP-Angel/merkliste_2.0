package de.theonebrack.merkliste_20

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import de.theonebrack.merkliste_20.Config.MerklisteProperties
import org.junit.jupiter.api.*
import org.springframework.http.HttpStatus
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
    fun whenLoginAndRedirectToItself_ThrowWithLoginErrorMessage() {
        stubFor(get(urlEqualTo("/login.html"))
                .withCookie("PHPSESSID", notMatching("4af537c3431cb512e70afd295d7fda1a"))
                .willReturn(aResponse()
                .withBodyFile("login.html")
        ))
        stubFor(post(urlEqualTo("/login.html"))
                .willReturn(aResponse()
                .withStatus(HttpStatus.SEE_OTHER.value())
                .withHeader("Location", "http://localhost:32140/login.html")
                .withHeader("Set-Cookie", "PHPSESSID=4af537c3431cb512e70afd295d7fda1a; path=/")
        ))
        stubFor(get(urlEqualTo("/login.html"))
                .withCookie("PHPSESSID", matching("4af537c3431cb512e70afd295d7fda1a"))
                .willReturn(aResponse()
                .withBodyFile("LoginFailedMaintenance.html")
        ))

        val cut = WebClient(props)

        val ex = Assertions.assertThrows(ResponseStatusException::class.java) {
            cut.login("user", "pass")
        }
        Assertions.assertTrue(ex.message.contains("Anmeldung nicht möglich! Der Authentifizierungsdienst steht zur Zeit nicht zur Verfügung."))
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