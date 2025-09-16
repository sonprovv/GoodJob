package com.project.job.ui.service.cleaningservice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.project.job.R
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceCleaningAdapter
import com.project.job.utils.SelectedRoomManager
import com.project.job.data.source.remote.api.response.CleaningService

class RoomDetailFragment : Fragment() {
    
    private val tasks: List<String> by lazy {
        arguments?.getStringArrayList("tasks") ?: emptyList()
    }
    
    private val duties: List<String> by lazy {
        arguments?.getStringArrayList("duties") ?: emptyList()
    }
    
    private val excludedTasks: List<String> by lazy {
        arguments?.getStringArrayList("excludedTasks") ?: emptyList()
    }
    
    // Service data properties
    private val uid: String by lazy {
        arguments?.getString("uid") ?: ""
    }
    
    private val serviceType: String by lazy {
        arguments?.getString("serviceType") ?: ""
    }
    
    private val serviceName: String by lazy {
        arguments?.getString("serviceName") ?: ""
    }
    
    private val image: String by lazy {
        arguments?.getString("image") ?: ""
    }
    
    private lateinit var adapter: DetailServiceCleaningAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use different layout based on service type
        return inflater.inflate(R.layout.fragment_room_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            setupCleaningView(view)
    }
    

    
    private fun setupCleaningView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_tasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DetailServiceCleaningAdapter(tasks)
        recyclerView.adapter = adapter
        
        // Debug logging
        android.util.Log.d("RoomDetailFragment", "Fragment created for room: $serviceName (uid: $uid)")
        android.util.Log.d("RoomDetailFragment", "serviceType: $serviceType")
        android.util.Log.d("RoomDetailFragment", "image: $image")
        android.util.Log.d("RoomDetailFragment", "tasks: $tasks")
        android.util.Log.d("RoomDetailFragment", "duties: $duties")
        android.util.Log.d("RoomDetailFragment", "excludedTasks: $excludedTasks")
        android.util.Log.d("RoomDetailFragment", "All selected rooms: ${SelectedRoomManager.getSelectedRooms().map { "${it.serviceName}(${it.uid})" }}")
        
        // Check if arguments are null
        android.util.Log.d("RoomDetailFragment", "Arguments: ${arguments}")
        android.util.Log.d("RoomDetailFragment", "Arguments keys: ${arguments?.keySet()?.toList()}")
    }
    
    private fun addRoomToManager() {
        val selectedService = CleaningService(
            uid = uid,
            serviceType = serviceType,
            serviceName = serviceName,
            image = image,
            tasks = tasks
        )
        SelectedRoomManager.addRoom(selectedService)
        
        // Debug logging
        android.util.Log.d("RoomDetailFragment", "Added room: $serviceName (uid: $uid)")
        android.util.Log.d("RoomDetailFragment", "Total rooms in manager: ${SelectedRoomManager.getSelectedRoomsCount()}")
    }
    
    private fun removeRoomFromManager() {
        SelectedRoomManager.removeRoom(uid)
    }
    
    private fun saveSelectedService() {
        val serviceData = mapOf(
            "uid" to uid,
            "serviceType" to serviceType,
            "serviceName" to serviceName,
            "image" to image,
            "tasks" to tasks
        )
        
        val sharedPref = requireContext().getSharedPreferences("selected_services", Context.MODE_PRIVATE)
        val gson = Gson()
        val serviceJson = gson.toJson(serviceData)
        
        with(sharedPref.edit()) {
            putString(uid, serviceJson)
            apply()
        }
    }
    
    private fun removeSelectedService() {
        val sharedPref = requireContext().getSharedPreferences("selected_services", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(uid)
            apply()
        }
    }
}
