package com.example.hikermanagementapp.util

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.hikermanagementapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoPickerHelper(
    private val fragment: Fragment,
    private val onPhotoSelected: (Uri) -> Unit // Callback when photo is selected
) {
    private var currentPhotoPath: String? = null // Temporary path for camera photo

    /**
     * Launcher for Photo Picker (gallery selection)
     * Android Photo Picker API (Android 13+)
     * Falls back to READ_MEDIA_IMAGES permission on older versions
     */
    private val pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let { onPhotoSelected(it) }
        }

    /**
     * Launcher for camera capture
     * Takes photo and saves to temporary file
     */
    private val takePicture: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && currentPhotoPath != null) {
                val photoUri = File(currentPhotoPath!!).toUri()
                onPhotoSelected(photoUri)
            }
        }

    /**
     * Permission launcher for CAMERA permission
     * Required for taking photos
     */
    private val requestCameraPermission: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchCamera()
            } else {
                showToast(fragment.getString(R.string.toast_camera_permission_denied))
            }
        }

    /**
     * Permission launcher for READ_MEDIA_IMAGES permission
     * Required for picking images from gallery (Android 13+)
     */
    private val requestReadImagesPermission: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                showToast(fragment.getString(R.string.toast_photo_permission_denied))
            }
        }

    /**
     * Shows a dialog to choose between taking a photo or picking one from the gallery.
     */
    fun showPhotoOptionsDialog() {
        val options = arrayOf(
            fragment.getString(R.string.dialog_take_photo),
            fragment.getString(R.string.dialog_choose_gallery),
            fragment.getString(R.string.action_cancel)
        )

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.dialog_add_photo_title))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> checkReadImagesPermissionAndLaunch()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    /**
     * Launches the camera to take a photo.
     */
    fun launchCamera() {
        checkCameraPermissionAndLaunch()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCameraIntent()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkReadImagesPermissionAndLaunch() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                else -> {
                    requestReadImagesPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun launchCameraIntent() {
        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath

            val photoUri = FileProvider.getUriForFile(
                fragment.requireContext(),
                "${fragment.requireContext().packageName}.fileprovider",
                photoFile
            )

            takePicture.launch(photoUri)
        } catch (e: Exception) {
            showToast(fragment.getString(R.string.toast_error_camera, e.message ?: ""))
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = fragment.requireContext().getExternalFilesDir(null)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(fragment.requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
