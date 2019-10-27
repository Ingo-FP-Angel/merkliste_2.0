package de.theonebrack.merkliste_20.Controller

import de.theonebrack.merkliste_20.Models.LoginFormData
import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.Services.BuecherhallenService
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
class DefaultController(
        private val buecherhallenService: BuecherhallenService
) {
    val logger = LoggerFactory.getLogger(DefaultController::class.java)

    @GetMapping("/api/media")
    fun get(@RequestParam username: String, @RequestParam password: String): List<Media> {
        logger.info("User $username requested all available media")
        return buecherhallenService.fetchAll(username, password)
    }
}