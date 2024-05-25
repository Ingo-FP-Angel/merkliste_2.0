package de.theonebrack.merkliste_20

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import de.theonebrack.merkliste_20.Config.MerklisteProperties
import de.theonebrack.merkliste_20.Models.Media
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
        stubFor(get(urlEqualTo("/merkliste.html")).willReturn(aResponse()
                .withBodyFile("merkliste.html")
        ))
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

        assertThatThrownBy { cut.login("user", "pass") }
            .isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("Anmeldung nicht möglich! Der Authentifizierungsdienst steht zur Zeit nicht zur Verfügung.")
    }

    @Test
    fun whenGettingMerkliste_ParseCorrectInfo() {
        val expectedEntries: List<Media> = listOf(
                Media("Per Anhalter durch die Galaxis / 4. Band Macht`s gut und danke für den Fisch", "Adams, Douglas", "Buch", "Signatur: ADAM Doug Science-Fiction", "suchergebnis-detail/medium/T018915385.html", -1),
                Media("Harry Potter", "", "Buch", "Signatur: Qak 5 ROWL HARR", "suchergebnis-detail/medium/T019497569.html", -1),
                Media("Der Herr der Ringe / Bd. 1. Die Gefährten", "Tolkien, J. R. R.", "Buch", "Signatur: TOLK John Fantasy", "suchergebnis-detail/medium/T008488736.html", -1)
        )

        val cut = WebClient(props)
        val allEntries: List<Media> = cut.getAllMedias()

        assertThat(allEntries)
            .hasSameElementsAs(expectedEntries)
    }

    @Test
    fun whenGettingDetails_FindAvailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html")

        assertThat(availability)
            .isEqualTo(1)
    }

    @Test
    fun whenGettingDetails_FindUnavailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T019497569.html")

        assertThat(availability)
            .isEqualTo(0)
    }

    @Test
    fun whenGettingDetails_NotFound() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T008488736.html")

        assertThat(availability)
            .isEqualTo(-1)
    }


    @Test
    fun whenGettingDetails_HandleAvailabilityInfoNotAvailable() {
        val cut = WebClient(props)

        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/AvailabilityInfoUnavailable.html")

        assertThat(availability)
            .isEqualTo(-2)
    }


    @Test
    fun whenGettingDetailsWithoutLocation_UseZentralbibliothek() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html")

        assertThat(availability)
            .isEqualTo(1)
    }

    @Test
    fun whenGettingDetailsForNiendorf_FindUnavailable() {
        val cut = WebClient(props)
        val availability: Int = cut.getMediaDetails("suchergebnis-detail/medium/T018915385.html", "Niendorf")

        assertThat(availability)
            .isEqualTo(0)
    }
}