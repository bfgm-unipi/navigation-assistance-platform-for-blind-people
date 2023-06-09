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
package com.google.ar.core.vibaplatform.kotlin.viba

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.widget.TextView
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.vibaplatform.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.vibaplatform.java.common.helpers.DepthSettings
import com.google.ar.core.vibaplatform.java.common.helpers.FullScreenHelper
import com.google.ar.core.vibaplatform.java.common.samplerender.SampleRender
import com.google.ar.core.vibaplatform.kotlin.common.helpers.ARCoreSessionLifecycleHelper
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
class VibaActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    companion object {
        private const val TAG = "VibaActivity"
    }

    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: VibaView
    lateinit var renderer: VibaRenderer

    val depthSettings = DepthSettings()
    private val pointsCoordinates = CollisionPointsCoordinates()

    /* --------------- Matteo --------------- */

    // image view references
    private lateinit var headImageView: ImageView
    private lateinit var leftTorsoImageView: ImageView
    private lateinit var rightTorsoImageView: ImageView
    private lateinit var leftFloorImageView: ImageView
    private lateinit var rightFloorImageView: ImageView

    /* -------------------------------------- */

    /* ------------------ GIANLUCA --------------- */

    lateinit var vibrator: Vibrator
    var vibratorIsActive = false
    private var tts: TextToSpeech? = null
    var ttsLastWarning = ""
    val indicationThreshold = 10
    var indicationCounters = hashMapOf<String, Int>(
        "head" to 0,
        "torso_left" to 0,
        "torso_right" to 0,
        "floor_left" to 0,
        "floor_right" to 0,
        "free" to indicationThreshold
    )


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
        renderer = VibaRenderer(this)
        lifecycle.addObserver(renderer)

        // Set up Hello AR UI.
        view = VibaView(this)
        lifecycle.addObserver(view)
        setContentView(view.root)

        // Sets up an example renderer using our HelloARRenderer.
        SampleRender(view.surfaceView, renderer, assets)

        depthSettings.onCreate(this)
        setupUserImages()

        /* ------------------ GIANLUCA --------------- */

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        var vibrationButton: Button = findViewById(R.id.th_up)
        var speakButton: Button = findViewById(R.id.th_down)
        vibrationButton.setOnClickListener{ onThresholdUp() }
        speakButton.setOnClickListener{ onThresholdDown() }
        tts = TextToSpeech(this, this)

        /* ------------------------------------------- */
    }

    /* ---------------- Biagio ---------------- */
    fun drawGrid() {
        // Retrieve display metrics
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val scale = displayMetrics.density
        val pixelsToTranslate = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics)

        for (key in pointsCoordinates.getKeys()) {
            val imageView = findViewById<ImageView>(resources.getIdentifier(key, "id", packageName))
            imageView.translationX = pointsCoordinates.getCoordinatesByPointId(key)!!.first * screenWidth.toFloat() - (imageView.width / 2.0f)
            imageView.translationY = pointsCoordinates.getCoordinatesByPointId(key)!!.second * screenHeight.toFloat() - (imageView.height / 2.0f)
            imageView.visibility = ImageView.VISIBLE

            val textView = findViewById<TextView>(resources.getIdentifier(key + "_distance", "id", packageName))
            textView.translationX = pointsCoordinates.getCoordinatesByPointId(key)!!.first * screenWidth.toFloat() - pixelsToTranslate/scale
            if (key == "h1") {
                textView.translationY = pointsCoordinates.getCoordinatesByPointId(key)!!.second * screenHeight.toFloat() - (pixelsToTranslate/scale * 2.3f)
            } else {
                textView.translationY = pointsCoordinates.getCoordinatesByPointId(key)!!.second * screenHeight.toFloat() + pixelsToTranslate/scale
            }
            textView.visibility = TextView.VISIBLE
        }
    }

    fun hideGrid() {
        for (key in pointsCoordinates.getKeys()) {
            val imageView = findViewById<ImageView>(resources.getIdentifier(key, "id", packageName))
            imageView.visibility = ImageView.INVISIBLE
            val textView = findViewById<TextView>(resources.getIdentifier(key + "_distance", "id", packageName))
            textView.visibility = TextView.INVISIBLE
        }
    }

    fun updateGrid(points: MutableMap<String, Pair<Float, Boolean>>) {
        if (points.isNotEmpty()) {
            for (key in pointsCoordinates.getKeys()) {
                val imageView = findViewById<ImageView>(resources.getIdentifier(key, "id", packageName))
                val textView = findViewById<TextView>(resources.getIdentifier(key + "_distance", "id", packageName))
                textView.text = (points[key]?.first ?: 0.0f).toString() + "m"

                if (points[key]?.second == true) {
                    imageView.setImageResource(android.R.drawable.presence_online)
                } else {
                    imageView.setImageResource(android.R.drawable.presence_invisible)
                }
            }
        }
    }

    /* ---------------------------------------- */

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

    /* --------------- Matteo --------------- */

    private fun setupUserImages() {
        // retrieve all the references
        headImageView = findViewById(R.id.user_head)
        leftTorsoImageView = findViewById(R.id.user_left_torso)
        rightTorsoImageView = findViewById(R.id.user_right_torso)
        leftFloorImageView  = findViewById(R.id.user_left_floor)
        rightFloorImageView = findViewById(R.id.user_right_floor)

        // setup the image resources
        headImageView.setImageResource(R.drawable.head)
        leftTorsoImageView.setImageResource(R.drawable.left_torso)
        rightTorsoImageView.setImageResource(R.drawable.right_torso)
        leftFloorImageView.setImageResource(R.drawable.left_floor)
        rightFloorImageView.setImageResource(R.drawable.right_floor)
    }

    fun drawUserImages() {
        // retrieve display metrics
        val displayMetrics = resources.displayMetrics
        val screenWidth  = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // move the image views
        headImageView.post {
            headImageView.translationX = (screenWidth / 2.0f)  - (headImageView.width / 2.0f)
            headImageView.translationY = screenHeight * 2 / 8.0f
            leftTorsoImageView.translationX  = (screenWidth / 2.0f)  - (leftTorsoImageView.width)
            leftTorsoImageView.translationY  = headImageView.translationY + headImageView.height
            rightTorsoImageView.translationX = (screenWidth / 2.0f)
            rightTorsoImageView.translationY = leftTorsoImageView.translationY
            leftFloorImageView.translationX  = (screenWidth / 2.0f)  - (leftFloorImageView.width)
            leftFloorImageView.translationY  = leftTorsoImageView.translationY + leftTorsoImageView.height
            rightFloorImageView.translationX = (screenWidth / 2.0f)
            rightFloorImageView.translationY = leftFloorImageView.translationY
        }

        // set the images visible
        headImageView.visibility = ImageView.VISIBLE
        leftTorsoImageView.visibility  = ImageView.VISIBLE
        rightTorsoImageView.visibility = ImageView.VISIBLE
        leftFloorImageView.visibility  = ImageView.VISIBLE
        rightFloorImageView.visibility = ImageView.VISIBLE
    }

    fun updateUserImages(listOfCloseBodyParts: MutableList<String>) {
        // check if there are no obstacles
        if (listOfCloseBodyParts.size == 1 && listOfCloseBodyParts.contains("free")) {
            headImageView.setImageResource(R.drawable.head)
            leftTorsoImageView.setImageResource(R.drawable.left_torso)
            rightTorsoImageView.setImageResource(R.drawable.right_torso)
            leftFloorImageView.setImageResource(R.drawable.left_floor)
            rightFloorImageView.setImageResource(R.drawable.right_floor)

            return
        }

        // update head image resources
        if (listOfCloseBodyParts.contains("head"))
            headImageView.setImageResource(R.drawable.head_red)
        else
            headImageView.setImageResource(R.drawable.head)

        // update left torso image
        if (listOfCloseBodyParts.contains("torso_left"))
            leftTorsoImageView.setImageResource(R.drawable.left_torso_red)
        else
            leftTorsoImageView.setImageResource(R.drawable.left_torso)

        // update right torso image
        if (listOfCloseBodyParts.contains("torso_right"))
            rightTorsoImageView.setImageResource(R.drawable.right_torso_red)
        else
            rightTorsoImageView.setImageResource(R.drawable.right_torso)

        // update left floor image
        if (listOfCloseBodyParts.contains("floor_left"))
            leftFloorImageView.setImageResource(R.drawable.left_floor_red)
        else
            leftFloorImageView.setImageResource(R.drawable.left_floor)

        // update right floor image
        if (listOfCloseBodyParts.contains("floor_right"))
            rightFloorImageView.setImageResource(R.drawable.right_floor_red)
        else
            rightFloorImageView.setImageResource(R.drawable.right_floor)
    }

    fun hideUserImages() {
        // set the images invisible
        headImageView.visibility = ImageView.INVISIBLE
        leftTorsoImageView.visibility  = ImageView.INVISIBLE
        rightTorsoImageView.visibility = ImageView.INVISIBLE
        leftFloorImageView.visibility  = ImageView.INVISIBLE
        rightFloorImageView.visibility = ImageView.INVISIBLE
    }

    /* -------------------------------------- */

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

    fun onThresholdUp() {
        val text: TextView = findViewById(R.id.threshold)
        val threshold: Float = text.text.toString().toFloat() + 100
        text.text = threshold.toString()
        renderer.setThreshold(threshold)
    }

    fun onThresholdDown() {
        val text: TextView = findViewById(R.id.threshold)
        val threshold: Float = text.text.toString().toFloat() - 100
        if (threshold > 0) {
            text.text = threshold.toString()
            renderer.setThreshold(threshold)
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

    fun speak(text: String, speechRate: Float) {
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

    fun elaborateIndications(indications: MutableList<String>): MutableList<String> {

        // Log.i("TEST_INDICATIONS", "---------------------------------")

        var iterator = indicationCounters.iterator()
        var newIndications = mutableListOf<String>()

        // Log.i("TEST_INDICATIONS", indications.toString())
        while (iterator.hasNext()) {

            val elem = iterator.next()
            val key = elem.key
            var value = elem.value

            if (key == "free") {
                if (indications.size == 0) {
                    if (value < indicationThreshold * 2) {
                        value += 1
                    }
                }
                else {
                    if (value > 0) {
                        value -= 1
                    }
                }
            } else {
                if (key in indications) {
                    if (value < indicationThreshold * 2) {
                        value += 1
                    }
                } else {
                    if (value > 0) {
                        value -= 1
                    }
                }
            }

            if (value > indicationThreshold) {
                newIndications.add(key)
            }

            elem.setValue(value)

            // Log.i("TEST_INDICATIONS", key + ": " + value + "(" + indications.size + ")")
        }
        // Log.i("TEST_INDICATIONS", "---------------------------------")
        return newIndications
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun warningVibration(indications: MutableList<String>){
        // Log.i("TEST_WARNING", indications.toString())

        if (indications.size > 0){
            if (indications.size == 1 && indications[0] == "free"){
                vibratorIsActive = false
                stopVibration()
            }
            else if (!vibratorIsActive) {
                vibratorIsActive = true
                startVibration(500L, 100L, 255)
            }
        }
    }

    fun warningSpeech(indications: MutableList<String>){

        if (indications.size == 0) {
            return
        }

        if ("free" !in indications) {

            var text = "Something went wrong"

            if (indications.size == 1) {
                when (indications[0]) {
                    "head" -> text = "Head level obstacle"
                    "torso_left" -> text = "Left torso level obstacle"
                    "torso_right" -> text = "Right torso level obstacle"
                    "floor_left" -> text = "Left floor level obstacle"
                    "floor_right" -> text = "Right floor level obstacle"
                }
            }
            else if (indications.size == 2) {
                when {
                    ( "torso_left"  in indications && "torso_right" in indications ) -> text = "Torso level obstacle"
                    ("floor_left" in indications && "floor_right" in indications) -> text = "Floor level obstacle"
                    ( "torso_left"  in indications && "floor_left" in indications ) -> text = "Full left obstacle"
                    ( "torso_right"  in indications && "floor_right" in indications ) -> text = "Full right obstacle"
                    ( "head" in indications && "torso_left" in indications) -> text = "Left torso and head obstacle"
                    ( "head" in indications && "torso_right" in indications) -> text = "Right torso and head obstacle"
                    ( "head" in indications && "floor_left" in indications) -> text = "Left floor and head obstacle"
                    ( "head" in indications && "floor_right" in indications) -> text = "Right floor and head obstacle"
                }
            }
            else {
                text = "Full body obstacle"
            }

            if (ttsLastWarning != text){
                speak(text, 0.7f)
                ttsLastWarning = text
            }
        }
        else{
            if (ttsLastWarning != "") {
                speak("No more obstacles", 0.7f)
            }
            ttsLastWarning = ""
        }
    }
    /* ------------------------------------------- */
}
