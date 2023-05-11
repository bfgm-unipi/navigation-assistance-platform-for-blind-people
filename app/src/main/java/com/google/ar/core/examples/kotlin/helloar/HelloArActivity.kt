/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.kotlin.helloar

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.os.Vibrator
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.examples.java.common.helpers.DepthSettings
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.util.Locale

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3D model.
 */
class HelloArActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    companion object {
        private const val TAG = "HelloArActivity"
    }

    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: HelloArView
    lateinit var renderer: HelloArRenderer

    val depthSettings = DepthSettings()

    /* ------------------ GIANLUCA --------------- */

    lateinit var vibrator: Vibrator
    private var tts: TextToSpeech? = null

    /* ------------------------------------------- */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ARCore session lifecycle helper and configuration.
        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
        // If Session creation or Session.resume() fails, display a message and log detailed
        // information.
        arCoreSessionHelper.exceptionCallback =
            { exception ->
                val message =
                    when (exception) {
                        is UnavailableUserDeclinedInstallationException ->
                            "Please install Google Play Services for AR"

                        is UnavailableApkTooOldException -> "Please update ARCore"
                        is UnavailableSdkTooOldException -> "Please update this app"
                        is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                        is CameraNotAvailableException -> "Camera not available. Try restarting the app."
                        else -> "Failed to create AR session: $exception"
                    }
                Log.e(TAG, "ARCore threw an exception", exception)
                view.snackbarHelper.showError(this, message)
            }

        // Configure session features, including: Lighting Estimation, Depth mode, Instant Placement.
        arCoreSessionHelper.beforeSessionResume = ::configureSession
        lifecycle.addObserver(arCoreSessionHelper)

        // Set up the Hello AR renderer.
        renderer = HelloArRenderer(this)
        lifecycle.addObserver(renderer)

        // Set up Hello AR UI.
        view = HelloArView(this)
        lifecycle.addObserver(view)
        setContentView(view.root)

        // Sets up an example renderer using our HelloARRenderer.
        SampleRender(view.surfaceView, renderer, assets)

        depthSettings.onCreate(this)

        /* ------------------ GIANLUCA --------------- */

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        var vibrationButton: Button = findViewById(R.id.vibration_button)
        var speakButton: Button = findViewById(R.id.speak_button)
        vibrationButton.setOnClickListener{ onVibrate() }
        speakButton.setOnClickListener{ onSpeak("Hello, the Text to Speech service is online", 0.7f) }
        tts = TextToSpeech(this, this)

        /* ------------------------------------------- */
    }

    fun printDistance(distanceId:String, pixelId: String, distance: Float, pixel_width: Float, pixel_height: Float) {

        val textViewDistanceId = resources.getIdentifier(distanceId, "id", packageName)
        val textDistance= findViewById<TextView>(textViewDistanceId)
        textDistance.text = distanceId +": " + distance + " m"

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val textViewPixelId = resources.getIdentifier(pixelId, "id", packageName)
        val textPixel = findViewById<TextView>(textViewPixelId)

        textPixel.x = pixel_width *  screenWidth.toFloat()
        textPixel.y = pixel_height *  screenHeight.toFloat()

        Log.i("Display partialscreenWidth AND screenWidth", ": " + pixel_width *  screenWidth + "," + screenWidth)

    }

    // Configure the session, using Lighting Estimation, and Depth mode.
    fun configureSession(session: Session) {
        session.configure(
            session.config.apply {
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                // Depth API is used if it is configured in Hello AR's settings.
                depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        Config.DepthMode.AUTOMATIC
                    } else {
                        Config.DepthMode.DISABLED
                    }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    /* ------------------ GIANLUCA --------------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startVibration(vibration_duration: Long, waiting_duration: Long, amplitude: Int) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(vibration_duration, waiting_duration),
                intArrayOf(amplitude, 0),
                0
            )
        )
    }

    fun stopVibration() {
        vibrator.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onVibrate() {
        startVibration(500L, 100L, 255)
        val vibrationButton: Button = findViewById(R.id.vibration_button)
        if (vibrationButton.text == "start vibration") {
            vibrationButton.text = "stop vibration"
            startVibration(500L, 100L, 255)
        }
        else {
            vibrationButton.text = "start vibration"
            stopVibration()
        }
    }

    override fun onInit(status: Int) {
        // TTS service initialization
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }
        }
    }
    fun onSpeak(text: String, speechRate: Float) {
        tts!!.setSpeechRate(speechRate)
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    fun warningVibration(indications: MutableList<String>){
        // TODO
    }

    fun warningSpeech(indications: MutableList<String>){
        // TODO
    }

    /* ------------------------------------------- */
}
