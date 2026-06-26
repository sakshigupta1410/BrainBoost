package com.example.brainboost

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.brainboost.databinding.ActivityMainBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var profileImage: ShapeableImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var fullName: TextView
    private lateinit var email: TextView

    private val PREF_PROFILE_IMAGE = "profile_image_local"
    private val prefs by lazy { getSharedPreferences("profile_prefs", MODE_PRIVATE) }

    private var previewPopup: View? = null

    // ------------------ Gallery Picker ------------------
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageSelection(it) }
        }



    // ------------------ Camera Result ------------------
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val savedUri = saveBitmapToInternal(it)
                    savedUri?.let { handleImageSelection(it) }
                }
            }
        }

    // ------------------ Permission ------------------
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }

    // ------------------ onCreate ------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 👉 ADD THIS HERE — correct place
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()


        auth = FirebaseAuth.getInstance()
        user = auth.currentUser ?: run {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding.tvHello.text = "Hello, ${user.displayName ?: "Player"}!"
        binding.ivHamburger.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.navigationView.setNavigationItemSelectedListener(this)

        setupDrawerHeader()
    }

    // ------------------ Drawer Header ------------------
    private fun setupDrawerHeader() {
        val header = binding.navigationView.getHeaderView(0)

        profileImage = header.findViewById(R.id.profileImage)
        cameraIcon = header.findViewById(R.id.cameraIcon)
        fullName = header.findViewById(R.id.fullName)
        email = header.findViewById(R.id.email)

        fullName.text = user.displayName ?: "BrainBoost User"
        email.text = user.email

        val savedUri = prefs.getString(PREF_PROFILE_IMAGE, null)
        if (savedUri != null)
            Picasso.get().load(Uri.parse(savedUri)).fit().centerCrop().into(profileImage)
        else
            profileImage.setImageResource(R.drawable.ic_user)

        cameraIcon.setOnClickListener { showPhotoOptions() }

        profileImage.setOnClickListener {
            showCenterProfilePreview()
        }
    }

    // ------------------ Photo Options Dialog ------------------
    private fun showPhotoOptions() {

        val hasPhoto = prefs.getString(PREF_PROFILE_IMAGE, null) != null

        val options = if (hasPhoto) {
            arrayOf("Choose from Gallery", "Take Photo", "Remove Photo")
        } else {
            arrayOf("Choose from Gallery", "Take Photo")
        }

        AlertDialog.Builder(this)
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> ensureCameraPermission()
                    2 -> if (hasPhoto) removeProfilePhoto()
                }
            }
            .show()
    }

    private fun removeProfilePhoto() {
        prefs.edit().remove(PREF_PROFILE_IMAGE).apply()
        profileImage.setImageResource(R.drawable.ic_user)
        Toast.makeText(this, "Profile photo removed!", Toast.LENGTH_SHORT).show()
    }

    private fun ensureCameraPermission() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (granted) openCamera()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    // ------------------ Save Images ------------------
    private fun handleImageSelection(uri: Uri) {
        val savedUri = copyToInternalStorage(uri)
        savedUri?.let {
            prefs.edit().putString(PREF_PROFILE_IMAGE, it.toString()).apply()
            Picasso.get().load(it).fit().centerCrop().into(profileImage)

            Toast.makeText(this, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToInternalStorage(uri: Uri): Uri? = try {
        val input = contentResolver.openInputStream(uri) ?: return null
        val file = File(filesDir, "profile_${UUID.randomUUID()}.jpg")
        val output = FileOutputStream(file)
        input.copyTo(output)
        input.close()
        output.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }

    private fun saveBitmapToInternal(bitmap: Bitmap): Uri? = try {
        val file = File(filesDir, "profile_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }

    // ------------------ Center Circle Preview Popup ------------------
    private fun showCenterProfilePreview() {

        // 1. Check if a custom photo URI is saved
        val savedUri = prefs.getString(PREF_PROFILE_IMAGE, null)

        if (savedUri == null) {
            // No custom profile photo uploaded, so don't show the preview.
            Toast.makeText(this, "No profile photo uploaded to preview.", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed only if a custom photo exists (savedUri != null)
        if (previewPopup != null) return

        val inflater = layoutInflater
        previewPopup = inflater.inflate(R.layout.popup_center_profile, null)

        val previewImage =
            previewPopup!!.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.ivCenterPreview)

        // Ensure the preview uses the image from the profileImage view, which should hold the custom photo
        previewImage.setImageDrawable(profileImage.drawable)

        addContentView(
            previewPopup,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        previewPopup!!.setOnClickListener {
            (previewPopup!!.parent as ViewGroup).removeView(previewPopup)
            previewPopup = null
        }
    }

    // ------------------ Navigation Drawer ------------------
    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {}
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_share -> shareApp()
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.nav_logout -> showLogoutDialog()
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out BrainBoost! Download now:\nhttps://play.google.com/store/apps/details?id=$packageName"
            )
        }
        startActivity(Intent.createChooser(intent, "Share BrainBoost using…"))
    }

    // ------------------ Logout ------------------
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> logout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putBoolean("show_onboarding", true).apply()

        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }


}
