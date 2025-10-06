package com.project.job.ui.payment

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.project.job.R
import com.project.job.databinding.FragmentPaymentQrBinding
import com.project.job.utils.addFadeClickEffect
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PaymentQrFragment(
    private val uid : String,
    private val jobID : String,
    private val serviceType : String,
    private val amount : Int,
    private val onDismissCallback: (() -> Unit)? = null
) : DialogFragment() {
    private var _binding: FragmentPaymentQrBinding? = null
    private val binding get() = _binding!!
    private var qrImageUrl: String = ""

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveQrCodeToGallery()
        } else {
            Toast.makeText(requireContext(), "Cần cấp quyền để lưu ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(serviceType == "HEALTHCARE"){
            binding.tvServiceTyoe.text = "Chăm sóc"
        }
        else if(serviceType == "CLEANING"){
            binding.tvServiceTyoe.text = "Dọn dẹp"
        }
        else if(serviceType == "MAINTENANCE") {
            binding.tvServiceTyoe.text = "Bảo trì"
        }

        binding.tvPrice.text = formatPrice(amount)

        binding.ivClose.addFadeClickEffect {
            dismiss()
        }

        // save qr local device
        binding.cardViewSaveQr.addFadeClickEffect {
            checkPermissionAndSaveQr()
        }
        val qrBase = "https://qr.sepay.vn/img?acc=VQRQAELKF0808&bank=MBBank"
        val des = "${uid}_${jobID}_${serviceType}"

        qrImageUrl =
            "${qrBase}&amount=${amount}&des=${des}"
        Glide.with(requireContext())
            .load(qrImageUrl)
            .placeholder(R.drawable.img_qr_error)
            .into(binding.ivQr)
    }

    private fun formatPrice(price: Int): CharSequence {
        return String.format("%,d", price) + " VND"
    }

    private fun checkPermissionAndSaveQr() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+ không cần permission để lưu vào MediaStore
                saveQrCodeToGallery()
            }

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                saveQrCodeToGallery()
            }

            else -> {
                // Yêu cầu permission
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveQrCodeToGallery() {
        Toast.makeText(requireContext(), "Đang tải mã QR...", Toast.LENGTH_SHORT).show()

        Glide.with(requireContext())
            .asBitmap()
            .load(qrImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImageToStorage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Cleanup nếu cần
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Toast.makeText(requireContext(), "Tải mã QR thất bại", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun saveImageToStorage(bitmap: Bitmap) {
        val fileName = "GoodJob_QR_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Sử dụng MediaStore
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/GoodJob"
                    )
                }

                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                // Android 9 trở xuống
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val goodJobDir = File(imagesDir, "GoodJob")
                if (!goodJobDir.exists()) {
                    goodJobDir.mkdirs()
                }
                val image = File(goodJobDir, fileName)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(requireContext(), "✅ Đã lưu mã QR vào thư viện", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "❌ Lỗi khi lưu mã QR: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            fos?.close()
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        // Gọi callback để finish activity khi user đóng dialog
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}