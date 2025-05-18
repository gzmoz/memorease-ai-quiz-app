package com.example.memorease

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.FragmentUploadVoiceBinding
import com.example.memorease.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class UploadVoiceFragment : Fragment() {

    private var _binding: FragmentUploadVoiceBinding? = null
    private val binding get() = _binding!!

    private var selectedVoiceUri: Uri? = null
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    private var uploadedBy: String = "Unknown"
    private var generatedQuestion: String? = null

    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUserId = arguments?.getString("targetUserId")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadVoiceBinding.inflate(inflater, container, false)

        CloudinaryService.init(requireContext())
        fetchUploaderName()

        binding.playVoiceButton.text = "Add Voice"

        binding.playVoiceButton.setOnClickListener {
            when (binding.playVoiceButton.text) {
                "Add Voice" -> openAudioPicker()
                "Play Voice" -> playVoice()
                "Pause Voice" -> pauseVoice()
            }
        }

        binding.generateQuestionButton.setOnClickListener {
            val description = binding.commentBox.text.toString().trim()

            if (description.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a comment before generating a question.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateQuestionFromDescription(description)
        }

        binding.uploadButton.setOnClickListener {
            val comment = binding.commentBox.text.toString().trim()
            val answer = binding.answerBox.text.toString().trim()

            if (selectedVoiceUri == null) {
                Toast.makeText(requireContext(), "Please select a voice file.", Toast.LENGTH_SHORT).show()
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

            uploadVoiceToCloudinary(selectedVoiceUri!!, comment, answer)
        }

        return binding.root
    }

    private val selectVoiceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedVoiceUri = result.data?.data
                selectedVoiceUri?.let {
                    setupMediaPlayer(it)
                    binding.playVoiceButton.text = "Play Voice"
                    Toast.makeText(requireContext(), "Audio file selected.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        selectVoiceLauncher.launch(intent)
    }

    private fun setupMediaPlayer(uri: Uri) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(requireContext(), uri)
            mediaPlayer?.setOnCompletionListener {
                binding.playVoiceButton.text = "Play Voice"
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error initializing media player: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun playVoice() {
        mediaPlayer?.let { player ->
            player.start()
            binding.playVoiceButton.text = "Pause Voice"
        }
    }

    private fun pauseVoice() {
        mediaPlayer?.let { player ->
            player.pause()
            binding.playVoiceButton.text = "Play Voice"
        }
    }

    private fun uploadVoiceToCloudinary(audioUri: Uri, comment: String, answer: String) {
        try {
            CloudinaryService.uploadFile(audioUri, "memories", "audio", onSuccess = { audioUrl ->
                saveMemoryToFirestore(audioUrl, comment, answer)
            }, onError = { error ->
                Toast.makeText(requireContext(), "Cloudinary Error: $error", Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Upload Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveMemoryToFirestore(audioUrl: String, comment: String, answer: String) {
        //val userId = FirebaseAuth.getInstance().currentUser?.uid
        val actualUserId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (actualUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "voice",
            "url" to audioUrl,
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


    /* private fun generateQuestionFromDescription(description: String) {
         if (description.isBlank()) {
             Toast.makeText(requireContext(), "Description cannot be empty!", Toast.LENGTH_SHORT).show()
             return
         }

         val input = QuestionRequest(inputs = "Generate a question about: $description")
         RetrofitClient.api.generateQuestion(input).enqueue(object : Callback<List<QuestionResponse>> {
             override fun onResponse(call: Call<List<QuestionResponse>>, response: Response<List<QuestionResponse>>) {
                 if (response.isSuccessful) {
                     val questionText = response.body()?.firstOrNull()?.generated_text
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

             override fun onFailure(call: Call<List<QuestionResponse>>, t: Throwable) {
                 binding.generatedQuestionTextView.text = "Network error: ${t.message}"
             }
         })
     }*/

    private fun fetchUploaderName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val surname = document.getString("surname") ?: ""
                    uploadedBy = "$name $surname"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user info: ${e.message}", Toast.LENGTH_LONG).show()
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
        mediaPlayer?.release()
        _binding=null
        }
}