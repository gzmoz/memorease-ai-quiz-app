package com.example.memorease

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.FragmentUploadTextBinding
import com.example.memorease.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class UploadTextFragment : Fragment() {

    private var _binding: FragmentUploadTextBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var uploadedBy: String = "Unknown"
    private var generatedQuestion: String? = null  // 🔥 Üretilen soruyu saklıyoruz

    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUserId = arguments?.getString("targetUserId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadTextBinding.inflate(inflater, container, false)

        fetchUploaderName()

        binding.generateQuestionButton.setOnClickListener {
            val description = binding.commentBox.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a memory description.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateQuestionFromDescription(description)
        }

        binding.uploadButton.setOnClickListener {
            val description = binding.commentBox.text.toString().trim()
            val answer = binding.answerBox.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a memory description.", Toast.LENGTH_SHORT).show()
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

            saveTextMemoryToFirestore(description, generatedQuestion!!, answer)
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


    private fun generateQuestionFromDescription(description: String) {
        if (description.isBlank()) {
            Toast.makeText(requireContext(), "Description cannot be empty!", Toast.LENGTH_SHORT).show()
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


    /**
     * Firestore'a Description + Soru + Cevap Kaydet
     */
    private fun saveTextMemoryToFirestore(description: String, question: String, answer: String) {
        //val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val actualUserId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "text",
            "description" to description,
            "question" to question,
            "answer" to answer,
            "uploadedBy" to uploadedBy,
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

    private fun navigateToHomeFragment() {
        val uploadMemoryFragment = UploadMemoryFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, uploadMemoryFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}