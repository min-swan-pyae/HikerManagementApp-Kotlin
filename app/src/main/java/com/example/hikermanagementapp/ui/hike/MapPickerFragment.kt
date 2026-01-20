package com.example.hikermanagementapp.ui.hike

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.hikermanagementapp.R
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import kotlin.coroutines.resume

class MapPickerFragment : Fragment() {
    private var mapView: MapView? = null
    private var marker: Marker? = null
    private var selectedLocation: GeoPoint? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            requireContext(),
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        Configuration.getInstance().userAgentValue = "HikerManagementApp/1.0"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_picker, container, false)

        mapView = view.findViewById(R.id.mapContainer)
        setupMap()

        view.findViewById<MaterialButton>(R.id.btnConfirmPick).setOnClickListener {
            confirmPick()
        }

        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearchLocation)
        val btnSearch = view.findViewById<MaterialButton>(R.id.btnSearch)

        btnSearch.setOnClickListener {
            val query = etSearch.text?.toString()?.trim()
            if (!query.isNullOrBlank()) {
                searchLocation(query)
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_enter_location_search), Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text?.toString()?.trim()
                if (!query.isNullOrBlank()) {
                    searchLocation(query)
                }
                true
            } else {
                false
            }
        }

        return view
    }

    private fun setupMap() {
        mapView?.let { map ->
            try {
                map.setTileSource(TileSourceFactory.MAPNIK)
            } catch (_: Exception) {
                map.setTileSource(TileSourceFactory.OpenTopo)
            }

            map.setMultiTouchControls(true)

            val mapController: IMapController = map.controller
            mapController.setZoom(15.0)

            val startPoint = GeoPoint(37.4220936, -122.083922)
            mapController.setCenter(startPoint)

            setupLocationOverlay(map)
            setupMapClickEvents(map)
            enableMyLocationIfPermitted()
        }
    }

    private fun setupLocationOverlay(map: MapView) {
        val ctx = requireContext()
        val locationProvider = GpsMyLocationProvider(ctx)
        myLocationOverlay = MyLocationNewOverlay(locationProvider, map).apply {
            enableMyLocation()
            enableFollowLocation()
        }
        map.overlays.add(myLocationOverlay)
    }

    private fun setupMapClickEvents(map: MapView) {
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { location ->
                    selectedLocation = location
                    updateMarker(location)
                    val message = getString(
                        R.string.map_location_selected,
                        String.format(Locale.getDefault(), "%.6f", location.latitude),
                        String.format(Locale.getDefault(), "%.6f", location.longitude)
                    )
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun longPressHelper(@Suppress("UNUSED_PARAMETER") p: GeoPoint?): Boolean {
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(mapEventsOverlay)
    }

    private fun updateMarker(location: GeoPoint) {
        mapView?.let { map ->
            marker?.let { map.overlays.remove(it) }

            marker = Marker(map).apply {
                position = location
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected Location"
                snippet = String.format(Locale.getDefault(), "%.6f, %.6f", location.latitude, location.longitude)
            }
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    private fun searchLocation(query: String) {
        if (!Geocoder.isPresent()) {
            Toast.makeText(requireContext(), getString(R.string.toast_geocoder_unavailable), Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), getString(R.string.toast_searching), Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    geocodeLocation(query)
                }

                if (result != null) {
                    mapView?.controller?.animateTo(result)
                    mapView?.controller?.setZoom(15.0)
                    selectedLocation = result
                    updateMarker(result)
                    Toast.makeText(requireContext(), getString(R.string.toast_location_found), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.toast_location_not_found), Toast.LENGTH_SHORT).show()
                }
            } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_error_searching), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun geocodeLocation(query: String): GeoPoint? {
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                cont.resume(GeoPoint(addr.latitude, addr.longitude))
                            } else {
                                cont.resume(null)
                            }
                        }
                        override fun onError(@Suppress("UNUSED_PARAMETER") errorMessage: String?) {
                            cont.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(requireContext(), Locale.getDefault()).getFromLocationName(query, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    GeoPoint(addr.latitude, addr.longitude)
                } else {
                    null
                }
            }
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            null
        }
    }

    private fun enableMyLocationIfPermitted() {
        val ctx = requireContext()
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(ctx)
                client.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        mapView?.controller?.animateTo(geoPoint)
                        selectedLocation = geoPoint
                        updateMarker(geoPoint)
                        Toast.makeText(ctx, getString(R.string.map_found_location), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, getString(R.string.map_unable_location), Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(ctx, getString(R.string.map_location_unavailable), Toast.LENGTH_LONG).show()
                }
            } catch (_: SecurityException) {
                Toast.makeText(ctx, getString(R.string.map_permission_required), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(ctx, getString(R.string.map_tap_to_select), Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmPick() {
        val location = selectedLocation
        if (location == null) {
            Toast.makeText(requireContext(), getString(R.string.map_select_first), Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), getString(R.string.current_location_loading), Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val address = withContext(Dispatchers.IO) {
                    reverseGeocode(location)
                }

                val displayAddress = address ?: run {
                    val lat = String.format(Locale.getDefault(), "%.6f", location.latitude)
                    val lng = String.format(Locale.getDefault(), "%.6f", location.longitude)
                    getString(R.string.map_location_format, lat, lng)
                }

                setFragmentResult("mapPick", Bundle().apply {
                    putDouble("lat", location.latitude)
                    putDouble("lng", location.longitude)
                    putString("address", displayAddress)
                })
                parentFragmentManager.popBackStack()
            } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun reverseGeocode(geoPoint: GeoPoint): String? {
        return try {
            if (!Geocoder.isPresent()) {
                return null
            }

            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resume(formatAddress(addresses.firstOrNull()))
                        }
                        override fun onError(@Suppress("UNUSED_PARAMETER") errorMessage: String?) {
                            cont.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val list = Geocoder(requireContext(), Locale.getDefault()).getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                formatAddress(list?.firstOrNull())
            }
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            null
        }
    }

    private fun formatAddress(address: Address?): String? {
        return address?.let {
            listOfNotNull(
                it.featureName,
                it.thoroughfare,
                it.subLocality,
                it.locality,
                it.adminArea,
                it.countryName
            ).joinToString(", ").takeIf { str -> str.isNotBlank() }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myLocationOverlay?.disableMyLocation()
        mapView = null
        marker = null
        myLocationOverlay = null
    }
}

