package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class BuecherhallenServiceTests {
    val webClientMock = mock(WebClient::class.java)

    @Test
    fun whenFetchAll_thenLoginGetListAndDetails() {
        val cut = BuecherhallenService(webClientMock)
        Mockito.`when`(webClientMock.getAllMedias()).thenReturn(listOf(Media("Test", "Buch", "Foo", "details.html", -1)))
        Mockito.`when`(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", null)

        verify(webClientMock).login("foo", "bar")
        assertEquals(1, result.size)
        assertEquals(2, result[0].availability)
    }

    @Test
    fun whenDifferentLocationIsGiven_thenPassLocationToWebClient() {
        val cut = BuecherhallenService(webClientMock)
        Mockito.`when`(webClientMock.getAllMedias()).thenReturn(listOf(Media("Test", "Buch", "Foo", "details.html", -1)))
        Mockito.`when`(webClientMock.getMediaDetails("details.html", "Niendorf")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", "Niendorf")

        verify(webClientMock).login("foo", "bar")
        assertEquals(1, result.size)
        assertEquals(2, result[0].availability)
    }

    @Test
    fun whenDetailsCallThrows_thenSetSpecialAvailabilityAndContinue() {
        val cut = BuecherhallenService(webClientMock)
        Mockito.`when`(webClientMock.getAllMedias()).thenReturn(listOf(
                Media("Test", "Buch", "Foo", "fail.html", -1),
                Media("Test", "Buch", "Foo", "details.html", -1)
        ))
        val exceptionOnDetailsPage = Error("DetailsFailed")
        Mockito.`when`(webClientMock.getMediaDetails("fail.html")).thenThrow(exceptionOnDetailsPage)
        Mockito.`when`(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", null)
        assertEquals(2, result.size)
        assertEquals(-3, result[0].availability)
        assertEquals(2, result[1].availability)

    }
}