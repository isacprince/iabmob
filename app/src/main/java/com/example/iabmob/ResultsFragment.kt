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
import com.google.android.gms.maps.model.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ResultsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentResultsBinding
    private val args: ResultsFragmentArgs by navArgs()
    private lateinit var map: GoogleMap
    private var fastestRouteDuration = Int.MAX_VALUE
    private var fastestMode: String? = null
    private var fastestPoints: JSONArray? = null

    private var cheapestRouteCost = Double.MAX_VALUE
    private var cheapestMode: String? = null
    private var cheapestPoints: JSONArray? = null

    private var sustainablePoints: JSONArray? = null
    private var sustainableMode: String? = null

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

        // Configura o botão para exibir a rota mais rápida
        binding.btnShowFastestRoute.setOnClickListener {
            fastestPoints?.let {
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())
            } ?: Toast.makeText(requireContext(), "Nenhuma rota salva", Toast.LENGTH_SHORT).show()
        }

        // Configura o botão para exibir a rota mais barata
        binding.btnShowCheapestRoute.setOnClickListener {
            cheapestPoints?.let {
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())
            } ?: Toast.makeText(requireContext(), "Nenhuma rota salva", Toast.LENGTH_SHORT).show()
        }

        // Configura o botão para exibir a rota mais sustentável
        binding.btnShowSustainableRoute.setOnClickListener {
            sustainablePoints?.let {
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())
            } ?: Toast.makeText(requireContext(), "Nenhuma rota sustentável encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        getRoutes(args.startLat, args.startLng, args.endLat, args.endLng)
    }

    private fun getRoutes(startLat: String, startLng: String, endLat: String, endLng: String) {
        val apiKey = "AIzaSyDcsRXFdlMQ6NZlmC127DN5g2c6kEy2XQw"
        val modes = listOf("driving", "transit", "walking", "bicycling")

        for (mode in modes) {
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$startLat,$startLng&destination=$endLat,$endLng&mode=$mode&key=$apiKey"
            fetchRoute(url, mode)
        }
    }

    private fun fetchRoute(url: String, mode: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Falha ao obter a rota para $mode", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonResponse = JSONObject(responseBody.string())
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("value").toInt()
                        val travelTime = legs.getJSONObject(0).getJSONObject("duration").getString("text")
                        val cost = calculateCost(mode, legs)

                        requireActivity().runOnUiThread {
                            if (mode == "driving" || mode == "transit") {
                                checkAndDisplayFastestRoute(mode, travelTime, duration, legs)
                                checkAndDisplayCheapestRoute(mode, cost, legs)
                            } else {
                                checkAndDisplaySustainableRoute(mode, duration, legs)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun calculateCost(mode: String, legs: JSONArray): Double {
        return when (mode) {
            "driving" -> {
                val distanceInMeters = legs.getJSONObject(0).getJSONObject("distance").getString("value").toInt()
                val distanceInKm = distanceInMeters / 1000.0
                distanceInKm * 1.40
            }
            "transit" -> {
                var transitCost = 0.0
                for (i in 0 until legs.length()) {
                    val leg = legs.getJSONObject(i)
                    val steps = leg.getJSONArray("steps")
                    for (j in 0 until steps.length()) {
                        val step = steps.getJSONObject(j)
                        if (step.getString("travel_mode") == "TRANSIT") {
                            transitCost += 4.40
                        }
                    }
                }
                transitCost
            }
            else -> 0.0
        }
    }

    private fun checkAndDisplayFastestRoute(mode: String, travelTime: String, duration: Int, legs: JSONArray) {
        if (duration < fastestRouteDuration) {
            fastestRouteDuration = duration
            fastestMode = mode
            fastestPoints = legs
        }

        binding.tvBestOption.text = HtmlCompat.fromHtml(
            getString(R.string.best_option, "$fastestMode: $travelTime"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun checkAndDisplayCheapestRoute(mode: String, cost: Double, legs: JSONArray) {
        if (cost < cheapestRouteCost) {
            cheapestRouteCost = cost
            cheapestMode = mode
            cheapestPoints = legs
        }

        binding.tvCheapestOption.text = HtmlCompat.fromHtml(
            getString(R.string.cheapest_option, "$cheapestMode: R$%.2f".format(cheapestRouteCost)),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun checkAndDisplaySustainableRoute(mode: String, duration: Int, legs: JSONArray) {
        // Prioridade: walking < bicycle < transit, se walking/bicycle > 1hr
        if (mode == "walking" && duration <= 3600) {
            sustainableMode = mode
            sustainablePoints = legs
        } else if (mode == "bicycling" && duration <= 3600 && sustainableMode != "walking") {
            sustainableMode = mode
            sustainablePoints = legs
        } else if (mode == "transit" && sustainableMode == null) {
            sustainableMode = mode
            sustainablePoints = legs
        }

        binding.tvSustainableOption.text = HtmlCompat.fromHtml(
            getString(R.string.sustainable_option, sustainableMode, legs.getJSONObject(0).getJSONObject("duration").getString("text")),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun drawRouteOnMap(legs: JSONArray, startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        map.clear()

        map.addMarker(MarkerOptions().position(LatLng(startLat, startLng)).title("Início"))
        map.addMarker(MarkerOptions().position(LatLng(endLat, endLng)).title("Destino"))

        for (i in 0 until legs.length()) {
            val leg = legs.getJSONObject(i)
            val steps = leg.getJSONArray("steps")

            for (j in 0 until steps.length()) {
                val step = steps.getJSONObject(j)
                val points = decodePolyline(step.getJSONObject("polyline").getString("points"))
                val polylineOptions = PolylineOptions().addAll(points).width(12f).geodesic(true)

                when (step.getString("travel_mode")) {
                    "WALKING" -> {
                        polylineOptions.pattern(listOf(Dot(), Gap(10f)))
                        polylineOptions.color(android.graphics.Color.GRAY)
                    }
                    "TRANSIT" -> {
                        polylineOptions.color(generateRandomColor())
                    }
                    else -> {
                        polylineOptions.color(android.graphics.Color.RED)
                    }
                }

                map.addPolyline(polylineOptions)
            }
        }

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

    private fun generateRandomColor(): Int {
        val random = java.util.Random()
        return android.graphics.Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

}
