package com.vinn.vhike.data.repository

import android.util.Log
import com.vinn.vhike.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepository @Inject constructor() {

    private val github: GitHub by lazy {
        try {
            GitHubBuilder().withOAuthToken(BuildConfig.GITHUB_TOKEN).build()
        } catch (e: IOException) {
            Log.e("GitHubRepository", "Failed to initialize GitHub client", e)
            throw RuntimeException("GitHub init failed", e)
        }
    }

    private val repository: GHRepository by lazy {
        try {
            github.getRepository("${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}")
        } catch (e: IOException) {
            Log.e("GitHubRepository", "Failed to get repository", e)
            throw RuntimeException("GitHub repo failed", e)
        }
    }

    suspend fun uploadFile(
        fileBytes: ByteArray,
        originalFileName: String,
        commitMessage: String
    ): String? = withContext(Dispatchers.IO) {

        val timestamp = System.currentTimeMillis()
        val baseName = originalFileName.substringBeforeLast('.', originalFileName)
        val extension = originalFileName.substringAfterLast('.', "")
        val newUniqueFilename = if (extension.isEmpty()) {
            "${baseName}_${timestamp}"
        } else {
            "${baseName}_${timestamp}.${extension}"
        }

        val pathInRepo = if (BuildConfig.GITHUB_TARGET_FOLDER.isEmpty()) {
            newUniqueFilename
        } else {
            "${BuildConfig.GITHUB_TARGET_FOLDER}/$newUniqueFilename"
        }

        try {
            Log.d("GitHubRepository", "Uploading file '$newUniqueFilename' to path: $pathInRepo")

            val response = repository.createContent()
                .path(pathInRepo)
                .content(fileBytes)
                .message(commitMessage)
                .commit()

            val content = response.content
            val htmlUrl = content.htmlUrl

            Log.i("GitHubRepository", "Successfully uploaded file. URL: $htmlUrl")

            return@withContext "$htmlUrl?raw=true"

        } catch (e: IOException) {
            Log.e("GitHubRepository", "Error uploading file to GitHub", e)
            return@withContext null
        }
    }
}