package com.example.iabmob

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.iabmob.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("email", "Usuário")

        binding.tvWelcome.text = "Seja Bem-vindo, $userEmail!"

        // Navegar para o MapFragment diretamente
        binding.btnMapa.setOnClickListener {
            findNavController().navigate(R.id.mapFragment)
        }

        binding.btnLogoff.setOnClickListener {
            with(sharedPreferences.edit()) {
                putBoolean("isLoggedIn", false)
                apply()
            }
            findNavController().navigate(R.id.loginFragment)
        }

        return binding.root
    }
}
