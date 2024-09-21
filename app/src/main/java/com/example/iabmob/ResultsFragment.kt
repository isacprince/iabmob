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
    private var fastestTime: String = ""
    private var fastestDistance: String = ""
    private var fastestPrice: String = ""

    private var cheapestRouteCost = Double.MAX_VALUE
    private var cheapestMode: String? = null
    private var cheapestPoints: JSONArray? = null
    private var cheapestTime: String = ""
    private var cheapestDistance: String = ""
    private var cheapestPrice: String = ""

    private var sustainablePoints: JSONArray? = null
    private var sustainableMode: String? = null
    private var sustainableTime: String = ""
    private var sustainableDistance: String = ""
    private var sustainablePrice: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

// No onViewCreated(), atualize o texto quando clicar nos botões:

// Configura o botão para exibir a rota mais rápida
        binding.btnShowFastestRoute.setOnClickListener {
            fastestPoints?.let {
                // Mostrar o mapa com a rota mais rápida
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())

                // Exibir as informações da rota mais rápida
                binding.routeInfoContainer.visibility = View.VISIBLE
                binding.tvRouteOptionName.text = "Rota Mais Rápida" // Nome da opção
                binding.tvRouteTime.text = getString(R.string.route_time, fastestTime)
                binding.tvRouteDistance.text = getString(R.string.route_distance, fastestDistance)
                binding.tvRoutePrice.text = getString(R.string.route_price, fastestPrice)
                binding.tvTransportType.text = getString(R.string.transport_type, fastestMode)
            } ?: Toast.makeText(requireContext(), "Nenhuma rota rápida salva", Toast.LENGTH_SHORT).show()
        }

// Configura o botão para exibir a rota mais barata
        binding.btnShowCheapestRoute.setOnClickListener {
            cheapestPoints?.let {
                // Mostrar o mapa com a rota mais barata
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())

                // Exibir as informações da rota mais barata
                binding.routeInfoContainer.visibility = View.VISIBLE
                binding.tvRouteOptionName.text = "Rota Mais Barata" // Nome da opção
                binding.tvRouteTime.text = getString(R.string.route_time, cheapestTime)
                binding.tvRouteDistance.text = getString(R.string.route_distance, cheapestDistance)
                binding.tvRoutePrice.text = getString(R.string.route_price, cheapestPrice)
                binding.tvTransportType.text = getString(R.string.transport_type, cheapestMode)
            } ?: Toast.makeText(requireContext(), "Nenhuma rota barata salva", Toast.LENGTH_SHORT).show()
        }

// Configura o botão para exibir a rota mais sustentável
        binding.btnShowSustainableRoute.setOnClickListener {
            sustainablePoints?.let {
                // Mostrar o mapa com a rota mais sustentável
                drawRouteOnMap(it, args.startLat.toDouble(), args.startLng.toDouble(), args.endLat.toDouble(), args.endLng.toDouble())

                // Exibir as informações da rota mais sustentável
                binding.routeInfoContainer.visibility = View.VISIBLE
                binding.tvRouteOptionName.text = "Rota Mais Sustentável" // Nome da opção
                binding.tvRouteTime.text = getString(R.string.route_time, sustainableTime)
                binding.tvRouteDistance.text = getString(R.string.route_distance, sustainableDistance)
                binding.tvRoutePrice.text = getString(R.string.route_price, sustainablePrice)
                binding.tvTransportType.text = getString(R.string.transport_type, sustainableMode)
            } ?: Toast.makeText(requireContext(), "Nenhuma rota sustentável salva", Toast.LENGTH_SHORT).show()
        }

    }

    private fun translateTransportMode(mode: String): String {
        return when (mode) {
            "driving" -> "carro"
            "transit" -> "público"
            "walking" -> "caminhada"
            "bicycling" -> "bicicleta"
            else -> "desconhecido"
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
                        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                        val cost = calculateCost(mode, legs)

                        requireActivity().runOnUiThread {
                            if (mode == "driving" || mode == "transit") {
                                checkAndDisplayFastestRoute(mode, travelTime, distance, duration, cost, legs)
                                checkAndDisplayCheapestRoute(mode, cost, travelTime, distance, legs)
                            } else {
                                checkAndDisplaySustainableRoute(mode, duration, travelTime, distance, legs)
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

    private fun checkAndDisplayFastestRoute(mode: String, travelTime: String, distance: String, duration: Int, cost: Double, legs: JSONArray) {
        if (duration < fastestRouteDuration) {
            fastestRouteDuration = duration
            fastestMode = mode
            fastestPoints = legs
            fastestTime = travelTime
            fastestDistance = distance
            fastestPrice = String.format("%.2f", cost) + " R$"
        }
    }

    private fun checkAndDisplayCheapestRoute(mode: String, cost: Double, travelTime: String, distance: String, legs: JSONArray) {
        if (cost < cheapestRouteCost) {
            cheapestRouteCost = cost
            cheapestMode = translateTransportMode(mode)
            cheapestPoints = legs
            cheapestTime = travelTime
            cheapestDistance = distance
            cheapestPrice = String.format("%.2f", cost) + " R$"
        }
    }

    private fun checkAndDisplaySustainableRoute(mode: String, duration: Int, travelTime: String, distance: String, legs: JSONArray) {
        if (mode == "walking" && duration <= 3600) {
            sustainableMode = translateTransportMode(mode)
            sustainablePoints = legs
            sustainableTime = travelTime
            sustainableDistance = distance
        } else if (mode == "bicycling" && duration <= 3600 && sustainableMode != "walking") {
            sustainableMode = translateTransportMode(mode)
            sustainablePoints = legs
            sustainableTime = travelTime
            sustainableDistance = distance
        } else if (mode == "transit" && sustainableMode == null) {
            sustainableMode = translateTransportMode(mode)
            sustainablePoints = legs
            sustainableTime = travelTime
            sustainableDistance = distance
        }
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
