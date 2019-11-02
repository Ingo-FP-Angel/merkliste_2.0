import kotlin.browser.window
import kotlin.js.json
import kotlinx.coroutines.await
import org.w3c.fetch.*
import Models.Media

external val process: dynamic

suspend fun fetchAvailableMedias(user: String, pass: String): Array<Media> {
    val backendBaseUrl = if (process.env.NODE_ENV == "production") "/api" else "http://localhost:8080/api"
    val responsePromise = window.fetch("$backendBaseUrl/media",
            object: RequestInit {
                override var method: String? = "GET"
                override var headers: dynamic = json("username" to user, "password" to pass)
            }
    )
    val response = responsePromise.await()

    if (!response.ok) {
        throw Error("Abrufen fehlgeschlagen mit Status Code ${response.status} und Hinweis ${response.text().await()}")
    }

    val jsonPromise = response.json()
    val json = jsonPromise.await()
    return json.unsafeCast<Array<Media>>()
}