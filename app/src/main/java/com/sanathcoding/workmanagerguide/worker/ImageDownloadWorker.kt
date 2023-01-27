package com.sanathcoding.workmanagerguide.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sanathcoding.workmanagerguide.core.util.ConstValue.NOTIFICATION_ID
import com.sanathcoding.workmanagerguide.R
import com.sanathcoding.workmanagerguide.core.util.WorkerKeys
import com.sanathcoding.workmanagerguide.data.remote.FileApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class ImageDownloadWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        startForegroundService()
        delay(5000L)
        val response = FileApi.instance.downloadImage()
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { stram ->
                    try {
                        stram.write(body.bytes())
                    } catch (e: IOException) {
                        return@withContext Result.failure(
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage
                            )
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toUri().toString()
                    )
                )
            }
        }
        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) return Result.retry()
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network Error"
                )
            )
        }
        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Unknown Error"
            )
        )

    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(
                    context,
                    NOTIFICATION_ID
                )
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Image Downloading...")
                    .setContentTitle("Download in progress")
                    .build()
            )
        )
    }

}