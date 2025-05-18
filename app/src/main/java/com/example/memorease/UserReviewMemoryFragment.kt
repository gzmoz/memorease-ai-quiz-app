package com.example.memorease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorease.adapters.MemoryAdapter
import com.example.memorease.databinding.FragmentUserReviewMemoryBinding
import com.example.memorease.models.Memory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserReviewMemoryFragment : Fragment() {

    private var _binding: FragmentUserReviewMemoryBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val memories = mutableListOf<Memory>()
    private var targetUserId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserReviewMemoryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchMemoriesFromFirestore()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUserId = arguments?.getString("targetUserId")
    }

    /*private fun setupRecyclerView() {
        val adapter = MemoryAdapter(requireContext(), memories)
        binding.recyclerViewMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMemories.adapter = adapter
    }*/

    private fun setupRecyclerView() {
        val adapter = MemoryAdapter(
            requireContext(),
            memories,
            onDeleteClick = { memoryToDelete ->
                showDeleteConfirmationDialog(memoryToDelete)
            },
            onEditClick = { memoryToEdit ->
                navigateToEditFragment(memoryToEdit)
            }
        )
        binding.recyclerViewMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMemories.adapter = adapter
    }

    private fun navigateToEditFragment(memory: Memory) {
        val bundle = Bundle().apply {
            putString("memoryId", memory.id)
            putString("description", memory.description)
            putString("type", memory.type)
            putString("url", memory.url)
            putString("targetUserId", targetUserId)  // ✅ BURASI ÖNEMLİ
        }

        val editFragment = EditMemoryFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, editFragment)
            .addToBackStack(null)
            .commit()
    }




    private fun showDeleteConfirmationDialog(memory: Memory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory?")
            .setPositiveButton("Yes") { _, _ ->
                deleteMemoryFromFirestore(memory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMemoryFromFirestore(memory: Memory) {
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("memories").document(memory.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory deleted", Toast.LENGTH_SHORT).show()
                memories.remove(memory)
                binding.recyclerViewMemories.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



    private fun fetchMemoriesFromFirestore() {
        // Eğer targetUserId geldiyse onu kullan, yoksa giriş yapan user'ı
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { documents ->
                memories.clear()
                for (document in documents) {
                    val memory = document.toObject(Memory::class.java)
                    memory.id = document.id
                    memories.add(memory)
                }
                binding.recyclerViewMemories.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load memories: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }




}
