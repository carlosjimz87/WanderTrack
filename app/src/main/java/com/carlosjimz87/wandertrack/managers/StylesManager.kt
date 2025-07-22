package com.carlosjimz87.wandertrack.managers

import android.content.Context
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.carlosjimz87.wandertrack.R
import com.google.android.gms.maps.model.MapStyleOptions

class StylesManager(private val context: Context) {
    private var lightMapStyle: MapStyleOptions? = null
    private var darkMapStyle: MapStyleOptions? = null

    private var lightAnimation: LottieCompositionSpec? = null
    private var darkAnimation: LottieCompositionSpec? = null

    fun preloadStyles() {
        preloadAnimationStyles()
        preloadMapStyles()
    }

    private fun preloadAnimationStyles() {
        if (lightAnimation == null) {
            lightAnimation = LottieCompositionSpec.RawRes(R.raw.loading_anim)
        }
        if (darkAnimation == null) {
            darkAnimation = LottieCompositionSpec.RawRes(R.raw.dark_loading_anim)
        }
    }

    fun getAnimationStyles(isDarkTheme: Boolean): LottieCompositionSpec {
        return if (isDarkTheme) darkAnimation!! else lightAnimation!!
    }

    private fun preloadMapStyles() {
        if (lightMapStyle == null) {
            lightMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        }
        if (darkMapStyle == null) {
            darkMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
        }
    }

    fun getMapStyle(isDarkTheme: Boolean): MapStyleOptions {
        return if (isDarkTheme) darkMapStyle!! else lightMapStyle!!
    }
}