package de.theonebrack.merkliste_20.Services

import de.theonebrack.merkliste_20.Models.Media
import de.theonebrack.merkliste_20.WebClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BuecherhallenServiceTests {
    val webClientMock = mock<WebClient>()

    @Test
    fun whenFetchAll_thenLoginGetListAndDetails() {
        val cut = BuecherhallenService(webClientMock)
        whenever(webClientMock.getAllMedias()).thenReturn(listOf(Media("Test", "Autor", "Buch", "Foo", "details.html", -1)))
        whenever(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", null, null)

        verify(webClientMock).login("foo", "bar")
        assertEquals(1, result.size)
        assertEquals(2, result[0].availability)
    }

    @Test
    fun whenDifferentLocationIsGiven_thenPassLocationToWebClient() {
        val cut = BuecherhallenService(webClientMock)
        whenever(webClientMock.getAllMedias()).thenReturn(listOf(Media("Test", "Autor", "Buch", "Foo", "details.html", -1)))
        whenever(webClientMock.getMediaDetails("details.html", "Niendorf")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", "Niendorf", null)

        verify(webClientMock).login("foo", "bar")
        assertEquals(1, result.size)
        assertEquals(2, result[0].availability)
    }

    @Test
    fun whenDetailsCallThrows_thenSetSpecialAvailabilityAndContinue() {
        val cut = BuecherhallenService(webClientMock)
        whenever(webClientMock.getAllMedias()).thenReturn(listOf(
                Media("Test", "Autor", "Buch", "Foo", "fail.html", -1),
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1)
        ))
        val exceptionOnDetailsPage = Error("DetailsFailed")
        whenever(webClientMock.getMediaDetails("fail.html")).thenThrow(exceptionOnDetailsPage)
        whenever(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", null, null)
        assertEquals(2, result.size)
        assertEquals(-3, result[0].availability)
        assertEquals(2, result[1].availability)
    }

    @Test
    fun whenMediaTypeFilterIsGiven_ApplyFilterOnMediaListRetrievedFromBuecherhallen() {
        val cut = BuecherhallenService(webClientMock)
        whenever(webClientMock.getAllMedias()).thenReturn(listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1)
        ))
        whenever(webClientMock.getMediaDetails("details.html")).thenReturn(2)

        val result = cut.fetchAll("foo", "bar", null, "books")

        assertEquals(1, result.size)
    }

    @Test
    fun whenMediaTypeFilterBooks_thenReturnBuch() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, "books")

        assertEquals(1, result.size)
        result.forEach { assertEquals("Buch", it.type) }
    }

    @Test
    fun whenMediaTypeFilterMusic_thenReturnCD() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, "music")

        assertEquals(1, result.size)
        result.forEach { assertEquals("CD", it.type) }
    }

    @Test
    fun whenMediaTypeFilterMovies_thenReturnDVDAndBluRay() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, "movies")

        assertEquals(2, result.size)
        result.forEach { assert(listOf("DVD", "Blu-ray Disc").contains(it.type)) }
    }

    @Test
    fun whenMediaTypeFilterIsNull_thenReturnInputList() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, null)

        assertSame(inputList, result)
    }

    @Test
    fun whenMediaTypeFilterIsEmpty_thenReturnInputList() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, "")

        assertSame(inputList, result)
    }

    @Test
    fun whenMediaTypeFilterNotSupported_thenReturnInputList() {
        val inputList = listOf(
                Media("Test", "Autor", "Buch", "Foo", "details.html", -1),
                Media("Test", "Autor", "DVD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Blu-ray Disc", "Foo", "details.html", -1),
                Media("Test", "Autor", "CD", "Foo", "details.html", -1),
                Media("Test", "Autor", "Noten", "Foo", "details.html", -1)
        )
        val cut = BuecherhallenService(webClientMock)

        val result = cut.filteredMediaListByType(inputList, "foo")

        assertSame(inputList, result)
    }
}