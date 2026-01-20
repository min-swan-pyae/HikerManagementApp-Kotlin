package com.example.hikermanagementapp.util

import android.os.Bundle
class SearchStateManager {

    private var savedSearchQuery: String = ""
    private var savedFilterParams: Bundle? = null
    private var isAdvancedFilterActive: Boolean = false

    fun getSearchQuery(): String = savedSearchQuery

    fun setSearchQuery(query: String) {
        savedSearchQuery = query
    }

    fun hasActiveSearch(): Boolean = savedSearchQuery.isNotEmpty()

    fun getFilterParams(): Bundle? = savedFilterParams
    fun setFilterParams(params: Bundle?) {
        savedFilterParams = params
        isAdvancedFilterActive = params != null
    }

    fun isFilterActive(): Boolean = isAdvancedFilterActive

    fun clearAll() {
        savedSearchQuery = ""
        savedFilterParams = null
        isAdvancedFilterActive = false
    }

    fun parseFilterParams(params: Bundle): FilterCriteria {
        val minLen = params.getDouble("minLen", Double.NaN).let {
            if (it.isNaN()) null else it
        }
        val maxLen = params.getDouble("maxLen", Double.NaN).let {
            if (it.isNaN()) null else it
        }
        val parking = if (params.containsKey("parking")) {
            params.getBoolean("parking")
        } else null

        return FilterCriteria(
            name = params.getString("name"),
            location = params.getString("location"),
            date = params.getString("date"),
            minLen = minLen,
            maxLen = maxLen,
            difficulty = params.getString("difficulty"),
            parking = parking,
            sortBy = params.getString("sortBy") ?: "date"
        )
    }

    data class FilterCriteria(
        val name: String?,
        val location: String?,
        val date: String?,
        val minLen: Double?,
        val maxLen: Double?,
        val difficulty: String?,
        val parking: Boolean?,
        val sortBy: String
    )
}
