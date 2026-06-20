package com.wreck2053.essentialkey

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

data class HttpResult(val statusCode: Int, val message: String)

fun interface HttpTransport {
    fun execute(config: ActionConfig): HttpResult
}

class HttpRequestExecutor(private val transport: HttpTransport = UrlConnectionTransport()) {
    fun execute(config: ActionConfig): HttpResult {
        val normalized = config.copy(method = config.method.uppercase(), url = config.url.trim())
        validate(normalized)?.let { return HttpResult(-1, it) }
        return try {
            transport.execute(normalized)
        } catch (error: Exception) {
            HttpResult(-1, error.message ?: error.javaClass.simpleName)
        }
    }

    internal fun validate(config: ActionConfig): String? {
        if (config.method !in setOf("GET", "POST")) return "Unsupported method"
        if (config.url.isBlank()) return "URL is empty"
        return try {
            val uri = URI(config.url)
            if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
                "URL must use http:// or https:// and include a host"
            } else {
                null
            }
        } catch (_: Exception) {
            "Invalid URL"
        }
    }
}

class UrlConnectionTransport : HttpTransport {
    override fun execute(config: ActionConfig): HttpResult {
        val connection = URL(config.url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = config.method
            connection.connectTimeout = 5_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            if (config.method == "POST") {
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(0)
                connection.outputStream.use { }
            }
            val status = connection.responseCode
            HttpResult(status, connection.responseMessage.orEmpty())
        } finally {
            connection.disconnect()
        }
    }
}

