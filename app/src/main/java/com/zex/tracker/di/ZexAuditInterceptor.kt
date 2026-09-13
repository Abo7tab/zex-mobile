package com.zex.tracker.di

import com.zex.tracker.core.logging.LiveTerminalLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZexAuditInterceptor @Inject constructor(
    private val terminalLogger: LiveTerminalLogger
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        
        var requestLog = "SYS//REQ > [${request.method}] ${request.url.encodedPath}"
        terminalLogger.log(requestLog)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            terminalLogger.log("SYS//ERR > [${request.method}] ${request.url.encodedPath} -> FAILED: ${e.message}")
            throw e
        }

        val t2 = System.nanoTime()
        val duration = (t2 - t1) / 1e6
        
        val status = response.code
        val statusString = if (status in 200..299) "OK" else "ERR"
        
        terminalLogger.log("SYS//RES > [$status $statusString] (${String.format("%.1f", duration)}ms) ${request.url.encodedPath}")
        
        return response
    }
}
