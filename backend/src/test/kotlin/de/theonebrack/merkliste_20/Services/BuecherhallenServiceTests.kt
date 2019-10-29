package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertEquals

class BuecherhallenServiceTests {
    val webClientMock = mock(WebClient::class.java)

    @Test
    fun whenFetchAll_thenLoginGetListAndDetails() {
        val cut = BuecherhallenService(webClientMock)
        Mockito.`when`(webClientMock.getAllMedias()).thenReturn(listOf(Media("Test", "Buch", "Foo", "details.html", -1)))
        Mockito.`when`(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar")

        verify(webClientMock).login("foo", "bar")
        assertEquals(1, result.size)
        assertEquals(2, result[0].availability)
    }
}