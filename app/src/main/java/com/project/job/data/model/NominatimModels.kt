package com.project.job.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model for Nominatim Search Response (Forward Geocoding)
 * API: https://nominatim.openstreetmap.org/search?q={query}&format=json
 */
data class NominatimSearchResult(
    @SerializedName("place_id")
    val placeId: Long,
    
    @SerializedName("licence")
    val licence: String?,
    
    @SerializedName("osm_type")
    val osmType: String?,
    
    @SerializedName("osm_id")
    val osmId: Long?,
    
    @SerializedName("lat")
    val lat: String,
    
    @SerializedName("lon")
    val lon: String,
    
    @SerializedName("display_name")
    val displayName: String,
    
    @SerializedName("class")
    val category: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("importance")
    val importance: Double?,
    
    @SerializedName("icon")
    val icon: String?,
    
    @SerializedName("address")
    val address: NominatimAddress?
) {
    fun getLatitude(): Double = lat.toDoubleOrNull() ?: 0.0
    fun getLongitude(): Double = lon.toDoubleOrNull() ?: 0.0
    
    fun getShortAddress(): String {
        return address?.let {
            listOfNotNull(
                it.road ?: it.suburb ?: it.neighbourhood,
                it.city ?: it.town ?: it.village,
                it.state,
                it.country
            ).joinToString(", ")
        } ?: displayName
    }
}

/**
 * Model for Nominatim Reverse Geocoding Response
 * API: https://nominatim.openstreetmap.org/reverse?lat={lat}&lon={lon}&format=json
 */
data class NominatimReverseResult(
    @SerializedName("place_id")
    val placeId: Long,
    
    @SerializedName("licence")
    val licence: String?,
    
    @SerializedName("osm_type")
    val osmType: String?,
    
    @SerializedName("osm_id")
    val osmId: Long?,
    
    @SerializedName("lat")
    val lat: String,
    
    @SerializedName("lon")
    val lon: String,
    
    @SerializedName("display_name")
    val displayName: String,
    
    @SerializedName("address")
    val address: NominatimAddress?,
    
    @SerializedName("boundingbox")
    val boundingBox: List<String>?
) {
    fun getFormattedAddress(): String {
        return address?.let {
            listOfNotNull(
                it.houseNumber?.let { num -> "$num " },
                it.road,
                it.suburb ?: it.neighbourhood,
                it.city ?: it.town ?: it.village,
                it.state,
                it.country
            ).joinToString(", ")
        } ?: displayName
    }
}

/**
 * Detailed address components from Nominatim
 */
data class NominatimAddress(
    @SerializedName("house_number")
    val houseNumber: String?,
    
    @SerializedName("road")
    val road: String?,
    
    @SerializedName("suburb")
    val suburb: String?,
    
    @SerializedName("neighbourhood")
    val neighbourhood: String?,
    
    @SerializedName("quarter")
    val quarter: String?,
    
    @SerializedName("village")
    val village: String?,
    
    @SerializedName("town")
    val town: String?,
    
    @SerializedName("city")
    val city: String?,
    
    @SerializedName("state")
    val state: String?,
    
    @SerializedName("postcode")
    val postcode: String?,
    
    @SerializedName("country")
    val country: String?,
    
    @SerializedName("country_code")
    val countryCode: String?
)
