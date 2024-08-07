package com.example.iabmob

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.iabmob.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentMapBinding
    private val locationPermissionRequestCode = 1
    private var lastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationPermission()

        binding.btnCalculateRoute.setOnClickListener {
            val startLocation = binding.etStartLocation.text.toString()
            val destination = binding.etDestination.text.toString()
            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                calculateRoute(startLocation, destination)
            } else {
                Toast.makeText(requireContext(), "Please enter both locations.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                map.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission is necessary", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
    }

    private fun calculateRoute(startLocation: String, destination: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val startAddresses = geocoder.getFromLocationName(startLocation, 1)
            val endAddresses = geocoder.getFromLocationName(destination, 1)

            if (startAddresses != null && endAddresses != null &&
                startAddresses.isNotEmpty() && endAddresses.isNotEmpty()) {

                val startLatLng = LatLng(startAddresses[0].latitude, startAddresses[0].longitude)
                val endLatLng = LatLng(endAddresses[0].latitude, endAddresses[0].longitude)

                // Navegar para o ResultsFragment com os dados
                val action = MapFragmentDirections.actionMapFragmentToResultsFragment(
                    startLocation = startLocation,
                    destination = destination,
                    startLat = startLatLng.latitude.toString(),
                    startLng = startLatLng.longitude.toString(),
                    endLat = endLatLng.latitude.toString(),
                    endLng = endLatLng.longitude.toString()
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Unable to find locations.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error finding locations.", Toast.LENGTH_SHORT).show()
        }
    }
}
