package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink

@Component
class BuecherhallenService(val webClient: WebClient) {
    val logger = LoggerFactory.getLogger(javaClass)

    fun fetchAll(username: String, password: String, location: String?, mediatype: String?): List<Media> {
        webClient.login(username, password)

        try {
            val result: List<Media> = filteredMediaListByType(webClient.getAllMedias(), mediatype)

            for (entry in result) {
                try {
                    val numberAvailable = if (location.isNullOrEmpty()) webClient.getMediaDetails(entry.url) else webClient.getMediaDetails(entry.url, location)
                    entry.availability = numberAvailable
                } catch (ex: Throwable) {
                    logger.error("Error while getting availability of ${entry.name} (${entry.url}): ${ex.message}")
                    entry.availability = -3
                }
            }

            return result
        } finally {
            webClient.logout()
        }
    }

    fun fetchAllFlux(username: String, password: String, location: String?, mediatype: String?, sink: FluxSink<Media>) {
        val t = Thread {
            webClient.login(username, password)

            try {
                val result: List<Media> = filteredMediaListByType(webClient.getAllMedias(), mediatype)

                for (entry in result) {
                    try {
                        val numberAvailable = if (location.isNullOrEmpty()) webClient.getMediaDetails(entry.url) else webClient.getMediaDetails(entry.url, location)
                        entry.availability = numberAvailable
                    } catch (ex: Throwable) {
                        logger.error("Error while getting availability of ${entry.name} (${entry.url}): ${ex.message}")
                        entry.availability = -3
                    }
                    sink.next(entry)
                }
            } finally {
                webClient.logout()
                sink.complete()
            }
        }
        t.start()
    }

    fun filteredMediaListByType(inputList: List<Media>, mediaTypeFilter: String?): List<Media> {
        if (mediaTypeFilter.isNullOrEmpty()) {
            return inputList
        }

        return when (mediaTypeFilter) {
            "books"  -> inputList.filter { it.type.equals("Buch") }
            "music"  -> inputList.filter { it.type.equals("CD") }
            "movies" -> inputList.filter { it.type.equals("DVD") || it.type.equals("Blu-ray Disc") }
            else -> inputList
        }
    }
}