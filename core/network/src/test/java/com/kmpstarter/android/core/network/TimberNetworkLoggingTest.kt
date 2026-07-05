package com.kmpstarter.android.core.network

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import timber.log.Timber

class TimberNetworkLoggingTest {
    private val tree = RecordingTree()

    @AfterEach
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `timber records network log messages`() {
        Timber.plant(tree)

        Timber.tag("OkHttp").d("GET https://api.example.com/user")

        assertEquals(
            listOf(LogEntry(tag = "OkHttp", message = "GET https://api.example.com/user")),
            tree.entries,
        )
    }

    private class RecordingTree : Timber.Tree() {
        val entries = mutableListOf<LogEntry>()

        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?,
        ) {
            entries += LogEntry(tag = tag, message = message)
        }
    }

    private data class LogEntry(
        val tag: String?,
        val message: String,
    )
}
