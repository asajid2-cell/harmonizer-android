package cc.harmonizerlabs.app.api.models

import com.google.gson.annotations.SerializedName

data class AutocroonerStylesResponse(
    @SerializedName("styles") val styles: List<AutocroonerStyle> = emptyList(),
)

data class AutocroonerStyle(
    @SerializedName("id")         val id: String,
    @SerializedName("name")       val name: String,
    @SerializedName("trackCount") val trackCount: Int = 0,
)

data class AutocroonerStyleDetail(
    @SerializedName("id")                   val id: String,
    @SerializedName("name")                 val name: String,
    @SerializedName("autocroonerSettings")  val autocroonerSettings: Map<String, Double> = emptyMap(),
)
