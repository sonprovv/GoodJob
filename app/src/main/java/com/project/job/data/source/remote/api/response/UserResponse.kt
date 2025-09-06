package com.project.job.data.source.remote.api.response


//{
//
//    "success": true,
//    "message": "Thành công",
//    "data": {
//    "user": {
//        "uid": "iEJgbv29ANeqtR2CE5iWWqWs3My2",
//        "username": "kain411thien",
//        "gender": "Nam",
//        "dob": "01/01/1990",
//        "avatar": "https://res.cloudinary.com/dvofgx21o/image/upload/v1754337546/jobs/byhangkho4twacw1owri.png",
//        "email": "kain411thien@gmail.com",
//        "tel": "0123456789",
//        "location": "Chưa cập nhật",
//        "role": "user"
//    },
//    "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjkyZTg4M2NjNDY2M2E2MzMyYWRhNmJjMWU0N2YzZmY1ZTRjOGI1ZDciLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vam9icy00YzllMyIsImF1ZCI6ImpvYnMtNGM5ZTMiLCJhdXRoX3RpbWUiOjE3NTYzMDQxMzQsInVzZXJfaWQiOiJpRUpnYnYyOUFOZXF0UjJDRTVpV1dxV3MzTXkyIiwic3ViIjoiaUVKZ2J2MjlBTmVxdFIyQ0U1aVdXcVdzM015MiIsImlhdCI6MTc1NjMwNDEzNCwiZXhwIjoxNzU2MzA3NzM0LCJlbWFpbCI6ImthaW40MTF0aGllbkBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsia2FpbjQxMXRoaWVuQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.Aqr70z6RVvncWV0d3gyGog7NdeK8Ur855VDrnVjgzIedBLhHEtPOPpHR37y7g8VUCQbv_9KdAEyrmuv5X1KqwIBdCSKM9153W7RTzAymQ-g7i3xnnRbQ7VbaIhX5aa0p1RrO-uMIyfdy5fR03mBJHT3P2MzpPNKECok2OvvMPzA6OXXC5VplboGAqdJ9CrqPsHPauVxT41CKiDxbisjduK1VjY2LrbYaKD_YvTJkF9X-qmw3oSSeiRlZdLD8ZfaBsY4mPK_Tx8_ehboQufzhF9FVPRucXt5Dvew8zS_8b_dJyrN27QvEz26KNSso_2wEWvoYG3m0sgyHlkg1FzmRCg"
//}
//}
data class UserResponse (
    val success: Boolean,
    val message: String,
    val data: DataUser
)
data class DataUser(
    val user: User,
    val token: String
)
data class User(
    val uid: String,
    val username: String,
    val gender: String,
    val dob: String,
    val avatar: String,
    val email: String,
    val tel: String,
    val location: String,
    val role: String,
    var google : Boolean = false
)