package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.springframework.stereotype.Component

@Component
class BuecherhallenService(val webClient: WebClient) {
    fun fetchAll(username: String, password: String, location: String?): List<Media> {
        webClient.login(username, password)

        val result: List<Media> = webClient.getAllMedias()

        for (entry in result) {
            val numberAvailable = if (location.isNullOrEmpty()) webClient.getMediaDetails(entry.url) else webClient.getMediaDetails(entry.url, location)
            entry.availability = numberAvailable
        }

        webClient.close()

        return result
    }
}