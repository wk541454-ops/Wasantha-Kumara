package com.example.data.repository

import com.google.firebase.database.FirebaseDatabase

object SecureUserRepository {
    fun saveUser(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        birthday: String,
        gender: String
    ) {
        val db = FirebaseDatabase.getInstance().reference

        // PUBLIC - visible to authenticated users
        val publicMap = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "fullName" to "$firstName $lastName".trim()
        )

        // PRIVATE - secured with Firebase Realtime Database Security Rules ($uid === auth.uid)
        val privateMap = mapOf(
            "email" to email,
            "phone" to phone,
            "birthday" to birthday,
            "gender" to gender,
            "createdAt" to System.currentTimeMillis()
        )

        db.child("users/$uid/public_profile").setValue(publicMap)
        db.child("users/$uid/private_data").setValue(privateMap)
    }
}
