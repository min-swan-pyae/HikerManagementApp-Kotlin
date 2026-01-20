package com.example.hikermanagementapp.ui.hike

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.databinding.FragmentHikeConfirmBinding
import com.example.hikermanagementapp.util.Reminders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class HikeConfirmFragment : Fragment() {
    private var _binding: FragmentHikeConfirmBinding? = null
    private val binding get() = _binding!!

    private val hikeVm: HikeViewModel by viewModels()

    private lateinit var draft: HikeDraft

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHikeConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        draft = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("draft", HikeDraft::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable("draft")!!
        }

        // Handle back button press with confirmation dialog
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardConfirmationDialog()
            }
        })

        // Ensure toolbar up button shows the same dialog
        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            ?.setNavigationOnClickListener { showDiscardConfirmationDialog() }

        // Display photo if available with accessible description
        draft.photoUri?.let { uriString ->
            try {
                val uri = uriString.toUri()
                binding.ivHikePhoto.setImageURI(uri)
                binding.ivHikePhoto.contentDescription = getString(R.string.cd_hike_photo, draft.name)
                binding.ivHikePhoto.visibility = View.VISIBLE
            } catch (_: Exception) {
                binding.ivHikePhoto.visibility = View.GONE
            }
        } ?: run {
            binding.ivHikePhoto.visibility = View.GONE
        }

        val ctx = requireContext()
        val parkingWord = if (draft.parkingAvailable) ctx.getString(R.string.value_available) else ctx.getString(R.string.value_unavailable)
        val parkingLabel = ctx.getString(R.string.label_parking) + ": " + parkingWord
        val lengthLabel = if (draft.lengthKm % 1.0 == 0.0) ctx.getString(R.string.chip_length_int, draft.lengthKm.toInt()) else ctx.getString(R.string.chip_length_float, draft.lengthKm)
        val desc = buildString {
            appendLine("${ctx.getString(R.string.label_name)}: ${draft.name}")
            appendLine("${ctx.getString(R.string.label_location)}: ${draft.location}")
            appendLine("${ctx.getString(R.string.label_date)}: ${draft.date}")
            appendLine(parkingLabel)
            appendLine("${ctx.getString(R.string.label_length)}: $lengthLabel")
            appendLine("${ctx.getString(R.string.label_difficulty)}: ${draft.difficulty}")
            draft.description?.let { appendLine("${ctx.getString(R.string.label_description)}: $it") }
            draft.elevationGainM?.let { val m = ctx.getString(R.string.value_meters, it); appendLine("${ctx.getString(R.string.label_elevation_gain)}: $m") }
            draft.rating?.takeIf { it > 0f }?.let { r ->
                val rs = if (r % 1f == 0f) r.toInt().toString() else String.format(Locale.getDefault(), "%.1f", r)
                appendLine("${ctx.getString(R.string.label_rating)}: $rs/5")
            }
        }
        binding.tvSummary.text = desc

        binding.btnEdit.setOnClickListener {
            showDiscardConfirmationDialog()
        }

        binding.btnSave.setOnClickListener { saveDraft() }
    }

    private fun showDiscardConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_unsaved_changes_title))
            .setMessage(getString(R.string.dialog_unsaved_changes_message))
            .setPositiveButton(getString(R.string.btn_continue_editing)) { _, _ ->
                // Return to form with draft preserved
                val args = Bundle().apply {
                    putParcelable("returnedDraft", draft)
                    draft.id?.let { putLong("hikeId", it) }
                }
                findNavController().navigate(R.id.action_hikeConfirm_to_hikeForm, args)
            }
            .setNegativeButton(getString(R.string.btn_discard)) { _, _ ->
                // Discard changes and go back to list, clearing the entire back stack to this destination
                findNavController().popBackStack(R.id.hikeListFragment, false)
            }
            .setNeutralButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun saveDraft() {
        // Preserve addedToCalendar status from existing hike if editing
        val editingId = draft.id ?: 0L
        if (editingId > 0L) {
            hikeVm.getById(editingId) { existing ->
                val hike = Hike(
                    id = draft.id ?: 0,
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
                    latitude = draft.latitude,
                    longitude = draft.longitude,
                    addedToCalendar = false
                )
                updateHike(hike)
            }
        } else {
            val hike = Hike(
                id = 0,
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
                latitude = draft.latitude,
                longitude = draft.longitude,
                addedToCalendar = false
            )
            insertHike(hike)
        }
    }

    private fun insertHike(hike: Hike) {
        hikeVm.insert(hike) { newId ->
            Reminders.scheduleReminder(requireContext(), newId, hike.name, hike.date)
            Toast.makeText(requireContext(), R.string.toast_hike_saved, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.hikeListFragment, false)
        }
    }

    private fun updateHike(hike: Hike) {
        hikeVm.update(hike) {
            Reminders.scheduleReminder(requireContext(), hike.id, hike.name, hike.date)
            Toast.makeText(requireContext(), R.string.toast_hike_updated, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.hikeListFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
