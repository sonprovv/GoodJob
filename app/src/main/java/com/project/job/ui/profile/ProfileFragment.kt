package com.project.job.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.project.job.databinding.FragmentProfileBinding
import com.project.job.ui.login.ChangPasswordActivity
import com.project.job.ui.login.LoginFragment
import com.project.job.ui.login.LoginResultListener
import com.project.job.utils.addFadeClickEffect

class ProfileFragment : Fragment(), LoginResultListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager

    private val updateProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Profile was updated, refresh UI
            updateUI()
        }
    }

    private fun updateUI() {
        val isLoggedIn = preferencesManager.getAuthToken() != null
        
        if (isLoggedIn) {
            // Show user information
            val sharedPref = preferencesManager.sharedPreferences
            val avatarUrl = sharedPref.getString("user_avatar", null)
            val userName = sharedPref.getString("user_name", "Người dùng")
            val userProvider = sharedPref.getString("user_provider", "")
            if(userProvider == "normal"){
                binding.llChangePass.visibility = View.VISIBLE
            }
            else {
                binding.llChangePass.visibility = View.GONE
            }
            Glide.with(this)
                .load(avatarUrl?.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.img_profile_picture_defaul)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(binding.ivProfilePicture)
                
            binding.apply {
                llLogout.visibility = View.VISIBLE
                tvWelcome.visibility = View.GONE
                cardViewButtonLogin.visibility = View.GONE
                tvFullname.visibility = View.VISIBLE
                tvFullname.text = userName
                tvViewProfile.visibility = View.VISIBLE
            }
        } else {
            // Show login UI
            binding.apply {
                llChangePass.visibility = View.GONE
                llLogout.visibility = View.GONE
                tvWelcome.visibility = View.VISIBLE
                cardViewButtonLogin.visibility = View.VISIBLE
                tvViewProfile.visibility = View.GONE
                tvFullname.visibility = View.GONE
                tvFullname.text = ""
                ivProfilePicture.setImageResource(R.drawable.img_profile_picture_defaul)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onLoginSuccess() {
        // Update UI when login is successful
        updateUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize preferences manager
        preferencesManager = PreferencesManager(requireContext())
        
        // Update UI based on login state
        updateUI()

        binding.cardViewButtonLogin.setOnClickListener {
            // Open LoginFragment
            val loginFragment = LoginFragment.newInstance()
            loginFragment.setLoginResultListener(this)
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }

        binding.llLogout.setOnClickListener {
            // Sign out from Google
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut().addOnCompleteListener {
                // Clear local data and update UI
                preferencesManager.clearAuthData()
                updateUI()
            }
        }

        binding.llChangePass.setOnClickListener{
            val intent = Intent(requireContext(), ChangPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.tvViewProfile.addFadeClickEffect {
            val intent = Intent(requireContext(), UpdateProfileActivity::class.java)
            updateProfileLauncher.launch(intent)
        }
    }


}