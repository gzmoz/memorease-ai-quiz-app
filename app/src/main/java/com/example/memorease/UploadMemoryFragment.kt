package com.example.memorease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.memorease.databinding.FragmentHomeBinding
import com.example.memorease.databinding.FragmentUploadMemoryBinding


class UploadMemoryFragment : Fragment() {

    private var _binding: FragmentUploadMemoryBinding? = null
    private val binding get() = _binding!!
    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUserId = arguments?.getString("targetUserId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadMemoryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTextMemory.setOnClickListener {
            val textFragment = UploadTextFragment().apply {
                arguments = Bundle().apply {
                    putString("targetUserId", targetUserId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, textFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnImageMemory.setOnClickListener {
            val imageFragment = UploadImageFragment().apply {
                arguments = Bundle().apply {
                    putString("targetUserId", targetUserId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, imageFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnVoiceMemory.setOnClickListener {
            val voiceFragment = UploadVoiceFragment().apply {
                arguments = Bundle().apply {
                    putString("targetUserId", targetUserId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, voiceFragment)
                .addToBackStack(null)
                .commit()
        }
    }





}