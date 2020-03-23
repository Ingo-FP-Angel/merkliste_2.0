import kotlin.js.Promise
@JsModule("can-ndjson-stream")

external fun ndjsonStream(stream: Any): ReadableStream

external interface Result {
    var done: Boolean
    var value: Any
}

external interface ReadableStreamDefaultReader {
    var read: () -> Promise<Result>
}

external interface ReadableStream {
    var getReader: () -> ReadableStreamDefaultReader
}
