package com.example.iabmob

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.iabmob.databinding.FragmentResultsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ResultsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentResultsBinding
    private val args: ResultsFragmentArgs by navArgs()
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startLocation = args.startLocation
        val destination = args.destination

        binding.tvStartLocation.text = getString(R.string.start_location, startLocation)
        binding.tvDestination.text = getString(R.string.destination, destination)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        getTravelTimeAndDrawRoute(args.startLat, args.startLng, args.endLat, args.endLng)
    }

    private fun getTravelTimeAndDrawRoute(startLat: String, startLng: String, endLat: String, endLng: String) {
        val apiKey = "AIzaSyDcsRXFdlMQ6NZlmC127DN5g2c6kEy2XQw"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$startLat,$startLng&destination=$endLat,$endLng&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to get travel time", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonResponse = JSONObject(responseBody.string())
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val duration = legs.getJSONObject(0).getJSONObject("duration")
                        val travelTime = duration.getString("text")

                        val overviewPolyline = routes.getJSONObject(0).getJSONObject("overview_polyline")
                        val points = overviewPolyline.getString("points")

                        requireActivity().runOnUiThread {
                            binding.tvEstimatedTime.text = HtmlCompat.fromHtml(
                                getString(R.string.estimated_time, travelTime),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            binding.tvBestOption.text = HtmlCompat.fromHtml(
                                getString(R.string.best_option, "Carro"), // Moque como "Carro"
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )

                            drawRouteOnMap(points, startLat.toDouble(), startLng.toDouble(), endLat.toDouble(), endLng.toDouble())
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "No route found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun drawRouteOnMap(points: String, startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        val polylineOptions = PolylineOptions()
        val decodedPath = decodePolyline(points)

        map.addMarker(MarkerOptions().position(LatLng(startLat, startLng)).title("Início"))
        map.addMarker(MarkerOptions().position(LatLng(endLat, endLng)).title("Destino"))

        polylineOptions.addAll(decodedPath)
        polylineOptions.width(12f)
        polylineOptions.color(android.graphics.Color.RED)
        polylineOptions.geodesic(true)

        map.addPolyline(polylineOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(startLat, startLng), 14f))
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }
        return poly
    }
}
