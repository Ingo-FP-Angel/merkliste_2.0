import kotlin.browser.window
import kotlin.js.json
import kotlinx.coroutines.await
import org.w3c.fetch.*
import Models.Media

external val process: dynamic

suspend fun fetchAvailableMedias(user: String, pass: String, location: String): Array<Media> {
    val backendBaseUrl = if (process.env.NODE_ENV == "production") "/api" else "http://localhost:8080/api"
    val responsePromise = window.fetch("$backendBaseUrl/media?location=$location",
            object: RequestInit {
                override var method: String? = "GET"
                override var headers: dynamic = json("username" to user, "password" to pass)
            }
    )
    val response = responsePromise.await()
    val json = response.json().await()

    if (!response.ok) {
        val errorResponse = json.unsafeCast<ErrorResponse>()
        throw Error("Abrufen fehlgeschlagen mit Status Code '${errorResponse.status} ${errorResponse.error}' und Hinweis '${errorResponse.message}'")
    }

    return json.unsafeCast<Array<Media>>()
}

data class ErrorResponse(val timestamp: String, val status: Int, val error: String, val message: String, val path: String)
