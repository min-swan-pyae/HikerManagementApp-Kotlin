package com.example.hikermanagementapp.ui.hike

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.data.Observation
import com.example.hikermanagementapp.databinding.FragmentHikeDetailBinding
import com.example.hikermanagementapp.ui.observation.ObservationAdapter
import com.example.hikermanagementapp.ui.observation.ObservationViewModel
import com.example.hikermanagementapp.util.CalendarHelper
import com.example.hikermanagementapp.util.ImportExportManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class HikeDetailFragment : Fragment() {
    private var _binding: FragmentHikeDetailBinding? = null
    private val binding get() = _binding!!

    private val hikeVm: HikeViewModel by viewModels()
    private val obsVm: ObservationViewModel by viewModels()

    private var hikeId: Long = -1
    private lateinit var obsAdapter: ObservationAdapter
    private var currentHike: Hike? = null
    private var currentObservations: List<Observation> = emptyList()
    private var mapPreview: MapView? = null
    private var previewMarker: Marker? = null
    private var addCalMenuItem: MenuItem? = null

    // Helper for import/export operations
    private lateinit var importExportManager: ImportExportManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHikeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hikeId = requireArguments().getLong("hikeId")

        // Initialize helper
        importExportManager = ImportExportManager(requireContext())

        mapPreview = binding.mapPreview

        obsAdapter = ObservationAdapter(
            onEdit = { obs -> navigateToObservationForm(obs.id) },
            onDelete = { obs -> confirmDeleteObservation(obs) }
        )
        binding.rvObservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvObservations.adapter = obsAdapter

        hikeVm.getById(hikeId) { hike -> hike?.let { bindHike(it) } }

        obsVm.observeByHike(hikeId).observe(viewLifecycleOwner) { list ->
            currentObservations = list
            obsAdapter.submitList(list)
            binding.tvEmptyObs.isVisible = list.isNullOrEmpty()
        }

        binding.fabAddObs.setOnClickListener { navigateToObservationForm(-1) }

        // Add menu for calendar/export actions
        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_hike_detail, menu)
                addCalMenuItem = menu.findItem(R.id.action_add_to_calendar)
                // Update state in case hike already loaded
                currentHike?.let { h -> addCalMenuItem?.isEnabled = !h.addedToCalendar }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_add_to_calendar -> {
                    addToCalendar(); true
                }
                R.id.action_export_json -> {
                    exportAsJson(); true
                }
                else -> false
            }
        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)
    }

    private fun bindHike(h: Hike) {
        currentHike = h
        // Display photo if available
        h.photoUri?.let { uriString ->
            try {
                val uri = uriString.toUri()
                binding.ivHikePhoto.setImageURI(uri)
                binding.ivHikePhoto.contentDescription = getString(R.string.cd_hike_photo, h.name)
                binding.ivHikePhoto.visibility = View.VISIBLE
            } catch (_: Exception) {
                binding.ivHikePhoto.visibility = View.GONE
            }
        } ?: run {
            binding.ivHikePhoto.visibility = View.GONE
        }

        binding.tvTitle.text = h.name
        val cg = binding.cgDetailAttributes
        val ctx = requireContext()
        cg.removeAllViews()
        val longLocView = binding.tvLongLocation
        val isLongLocation = h.location.length > 40

        fun makeChip(label: String, cd: String): Chip {
            val themedCtx = ContextThemeWrapper(ctx, R.style.HikeAttributeChip)
            return Chip(themedCtx).apply {
                text = label
                contentDescription = cd
                isClickable = false
                isCheckable = false
            }
        }

        if (isLongLocation) {
            longLocView.visibility = View.VISIBLE
            longLocView.text = h.location
            longLocView.contentDescription = ctx.getString(R.string.cd_location, h.location)
        } else {
            longLocView.visibility = View.GONE
            cg.addView(makeChip(h.location, ctx.getString(R.string.cd_location, h.location)))
        }

        cg.addView(makeChip(h.date, ctx.getString(R.string.cd_date, h.date)))
        val lengthLabel = if (h.lengthKm % 1.0 == 0.0) ctx.getString(R.string.chip_length_int, h.lengthKm.toInt()) else ctx.getString(R.string.chip_length_float, h.lengthKm)
        val lengthCd = if (h.lengthKm % 1.0 == 0.0) ctx.resources.getQuantityString(R.plurals.cd_length_kilometers, h.lengthKm.toInt(), h.lengthKm.toInt()) else ctx.getString(R.string.chip_length_float, h.lengthKm)
        cg.addView(makeChip(lengthLabel, lengthCd))
        cg.addView(makeChip(ctx.getString(R.string.chip_difficulty, h.difficulty), ctx.getString(R.string.cd_difficulty, h.difficulty)))
        val parkingLabel = if (h.parkingAvailable) ctx.getString(R.string.chip_parking_available) else ctx.getString(R.string.chip_parking_unavailable)
        val parkingCd = if (h.parkingAvailable) ctx.getString(R.string.cd_parking_available) else ctx.getString(R.string.cd_parking_unavailable)
        cg.addView(makeChip(parkingLabel, parkingCd))

        h.elevationGainM?.takeIf { it > 0 }?.let {
            val elevCd = ctx.resources.getQuantityString(R.plurals.cd_elevation_gain_meters, it, it)
            cg.addView(makeChip(ctx.getString(R.string.chip_elevation_gain, it), elevCd))
        }

        h.rating?.takeIf { it > 0f }?.let { r ->
            val rs = if (r % 1f == 0f) r.toInt().toString() else String.format(Locale.getDefault(), "%.1f", r)
            cg.addView(makeChip(ctx.getString(R.string.chip_rating, rs), ctx.getString(R.string.cd_rating, rs)))
        }

        val desc = h.description?.trim()
        if (!desc.isNullOrEmpty()) {
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvDescription.text = desc
        } else {
            binding.tvDescription.visibility = View.GONE
        }

        // Setup map preview if coordinates available
        if (h.latitude != null && h.longitude != null) {
            initMapPreview(h.latitude, h.longitude, h.name)
        } else {
            mapPreview?.visibility = View.GONE
        }

        // Reflect calendar state on menu item
        addCalMenuItem?.isEnabled = !h.addedToCalendar
        addCalMenuItem?.title = if (h.addedToCalendar) getString(R.string.menu_added_to_calendar) else getString(R.string.menu_add_to_calendar)
    }

    private fun navigateToObservationForm(obsId: Long) {
        val args = Bundle().apply {
            putLong("hikeId", hikeId)
            if (obsId != -1L) putLong("observationId", obsId)
        }
        findNavController().navigate(R.id.action_hikeDetail_to_observationForm, args)
    }

    private fun confirmDeleteObservation(obs: Observation) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_observation_title)
            .setMessage(R.string.dialog_delete_observation_message)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                obsVm.delete(obs) {
                    Toast.makeText(requireContext(), R.string.toast_observation_deleted, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun addToCalendar() {
        val hike = currentHike ?: return
        if (hike.addedToCalendar) {
            Toast.makeText(requireContext(), R.string.toast_already_in_calendar, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = CalendarHelper.createCalendarIntent(hike)
        if (intent == null) {
            Toast.makeText(requireContext(), R.string.error_invalid_date, Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(intent)

        val updated = hike.copy(addedToCalendar = true)
        currentHike = updated
        addCalMenuItem?.isEnabled = false
        addCalMenuItem?.title = getString(R.string.menu_added_to_calendar)
        hikeVm.update(updated)
        Toast.makeText(requireContext(), R.string.toast_event_launched, Toast.LENGTH_SHORT).show()
    }

    private fun initMapPreview(lat: Double, lng: Double, title: String) {
        val map = mapPreview ?: return
        try { map.setTileSource(TileSourceFactory.MAPNIK) } catch (_: Exception) { }
        map.setMultiTouchControls(false)
        map.visibility = View.VISIBLE
        val controller: IMapController = map.controller
        val point = GeoPoint(lat, lng)
        controller.setZoom(14.0)
        controller.setCenter(point)
        // Marker
        previewMarker?.let { map.overlays.remove(it) }
        val marker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
        }
        previewMarker = marker
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun exportAsJson() {
        val hike = currentHike ?: return

        val fullText = importExportManager.createExportText(hike, currentObservations)

        ShareCompat.IntentBuilder(requireActivity())
            .setType("text/plain")
            .setSubject(getString(R.string.share_hike_subject, hike.name))
            .setChooserTitle(R.string.share_hike_title)
            .setText(fullText)
            .startChooser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mapPreview?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapPreview?.onPause()
    }
}
