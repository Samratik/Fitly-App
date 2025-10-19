package com.example.fitinmotion

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fitinmotion.PoseAnalyzer
import com.example.fitinmotion.PoseOverlayView

private const val TAG = "FitInMotion"

@Composable
fun WorkoutScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCam by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCam = granted }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) hasCam = true else launcher.launch(Manifest.permission.CAMERA)
    }

    Scaffold { _ ->
        if (!hasCam) {
            Text("Нужно разрешение на камеру", modifier = Modifier.fillMaxSize())
            return@Scaffold
        }
        CameraWithPose(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CameraWithPose(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    val overlayView = remember { PoseOverlayView(context) }
    var viewSize by remember { mutableStateOf(Size(1, 1)) }

    // узнаём размер overlay для корректной проекции
    DisposableEffect(Unit) {
        val vto = overlayView.viewTreeObserver
        val l = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            viewSize = Size(overlayView.width.coerceAtLeast(1), overlayView.height.coerceAtLeast(1))
        }
        vto.addOnGlobalLayoutListener(l)
        onDispose { vto.removeOnGlobalLayoutListener(l) }
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = PoseAnalyzer(
                viewSizeProvider = { viewSize }
            ) { _, projected ->
                val w = viewSize.width.toFloat()
                // зеркалим точки по горизонтали
                val mirrored = projected.mapValues { (_, p) ->
                    android.graphics.PointF(w - p.x, p.y)
                }
                overlayView.updateLandmarks(mirrored)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    analysis
                )
                Log.d(TAG, "Camera + Pose bound")
            } catch (e: Exception) {
                Log.e(TAG, "Bind failed", e)
            }
        }, executor)

        onDispose {
            try { cameraProviderFuture.get().unbindAll() } catch (_: Throwable) {}
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        AndroidView(factory = { overlayView }, modifier = Modifier.fillMaxSize())
    }
}