package com.example.hikermanagementapp.ui.observation

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Observation
import com.example.hikermanagementapp.databinding.FragmentObservationFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ObservationFormFragment : Fragment() {
    private var _binding: FragmentObservationFormBinding? = null
    private val binding get() = _binding!!

    private val obsVm: ObservationViewModel by viewModels()

    private var hikeId: Long = -1
    private var observationId: Long = -1
    private var selectedMillis: Long = System.currentTimeMillis()
    private var selectedPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            handlePhotoResult(uri)
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            val photoUri = File(currentPhotoPath!!).toUri()
            handlePhotoResult(photoUri)
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toast_camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestReadImagesPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(requireContext(), getString(R.string.toast_photo_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObservationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hikeId = requireArguments().getLong("hikeId")
        observationId = requireArguments().getLong("observationId", -1)

        // Handle back button press with confirmation dialog
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackConfirmationDialog()
            }
        })

        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            ?.setNavigationOnClickListener { showBackConfirmationDialog() }

        updateDateTimeField(selectedMillis)

        binding.tilDateTime.setEndIconOnClickListener { pickDateTime() }
        binding.etDateTime.setOnClickListener { pickDateTime() }

        binding.btnPickPhotoObs.setOnClickListener { showPhotoOptionsDialog() }
        binding.btnClearPhotoObs.setOnClickListener {
            selectedPhotoUri = null
            binding.ivPhotoObs.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // restore a draft passed back from confirm over DB load
        val hasDraftFromConfirm = arguments?.let { a ->
            a.containsKey("observation") || a.containsKey("timestamp") || a.containsKey("comments") || a.containsKey("photoUri")
        } == true
        if (hasDraftFromConfirm) {
            binding.etObservation.setText(requireArguments().getString("observation") ?: "")
            selectedMillis = requireArguments().getLong("timestamp", selectedMillis)
            updateDateTimeField(selectedMillis)
            binding.etComments.setText(requireArguments().getString("comments") ?: "")
            requireArguments().getString("photoUri")?.let { uriStr ->
                try {
                    selectedPhotoUri = uriStr.toUri()
                    binding.ivPhotoObs.setImageURI(selectedPhotoUri)
                } catch (_: Exception) { /* ignore bad uri */ }
            }
        } else if (observationId != -1L) {
            obsVm.getById(observationId) { obs ->
                obs?.let { fillForm(it) }
            }
        }

        binding.btnSaveObs.setOnClickListener {
            val observationText = binding.etObservation.text?.toString()?.trim().orEmpty()
            if (observationText.isEmpty()) {
                binding.tilObservation.error = getString(R.string.error_required)
                Toast.makeText(requireContext(), getString(R.string.toast_observation_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else binding.tilObservation.error = null

            val comments = binding.etComments.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() }

            // Navigate to confirmation screen
            val args = Bundle().apply {
                putLong("hikeId", hikeId)
                if (observationId != -1L) putLong("observationId", observationId)
                putString("observation", observationText)
                putLong("timestamp", selectedMillis)
                putString("comments", comments)
                putString("photoUri", selectedPhotoUri?.toString())
            }
            findNavController().navigate(R.id.action_observationForm_to_observationConfirm, args)
        }
    }

    private fun showBackConfirmationDialog() {
        // Check if there are any changes
        val hasChanges = hasFormChanges()

        if (!hasChanges) {
            val popped = findNavController().popBackStack(R.id.hikeDetailFragment, false)
            if (!popped) {
                val args = Bundle().apply { putLong("hikeId", hikeId) }
                findNavController().navigate(R.id.hikeDetailFragment, args)
            }
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_unsaved_changes_title))
            .setMessage(getString(R.string.dialog_unsaved_changes_message))
            .setPositiveButton(getString(R.string.btn_continue_editing)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_discard)) { _, _ ->
                // Always return to hike detail, skipping confirm screen
                val popped = findNavController().popBackStack(R.id.hikeDetailFragment, false)
                if (!popped) {
                    val args = Bundle().apply { putLong("hikeId", hikeId) }
                    findNavController().navigate(R.id.hikeDetailFragment, args)
                }
            }
            .setNeutralButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun hasFormChanges(): Boolean {
        val observation = binding.etObservation.text?.toString()?.trim()
        val comments = binding.etComments.text?.toString()?.trim()

        return !observation.isNullOrBlank() || !comments.isNullOrBlank() || selectedPhotoUri != null
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf(
            getString(R.string.dialog_take_photo),
            getString(R.string.dialog_choose_gallery),
            getString(R.string.action_cancel)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_add_photo_title))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> checkReadImagesPermissionAndLaunch()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
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
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                else -> {
                    requestReadImagesPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath

            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            takePicture.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.toast_error_camera, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "OBS_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun handlePhotoResult(uri: Uri) {
        try {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (_: Exception) {
        }
        selectedPhotoUri = uri
        binding.ivPhotoObs.setImageURI(uri)
    }

    private fun fillForm(obs: Observation) {
        binding.etObservation.setText(obs.observation)
        binding.etComments.setText(obs.comments ?: "")
        selectedMillis = obs.timestamp
        updateDateTimeField(selectedMillis)
        obs.photoUri?.let {
            try {
                selectedPhotoUri = it.toUri()
                binding.ivPhotoObs.setImageURI(selectedPhotoUri)
            } catch (_: Exception) {
                binding.ivPhotoObs.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    private fun pickDateTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            TimePickerDialog(requireContext(), { _, h, min ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d, h, min, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedMillis = picked.timeInMillis
                updateDateTimeField(selectedMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateTimeField(millis: Long) {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.etDateTime.setText(fmt.format(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
