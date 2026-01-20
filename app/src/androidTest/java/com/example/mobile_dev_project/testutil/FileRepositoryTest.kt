package com.example.mobile_dev_project.testutil

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile_dev_project.data.repository.FileRepository
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject


/**
 * FileRepositoryTest
 *
 * Unit tests for FileRepository:
 * - createFile()
 * - downloadFile() using MockWebServer
 * - deleteDirectoryContents()
 *
 * Uses Hilt for dependency injection and OkHttp MockWebServer for HTTP testing.
 *
 * Reference: https://tomaytotomato.com/unit-testing-okhttp/
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FileRepositoryTest {

    @get:Rule
    private lateinit var server: MockWebServer

    @Inject
    lateinit var repository: FileRepository

    @Inject
    lateinit var mockClient: OkHttpClient

    private lateinit var tempDir: File
    /**
     * Setup executed before each test:
     * - Starts the MockWebServer
     * - Initializes FileRepository with OkHttpClient
     * - Creates a temporary directory for file operations
     */
    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val okHttpClient = OkHttpClient.Builder().build()
        repository = FileRepository(okHttpClient)
        tempDir = File(
            System.getProperty("java.io.tmpdir"),
            "file_repo_test_${System.currentTimeMillis()}"
        ).apply { mkdirs() }
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    // =====================================
    // createFile() Tests
    // =====================================
    /** Verifies that createFile() correctly creates a file in the given directory */
    @Test
    fun createFile_creates_file_correctly() {
        val file = repository.createFile(tempDir, "test.txt")
        Assert.assertEquals("test.txt", file.name)
        Assert.assertEquals(tempDir, file.parentFile)
    }

    // =====================================
    // downloadFile() using Mockito mock client
    // =====================================
    /** Verifies that downloadFile() succeeds when server returns HTTP 200 */
    @Test
    fun downloadFile_success_with_mock_response() {
        // Prepare mock call + response
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("{\"message\":\"success\"}")
        server.enqueue(mockResponse) // Enqueue the response

        val baseUrl = server.url("/").toString()

        val targetFile = File(tempDir, "mockfile.txt")

        val result = repository.downloadFile(baseUrl, targetFile)

        Assert.assertTrue(result)
        Assert.assertTrue(targetFile.exists())

        assertEquals("{\"message\":\"success\"}", targetFile.readText())

        val recordedRequest = server.takeRequest()
        assertEquals("/", recordedRequest.path)

    }
    /** Verifies that downloadFile() fails when server returns an error response */
    @Test
    fun downloadFile_fails_on_error_response() {
        // Prepare mock call + response for failure (404 Not Found)
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("{\"message\":\"Not Found\"}")
        server.enqueue(mockResponse)

        val baseUrl = server.url("/").toString()
        val targetFile = File(tempDir, "fail.txt")
        val result = repository.downloadFile(baseUrl, targetFile)

        // Assert that the download failed
        Assert.assertFalse(result)
        Assert.assertFalse(targetFile.exists())

        // Ensure the mock server received the expected request
        val recordedRequest = server.takeRequest()
        assertEquals("/", recordedRequest.path)
    }
    // =====================================
    // deleteDirectoryContents() Tests
    // =====================================
    /** Verifies that all files in a directory are deleted */
    @Test
    fun deleteDirectoryContents_removes_all_files() {
        File(tempDir, "a.txt").writeText("a")
        File(tempDir, "b.txt").writeText("b")

        repository.deleteDirectoryContents(tempDir)

        Assert.assertEquals(0, tempDir.listFiles()?.size ?: -1)
    }
    /** Verifies that deleteDirectoryContents() does nothing if the directory is already empty */
    @Test
    fun deleteDirectoryContents_does_nothing_when_directory_is_empty() {
        Assert.assertEquals(0, tempDir.listFiles()?.size ?: 0)

        repository.deleteDirectoryContents(tempDir)

        Assert.assertEquals(0, tempDir.listFiles()?.size ?: -1)
    }
    /** Verifies that deleteDirectoryContents() also removes subdirectories and their contents */
    @Test
    fun deleteDirectoryContents_removes_subdirectories() {
        val subDir = File(tempDir, "subdir").apply { mkdirs() }
        File(subDir, "subfile.txt").writeText("subfile")
        File(tempDir, "file1.txt").writeText("file1")

        Assert.assertEquals(2, tempDir.listFiles()?.size)

        repository.deleteDirectoryContents(tempDir)

        Assert.assertEquals(0, tempDir.listFiles()?.size ?: -1)
    }

}
