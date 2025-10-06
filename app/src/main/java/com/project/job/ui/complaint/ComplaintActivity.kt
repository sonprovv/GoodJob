package com.project.job.ui.complaint

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityComplaintBinding

class ComplaintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityComplaintBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Danh sách loại khiếu nại
        val types = arrayOf(
            "Dịch vụ không đúng cam kết",
            "Nhân viên không đến đúng giờ",
            "Chi phí sai lệch / thanh toán",
            "Thái độ nhân viên không phù hợp",
            "Khác"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter

        // Sự kiện khi nhấn "Gửi khiếu nại"
        binding.btnSubmit.setOnClickListener {
            val type = binding.spinnerType.selectedItem.toString()
            val description = binding.edtDescription.text.toString()

            if (description.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả khiếu nại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Gửi dữ liệu khiếu nại tới server (API)
            // Có thể đính kèm file từ btnAttach

            binding.txtResult.text = "✅ Khiếu nại đã được gửi!\nLoại: $type\nMô tả: $description"
        }

        // Nút đính kèm (chưa xử lý upload)
        binding.btnAttach.setOnClickListener {
            Toast.makeText(this, "Chức năng đính kèm file (chưa triển khai)", Toast.LENGTH_SHORT).show()
        }
    }
}