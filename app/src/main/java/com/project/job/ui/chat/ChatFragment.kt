package com.project.job.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.databinding.FragmentChatBinding
import com.project.job.ui.chat.messagetab.MessageFragment
import com.project.job.ui.chat.notificationtab.NotificationMesFragment
import com.project.job.ui.chat.taskerfavorite.TaskerFavoriteActivity
import com.project.job.utils.addFadeClickEffect


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up ViewPager2 with adapter
        val adapter = ViewPagerAdapter(this)
        binding.viewPagerChat.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayoutChat, binding.viewPagerChat) { tab, position ->
            tab.text = when (position) {
                0 -> "Nhắn tin"
                else -> "Thông báo"
            }
        }.attach()
        binding.llFavTasker.addFadeClickEffect {
            val intent = Intent(requireContext(), TaskerFavoriteActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    // ViewPager2 adapter to manage fragments
    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2 // Number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MessageFragment()
                1 -> NotificationMesFragment()

                else -> MessageFragment() // Fallback
            }
        }
    }

}