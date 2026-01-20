package com.example.hikermanagementapp.ui.hike

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.databinding.FragmentHikeListBinding
import com.example.hikermanagementapp.ui.observation.ObservationViewModel
import com.example.hikermanagementapp.util.ImportExportManager
import com.example.hikermanagementapp.util.SearchStateManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

class HikeListFragment : Fragment() {
    private var _binding: FragmentHikeListBinding? = null
    private val binding get() = _binding!!

    private val hikeVm: HikeViewModel by viewModels()
    private val obsVm: ObservationViewModel by viewModels()

    private lateinit var adapter: TagHikeAdapter

    // Helpers for state management and import/export
    private val searchStateManager = SearchStateManager()
    private lateinit var importExportManager: ImportExportManager

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { handleImportUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHikeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize helper
        importExportManager = ImportExportManager(requireContext())

        setupMenu()
        setupRecyclerView()
        setupSearchBar()
        setupFilterChip()
        setupFab()
        setupAdvancedSearchListener()
        observeHikes()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_hike_list, menu)
                menuInflater.inflate(R.menu.menu_hike_list_extra, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_filters -> {
                    AdvancedSearchBottomSheet().show(parentFragmentManager, "advancedSearch")
                    true
                }
                R.id.action_reset_db -> {
                    confirmResetDatabase()
                    true
                }
                R.id.action_import_json -> {
                    showImportChoiceDialog()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = TagHikeAdapter(
            onClick = { hike ->
                val action = R.id.action_hikeList_to_hikeDetail
                val args = Bundle().apply { putLong("hikeId", hike.id) }
                findNavController().navigate(action, args)
            },
            onEdit = { hike ->
                val action = R.id.action_hikeList_to_hikeForm
                val args = Bundle().apply { putLong("hikeId", hike.id) }
                findNavController().navigate(action, args)
            },
            onDelete = { hike -> confirmDeleteHike(hike) }
        )
        binding.rvHikes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHikes.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener { text ->
            // Only allow search if advanced filter is not active
            if (searchStateManager.isFilterActive()) {
                return@addTextChangedListener
            }

            val query = text?.toString()?.trim().orEmpty()
            searchStateManager.setSearchQuery(query)
            performSearch(query)
        }
    }

    private fun setupFilterChip() {
        binding.chipFilterActive.setOnCloseIconClickListener {
            clearAdvancedFilter()
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_hikeList_to_hikeForm)
        }
    }

    private fun setupAdvancedSearchListener() {
        setFragmentResultListener("advancedSearch") { _, result ->
            if (result.getBoolean("clear", false)) {
                clearAdvancedFilter()
                return@setFragmentResultListener
            }

            searchStateManager.setFilterParams(result)
            applyAdvancedFilter(result, showToast = true)
        }
    }

