package com.sanathcoding.workmanagerguide

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter
import com.sanathcoding.workmanagerguide.core.util.ConstValue.UNIQUE_WORK_NAME
import com.sanathcoding.workmanagerguide.core.util.WorkerKeys
import com.sanathcoding.workmanagerguide.ui.theme.WorkManagerGuideTheme
import com.sanathcoding.workmanagerguide.worker.ColorFilterWorker
import com.sanathcoding.workmanagerguide.worker.ImageDownloadWorker

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val imageDownloadRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).build()
            val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>()
                .build()
            val workManager = WorkManager.getInstance(applicationContext)

            WorkManagerGuideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val workerInfos = workManager
                        .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
                        .observeAsState()
                        .value

                    val imageDownloadInfos = remember(key1 = workerInfos) {
                        workerInfos?.find { it.id == imageDownloadRequest.id }
                    }
                    val colorFilterInfos = remember(key1 = workerInfos) {
                        workerInfos?.find { it.id == colorFilterRequest.id }
                    }
                    val imageUri by derivedStateOf {
                        val downloadUri = imageDownloadInfos?.outputData
                            ?.getString(WorkerKeys.IMAGE_URI)?.toUri()
                        val colorFilterUri = colorFilterInfos?.outputData
                            ?.getString(WorkerKeys.FILTER_URI)?.toUri()
                        colorFilterUri ?: downloadUri
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imageUri?.let { uri ->
                            Image(
                                painter = rememberImagePainter(
                                    data = uri
                                ), contentDescription = "Image.jpg",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Button(onClick = {
                            workManager.beginUniqueWork(
                                UNIQUE_WORK_NAME,
                                ExistingWorkPolicy.KEEP,
                                imageDownloadRequest
                            ).then(
                                colorFilterRequest
                            ).enqueue()
                        }, enabled = imageDownloadInfos?.state != WorkInfo.State.RUNNING) {
                            Text(text = "Start Download")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        when(imageDownloadInfos?.state) {
                            WorkInfo.State.ENQUEUED -> Text("Download Enqueued")
                            WorkInfo.State.RUNNING -> Text("Image Downloading...")
                            WorkInfo.State.SUCCEEDED -> Text("Download Succeeded")
                            WorkInfo.State.FAILED -> Text("Download Failed")
                            WorkInfo.State.BLOCKED -> Text("Download Blocked")
                            WorkInfo.State.CANCELLED -> Text("Download Cancelled")
                            null -> Text("")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        when(colorFilterInfos?.state) {
                            WorkInfo.State.ENQUEUED -> Text("Color filter Enqueued")
                            WorkInfo.State.RUNNING -> Text("Applying Color filter...")
                            WorkInfo.State.SUCCEEDED -> Text("Color filter Succeeded")
                            WorkInfo.State.FAILED -> Text("Color filter Failed")
                            WorkInfo.State.BLOCKED -> Text("Color filter Blocked")
                            WorkInfo.State.CANCELLED -> Text("Color filter Cancelled")
                            null -> Text("")
                        }
                    }
                }
            }
        }
    }
}