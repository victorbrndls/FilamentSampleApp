package com.victorbrndls.sampleapp

import android.animation.ValueAnimator
import android.opengl.Matrix
import android.view.Choreographer
import android.view.TextureView
import android.view.animation.LinearInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import java.nio.ByteBuffer


class ModelRenderer {

	companion object {
		private val engine by lazy { Engine.create() }
	}

	private lateinit var textureView: TextureView
	private lateinit var lifecycle: Lifecycle

	private lateinit var choreographer: Choreographer
	private lateinit var uiHelper: UiHelper

	private lateinit var modelViewer: ModelViewer

	private val frameScheduler = FrameCallback()

	private val lifecycleObserver = object : DefaultLifecycleObserver {
		override fun onResume(owner: LifecycleOwner) {
			choreographer.postFrameCallback(frameScheduler)
		}

		override fun onPause(owner: LifecycleOwner) {
			choreographer.removeFrameCallback(frameScheduler)
		}
	}

	fun onSurfaceAvailable(textureView: TextureView, lifecycle: Lifecycle) {
		this.textureView = textureView
		this.lifecycle = lifecycle

		choreographer = Choreographer.getInstance()
		uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
			isOpaque = false
		}

		modelViewer = ModelViewer(
			textureView = textureView,
			engine = engine,
			uiHelper = uiHelper,
		)

		createRenderables()

		modelViewer.scene.skybox = null
		modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
		modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
			clear = true
		}

		setupView()

//		textureView.setOnTouchListener { _, event ->
//			modelViewer.onTouchEvent(event)
//			true
//		}

		modelViewer.lookAt(
			eyePos = doubleArrayOf(3.050130844116211, 2.2390565872192383, -0.7212893962860107),
			target = doubleArrayOf(2.3990297317504883, 1.7684307098388672, -1.3167545795440674),
			upward = doubleArrayOf(-0.3064250349998474, 0.7785114645957947, -0.28024131059646606),
		)

		lifecycle.addObserver(lifecycleObserver)
	}

	private fun setupView() {
		modelViewer.view.apply {
			renderQuality = renderQuality.apply {
				hdrColorBuffer = View.QualityLevel.MEDIUM
			}

			isPostProcessingEnabled = false
		}
	}

	private fun createRenderables() {
		val buffer = textureView.context.assets.open("models/cube.glb").use { input ->
			val bytes = ByteArray(input.available())
			input.read(bytes)
			ByteBuffer.wrap(bytes)
		}

		modelViewer.loadModelGlb(buffer)
		modelViewer.transformToUnitCube()
	}

	fun rotate(forward: Boolean) {
		val tcm = engine.transformManager
		val animator = ValueAnimator.ofFloat(0f, 90f).apply {
			interpolator = LinearInterpolator()
			duration = 3000
		}

		val entitiesToAnimate = modelViewer.asset!!.entities.toList().filter { entity ->
			val name = modelViewer.asset!!.getName(entity)
			// The pieces I want to animate are named "Color*" and "Cube*" in the glb model
			name.startsWith("Color") || !name.startsWith("Cube")
		}

		entitiesToAnimate.forEach { entity ->
			val initialTransform = FloatArray(16)
			tcm.getTransform(entity, initialTransform)

			val animationMatrix = FloatArray(16)

			val rotateY = if (forward) -1f else 1f

			animator.addUpdateListener {
				val angle = it.animatedValue as Float

				System.arraycopy(initialTransform, 0, animationMatrix, 0, 16)

				Matrix.translateM(
					animationMatrix,
					0,
					-initialTransform[12],
					-initialTransform[13],
					-initialTransform[14],
				)
				Matrix.rotateM(animationMatrix, 0, angle, 0f, rotateY, 0f)
				Matrix.translateM(
					animationMatrix,
					0,
					initialTransform[12],
					initialTransform[13],
					initialTransform[14],
				)

				tcm.setTransform(entity, animationMatrix)
			}
		}

		animator.start()
	}

	inner class FrameCallback : Choreographer.FrameCallback {
		override fun doFrame(frameTimeNanos: Long) {
			choreographer.postFrameCallback(this)
			modelViewer.render(frameTimeNanos)
		}
	}

}
