package com.example.memorease

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.memorease.databinding.FragmentEditMemoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditMemoryFragment : Fragment() {

    private var _binding: FragmentEditMemoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var memoryId: String
    private var initialDescription: String? = null
    private var initialType: String? = null
    private var initialUrl: String? = null
    private var selectedMediaUri: Uri? = null

    private val firestore = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var targetUserId: String? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.data != null) {
                selectedMediaUri = it.data?.data
                binding.imageViewPreview.setImageURI(selectedMediaUri)
            }
        }

    private val audioPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.data != null) {
                selectedMediaUri = it.data?.data
                Toast.makeText(requireContext(), "New audio selected", Toast.LENGTH_SHORT).show()
                isPlaying = false
                binding.playButton.setImageResource(R.drawable.ic_play)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        targetUserId = arguments?.getString("targetUserId")
        memoryId = arguments?.getString("memoryId") ?: return
        initialDescription = arguments?.getString("description")
        initialType = arguments?.getString("type")
        initialUrl = arguments?.getString("url")

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.memory_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter

        binding.editTextDescription.setText(initialDescription)
        val typeIndex = adapter.getPosition(initialType)
        binding.spinnerType.setSelection(if (typeIndex >= 0) typeIndex else 0)

        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("memories").document(memoryId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val question = document.getString("question") ?: ""
                    val answer = document.getString("answer") ?: ""
                    binding.editTextQuestion.setText(question)
                    binding.editTextAnswer.setText(answer)
                }
            }

        when (initialType) {
            "image" -> {
                binding.imageViewPreview.visibility = View.VISIBLE
                binding.audioSection.visibility = View.GONE
                initialUrl?.let { url ->
                    Glide.with(requireContext()).load(url).into(binding.imageViewPreview)
                }
                binding.imageViewPreview.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher.launch(intent)
                }
            }

            "voice" -> {
                binding.audioSection.visibility = View.VISIBLE
                binding.imageViewPreview.visibility = View.GONE

                binding.playButton.setOnClickListener {
                    val uriToPlay = selectedMediaUri?.toString() ?: initialUrl
                    if (uriToPlay != null) {
                        if (!isPlaying) {
                            playAudio(uriToPlay)
                        } else {
                            pauseAudio()
                        }
                    }
                }

                binding.editAudioButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "audio/*"
                    audioPickerLauncher.launch(intent)
                }
            }

            else -> {
                binding.imageViewPreview.visibility = View.GONE
                binding.audioSection.visibility = View.GONE
            }
        }

        binding.buttonSaveChanges.setOnClickListener {
            updateMemoryInFirestore()
        }
    }

    private fun playAudio(audioUrl: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), Uri.parse(audioUrl))
            prepare()
            start()
            this@EditMemoryFragment.isPlaying = true
            binding.playButton.setImageResource(R.drawable.ic_pause)

            setOnCompletionListener {
                this@EditMemoryFragment.isPlaying = false
                binding.playButton.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        binding.playButton.setImageResource(R.drawable.ic_play)
    }

    private fun updateMemoryInFirestore() {
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updatedDescription = binding.editTextDescription.text.toString().trim()
        val updatedType = binding.spinnerType.selectedItem.toString()
        val updatedUrl = selectedMediaUri?.toString() ?: initialUrl
        val updatedQuestion = binding.editTextQuestion.text.toString().trim()
        val updatedAnswer = binding.editTextAnswer.text.toString().trim()

        if (updatedDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "description" to updatedDescription,
            "type" to updatedType,
            "url" to updatedUrl,
            "question" to updatedQuestion,
            "answer" to updatedAnswer
        )

        firestore.collection("users").document(userId)
            .collection("memories").document(memoryId)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory updated successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }
}
