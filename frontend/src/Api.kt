import kotlinx.coroutines.await
import kotlin.browser.window
import Models.Media

external val process: dynamic

suspend fun fetchAvailableMedias(user: String, pass: String): Array<Media> {
    val backendBaseUrl = if (process.env.NODE_ENV == "production") "/api" else "http://localhost:8080/api"
    val responsePromise = window.fetch("$backendBaseUrl/media?username=$user&password=$pass")
    val response = responsePromise.await()
    val jsonPromise = response.json()
    val json = jsonPromise.await()
    return json.unsafeCast<Array<Media>>()
}