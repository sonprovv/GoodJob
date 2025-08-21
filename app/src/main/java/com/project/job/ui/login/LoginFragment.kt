package com.project.job.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.job.R

class LoginFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        // Inflate the layout for this bottom sheet
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Xử lý sự kiện đăng nhập
        view.findViewById<View>(R.id.btn_Login)?.setOnClickListener {
            // Thêm logic xử lý đăng nhập tại đây
            // Ví dụ: kiểm tra thông tin đăng nhập và đóng bottom sheet
            dismiss() // Đóng bottom sheet sau khi đăng nhập thành công
        }

        // Xử lý nút "Quên mật khẩu" (nếu có)
        view.findViewById<View>(R.id.btn_Register)?.setOnClickListener {
            // Thêm logic xử lý quên mật khẩu (có thể mở một bottom sheet khác)
            dismiss() // Đóng bottom sheet hiện tại
        }
        view.findViewById<View>(R.id.iv_Close)?.setOnClickListener {
            // Đóng bottom sheet khi người dùng nhấn nút hủy
            dismiss()
        }
        view.findViewById<View>(R.id.iv_GoogleLogin)?.setOnClickListener {
            // Thêm logic xử lý đăng nhập bằng Google
            // Ví dụ: mở một trình xác thực Google hoặc thực hiện đăng nhập
            dismiss() // Đóng bottom sheet sau khi đăng nhập thành công
        }
    }

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}