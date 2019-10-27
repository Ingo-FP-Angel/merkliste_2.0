import kotlinx.coroutines.await
import kotlin.browser.window
import Models.Media

suspend fun fetchAvailableMedias(user: String, pass: String): Array<Media> {
    val responsePromise = window.fetch("http://localhost:8080/?username=$user&password=$pass")
    val response = responsePromise.await()
    val jsonPromise = response.json()
    val json = jsonPromise.await()
    return json.unsafeCast<Array<Media>>()
}
