package de.theonebrack.merkliste_20.Controller

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
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = arrayOf("*"))
class DefaultController {
    val logger = LoggerFactory.getLogger(DefaultController::class.java)

    @GetMapping("/")
    fun get(@RequestParam username: String, @RequestParam password: String): List<Media> {
        val client = HttpClient(Apache) {
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

        var result: List<Media> = listOf()

        runBlocking {
            logger.info("Get login page")
            val loginPage = client.get<String>("https://www.buecherhallen.de/login.html")

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
                    url("https://www.buecherhallen.de/login.html")
                    header("Referer", "https://www.buecherhallen.de/login.html")
                    body = TextContent(loginData.toString(), contentType = ContentType.Application.FormUrlEncoded)
                }

                logger.info("Fetching merkliste.html")
                val merkliste = client.get<String>("https://www.buecherhallen.de/merkliste.html")

                Jsoup.parse(merkliste).run {
                    val list = selectFirst(".search-results-list")

                    result = list.select("div.search-results-text").map {
                        Media(
                                name = it.select("h2>a").text(),
                                type = it.select(".search-results-media-type-text").text(),
                                signature = it.select(".search-results-details-signatur").text(),
                                url = it.select("h2>a").attr("href"),
                                availability = -1
                        )
                    }
                }
            }
        }

        client.close()
        return result
    }
}