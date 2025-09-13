package com.project.job.utils

import com.project.job.data.source.remote.api.response.CleaningService

object SelectedRoomManager {
    private val selectedRooms = mutableListOf<CleaningService>()
    
    fun addRoom(room: CleaningService) {
        // Create unique identifier by adding timestamp
        val uniqueRoom = room.copy(uid = "${room.uid}_${System.currentTimeMillis()}")
        selectedRooms.add(uniqueRoom)
    }
    
    fun removeRoom(originalUid: String) {
        // Remove the most recent room with this original uid
        val roomToRemove = selectedRooms.lastOrNull { it.uid.startsWith("${originalUid}_") }
        if (roomToRemove != null) {
            selectedRooms.remove(roomToRemove)
        }
    }
    
    fun getSelectedRooms(): List<CleaningService> {
        return selectedRooms.toList()
    }
    
    fun getSelectedRoomsCount(): Int {
        return selectedRooms.size
    }
    
    fun clearAllRooms() {
        selectedRooms.clear()
    }
    
    fun isRoomSelected(originalUid: String): Boolean {
        return selectedRooms.any { it.uid.startsWith("${originalUid}_") }
    }
    
    fun getSelectedRoomNames(): String {
        return selectedRooms.joinToString(", ") { it.serviceName }
    }
}
