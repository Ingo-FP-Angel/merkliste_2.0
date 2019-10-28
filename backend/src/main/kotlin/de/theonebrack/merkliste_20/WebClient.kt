package de.theonebrack.merkliste_20

import de.theonebrack.merkliste_20.Config.MerklisteProperties
import de.theonebrack.merkliste_20.Models.LoginFormData
import de.theonebrack.merkliste_20.Models.Media
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.BrowserUserAgent
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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebClient(val merklisteProperties: MerklisteProperties) {
    val logger = LoggerFactory.getLogger(WebClient::class.java)
    val baseUrl: String = merklisteProperties.baseUrl
    val client: HttpClient = HttpClient(Apache) {
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
                client.post<Any> {
                    url("$baseUrl/login.html")
                    header("Referer", "$baseUrl/login.html")
                    body = TextContent(loginData.toString(), contentType = ContentType.Application.FormUrlEncoded)
                }
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
                            type = it.select(".search-results-media-type-text").text(),
                            signature = it.select(".search-results-details-signatur").text(),
                            url = it.select("h2>a").attr("href"),
                            availability = -1 // ToDo: get availability from details pages later
                    )
                }
            }
        }
        logger.info("Found ${result.size} entries")
        return result
    }

    fun getMediaDetails(url: String): Int {
        var result = -1
        runBlocking {
            logger.info("Fetching $url")

            val detailsPage = client.get<String>("$baseUrl/$url")

            Jsoup.parse(detailsPage).run {
                val availableEntries = select("li.record-available")

                result = getAvailabilityForLocation(availableEntries)

                if (result == -1) {
                    val unavailableEntries = select("li.record-not-available")

                    result = getAvailabilityForLocation(unavailableEntries)
                }
            }
        }
        return result
    }

    private fun Document.getAvailabilityForLocation(availabilityPerLocationList: Elements): Int {
        var result = -1
        for (entry in availabilityPerLocationList) {
            val location = selectFirst(".medium-availability-item-title-location").text()
            if (location == "Zentralbibliothek") {
                val availabilityString = selectFirst(".medium-availability-item-title-count").text()
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