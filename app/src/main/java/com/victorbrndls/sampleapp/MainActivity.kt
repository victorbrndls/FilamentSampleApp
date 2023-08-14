package com.victorbrndls.sampleapp

import android.os.Bundle
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.utils.Utils
import com.victorbrndls.sampleapp.ui.theme.FilamentSampleAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

	companion object {
		init {
			Utils.init()
		}
	}

	@OptIn(ExperimentalFoundationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			FilamentSampleAppTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					val pagerState = rememberPagerState()

					// true rotates forward, false rotates backward
					val animations = listOf(
						true,
						true,
						false,
						false,
						false,
						true,
						false,
						true,
						true,
						true,
						false,
						false
					)

					VerticalPager(
						state = pagerState,
						pageCount = animations.size,
						beyondBoundsPageCount = 1,
						key = { it },
					) { page ->
						val cubeModelRenderer = remember { ModelRenderer() }

						LaunchedEffect(pagerState.currentPage) {
							if (pagerState.currentPage != page) return@LaunchedEffect

							delay(1_000)
							cubeModelRenderer.rotate(animations[page])
						}

						Box {
							Text(
								text = (page + 1).toString(),
								fontSize = 22.sp,
								modifier = Modifier.align(Alignment.TopStart)
							)

							AndroidView(
								factory = {
									TextureView(it).apply {
										cubeModelRenderer.onSurfaceAvailable(this, lifecycle)
									}
								},
								update = {},
								modifier = Modifier
									.widthIn(max = 400.dp)
									.fillMaxWidth(0.85f)
									.aspectRatio(1f)
							)
						}
					}
				}
			}
		}
	}
}
