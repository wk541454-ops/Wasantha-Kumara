package com.example.data.repository

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LoginAlertManager {
    fun checkAndAlert(
        context: Context,
        uid: String,
        onSecuritySignOut: () -> Unit = {}
    ) {
        if (uid.isEmpty()) return

        val db = FirebaseDatabase.getInstance()
            .getReference("users/$uid/private_data/loginHistory")

        val device = android.os.Build.MODEL
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isNewDevice = !snapshot.children.any {
                    it.child("device").getValue(String::class.java) == device
                }

                // Save login entry in history
                db.push().setValue(mapOf("device" to device, "time" to time))

                if (isNewDevice && snapshot.childrenCount > 0) {
                    // Security alert for new device login
                    AlertDialog.Builder(context)
                        .setTitle("Security Alert 🛡️")
                        .setMessage("ඔබේ account එකට අලුත් device එකකින් login වුනා\n\nDevice: $device\nTime: $time\n\nඔබද?")
                        .setPositiveButton("ඔව්, මම තමයි") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNegativeButton("නෑ, මම නෙවෙයි") { dialog, _ ->
                            dialog.dismiss()
                            try {
                                FirebaseAuth.getInstance().signOut()
                            } catch (e: Exception) {}
                            Toast.makeText(context, "Account secure කරනවා, password reset කරන්න", Toast.LENGTH_LONG).show()
                            onSecuritySignOut()
                        }
                        .setCancelable(false)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
