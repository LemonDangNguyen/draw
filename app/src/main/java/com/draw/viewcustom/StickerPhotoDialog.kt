package com.draw.viewcustom

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.draw.R
import com.draw.adapter.ImageAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerPhotoDialog : BottomSheetDialogFragment() {
    private lateinit var rvImages: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var imagePaths: MutableList<String> = mutableListOf()
    // Định nghĩa hằng số REQUEST_PERMISSION
    companion object {
        private const val REQUEST_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView = inflater.inflate(R.layout.bottom_sheet_insert_photo, container, false)

        // Thiết lập RecyclerView
        rvImages = dialogView.findViewById(R.id.rvImages)
        rvImages.layoutManager = GridLayoutManager(context, 3)

        // Kiểm tra và yêu cầu quyền truy cập
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            loadImages()
        }

        return dialogView
    }

    private fun loadImages() {

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor: Cursor? = context?.contentResolver?.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                imagePaths.add(imagePath)
                Log.d("2tdp", "Found image path: $imagePath")
            }
            Log.d("2tdp", "Total images loaded: ${imagePaths.size}")

            imageAdapter = ImageAdapter(requireContext(), imagePaths)
            rvImages.adapter = imageAdapter
        } ?: run {
            Log.d("2tdp", "No images found")
            Toast.makeText(context, "No images found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("2tdp", "Requesting permissions result")
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("2tdp", "Permission granted, loading images")
                loadImages()
            } else {
                Log.d("2tdp", "Permission denied")
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
