package de.theonebrack.merkliste_20

import de.theonebrack.merkliste_20.Config.MerklisteProperties
import de.theonebrack.merkliste_20.Models.LoginFormData
import de.theonebrack.merkliste_20.Models.Media
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.BrowserUserAgent
import io.ktor.client.features.RedirectResponseException
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
@Scope(value = WebApplicationContext.SCOPE_REQUEST,
        proxyMode = ScopedProxyMode.TARGET_CLASS)
class WebClient(merklisteProperties: MerklisteProperties) {
    private val skipTypes = listOf("eAudio", "eBook", "eInfo", "eMusik", "eVideo")
    private val logger = LoggerFactory.getLogger(javaClass)
    private val baseUrl: String = merklisteProperties.baseUrl
    private val client: HttpClient = HttpClient(Apache) {
        engine {
            followRedirects = false
            socketTimeout = 30_000
            connectTimeout = 10_000
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        BrowserUserAgent()
    }

    fun login(username: String, password: String) {
        runBlocking {
            logger.info("Get login page")
            try {
                val loginPage = client.get<String>("$baseUrl/login.html")

                Jsoup.parse(loginPage).run {
                    val loginForm: Element = selectFirst("#tl_login")

                    val loginData = LoginFormData(
                            loginForm.getElementsByAttributeValue("name", "FORM_SUBMIT").first().attr("value"),
                            loginForm.getElementsByAttributeValue("name", "REQUEST_TOKEN").first().attr("value"),
                            username,
                            password
                    )

                    logger.info("Post login form")
                    try {
                        val loginResult = client.post<String> {
                            url("$baseUrl/login.html")
                            header("Referer", "$baseUrl/login.html")
                            body = TextContent(loginData.toString(), contentType = ContentType.Application.FormUrlEncoded)
                        }
                        logger.debug("Login result")
                        logger.debug(loginResult)
                    } catch (ex: RedirectResponseException) {
                        val response = ex.response
                        if (response.headers["Location"] == "$baseUrl/login.html") {
                            val loginPageWithReason = client.get<String>("$baseUrl/login.html")
                            Jsoup.parse(loginPageWithReason).run {
                                val form: Element = selectFirst("#tl_login")
                                val reason = form.getElementsByClass("error").first().text()
                                logger.error("Login failed: $reason")
                                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Login bei buecherhalle.de fehlgeschlagen: $reason")
                            }
                        }
                    }
                }
            } catch (ex: ConnectException) {
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Verbindung zu buecherhalle.de fehlgeschlagen: ${ex.message}")
            }
        }
    }

    fun getAllMedias(): List<Media> {
        lateinit var result: List<Media>
        runBlocking {
            logger.info("Fetching merkliste.html")
            val merkliste = client.get<String>("$baseUrl/merkliste.html")

            Jsoup.parse(merkliste).run {
                val list = selectFirst(".search-results-list")

                result = list.select("div.search-results-text").map {
                    Media(
                            name = it.select("h2>a").text(),
                            author = it.select(".search-results-details-personen>a").text(),
                            type = it.select(".search-results-media-type-text").text(),
                            signature = it.select(".search-results-details-signatur").text(),
                            url = it.select("h2>a").attr("href"),
                            availability = -1
                    )
                }.filter { !skipTypes.contains(it.type) }
            }
        }
        logger.info("Found ${result.size} entries")
        return result
    }

    fun getMediaDetails(url: String, location: String = "Zentralbibliothek"): Int {
        var result = -1
        runBlocking {
            logger.info("Fetching $url")

            val detailsPage = client.get<String>("$baseUrl/$url")

            Jsoup.parse(detailsPage).run {
                val availabilityErrorMessage =  select(".availability-message")
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
                client.get<String>("$baseUrl/logout.html")
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
        if ((availabilityErrorMessage?.size ?: 0) == 0 ) {
            return false
        }

        val reason = availabilityErrorMessage!![0].text()
        logger.warn("Could not get availability information: $reason")
        return true
    }

    private fun Document.getAvailabilityForLocation(availabilityPerLocationList: Elements, location: String): Int {
        var result = -1
        for (entry in availabilityPerLocationList) {
            val locationOfAvailability = Selector.selectFirst(".medium-availability-item-title-location", entry).text()
            if (locationOfAvailability == location) {
                val availabilityString = Selector.selectFirst(".medium-availability-item-title-count", entry).text()
                result = availabilityString.split("/")[0].toInt()
                break
            }
        }
        return result
    }

    fun close() {
        client.close()
    }
}