package de.theonebrack.merkliste_20.Controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.notMatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.server.ResponseStatusException

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultControllerTestsIT {
    private val validSessionId = "555537c3431cb512e70afd295d7fda1a"
    private val invalidSessionId = "4af537c3431cb512e70afd295d7fda1a"
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    private var wiremock: WireMockServer? = null

    @BeforeAll
    fun setupWireMock() {
        wiremock = WireMockServer(32140)
        WireMock.configureFor(32140)
        wiremock?.start()
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/login.html"))
                .willReturn(WireMock.aResponse()
                        .withBodyFile("login.html")
                ))
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/login.html"))
                .withRequestBody(matching(".*username=foo&password=bar"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SEE_OTHER.value())
                        .withHeader("Location", "http://localhost:32140/merkliste.html")
                        .withHeader("Set-Cookie", "PHPSESSID=$validSessionId; path=/")
                ))
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/login.html"))
                .withRequestBody(notMatching(".*username=foo&password=bar"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.SEE_OTHER.value())
                        .withHeader("Location", "http://localhost:32140/login.html")
                        .withHeader("Set-Cookie", "PHPSESSID=$invalidSessionId; path=/")
                ))
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/login.html"))
                .withCookie("PHPSESSID", matching(invalidSessionId))
                .willReturn(WireMock.aResponse()
                        .withBodyFile("LoginFailedMaintenance.html")
                ))
        // force error; calling login form with valid session is not supposed to happen in real life
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/login.html"))
                .withCookie("PHPSESSID", matching(validSessionId))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                ))

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/logout.html"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Set-Cookie", "PHPSESSID=deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT")
                ))

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/merkliste.html"))
                .withCookie("PHPSESSID", matching(validSessionId))
                .willReturn(WireMock.aResponse()
                .withBodyFile("merkliste.html")
        ))
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/suchergebnis-detail/medium/T018915385.html"))
                .withCookie("PHPSESSID", matching(validSessionId))
                .willReturn(WireMock.aResponse()
                .withBodyFile("Anhalter.html")
        ))
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/suchergebnis-detail/medium/T019497569.html"))
                .withCookie("PHPSESSID", matching(validSessionId))
                .willReturn(WireMock.aResponse()
                .withBodyFile("Potter.html")
        ))
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/suchergebnis-detail/medium/T008488736.html"))
                .withCookie("PHPSESSID", matching(validSessionId))
                .willReturn(WireMock.aResponse()
                .withBodyFile("HdR.html")
        ))
    }

    @Test
    fun whenGettingAllMedia_returnAllMedia() {
        mockMvc.get("/api/media") {
            accept = MediaType.APPLICATION_JSON
            headers {
                header("username", "foo")
                header("password", "bar")
            }
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("[{\"name\":\"Per Anhalter durch die Galaxis / 4. Band Macht`s gut und danke für den Fisch\",\"author\":\"Adams, Douglas\",\"type\":\"Buch\",\"signature\":\"Signatur: 1 @ADAM Doug Science-Fiction\",\"url\":\"suchergebnis-detail/medium/T018915385.html\",\"availability\":1},{\"name\":\"Harry Potter\",\"author\":\"\",\"type\":\"Buch\",\"signature\":\"Signatur: Qak 5 ROWL HARR\",\"url\":\"suchergebnis-detail/medium/T019497569.html\",\"availability\":0},{\"name\":\"Der Herr der Ringe / Bd. 1. Die Gefährten\",\"author\":\"Tolkien, J. R. R.\",\"type\":\"Buch\",\"signature\":\"Signatur: 1 @TOLK John Fantasy\",\"url\":\"suchergebnis-detail/medium/T008488736.html\",\"availability\":-1}]") }
        }
    }

    @Test
    fun whenLoginFails_returnCorrespondingErrorMessage() {
        val result = mockMvc.get("/api/media") {
            accept = MediaType.APPLICATION_JSON
            headers {
                header("username", "foo")
                header("password", "baz")
            }
        }.andExpect {
            status { is5xxServerError() }
        }.andReturn()

        val exception = result.resolvedException!! as ResponseStatusException
        assertEquals(HttpStatus.BAD_GATEWAY, exception.status)
        assertEquals("Login bei buecherhalle.de fehlgeschlagen: Anmeldung nicht möglich! Der Authentifizierungsdienst steht zur Zeit nicht zur Verfügung.", exception.reason)
    }

    @Test
    fun whenRequestRunInParallel_eachRequestUsesOwnWebClient() {
        runBlocking {
            val jobs = List(2) {
                launch {
                    // increase chance that second session still active after first one ends
                    if (it > 0) delay(100L)

                    mockMvc.get("/api/media") {
                        accept = MediaType.APPLICATION_JSON
                        headers {
                            header("username", "foo")
                            header("password", "bar")
                        }
                    }.andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                        content { json("[{\"name\":\"Per Anhalter durch die Galaxis / 4. Band Macht`s gut und danke für den Fisch\",\"author\":\"Adams, Douglas\",\"type\":\"Buch\",\"signature\":\"Signatur: 1 @ADAM Doug Science-Fiction\",\"url\":\"suchergebnis-detail/medium/T018915385.html\",\"availability\":1},{\"name\":\"Harry Potter\",\"author\":\"\",\"type\":\"Buch\",\"signature\":\"Signatur: Qak 5 ROWL HARR\",\"url\":\"suchergebnis-detail/medium/T019497569.html\",\"availability\":0},{\"name\":\"Der Herr der Ringe / Bd. 1. Die Gefährten\",\"author\":\"Tolkien, J. R. R.\",\"type\":\"Buch\",\"signature\":\"Signatur: 1 @TOLK John Fantasy\",\"url\":\"suchergebnis-detail/medium/T008488736.html\",\"availability\":-1}]") }
                    }
                }
            }
            jobs.forEach { it.join() }
        }
    }
}