    private fun observeHikes() {
        hikeVm.hikes.observe(viewLifecycleOwner) { allHikes ->
            // When base data changes, reapply the current filter/search
            // When base data changes, reapply the current filter/search
            when {
                    // Reapply advanced filter
                searchStateManager.isFilterActive() && searchStateManager.getFilterParams() != null -> {
                    // Reapply advanced filter
                    applyAdvancedFilter(searchStateManager.getFilterParams()!!, showToast = false)
                    // Reapply search
                }
                searchStateManager.hasActiveSearch() -> {
                    // Reapply search
                    // Show all hikes
                    performSearch(searchStateManager.getSearchQuery())
                }
                else -> {
                    // Show all hikes
                    adapter.submitList(allHikes)
                    binding.tvEmpty.isVisible = allHikes.isEmpty()
                }
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            // Reset to show all hikes from the base observable
            hikeVm.hikes.value?.let { allHikes ->
                adapter.submitList(allHikes)
                binding.tvEmpty.isVisible = allHikes.isEmpty()
            }
        } else {
            // Perform search and observe results
            hikeVm.searchByNameContains(query).observe(viewLifecycleOwner) { searchResults ->
                adapter.submitList(searchResults)
                binding.tvEmpty.isVisible = searchResults.isEmpty()
            }
        }
    }

    private fun applyAdvancedFilter(result: Bundle, showToast: Boolean) {
        val criteria = searchStateManager.parseFilterParams(result)

        // Hide search bar and show filter chip
        binding.tilSearch.isVisible = false
        binding.chipFilterActive.isVisible = true

        val ld = hikeVm.advancedSearch(
            criteria.name,
            criteria.location,
            criteria.minLen,
            criteria.maxLen,
            criteria.date,
            criteria.difficulty,
            criteria.parking
        )
        ld.observe(viewLifecycleOwner) { list ->
            val sorted = when (criteria.sortBy) {
                "name" -> list.sortedBy { it.name.lowercase() }
                "length" -> list.sortedBy { it.lengthKm }
                else -> list.sortedByDescending { it.date }
            }
            adapter.submitList(sorted)
            binding.tvEmpty.isVisible = sorted.isEmpty()

            if (showToast) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_filters_applied, sorted.size),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearAdvancedFilter() {
        searchStateManager.clearAll()
        // Reset to base hike list
        binding.tilSearch.isVisible = true
        binding.chipFilterActive.isVisible = false
        binding.etSearch.setText("")

        // Reset to base hike list
        hikeVm.hikes.value?.let { allHikes ->
            adapter.submitList(allHikes)
            binding.tvEmpty.isVisible = allHikes.isEmpty()
        }
    }

    private fun showImportChoiceDialog() {
        val options = arrayOf(
            getString(R.string.menu_import_json),
            getString(R.string.menu_import_json_paste)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_import_json)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> startImportPicker()
                    1 -> showPasteImportDialog()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showPasteImportDialog() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        val input = EditText(requireContext()).apply {
            minLines = 6
            maxLines = 15
            hint = getString(R.string.dialog_import_hint)
            isSingleLine = false
            setHorizontallyScrolling(false)
        }

        val scrollView = ScrollView(requireContext()).apply {
            val maxHeight = (300 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = maxHeight
            }
            addView(input)
        }

        container.addView(scrollView)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_import_title)
            .setMessage(R.string.dialog_import_message)
            .setView(container)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.menu_import_json) { _, _ ->
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.isBlank()) {
                    Toast.makeText(requireContext(), getString(R.string.toast_nothing_to_import), Toast.LENGTH_SHORT).show()
                } else {
                    importFromJson(text)
                }
            }
            .show()
    }

    private fun startImportPicker() {
        try {
            importJsonLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.toast_unable_to_open_picker, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImportUri(uri: Uri) {
        try {
            val cr = requireContext().contentResolver
            cr.openInputStream(uri)?.use { input ->
                val text = BufferedReader(InputStreamReader(input)).readText()
                importFromJson(text)
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.toast_failed_to_read_file), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.toast_import_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun importFromJson(text: String) {
        val parsedData = importExportManager.parseImportJson(text)

        if (parsedData == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_invalid_json, "Invalid format"),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        hikeVm.checkDuplicate(
            parsedData.name,
            parsedData.location,
            parsedData.date,
            parsedData.lengthKm,
            parsedData.difficulty,
            parsedData.parkingAvailable,
            parsedData.elevationGainM,
            parsedData.latitude,
            parsedData.longitude
        ) { isDuplicate ->
            if (isDuplicate) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_import_duplicate),
                    Toast.LENGTH_LONG
                ).show()
                return@checkDuplicate
            }

            val hike = importExportManager.toHikeEntity(parsedData)

            hikeVm.insert(hike) { newId ->
                // Insert observations
                for (obsData in parsedData.observations) {
                    val obs = importExportManager.toObservationEntity(obsData, newId)
                    obsVm.insert(obs)
                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_import_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmDeleteHike(hike: Hike) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_delete_hike_title))
            .setMessage(getString(R.string.dialog_delete_hike_message))
            .setNegativeButton(getString(R.string.action_cancel), null)
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                hikeVm.delete(hike) {
                    Toast.makeText(requireContext(), getString(R.string.toast_hike_deleted), Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun confirmResetDatabase() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_reset_db_title))
            .setMessage(getString(R.string.dialog_reset_db_message))
            .setNegativeButton(getString(R.string.action_cancel), null)
            .setPositiveButton(getString(R.string.action_reset)) { _, _ ->
                obsVm.deleteAll {}
                hikeVm.deleteAll {
                    Toast.makeText(requireContext(), getString(R.string.toast_database_reset), Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
