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
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.User
import com.project.job.databinding.ActivityUpdateProfileBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.profile.viewmodel.UpdateProfileViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: UpdateProfileViewModel
    private lateinit var user: User
    private lateinit var loadingDialog: LoadingDialog

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
                ContextCompat.checkSelfPermission(
                    this,
                    cameraPermission
                ) == PackageManager.PERMISSION_GRANTED -> openCamera()

                ContextCompat.checkSelfPermission(
                    this,
                    storagePermission
                ) == PackageManager.PERMISSION_GRANTED -> openGallery()
            }
        } else {
            // Permission denied, show a message
            Toast.makeText(
                this,
                "Permission is required to access this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Launcher for location picker from MapActivity
    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val selectedAddress = data.getStringExtra("selected_address")
                val selectedLatitude = data.getDoubleExtra("selected_latitude", 0.0)
                val selectedLongitude = data.getDoubleExtra("selected_longitude", 0.0)

                if (!selectedAddress.isNullOrEmpty()) {
                    // Xử lý địa chỉ để loại bỏ tọa độ trước khi hiển thị
                    val cleanAddress = when {
                        // Kiểm tra nếu chỉ có tọa độ không có địa chỉ
                        selectedAddress.matches(Regex("^\\d+(\\.\\d+)?,\\s*Lng:\\s*\\d+(\\.\\d+)?.*")) || // Format: 386665, Lng: 106,343867
                                selectedAddress.matches(Regex("^\\d+(\\.\\d+)?,\\s*\\d+(\\.\\d+)?$")) -> { // Format: 20.123, 106.456
                            "Chưa có địa chỉ cụ thể"
                        }

                        selectedAddress.contains("°") && selectedAddress.contains(",") -> {
                            // Nếu có tọa độ kèm địa chỉ, lấy phần sau dấu phẩy đầu tiên
                            val firstCommaIndex = selectedAddress.indexOf(",")
                            if (firstCommaIndex != -1 && firstCommaIndex < selectedAddress.length - 1) {
                                selectedAddress.substring(firstCommaIndex + 1).trim()
                            } else {
                                selectedAddress
                            }
                        }

                        selectedAddress.contains(",") -> {
                            // Nếu chỉ có dấu phẩy thông thường, lấy phần sau dấu phẩy đầu tiên
                            selectedAddress.substringAfter(",").trim()
                        }

                        else -> selectedAddress
                    }

                    // Update location field with clean address
                    binding.tvLocation.setText(cleanAddress)
                    user = user.copy(location = cleanAddress)

                    // Save coordinates to preferences for future map usage
                    preferencesManager.saveLocationCoordinates(selectedLatitude, selectedLongitude)

                    Toast.makeText(
                        this,
                        "✅ Đã cập nhật địa chỉ: $cleanAddress",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d(
                        "UpdateProfile",
                        "Location updated: $selectedAddress (Lat: $selectedLatitude, Lng: $selectedLongitude)"
                    )
                }
            }
        }
    }
    private val startForCameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri?.let { uri ->
                    loadImage(uri)
                }
            }
        }

    private val startForGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                    options[item] == "Chụp ảnh" -> checkPermissionAndOpen(
                        cameraPermission,
                        ::openCamera
                    )

                    options[item] == "Chọn từ thư viện" -> checkPermissionAndOpen(
                        storagePermission,
                        ::openGallery
                    )

                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkPermissionAndOpen(permission: String, onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(this, "Cần cấp quyền để sử dụng tính năng này", Toast.LENGTH_SHORT)
                    .show()
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
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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
        // Store the image URI for later use in upload
        imageUri = uri

        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivProfilePicture)
    }

    /**
     * Convert Uri to File for upload
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                // Create a temporary file
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempFile = File.createTempFile("avatar_$timeStamp", ".jpg", cacheDir)
                
                val outputStream = FileOutputStream(tempFile)
                stream.copyTo(outputStream)
                
                outputStream.close()
                stream.close()
                
                Log.d("UpdateProfile", "Uri converted to file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
                tempFile
            }
        } catch (e: Exception) {
            Log.e("UpdateProfile", "Error converting Uri to File", e)
            Toast.makeText(this, "Lỗi khi xử lý file ảnh", Toast.LENGTH_SHORT).show()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        // Initialize RetrofitClient if not already initialized
        if (!RetrofitClient.isInitialized()) {
            RetrofitClient.initialize(applicationContext)
        }

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel = UpdateProfileViewModel()

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }

        // hien thi du lieu len view
        preferencesManager = PreferencesManager(this)
        val token = preferencesManager.getAuthToken() ?: ""
        val uid = preferencesManager.getUserData()["user_id"] ?: ""
        val userName = preferencesManager.getUserData()["user_name"] ?: "User"
        val urlImage = preferencesManager.getUserData()["user_avatar"] ?: ""
        val phone = preferencesManager.getUserData()["user_phone"] ?: ""
        val email = preferencesManager.getUserData()["user_email"] ?: ""
        val gender = preferencesManager.getUserData()["user_gender"] ?: ""
        val birthdate = preferencesManager.getUserData()["user_birthdate"] ?: ""
        var location = preferencesManager.getUserData()["user_location"] ?: ""
        val role = preferencesManager.getUserData()["user_role"] ?: ""
        val provider = preferencesManager.getUserData()["user_provider"] ?: ""

        // Xử lý hiển thị địa chỉ - loại bỏ tọa độ và chỉ hiển thị địa chỉ
//        val firstCommaIndex = location.indexOf(",")
//        location = location.substring(firstCommaIndex + 1).trim()




        binding.edtFullname.setText(userName)
        binding.edtPhone.setText(phone)
        binding.tvEmail.setText(email)
        binding.rg.check(if (gender == "Nam") R.id.rb_male else R.id.rb_female)
        binding.tvLocation.setText(location)

        binding.ivMap.setOnClickListener {
            // Open MapActivity for location selection
            val intent = Intent(this, com.project.job.ui.map.MapActivity::class.java)
            intent.putExtra("source", "update_profile")
            intent.putExtra("current_location", user.location)
            locationPickerLauncher.launch(intent)
        }

        // Convert date from ISO format to display format
        val formattedBirthdate = try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = isoFormat.parse(birthdate)
            if (date != null) displayFormat.format(date) else birthdate
        } catch (e: Exception) {
            birthdate // Fallback to original if parsing fails
        }
        binding.tvDob.setText(formattedBirthdate)
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
            showDatePickerDialog()
        }

        // Initialize user
        user = User(
            uid = uid,
            username = userName,
            dob = binding.tvDob.text.toString(),
            avatar = urlImage,
            tel = phone,
            email = email,
            gender = gender,
            location = location,
            role = role,
            provider = provider
        )

        binding.rg.setOnCheckedChangeListener { _, checkedId ->
            // Update gender based on selection
            user = user.copy(
                gender = when (checkedId) {
                    R.id.rb_male -> "Nam"
                    R.id.rb_female -> "Nữ"
                    else -> user.gender
                }
            )
        }

        binding.cardViewButtonSave.setOnClickListener {
            // Create a copy of the user with all required fields
            val updatedUser = user.copy(
                uid = user.uid.ifEmpty { preferencesManager.getUserData()["user_id"] ?: "" },
                username = binding.edtFullname.text.toString(),
                gender = if (binding.rbMale.isChecked) "Nam" else "Nữ",
                dob = convertDateToISOFormat(binding.tvDob.text.toString()),
                email = user.email.ifEmpty { preferencesManager.getUserData()["user_email"] ?: "" },
                tel = binding.edtPhone.text.toString(),
                location = user.location.ifEmpty { "Chưa cập nhật" },
                role = user.role.ifEmpty { "user" },
                provider = user.provider.ifEmpty { "normal" },
                avatar = user.avatar // This will be updated after avatar upload if needed
            )

            Log.d("UpdateProfile", "Updating user: $updatedUser")

            // Only upload avatar if a new image was selected
            if (imageUri != null) {
                // Convert Uri to File first
                val avatarFile = uriToFile(imageUri!!)
                if (avatarFile != null) {
                    // Start the avatar upload, which will also update the profile after success
                    viewModel.updateAvatar(
                        avatarFile = avatarFile,
                        user = updatedUser
                    )
                } else {
                    // If conversion failed, show error and just update profile without avatar
                    Toast.makeText(this, "Không thể xử lý file ảnh, cập nhật thông tin mà không thay đổi avatar", Toast.LENGTH_LONG).show()
                    viewModel.updateProfile(updatedUser)
                }
            } else {
                // If no new image, just update the profile
                viewModel.updateProfile(updatedUser)
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading state
            launch {
                viewModel.loading.collect { isLoading ->
                    if (isLoading) {
                        loadingDialog.show()
                        binding.cardViewButtonSave.isEnabled = false
                        binding.cardViewCamera.isEnabled = false
                        binding.ivBack.isEnabled = false
                        binding.ivCalendar.isEnabled = false
                    } else {
                        loadingDialog.hide()
                        binding.cardViewButtonSave.isEnabled = true
                        binding.cardViewCamera.isEnabled = true
                        binding.ivBack.isEnabled = true
                        binding.ivCalendar.isEnabled = true
                    }
                }
            }

            // Collect updated user data
            launch {
                viewModel.userData.collect { updatedUser ->
                    updatedUser?.let { user ->
                        // Update the local user object
                        this@UpdateProfileActivity.user = user

                        // Update the UI with the new data
                        binding.edtFullname.setText(user.username)
                        binding.edtPhone.setText(user.tel)

                        // Convert date from ISO format to display format
                        val formattedDate = try {
                            val isoFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            )
                            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val date = isoFormat.parse(user.dob)
                            if (date != null) displayFormat.format(date) else user.dob
                        } catch (e: Exception) {
                            user.dob // Fallback to original if parsing fails
                        }
                        binding.tvDob.setText(formattedDate)

                        // Update gender radio buttons
                        if (user.gender.equals("Nam", ignoreCase = true)) {
                            binding.rbMale.isChecked = true
                        } else {
                            binding.rbFemale.isChecked = true
                        }

                        // Load the avatar if available
                        if (user.avatar.isNotEmpty()) {
                            Glide.with(this@UpdateProfileActivity)
                                .load(user.avatar)
                                .placeholder(R.drawable.img_profile_picture_defaul)
                                .error(R.drawable.img_profile_picture_defaul)
                                .into(binding.ivProfilePicture)
                        }
                    }
                }
            }

            // Collect success state
            launch {
                viewModel.success_change.collect { isSuccess ->
                    if (isSuccess) {
                        // Check if we have updated user data
                        viewModel.userData.value?.let { updatedUser ->
                            // Save the updated user data
                            preferencesManager.saveUser(
                                updatedUser
                            )
                            // Update the local user object
                            user = updatedUser
                        }

                        // Show success message
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Cập nhật thông tin thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Set result to indicate successful update and finish
                        setResult(RESULT_OK)
                        finish()
                    }

                }
            }

        }
    }

    private fun convertDateToISOFormat(dateString: String): String {
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = displayFormat.parse(dateString)
            if (date != null) {
                isoFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString // Fallback to original if parsing fails
        }
    }

    private fun showDatePickerDialog() {
        // Get current date or use a default date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Parse existing date if available
        val currentDate = binding.tvDob.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(currentDate)
                if (date != null) {
                    calendar.time = date
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Create date picker dialog
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date
                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%d",
                    selectedDay,
                    selectedMonth + 1, // Month is 0-based
                    selectedYear
                )
                // Update the date field
                binding.tvDob.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set maximum date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Show the dialog
        datePickerDialog.show()
    }
}