package extractdata.pojo;

import com.google.gson.annotations.SerializedName;

public enum WaypointType {
    @SerializedName("ort")
    VILLAGE,
    @SerializedName("peak")
    PEAK,
    @SerializedName("pass")
    PASS,
    @SerializedName("hut")
    HUT,
    @SerializedName("climb")
    CLIMBINGAREA,
    @SerializedName("eisfa")
    ICEFALL,
    @SerializedName("viafe")
    VIAFERRATA,
    @SerializedName("wand")
    WAND,
    @SerializedName("lake")
    LAKE,
    @SerializedName("ruin")
    RUIN,
    @SerializedName("bridg")
    BRIDGE,
    @SerializedName("cave")
    CAVE,
    @SerializedName("point")
    OTHER
}