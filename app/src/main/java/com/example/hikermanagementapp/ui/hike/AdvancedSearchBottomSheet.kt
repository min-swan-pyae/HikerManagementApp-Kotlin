package com.example.hikermanagementapp.ui.hike

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.chip.ChipGroup
import android.widget.RadioGroup
import com.example.hikermanagementapp.R
import java.util.Calendar

/**
 * FEATURE D: Search - Advanced Search Filter
 *
 * Search Criteria:
 * - Name: Partial text match (e.g., "Snow" finds "Snowdon")
 * - Location: Partial text match
 * - Length Range: Min/Max kilometers (using RangeSlider)
 * - Date: Exact date match (DatePicker)
 * - Difficulty: Easy, Moderate, or Hard (ChipGroup)
 * - Parking: Available or Unavailable (ChipGroup)
 *
 * Sort Options:
 * - By Date (newest first)
 * - By Name (alphabetical)
 * - By Length (shortest first)
 */
class AdvancedSearchBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_advanced_search_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etLocation = view.findViewById<TextInputEditText>(R.id.etLocation)
        val tilDate = view.findViewById<TextInputLayout>(R.id.tilDate)
        val etDate = view.findViewById<TextInputEditText>(R.id.etDate)
        val rsLength = view.findViewById<RangeSlider>(R.id.rsLength)
        val chipGroupDifficulty = view.findViewById<ChipGroup>(R.id.chipGroupDifficulty)
        val chipGroupParking = view.findViewById<ChipGroup>(R.id.chipGroupParking)
        val rgSort = view.findViewById<RadioGroup>(R.id.rgSort)

        // Date picker setup
        val dateClicker = View.OnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                etDate.setText("$y-$mm-$dd")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        tilDate.setEndIconOnClickListener { etDate.setText("") }
        etDate.setOnClickListener(dateClicker)

        // Apply button - collect all selected filters
        view.findViewById<View>(R.id.btnApply).setOnClickListener {
            val name = etName.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            val location = etLocation.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            val date = etDate.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

            // RangeSlider values for length filter
            val values = rsLength.values
            val minLen = values.getOrNull(0)?.toDouble()
            val maxLen = values.getOrNull(1)?.toDouble()
            val effectiveMin = if (minLen == null || minLen <= 0.0) null else minLen
            val effectiveMax = if (maxLen == null || maxLen >= 1000.0) null else maxLen

            // Get selected difficulty chip
            val difficulty = when (chipGroupDifficulty.checkedChipId) {
                R.id.chipDiffEasy -> getString(R.string.filter_easy)
                R.id.chipDiffModerate -> getString(R.string.filter_moderate)
                R.id.chipDiffHard -> getString(R.string.filter_hard)
                else -> null
            }

            // Get selected parking chip
            val parking: Boolean? = when (chipGroupParking.checkedChipId) {
                R.id.chipParkYes -> true
                R.id.chipParkNo -> false
                else -> null
            }

            // Get selected sort option
            val sortBy = when (rgSort.checkedRadioButtonId) {
                R.id.rbSortName -> "name"
                R.id.rbSortLength -> "length"
                else -> "date"
            }

            // Pass all filter criteria back to HikeListFragment
            parentFragmentManager.setFragmentResult("advancedSearch", Bundle().apply {
                putString("name", name)
                putString("location", location)
                putString("date", date)
                effectiveMin?.let { putDouble("minLen", it) }
                effectiveMax?.let { putDouble("maxLen", it) }
                difficulty?.let { putString("difficulty", it) }
                parking?.let { putBoolean("parking", it) }
                putString("sortBy", sortBy)
            })
            dismiss()
        }

        // Clear button - remove all filters
        view.findViewById<View>(R.id.btnClear).setOnClickListener {
            parentFragmentManager.setFragmentResult("advancedSearch", Bundle().apply { putBoolean("clear", true) })
            dismiss()
        }
    }
}
