package com.example.memorease

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.FragmentUploadImageBinding
import com.example.memorease.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class UploadImageFragment : Fragment() {

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var uploadedBy: String = "Unknown"
    private var targetUserId: String? = null
    private var generatedQuestion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUserId = arguments?.getString("targetUserId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)

        val config = mapOf(
            "cloud_name" to "dqkgrjl0b",
            "api_key" to "713947242619374",
            "api_secret" to "3QkSfPPV2bC9cHOpO6iP_x4vUnc"
        )
        MediaManager.init(requireContext(), config)

        fetchUploaderName()

        binding.addImageButton.setOnClickListener {
            checkAndRequestGalleryPermission()
        }

        binding.generateQuestionButton.setOnClickListener {
            val description = binding.commentBox.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a comment before generating a question.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateQuestionFromDescription(description)
        }

        binding.uploadButton.setOnClickListener {
            val comment = binding.commentBox.text.toString().trim()
            val answer = binding.answerBox.text.toString().trim()

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please select an image.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (generatedQuestion.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please generate a question first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (answer.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImageToCloudinary(selectedImageUri!!, comment, answer)
        }

        return binding.root
    }

    private fun fetchUploaderName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val surname = document.getString("surname") ?: ""
                    uploadedBy = "$name $surname"
                } else {
                    firestore.collectionGroup("relatives")
                        .whereEqualTo("email", FirebaseAuth.getInstance().currentUser?.email)
                        .get()
                        .addOnSuccessListener { relativeDocs ->
                            if (!relativeDocs.isEmpty) {
                                val relative = relativeDocs.documents[0]
                                uploadedBy = relative.getString("fullName") ?: "Unknown Relative"
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user info: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToCloudinary(imageUri: Uri, comment: String, answer: String) {
        CloudinaryService.uploadFile(imageUri, "memories", "image", onSuccess = { imageUrl ->
            saveMemoryToFirestore(imageUrl, comment, answer)
        }, onError = { error ->
            Toast.makeText(requireContext(), "Cloudinary Error: $error", Toast.LENGTH_LONG).show()
        })
    }

    private fun saveMemoryToFirestore(imageUrl: String, comment: String, answer: String) {
        val actualUserId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "image",
            "url" to imageUrl,
            "uploadedBy" to uploadedBy,
            "description" to comment,
            "question" to generatedQuestion,
            "answer" to answer,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(actualUserId)
            .collection("memories").document(memoryId)
            .set(memoryData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory successfully uploaded with question and answer!", Toast.LENGTH_LONG).show()
                navigateToHomeFragment()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateQuestionFromDescription(description: String) {
        if (description.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a comment before generating a question.", Toast.LENGTH_SHORT).show()
            return
        }

        val input = QuestionRequest(text = description)
        RetrofitClient.api.generateQuestion(input).enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                if (response.isSuccessful) {
                    val questionText = response.body()?.question
                    generatedQuestion = questionText

                    if (!questionText.isNullOrEmpty()) {
                        binding.generatedQuestionTextView.text = questionText
                    } else {
                        binding.generatedQuestionTextView.text = "No question generated."
                    }
                } else {
                    binding.generatedQuestionTextView.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                binding.generatedQuestionTextView.text = "Network error: ${t.message}"
            }
        })
    }

    private fun navigateToHomeFragment() {
        val uploadMemoryFragment = UploadMemoryFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, uploadMemoryFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissions(arrayOf(permission), GALLERY_PERMISSION_CODE)
        }
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(binding.addImageButton)
                }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1001
    }
}
