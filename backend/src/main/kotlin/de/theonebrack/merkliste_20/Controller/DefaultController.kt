package de.theonebrack.merkliste_20.Controller

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.Services.BuecherhallenService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = arrayOf("*"))
class DefaultController(
        private val buecherhallenService: BuecherhallenService
) {
    val logger = LoggerFactory.getLogger(DefaultController::class.java)

    @GetMapping("/api/media")
    fun get(@RequestHeader("username") username: String,
            @RequestHeader("password") password: String,
            @RequestParam("location", required = false) location: String?)
            : List<Media> {
        logger.info("Incoming request for all available media")
        val media = buecherhallenService.fetchAll(username, password, location)
        logger.info("Request finished")
        return media
    }
}