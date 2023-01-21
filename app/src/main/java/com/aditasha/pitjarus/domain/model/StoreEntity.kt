package com.aditasha.pitjarus.domain.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("store")
data class StoreEntity(
    @PrimaryKey(true)
    @ColumnInfo("room_id")
    val roomId: Int = 0,

    @ColumnInfo("store_id")
    val storeId: String? = null,

    @ColumnInfo("store_code")
    val storeCode: String? = null,

    @ColumnInfo("store_name")
    val storeName: String? = null,

    @ColumnInfo("address")
    val address: String? = null,

    @ColumnInfo("dc_id")
    val dcId: String? = null,

    @ColumnInfo("dc_name")
    val dcName: String? = null,

    @ColumnInfo("account_id")
    val accountId: String? = null,

    @ColumnInfo("account_name")
    val accountName: String? = null,

    @ColumnInfo("subchannel_id")
    val subchannelId: String? = null,

    @ColumnInfo("subchannel_name")
    val subchannelName: String? = null,

    @ColumnInfo("channel_id")
    val channelId: String? = null,

    @ColumnInfo("channel_name")
    val channelName: String? = null,

    @ColumnInfo("area_id")
    val areaId: String? = null,

    @ColumnInfo("area_name")
    val areaName: String? = null,

    @ColumnInfo("region_id")
    val regionId: String? = null,

    @ColumnInfo("region_name")
    val regionName: String? = null,

    @ColumnInfo("latitude")
    val latitude: String? = null,

    @ColumnInfo("longitude")
    val longitude: String? = null,

    @ColumnInfo("visited")
    val visited: Boolean? = null,

    @ColumnInfo("last_visited")
    val lastVisited: Long? = null,

    @ColumnInfo("distance")
    val distance: String = "1m",

    @ColumnInfo("picture")
    val picture: String? = null
) : Parcelable