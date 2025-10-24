package com.example.fitinmotion

import android.graphics.PointF
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fitinmotion.PushUpCounterMP
import com.example.fitinmotion.SquatCounterMP
import com.example.fitinmotion.PoseIdx
import java.util.concurrent.Executors

@Composable
fun WorkoutScreenCore(
    exerciseType: ExerciseType,
    currentReps: Int,
    onRepsChange: (Int) -> Unit,
    counter: Any,                    // PushUpCounterMP или SquatCounterMP
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    val overlayView = remember { PoseOverlayView(context) }
    var viewSize by remember { mutableStateOf(Size(1,1)) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val vto = overlayView.viewTreeObserver
        val l = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            viewSize = Size(overlayView.width.coerceAtLeast(1), overlayView.height.coerceAtLeast(1))
        }
        vto.addOnGlobalLayoutListener(l)
        onDispose {
            vto.removeOnGlobalLayoutListener(l)
            cameraExecutor.shutdown()
        }
    }

    // запуск камеры + анализатора MediaPipe
    DisposableEffect(lifecycleOwner, exerciseType) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analyzer = MediaPipePoseAnalyzer(
                context = context,
                viewSizeProvider = { viewSize }
            ) { map: Map<Int, PointF> ->
                val w = viewSize.width.toFloat()
                val mirrored = map.mapValues { (_, p) -> PointF(w - p.x, p.y) }

                overlayView.updateLandmarks(mirrored)

                // обновляем счётчик
                when (counter) {
                    is PushUpCounterMP -> {
                        val before = counter.reps
                        counter.onLandmarks(mirrored)
                        if (counter.reps != before) onRepsChange(counter.reps)
                    }
                    is SquatCounterMP -> {
                        val before = counter.reps
                        counter.onLandmarks(mirrored)
                        if (counter.reps != before) onRepsChange(counter.reps)
                    }
                }
            }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(960, 540))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, analyzer) }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA, // фронталка + зеркалим точки выше
                    preview,
                    analysis
                )
            } catch (_: Throwable) {}
        }, executor)

        onDispose { try { providerFuture.get().unbindAll() } catch (_: Throwable) {} }
    }

    // UI поверх камеры
    Box(Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        AndroidView({ overlayView }, modifier = Modifier.fillMaxSize())

        // Верхняя панель c режимом и счётом
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AssistChip(onClick = onBack, label = { Text("Назад") })
            val title = if (exerciseType == ExerciseType.PUSHUPS) "Отжимания" else "Приседания"
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), shape = MaterialTheme.shapes.large) {
                Text("$title · Reps: $currentReps",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}