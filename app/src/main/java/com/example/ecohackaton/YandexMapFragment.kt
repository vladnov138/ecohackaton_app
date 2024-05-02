package com.example.ecohackaton

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ecohackaton.databinding.FragmentYandexMapBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YandexMapFragment : Fragment() {
    private var _binding: FragmentYandexMapBinding? = null
    private val binding get() = _binding!!
    private val CIRCLE_CENTER: Point = Point(59.956, 30.323)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapKitFactory.setApiKey("")
        _binding = FragmentYandexMapBinding.inflate(inflater)
        val locationManager = requireContext().getSystemService(android.location.LocationManager::class.java)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@onCreateView binding.root
        }
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        MapKitFactory.initialize(context)
        binding.mapview.getMap().move(
            CameraPosition(Point(location?.latitude ?: 0.0, location?.longitude ?: 0.0), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0.2f),
            null
        )
        val circle = Circle(
            Point(location?.latitude ?: 0.0, location?.longitude ?: 0.0),
            400f
        )
        binding.mapview.getMap().mapObjects.addCircle(circle).apply {
            strokeWidth = 2f
            strokeColor = ContextCompat.getColor(requireContext(), R.color.red)
            fillColor = ContextCompat.getColor(requireContext(), R.color.red)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YandexMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}