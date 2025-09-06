package com.project.job.ui.profile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityUpdateProfileBinding
import com.project.job.utils.addFadeClickEffect
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var preferencesManager: PreferencesManager
    
    private var imageUri: Uri? = null
    private val cameraPermission = Manifest.permission.CAMERA
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with the operation
            when {
                ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED -> openCamera()
                ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED -> openGallery()
            }
        } else {
            // Permission denied, show a message
            Toast.makeText(this, "Permission is required to access this feature", Toast.LENGTH_SHORT).show()
        }
    }
    private val startForCameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri?.let { uri ->
                loadImage(uri)
            }
        }
    }
    
    private val startForGalleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                imageUri = it
                loadImage(it)
            }
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>(
            "Chụp ảnh",
            "Chọn từ thư viện",
            "Hủy"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh đại diện")
            .setItems(options) { dialog, item ->
                when {
                    options[item] == "Chụp ảnh" -> checkPermissionAndOpen(cameraPermission, ::openCamera)
                    options[item] == "Chọn từ thư viện" -> checkPermissionAndOpen(storagePermission, ::openGallery)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }
    
    private fun checkPermissionAndOpen(permission: String, onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(this, "Cần cấp quyền để sử dụng tính năng này", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Lỗi khi tạo file ảnh", Toast.LENGTH_SHORT).show()
                null
            }
            
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    it
                )
                imageUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startForCameraResult.launch(takePictureIntent)
            }
        }
    }
    
    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startForGalleryResult.launch(pickPhoto)
    }
    
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            imageUri = FileProvider.getUriForFile(
                this@UpdateProfileActivity,
                "${packageName}.provider",
                this
            )
        }
    }
    
    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivProfilePicture)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()

        }

        // hien thi du lieu len view
        preferencesManager = PreferencesManager(this)
        val userName = preferencesManager.getUserData()["user_name"] ?: "User"
        val urlImage = preferencesManager.getUserData()["user_avatar"] ?: ""
        val phone = preferencesManager.getUserData()["user_phone"] ?: ""
        val email = preferencesManager.getUserData()["user_email"] ?: ""
        val gender = preferencesManager.getUserData()["user_gender"] ?: ""
        val birthdate = preferencesManager.getUserData()["user_birthdate"] ?: ""
        binding.edtFullname.setText(userName)
        binding.edtPhone.setText(phone)
        binding.tvEmail.setText(email)
        binding.rg.check(if (gender == "Nam") R.id.rb_male else R.id.rb_female)
        binding.edtDob.setText(birthdate)
        // su dung Glide de load hinh anh
        Glide.with(this)
            .load(urlImage)
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivProfilePicture)

        // xu ly su kien click
        binding.cardViewCamera.setOnClickListener {
            showImagePickerDialog()
        }

        binding.ivCalendar.addFadeClickEffect {
            // Handle calendar icon click
        }

        binding.rg.setOnCheckedChangeListener { _, checkedId ->
            // Handle radio button selection
            when (checkedId) {
                R.id.rb_male -> {
                    // Handle male radio button selection
                }

                R.id.rb_female -> {
                    // Handle female radio button selection
                }
            }
        }

        binding.cardViewButtonSave.setOnClickListener {

        }

    }
}