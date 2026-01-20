package com.example.hikermanagementapp.ui.hike

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.databinding.FragmentHikeFormBinding
import com.example.hikermanagementapp.ui.common.NoFilterArrayAdapter
import com.example.hikermanagementapp.util.PhotoPickerHelper
import com.example.hikermanagementapp.util.PermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

class HikeFormFragment : Fragment() {
    private var _binding: FragmentHikeFormBinding? = null
    private val binding get() = _binding!!

    private val hikeVm: HikeViewModel by viewModels()

    private var editingHikeId: Long? = null
    private var selectedPhotoUri: Uri? = null
    private var isEditing = false

    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    private var currentDraft: HikeDraft? = null

    // Photo picker helper
    private lateinit var photoPickerHelper: PhotoPickerHelper

    private val requestLocationPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchAndFillLocation() else Toast.makeText(requireContext(), getString(R.string.toast_location_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHikeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize photo picker helper
        photoPickerHelper = PhotoPickerHelper(this) { uri ->
            handlePhotoResult(uri)
        }

        // Check if editing FIRST
        val argId = arguments?.getLong("hikeId", -1L) ?: -1L
        if (argId != -1L) {
            isEditing = true
            editingHikeId = argId
        }

        // Handle back button press with confirmation dialog
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackConfirmationDialog()
            }
        })

        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            ?.setNavigationOnClickListener { showBackConfirmationDialog() }

        // Difficulty dropdown
        val difficulties = resources.getStringArray(R.array.difficulty_levels).toList()
        val adapter = NoFilterArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, difficulties)
        binding.ddDifficulty.setAdapter(adapter)
        binding.ddDifficulty.setOnItemClickListener { _, _, _, _ -> updateContinueEnabled() }

        // Date picker
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
        binding.etDate.setOnClickListener { showDatePicker() }

        // Live validation - enable on any change when editing
        binding.etName.addTextChangedListener { updateContinueEnabled() }
        binding.etLocation.addTextChangedListener { updateContinueEnabled() }
        binding.etDate.addTextChangedListener { updateContinueEnabled() }
        binding.etLength.addTextChangedListener { updateContinueEnabled() }
        binding.etDescription.addTextChangedListener { updateContinueEnabled() }
        binding.sliderElevation.addOnChangeListener { _, _, _ -> updateContinueEnabled() }
        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ -> updateContinueEnabled() }
        binding.switchParking.setOnCheckedChangeListener { _, _ -> updateContinueEnabled() }

        // Photo buttons
        binding.btnPickPhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }
        binding.btnClearPhoto.setOnClickListener {
            selectedPhotoUri = null
            binding.ivPhoto.setImageResource(R.drawable.ic_launcher_foreground)
            updateContinueEnabled()
        }

        // Use current location
        binding.btnUseLocation.setOnClickListener {
            val hasPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) fetchAndFillLocation() else requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Pick on map
        binding.btnPickOnMap.setOnClickListener {
            // Save the current form state as a draft before navigating
            saveCurrentFormState()
            findNavController().navigate(R.id.action_hikeForm_to_mapPicker)
        }

        // Load hike data - priority: draft from confirm > current draft > original hike
        if (savedInstanceState == null) {
            // Check if we have a draft from confirm fragment first
            val returnedDraft = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable("returnedDraft", HikeDraft::class.java)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable("returnedDraft")
            }

            if (returnedDraft != null) {
                // User clicked "Continue Editing" from confirm screen - restore their draft
                currentDraft = returnedDraft
                fillFormFromDraft(returnedDraft)
            } else if (currentDraft != null) {
                // Restore from saved draft (when returning from map picker)
                fillFormFromDraft(currentDraft!!)
            } else if (isEditing) {
                // Load original hike data only on initial creation
                hikeVm.getById(argId) { hike ->
                    hike?.let {
                        fillForm(it)
                        selectedLat = it.latitude
                        selectedLng = it.longitude
                    }
                }
            }
        } else {
            // Restore from saved draft if available
            currentDraft?.let { fillFormFromDraft(it) }
        }

        binding.btnContinue.setOnClickListener {
            val draft = validateAndBuildDraft() ?: return@setOnClickListener
            val hikeDraft = HikeDraft(
                id = editingHikeId,
                name = draft.name,
                location = draft.location,
                date = draft.date,
                parkingAvailable = draft.parkingAvailable,
                lengthKm = draft.lengthKm,
                difficulty = draft.difficulty,
                description = draft.description,
                elevationGainM = draft.elevationGainM,
                rating = draft.rating,
                photoUri = draft.photoUri,
                latitude = selectedLat,
                longitude = selectedLng
            )
            val args = Bundle().apply { putParcelable("draft", hikeDraft) }
            findNavController().navigate(R.id.action_hikeForm_to_hikeConfirm, args)
        }

        setupMapResultListener()

        updateContinueEnabled()
    }

    private fun saveCurrentFormState() {
        // Save the current form state before navigating away
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val location = binding.etLocation.text?.toString()?.trim() ?: ""
        val date = binding.etDate.text?.toString()?.trim() ?: ""
        val lengthStr = binding.etLength.text?.toString()?.trim() ?: ""
        val lengthKm = lengthStr.toDoubleOrNull() ?: 0.0
        val difficulty = binding.ddDifficulty.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim()
        val parkingAvailable = binding.switchParking.isChecked
        val elevationGainM = binding.sliderElevation.value.toInt().takeIf { it > 0 }
        val rating = binding.ratingBar.rating.takeIf { it > 0 }

        currentDraft = HikeDraft(
            id = editingHikeId,
            name = name,
            location = location,
            date = date,
            parkingAvailable = parkingAvailable,
            lengthKm = lengthKm,
            difficulty = difficulty,
            description = description?.ifBlank { null },
            elevationGainM = elevationGainM,
            rating = rating,
            photoUri = selectedPhotoUri?.toString(),
            latitude = selectedLat,
            longitude = selectedLng
        )
    }

    private fun showBackConfirmationDialog() {
        // Check if there are any changes
        val hasChanges = hasFormChanges()

        if (!hasChanges) {
            // No changes, go back to hike list directly instead of just one step
            val popped = findNavController().popBackStack(R.id.hikeListFragment, false)
            if (!popped) {
                // Fallback in case the destination is not on the back stack
                findNavController().navigate(R.id.hikeListFragment)
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
                // Always return to hike list, skipping confirm screen beneath
                val popped = findNavController().popBackStack(R.id.hikeListFragment, false)
                if (!popped) {
                    findNavController().navigate(R.id.hikeListFragment)
                }
            }
            .setNeutralButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun hasFormChanges(): Boolean {
        val name = binding.etName.text?.toString()?.trim()
        val location = binding.etLocation.text?.toString()?.trim()
        val date = binding.etDate.text?.toString()?.trim()
        val length = binding.etLength.text?.toString()?.trim()
        val difficulty = binding.ddDifficulty.text?.toString()?.trim()
        val description = binding.etDescription.text?.toString()?.trim()

        // If any field has content, consider it as having changes
        return !name.isNullOrBlank() || !location.isNullOrBlank() || !date.isNullOrBlank() ||
                !length.isNullOrBlank() || !difficulty.isNullOrBlank() || !description.isNullOrBlank() ||
                selectedPhotoUri != null
    }

        // for both creating and editing
    private fun setupMapResultListener() {
        setFragmentResultListener("mapPick") { _, bundle ->
            val address = bundle.getString("address")
            val latitude = bundle.getDouble("lat")
            val longitude = bundle.getDouble("lng")
            val fallbackLocation = String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude)
            val locationText = address ?: fallbackLocation
            // Restore the draft first (if exists), then update location

            currentDraft?.let { draft ->
                fillFormFromDraft(draft)
            }
            // Force update the location field and coordinates with new selection

            view?.post {
                selectedLat = latitude
                selectedLng = longitude
                binding.etLocation.setText(locationText)
                binding.etLocation.clearFocus()
                // Update the draft with new location

                saveCurrentFormState()

                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_location_updated, locationText),
                    Toast.LENGTH_SHORT
                ).show()
                updateContinueEnabled()
            }
        }
    }

    private fun showPhotoOptionsDialog() {
        photoPickerHelper.showPhotoOptionsDialog()
    }

    private fun handlePhotoResult(uri: Uri) {
        selectedPhotoUri = uri
        binding.ivPhoto.setImageURI(uri)
        updateContinueEnabled()
    }

    private fun fetchAndFillLocation() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        Toast.makeText(requireContext(), getString(R.string.current_location_loading), Toast.LENGTH_SHORT).show()

        val client = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireContext())
        val cts = com.google.android.gms.tasks.CancellationTokenSource()
        try {
            client.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        selectedLat = loc.latitude
                        selectedLng = loc.longitude
                        viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            val address = reverseGeocode(loc.latitude, loc.longitude)
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                binding.etLocation.setText(address)
                                updateContinueEnabled()
                            }
                        }
                    } else {
                        try {
                            if (!PermissionHelper.hasLocationPermission(this)) return@addOnSuccessListener
                            client.lastLocation
                                .addOnSuccessListener { last ->
                                    if (last != null) {
                                        selectedLat = last.latitude
                                        selectedLng = last.longitude
                                        viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            val address = reverseGeocode(last.latitude, last.longitude)
                                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                binding.etLocation.setText(address)
                                                updateContinueEnabled()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), getString(R.string.toast_location_unavailable), Toast.LENGTH_SHORT).show()
                                }
                        } catch (_: SecurityException) {
                            Toast.makeText(requireContext(), getString(R.string.toast_location_permission_denied), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.toast_location_unavailable), Toast.LENGTH_SHORT).show()
                }
        } catch (_: SecurityException) {
            Toast.makeText(requireContext(), getString(R.string.toast_location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String {
        if (!Geocoder.isPresent()) {
            return createLocationString(lat, lng)
        }

        return try {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val address = formatAddress(addresses.firstOrNull())
                            val result = address.ifEmpty { createLocationString(lat, lng) }
                            if (cont.isActive) cont.resume(result)
                        }
                        override fun onError(@Suppress("UNUSED_PARAMETER") errorMessage: String?) {
                            if (cont.isActive) cont.resume(createLocationString(lat, lng))
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(requireContext(), Locale.getDefault()).getFromLocation(lat, lng, 1)
                val address = addresses?.firstOrNull()
                val result = formatAddress(address)
                result.ifEmpty { createLocationString(lat, lng) }
            }
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            createLocationString(lat, lng)
        }
    }

    private fun formatAddress(addr: Address?): String {
        if (addr == null) return ""

        val parts = mutableListOf<String>()
        addr.featureName?.let { if (it != addr.thoroughfare) parts.add(it) }
        addr.thoroughfare?.let { parts.add(it) }
        addr.subLocality?.let { parts.add(it) }
        addr.locality?.let { parts.add(it) }
        addr.adminArea?.let { parts.add(it) }
        addr.countryName?.let { parts.add(it) }

        return if (parts.isNotEmpty()) {
            parts.distinct().joinToString(", ")
        } else {
            ""
        }
    }

    private fun createLocationString(lat: Double, lng: Double): String {
        return String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val picker = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val mm = String.format(Locale.getDefault(), "%02d", m + 1)
                val dd = String.format(Locale.getDefault(), "%02d", d)
                val dateString = String.format(Locale.getDefault(), "%d-%s-%s", y, mm, dd)
                binding.etDate.setText(dateString)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    private fun updateContinueEnabled() {
        if (isEditing) {
            binding.btnContinue.isEnabled = true
        } else {
            val hasName = binding.etName.text?.isNotBlank() == true
            val hasLocation = binding.etLocation.text?.isNotBlank() == true
            val hasDate = binding.etDate.text?.isNotBlank() == true
            val hasLength = binding.etLength.text?.isNotBlank() == true
            val hasDifficulty = binding.ddDifficulty.text?.isNotBlank() == true

            binding.btnContinue.isEnabled = hasName && hasLocation && hasDate && hasLength && hasDifficulty
        }
    }

    private fun validateAndBuildDraft(): Hike? {
        val name = binding.etName.text?.toString()?.trim()
        val location = binding.etLocation.text?.toString()?.trim()
        val date = binding.etDate.text?.toString()?.trim()
        val lengthStr = binding.etLength.text?.toString()?.trim()
        val difficulty = binding.ddDifficulty.text?.toString()?.trim()
        val description = binding.etDescription.text?.toString()?.trim()

        if (name.isNullOrBlank() || location.isNullOrBlank() || date.isNullOrBlank() ||
            lengthStr.isNullOrBlank() || difficulty.isNullOrBlank()) {
            Toast.makeText(requireContext(), getString(R.string.toast_fill_required_fields), Toast.LENGTH_SHORT).show()
            return null
        }

        val lengthKm = lengthStr.toDoubleOrNull()
        if (lengthKm == null || lengthKm <= 0) {
            Toast.makeText(requireContext(), getString(R.string.toast_valid_length), Toast.LENGTH_SHORT).show()
            return null
        }

        if (lengthKm >= 1000) {
            Toast.makeText(requireContext(), getString(R.string.toast_length_max_exceeded), Toast.LENGTH_SHORT).show()
            return null
        }

        val parkingAvailable = binding.switchParking.isChecked
        val elevationGainM = binding.sliderElevation.value.toInt().takeIf { it > 0 }
        val rating = binding.ratingBar.rating.takeIf { it > 0 }

        return Hike(
            id = editingHikeId ?: 0,
            name = name,
            location = location,
            date = date,
            parkingAvailable = parkingAvailable,
            lengthKm = lengthKm,
            difficulty = difficulty,
            description = description?.ifBlank { null },
            elevationGainM = elevationGainM,
            rating = rating,
            photoUri = selectedPhotoUri?.toString(),
            latitude = selectedLat,
            longitude = selectedLng
        )
    }

    private fun fillFormFromDraft(draft: HikeDraft) {
        binding.etName.setText(draft.name)
        binding.etLocation.setText(draft.location)
        binding.etDate.setText(draft.date)
        binding.switchParking.isChecked = draft.parkingAvailable
        binding.etLength.setText(draft.lengthKm.toString())
        binding.ddDifficulty.setText(draft.difficulty, false)
        binding.etDescription.setText(draft.description ?: "")
        binding.sliderElevation.value = draft.elevationGainM?.toFloat() ?: 0f
        binding.ratingBar.rating = draft.rating ?: 0f

        selectedLat = draft.latitude
        selectedLng = draft.longitude

        draft.photoUri?.let {
            try {
                selectedPhotoUri = it.toUri()
                binding.ivPhoto.setImageURI(selectedPhotoUri)
            } catch (_: Exception) {
                binding.ivPhoto.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    private fun fillForm(hike: Hike) {
        binding.etName.setText(hike.name)
        binding.etLocation.setText(hike.location)
        binding.etDate.setText(hike.date)
        binding.switchParking.isChecked = hike.parkingAvailable
        binding.etLength.setText(hike.lengthKm.toString())
        binding.ddDifficulty.setText(hike.difficulty, false)
        binding.etDescription.setText(hike.description ?: "")
        binding.sliderElevation.value = hike.elevationGainM?.toFloat() ?: 0f
        binding.ratingBar.rating = hike.rating ?: 0f

        hike.photoUri?.let {
            try {
                selectedPhotoUri = it.toUri()
                binding.ivPhoto.setImageURI(selectedPhotoUri)
            } catch (_: Exception) {
                binding.ivPhoto.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
