package de.theonebrack.merkliste_20.Controller

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.Services.BuecherhallenService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux

@RestController
@CrossOrigin(origins = arrayOf("*"))
class FluxController(
        private val buecherhallenService: BuecherhallenService
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/api/flux/media", produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    fun getFlux(@RequestHeader("username") username: String,
                @RequestHeader("password") password: String,
                @RequestParam("location", required = false) location: String?,
                @RequestParam("mediatype", required = false) mediatype: String?)
            : Flux<Media> {
        logger.info("Incoming request for all available media")
        logger.debug("Requested location: $location")
        logger.debug("Requested mediatype: $mediatype")
        val processor = DirectProcessor.create<Media>().serialize()
        val sink = processor.sink()
        buecherhallenService.fetchAllFlux(username, password, location, mediatype, sink)
        logger.info("Request finished")
        return processor.map { it }
    }
}