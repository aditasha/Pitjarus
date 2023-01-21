package com.aditasha.pitjarus.data.repository

import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.domain.model.StoresItem

object DataMapper {

    fun mapResponseToEntity(input: List<StoresItem>): List<StoreEntity> {
        val entityList = mutableListOf<StoreEntity>()

        for (i in input) {
            val entity = StoreEntity(
                storeId = i.storeId,
                storeCode = i.storeCode,
                storeName = i.storeName,
                address = i.address,
                dcId = i.dcId,
                dcName = i.dcName,
                accountId = i.accountId,
                accountName = i.accountName,
                subchannelId = i.subchannelId,
                subchannelName = i.subchannelName,
                channelId = i.channelId,
                channelName = i.channelName,
                areaId = i.areaId,
                areaName = i.areaName,
                regionId = i.regionId,
                regionName = i.regionName,
                latitude = i.latitude,
                longitude = i.longitude
            )
            entityList.add(entity)
        }
        return entityList
    }
}