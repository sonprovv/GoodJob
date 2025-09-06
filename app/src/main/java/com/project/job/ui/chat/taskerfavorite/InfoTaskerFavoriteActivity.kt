package com.project.job.ui.chat.taskerfavorite

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.project.job.R
import com.project.job.databinding.ActivityInfoTaskerFavoriteBinding
import com.project.job.utils.addFadeClickEffect

class InfoTaskerFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoTaskerFavoriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInfoTaskerFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivClose.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.cardViewButtonInfo.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up click listeners for each CardView to toggle visibility
        binding.cardViewContentWhatIsTaskerFavorite.setOnClickListener {
            toggleVisibility(binding.tvDescriptionWhatIsTaskerFavorite)
        }
        binding.cardViewContentWhyTaskerFavorite.setOnClickListener {
            toggleVisibility(binding.tvDescriptionWhyTaskerFavorite)
        }
        binding.cardViewContentWhatIsDateTaskerFavorite.setOnClickListener {
            toggleVisibility(binding.tvDescriptionWhatIsDateTaskerFavorite)
        }
        binding.cardViewContentHowUseTaskerFavorite.setOnClickListener {
            toggleVisibility(binding.tvDescriptionHowUseTaskerFavorite)
        }
    }

    private fun toggleVisibility(targetView: android.view.View) {
        // List of all description TextViews
        val allDescriptions = listOf(
            binding.tvDescriptionWhatIsTaskerFavorite,
            binding.tvDescriptionWhyTaskerFavorite,
            binding.tvDescriptionWhatIsDateTaskerFavorite,
            binding.tvDescriptionHowUseTaskerFavorite
        )

        // Toggle visibility of the target view and hide all others
        allDescriptions.forEach { view ->
            val isTarget = view == targetView
            view.visibility = if (isTarget) {
                if (view.visibility == android.view.View.VISIBLE) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
            } else {
                android.view.View.GONE
            }
            // Handle icon visibility for WhatIsTaskerFavorite
            if (view == binding.tvDescriptionWhatIsTaskerFavorite) {
                binding.tvTitleWhatIsTaskerFavorite.setTextColor(
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        getColor(R.color.cam)
                    } else {
                        getColor(R.color.black)
                    }
                )
                binding.viewWhatIsTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                binding.ivNarrowDownWhatIsTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                binding.ivNarrowUpWhatIsTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
            }
            // Handle icon visibility for WhyTaskerFavorite
            if (view == binding.tvDescriptionWhyTaskerFavorite) {
                binding.tvTitleWhyTaskerFavorite.setTextColor(
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        getColor(R.color.cam)
                    } else {
                        getColor(R.color.black)
                    }
                )
                binding.viewWhyTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                binding.ivNarrowDownWhyTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                binding.ivNarrowUpWhyTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
            }

            // Handle icon visibility for WhatIsDateTaskerFavorite
            if (view == binding.tvDescriptionWhatIsDateTaskerFavorite) {
                binding.tvTitleWhatIsDateTaskerFavorite.setTextColor(
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        getColor(R.color.cam)
                    } else {
                        getColor(R.color.black)
                    }
                )
                binding.viewWhatIsDateTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                binding.ivNarrowDownWhatIsDateTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                binding.ivNarrowUpWhatIsDateTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
            }
            // Handle icon visibility for HowUseTaskerFavorite
            if (view == binding.tvDescriptionHowUseTaskerFavorite) {
                binding.tvTitleHowUseTaskerFavorite.setTextColor(
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        getColor(R.color.cam)
                    } else {
                        getColor(R.color.black)
                    }
                )
                binding.viewHowUseTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                binding.ivNarrowDownHowUseTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                binding.ivNarrowUpHowUseTaskerFavorite.visibility =
                    if (isTarget && view.visibility == android.view.View.VISIBLE) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
            }

        }
    }
}