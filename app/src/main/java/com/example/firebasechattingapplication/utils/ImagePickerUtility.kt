package com.example.firebasechattingapplication.utils


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.example.firebasechattingapplication.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

abstract class ImagePickerUtility : Fragment() {

    private var mActivity: Activity? = null
    private var mCode = 0
    private lateinit var mImageFile: File
    private var type = ""

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED  //permission for android 14 -> need to handle the selected photos
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsFor13 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )

    private val permissions1 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.isNotEmpty()) {
                val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                val camera = permissions[Manifest.permission.CAMERA]
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    if (camera == true) {
                        imageDialog()
                    } else {
                        checkPermissionDenied(permissions.keys.first())
                    }
                } else {
                    if (readStorage == true && writeStorage == true && camera == true) {
                        imageDialog()
                    } else {
                        checkPermissionDenied(permissions.keys.first())
                    }
                }
            }
        }
    private fun rotateImageIfRequired(imageFile: File): File {
        val path = imageFile.absolutePath
        val ei = androidx.exifinterface.media.ExifInterface(path)

        val orientation = ei.getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        )

        // If it's already normal, don't waste memory processing it
        if (orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL) {
            return imageFile
        }

        val bitmap = BitmapFactory.decodeFile(path)
        val matrix = android.graphics.Matrix()

        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return imageFile
        }

        val rotatedBitmap = android.graphics.Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // Overwrite the original file with the correctly rotated image
        imageFile.outputStream().use { out ->
            rotatedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)  //compress
        }
        bitmap.recycle()
        rotatedBitmap.recycle()

        return imageFile
    }

    private val imageCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val rotatedFile = rotateImageIfRequired(mImageFile)  //fix rotation of image to right
                val uri = Uri.fromFile(rotatedFile)
                val picturePath = rotatedFile.absolutePath


                val sizeInBytes = rotatedFile.length() // size in Bytes
                val sizeInKB = sizeInBytes / 1024

                Log.d("lerkglkejrg", "Camera Image Size: $sizeInKB KB")

                selectedImage(picturePath, mCode, type, uri)
            }
        }

    private val imageGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                val picturePath = getAbsolutePath(uri!!)

                // Get size using ContentResolver
                val inputStream = requireActivity().contentResolver.openAssetFileDescriptor(uri, "r")
                val sizeInBytes = inputStream?.length ?: 0
                val sizeInKB = sizeInBytes / 1024
                inputStream?.close()

                Log.d("lerkglkejrg", "Gallery Image Size: $sizeInKB KB")

                selectedImage(picturePath, mCode, type, uri)
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasPermissions(permissionsFor13)) {
                imageDialog()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                checkPermissionDenied(Manifest.permission.CAMERA)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_IMAGES)//00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000103
            }
            else {
                requestPermission()
            }
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (hasPermissions(permissions)) {
                    imageDialog()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    checkPermissionDenied(Manifest.permission.CAMERA)
                } else {
                    requestPermission()
                }

            } else {
                if (hasPermissions(permissions1)) {
                    imageDialog()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    checkPermissionDenied(Manifest.permission.CAMERA)
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun imageDialog() {
        mActivity = requireActivity()
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.camera_gallery_popup)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val camera = dialog.findViewById<TextView>(R.id.tvCamera)
        val gallery = dialog.findViewById<TextView>(R.id.tvGallery)
        val cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            captureImage()
        }
        gallery.setOnClickListener {
            dialog.dismiss()
            openGalleryForImage()
        }
        dialog.show()
    }

    private fun captureImage() {
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            Objects.requireNonNull(requireActivity()),
            "com.example.firebasechattingapplication.fileprovider", photoFile!!
        )
        mImageFile = photoFile
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        imageCameraLauncher.launch(intent)
    }

    private fun openGalleryForImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageGalleryLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionDenied(permissions: String) {
        mActivity = requireActivity()
        if (shouldShowRequestPermissionRationale(permissions)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle("Permissions Required")
                    .setMessage("Please allow permissions to fetch image")
                    .setPositiveButton(
                        "Ok"
                    ) { dialog, which -> requestPermission() }
                    .setNegativeButton(
                        "Cancel"
                    ) { dialog, which ->

                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.black
                    )
                )
            }
            dialog.show()
        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle("Permissions Required")
                    .setMessage("Camera and gallery permission is required to share the media in chat.")
                    .setCancelable(false)
                    .setPositiveButton("Open settings") { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                requireActivity().packageName,
                                null
                            )
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.black
                    )
                )
            }
            dialog.show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMultiplePermissions.launch(permissionsFor13)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            requestMultiplePermissions.launch(permissions)
        } else {
            requestMultiplePermissions.launch(permissions1)
        }
    }

    @SuppressLint("Recycle")
    private fun getAbsolutePath(uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity!!.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }


    abstract fun selectedImage(imagePath: String?, code: Int, type: String, uri: Uri)

}