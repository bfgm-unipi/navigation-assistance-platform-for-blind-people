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

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
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

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3D model.
 */
class HelloArActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HelloArActivity"
    }

    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: HelloArView
    lateinit var renderer: HelloArRenderer

    val depthSettings = DepthSettings()

    /* --------------- Matteo --------------- */

    // image view references
    private lateinit var headImageView: ImageView
    private lateinit var leftChestImageView: ImageView
    private lateinit var rightChestImageView: ImageView
    private lateinit var leftLegImageView: ImageView
    private lateinit var rightLegImageView: ImageView

    /* -------------------------------------- */

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
        setupUserImages()
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

    /* --------------- Matteo --------------- */

    private fun setupUserImages() {
        // retrieve all the references
        headImageView = findViewById(R.id.user_head)
        leftChestImageView = findViewById(R.id.user_left_chest)
        rightChestImageView = findViewById(R.id.user_right_chest)
        leftLegImageView  = findViewById(R.id.user_left_leg)
        rightLegImageView = findViewById(R.id.user_right_leg)

        // setup the image resources
        headImageView.setImageResource(R.drawable.head)
        leftChestImageView.setImageResource(R.drawable.left_chest)
        rightChestImageView.setImageResource(R.drawable.right_chest)
        leftLegImageView.setImageResource(R.drawable.left_leg)
        rightLegImageView.setImageResource(R.drawable.right_leg)

        /*/ setup their size
        headImageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val container = findViewById<View>(R.id.my_layout)
        val params = headImageView.layoutParams as ViewGroup.LayoutParams
        params.width = (container.width * 0.5).toInt()
        params.height = (container.height * 0.75).toInt()
        headImageView.layoutParams = params*/


    }

    fun drawUserImages() {
        // retrieve display metrics
        val displayMetrics = resources.displayMetrics
        val screenWidth  = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        /*/ TEST: setup image size
        setupProportionalImageSize(headImageView)
        setupProportionalImageSize(leftChestImageView)
        setupProportionalImageSize(rightChestImageView)
        setupProportionalImageSize(leftLegImageView)
        setupProportionalImageSize(rightLegImageView)*/

        // move the image views
        headImageView.post {
            headImageView.translationX = (screenWidth / 2.0f)  - (headImageView.width / 2.0f)
            headImageView.translationY = screenHeight * 2 / 8.0f
            leftChestImageView.translationX  = (screenWidth / 2.0f)  - (leftChestImageView.width)
            leftChestImageView.translationY  = headImageView.translationY + headImageView.height
            rightChestImageView.translationX = (screenWidth / 2.0f)
            rightChestImageView.translationY = leftChestImageView.translationY
            leftLegImageView.translationX  = (screenWidth / 2.0f)  - (leftLegImageView.width)
            leftLegImageView.translationY  = leftChestImageView.translationY + leftChestImageView.height
            rightLegImageView.translationX = (screenWidth / 2.0f)
            rightLegImageView.translationY = leftLegImageView.translationY
        }

        // set the images visible
        headImageView.visibility = ImageView.VISIBLE
        leftChestImageView.visibility  = ImageView.VISIBLE
        rightChestImageView.visibility = ImageView.VISIBLE
        leftLegImageView.visibility  = ImageView.VISIBLE
        rightLegImageView.visibility = ImageView.VISIBLE
    }

    // TODO: fix proportional resize
    private fun setupProportionalImageSize(imageView: ImageView) {
        val parentLayout = findViewById<RelativeLayout>(R.id.my_layout)
        val layoutParams = RelativeLayout.LayoutParams(
            (parentLayout.width * 0.25).toInt(),
            (parentLayout.height * 0.25).toInt()
        )
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        imageView.layoutParams = layoutParams
    }

    fun updateUserImages() {
        // TODO: setup the update image resources
        headImageView.setImageResource(R.drawable.head_red)
        leftChestImageView.setImageResource(R.drawable.left_chest_red)
        rightChestImageView.setImageResource(R.drawable.right_chest)
        leftLegImageView.setImageResource(R.drawable.left_leg)
        rightLegImageView.setImageResource(R.drawable.right_leg_red)
    }

    fun hideUserImages() {
        // set the images invisible
        headImageView.visibility = ImageView.INVISIBLE
        leftChestImageView.visibility  = ImageView.INVISIBLE
        rightChestImageView.visibility = ImageView.INVISIBLE
        leftLegImageView.visibility  = ImageView.INVISIBLE
        rightLegImageView.visibility = ImageView.INVISIBLE
    }

    /* -------------------------------------- */
}
