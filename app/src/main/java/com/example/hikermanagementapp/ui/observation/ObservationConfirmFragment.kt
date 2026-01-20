package com.example.hikermanagementapp.ui.observation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Observation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class ObservationConfirmFragment : Fragment() {
    private val obsVm: ObservationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_observation_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val hikeId = args.getLong("hikeId")
        val observationId = args.getLong("observationId", -1L)
        val observationText = args.getString("observation") ?: ""
        val timestamp = args.getLong("timestamp")
        val comments = args.getString("comments")
        val photoUri = args.getString("photoUri")

        // Handle back button press with confirmation dialog
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardConfirmationDialog(observationId, hikeId, observationText, timestamp, comments, photoUri)
            }
        })

        val ivPhoto = view.findViewById<ImageView>(R.id.ivObservationPhoto)
        val tvSummary = view.findViewById<TextView>(R.id.tvObsSummary)
        val btnEdit = view.findViewById<Button>(R.id.btnObsEdit)
        val btnSave = view.findViewById<Button>(R.id.btnObsSave)

        // Display photo if available
        photoUri?.let { uriString ->
            try {
                val uri = uriString.toUri()
                ivPhoto.setImageURI(uri)
                ivPhoto.visibility = View.VISIBLE
            } catch (_: Exception) {
                ivPhoto.visibility = View.GONE
            }
        } ?: run {
            ivPhoto.visibility = View.GONE
        }

        // Display summary
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateTimeStr = fmt.format(timestamp)

        val summary = buildString {
            appendLine("${getString(R.string.label_observation)}: $observationText")
            appendLine("${getString(R.string.label_date_time)}: $dateTimeStr")
            comments?.let { appendLine("${getString(R.string.label_comments)}: $it") }
        }
        tvSummary.text = summary

        btnEdit.setOnClickListener {
            showDiscardConfirmationDialog(observationId, hikeId, observationText, timestamp, comments, photoUri)
        }

        btnSave.setOnClickListener {
            val obs = Observation(
                id = if (observationId == -1L) 0 else observationId,
                hikeId = hikeId,
                observation = observationText,
                timestamp = timestamp,
                comments = comments,
                photoUri = photoUri
            )

            if (observationId == -1L) {
                obsVm.insert(obs) {
                    Toast.makeText(requireContext(), getString(R.string.toast_observation_saved), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.hikeDetailFragment, false)
                }
            } else {
                obsVm.update(obs) {
                    Toast.makeText(requireContext(), getString(R.string.toast_observation_updated), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.hikeDetailFragment, false)
                }
            }
        }
    }

    private fun showDiscardConfirmationDialog(
        observationId: Long,
        hikeId: Long,
        observationText: String,
        timestamp: Long,
        comments: String?,
        photoUri: String?
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_unsaved_changes_title))
            .setMessage(getString(R.string.dialog_unsaved_changes_message))
            .setPositiveButton(getString(R.string.btn_continue_editing)) { _, _ ->
                // Return to form with data preserved
                val args = Bundle().apply {
                    putLong("hikeId", hikeId)
                    if (observationId != -1L) putLong("observationId", observationId)
                    putString("observation", observationText)
                    putLong("timestamp", timestamp)
                    putString("comments", comments)
                    putString("photoUri", photoUri)
                }
                findNavController().navigate(R.id.action_observationConfirm_to_observationForm, args)
            }
            .setNegativeButton(getString(R.string.btn_discard)) { _, _ ->
                // Discard changes and go back to hike detail
                findNavController().popBackStack(R.id.hikeDetailFragment, false)
            }
            .setNeutralButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
