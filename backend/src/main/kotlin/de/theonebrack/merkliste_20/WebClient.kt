package de.theonebrack.merkliste_20

import de.theonebrack.merkliste_20.Config.MerklisteProperties
import de.theonebrack.merkliste_20.Models.LoginFormData
import de.theonebrack.merkliste_20.Models.Media
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Selector
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import java.net.ConnectException

@Component
@Scope(
    value = WebApplicationContext.SCOPE_REQUEST,
    proxyMode = ScopedProxyMode.TARGET_CLASS
)
class WebClient(merklisteProperties: MerklisteProperties) {
    private val skipTypes = listOf("eAudio", "eBook", "eInfo", "eMusik", "eVideo")
    private val signaturRegex = Regex(".* (Signatur: .*) Medienart.*")
    private val logger = LoggerFactory.getLogger(javaClass)
    private val baseUrl: String = merklisteProperties.baseUrl
    private val client: HttpClient = HttpClient(CIO) {
        engine {
            requestTimeout = 30_000
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        BrowserUserAgent()
    }

    fun login(username: String, password: String) {
        runBlocking {
            logger.info("Get login page")
            val loginPage = client.get("$baseUrl/login.html").body<String>()

            Jsoup.parse(loginPage).run {
                val loginForm: Element = selectFirst("#tl_login") ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login-Seite von buecherhalle.de enthält nicht das erwartete Formular."
                )

                val loginData = LoginFormData(
                    loginForm.getElementsByAttributeValue("name", "FORM_SUBMIT").first()?.attr("value")
                        ?: throw ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Login-Seite von buecherhalle.de enthält nicht die erwarteten Formular-Attribute."
                        ),
                    loginForm.getElementsByAttributeValue("name", "REQUEST_TOKEN").first()?.attr("value")
                        ?: throw ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Login-Seite von buecherhalle.de enthält nicht die erwarteten Formular-Attribute."
                        ),
                    username,
                    password
                )

                logger.info("Post login form")
                try {
                    val loginResult = client.post {
                        url("$baseUrl/login.html")
                        header("Referer", "$baseUrl/login.html")
                        setBody(TextContent(loginData.toString(), contentType = ContentType.Application.FormUrlEncoded))
                    }
                    logger.debug("Login result")
                    logger.debug(loginResult.body<String>())
                } catch (ex: RedirectResponseException) {
                    val response = ex.response
                    if (response.headers["Location"] == "$baseUrl/login.html") {
                        val loginPageWithReason = client.get("$baseUrl/login.html").body<String>()
                        Jsoup.parse(loginPageWithReason).run {
                            val form: Element = selectFirst("#tl_login") ?: throw ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Login-Seite von buecherhalle.de enthält nicht das erwartete Formular."
                            )
                            val reason = form.getElementsByClass("error").first()?.text() ?: "unknown error"
                            logger.error("Login failed: $reason")
                            throw ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "Login bei buecherhalle.de fehlgeschlagen: $reason"
                            )
                        }
                    }
                } catch (ex: ConnectException) {
                    logger.error("Login not possible", ex)
                    throw ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Login-Seite von buecherhalle.de nicht erreichbar: ${ex.message}"
                    )
                }
            }
        }
    }

    fun getAllMedias(): List<Media> {
        lateinit var result: List<Media>
        runBlocking {
            logger.info("Fetching merkliste.html")
            val merkliste = client.get("$baseUrl/merkliste.html").body<String>()

            Jsoup.parse(merkliste).run {
                val list = selectFirst(".search-results-list")

                result = list?.select("div.search-results-text")
                    ?.map {
                        Media(
                            name = it.select("h2>a").text(),
                            author = it.select(".search-results-details .search-results-details-personen a").text(),
                            type = it.select(".search-results-media-type-text").text(),
                            signature = getSignaturFromAllDetails(it.select(".search-results-details").text()),
                            url = it.select("h2>a").attr("href"),
                            availability = -1
                        )
                    }
                    ?.filter { !skipTypes.contains(it.type) }
                    ?: emptyList()
            }
        }
        logger.info("Found ${result.size} entries")
        return result
    }

    fun getMediaDetails(url: String, location: String = "Zentralbibliothek"): Int {
        var result = -1
        runBlocking {
            logger.info("Fetching $url")

            val detailsPage = client.get("$baseUrl/$url").body<String>()

            Jsoup.parse(detailsPage).run {
                val availabilityErrorMessage = select(".availability-message")
                if (availabilityErrorMessagePresent(availabilityErrorMessage)) {
                    result = -2
                }

                if (result == -1) {
                    val availableEntries = select("li.record-available")
                    result = getAvailabilityForLocation(availableEntries, location)
                }

                if (result == -1) {
                    val unavailableEntries = select("li.record-not-available")
                    result = getAvailabilityForLocation(unavailableEntries, location)
                }
            }
        }
        return result
    }

    fun logout() {
        runBlocking {
            logger.info("Logout")

            try {
                client.get("$baseUrl/logout.html")
            } catch (ex: RedirectResponseException) {
                val response = ex.response
                if (response.headers["Location"] != "$baseUrl/login.html") {
                    logger.warn("Logout redirected to ${response.headers["Location"]} instead of login page.")
                    logger.debug(response.toString())
                    return@runBlocking // don't know why this is necessary...
                }
            }
        }
    }

    private fun availabilityErrorMessagePresent(availabilityErrorMessage: Elements?): Boolean {
        if ((availabilityErrorMessage?.size ?: 0) == 0) {
            return false
        }

        val reason = availabilityErrorMessage!![0].text()
        logger.warn("Could not get availability information: $reason")
        return true
    }

    private fun getAvailabilityForLocation(availabilityPerLocationList: Elements, location: String): Int {
        var result = -1
        for (entry in availabilityPerLocationList) {
            val locationOfAvailability = Selector.selectFirst(".medium-availability-item-title-location", entry)?.text()
            if (locationOfAvailability == location) {
                val availabilityString = Selector.selectFirst(".medium-availability-item-title-count", entry)?.text()
                    ?: "-2/-2" // treat null like no info available
                result = availabilityString.split("/")[0].toInt()
                break
            }
        }
        return result
    }

    private fun getSignaturFromAllDetails(details: String): String {
        val match = signaturRegex.find(details)
        return match?.destructured?.component1() ?: ""
    }
}
