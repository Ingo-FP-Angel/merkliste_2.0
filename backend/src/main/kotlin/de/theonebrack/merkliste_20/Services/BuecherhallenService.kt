package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BuecherhallenService(val webClient: WebClient) {
    val logger = LoggerFactory.getLogger(javaClass)
    fun fetchAll(username: String, password: String, location: String?): List<Media> {
        webClient.login(username, password)

        val result: List<Media> = webClient.getAllMedias()

        for (entry in result) {
            try {
                val numberAvailable = if (location.isNullOrEmpty()) webClient.getMediaDetails(entry.url) else webClient.getMediaDetails(entry.url, location)
                entry.availability = numberAvailable
            } catch (ex: Throwable) {
                logger.error("Error while getting availability of ${entry.name} (${entry.url}): ${ex.message}")
                entry.availability = -3
            }
        }

        webClient.close()

        return result
    }
}