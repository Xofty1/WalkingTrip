package com.hoofit.app

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.hoofit.app.data.Trail
import com.hoofit.app.databinding.DialogTrailInfoBinding
import com.hoofit.app.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val PERMISSIONS_REQUEST_FINE_LOCATION = 1
    private var binding: FragmentMapBinding? = null
    private lateinit var mapObjectTapListeners: Array<MapObjectTapListener?>
    private var mapTrailListener: MapObjectTapListener? = null
    private var dialog: AlertDialog? = null
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var mapView: MapView
    private var currentLocation: Location? = null
    private var userLocationMarker: PlacemarkMapObject? = null

    private fun showTrailInfoDialog(trail: Trail, polyline: PolylineMapObject) {
        dialog?.takeIf { it.isShowing }?.dismiss()

        val bindingInfo = DialogTrailInfoBinding.inflate(layoutInflater)
        bindingInfo.dialogTitle.text = trail.name
        bindingInfo.dialogMessage.text = trail.description

        val builder = AlertDialog.Builder(context)
        builder.setView(bindingInfo.root)
        dialog = builder.create()

        bindingInfo.root.scaleX = 0f
        bindingInfo.root.scaleY = 0f

        dialog?.setOnShowListener {
            bindingInfo.root.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start()
        }


        dialog?.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingInfo.buttonDismiss.setOnClickListener {
            dialog?.dismiss()
            polyline.setStrokeColor(resources.getColor(R.color.orange))
        }

        bindingInfo.buttonToTrail.setOnClickListener {
            dialog?.dismiss()
            polyline.setStrokeColor(resources.getColor(R.color.orange))
//            val fragment = InfoTrailFragment()
//            val bundle = Bundle().apply {
//                putSerializable("trail", trail)
//            }
//            fragment.arguments = bundle
//            val transaction = parentFragmentManager.beginTransaction()
//            MainActivity.makeTransaction(transaction, fragment)
        }

        dialog?.setOnDismissListener {
            polyline.setStrokeColor(resources.getColor(R.color.orange))
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates() // Остановить обновление местоположения при переходе на другой фрагмент
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates() // Возобновить обновление местоположения при возврате на фрагмент
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
//        viewModel.loadTrails()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        MapKitFactory.initialize(requireContext())
        binding = FragmentMapBinding.inflate(layoutInflater)
        mapView = binding!!.mapView
        mapObjects = mapView.map.mapObjects.addCollection()
        requestLocationPermission()

        val bundle = arguments
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    updateMarker(Point(location.latitude, location.longitude))
                    Log.d(
                        "Location",
                        "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    )
                    if (currentLocation == null && bundle == null) {
                        mapView.map.move(
                            CameraPosition(
                                Point(location.latitude, location.longitude),
                                5.0f, 0.0f, 0.0f
                            )
                        )
                    }
                    currentLocation = location
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }

        if (bundle != null) {
            val trail = bundle.getSerializable("trail") as Trail
            makeTrail(trail)
        } else {
            makeMap()
        }

        return binding!!.root
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun updateMarker(point: Point) {
        if (userLocationMarker == null) {
            userLocationMarker = mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(requireContext(), R.drawable.search_result)
            )
        } else {
            userLocationMarker!!.geometry = point
        }
        userLocationMarker!!.setIconStyle(IconStyle().setScale(0.7f))
    }

    fun makeMap() {


//        mapView.getMap().move(new CameraPosition(new Point(reserves.getReserves().get(0).getTrails().get(0).getCoordinatesList().get(0).getLatitude(), reserves.getReserves().get(0).getTrails().get(0).getCoordinatesList().get(0).getLongitude()), 5.0F, 0.0F, 0.0F));
        val trailsCount = if (HoofitApp.allTrails == null)
            0
        else
            HoofitApp.allTrails!!.size
        Log.d("firebase", "size $trailsCount")
        mapObjectTapListeners = arrayOfNulls(trailsCount)

        for (i in 0 until trailsCount) {
            val points =
                HoofitApp.allTrails!!.get(i).coordinatesList?.map {
                    Point(
                        it.latitude,
                        it.longitude
                    )
                }
            val polyline = points?.let { Polyline(it) }?.let { mapObjects.addPolyline(it) }
            mapObjectTapListeners[i] =
                polyline?.let { createTapListener(HoofitApp.allTrails!![i], it) }
            mapObjectTapListeners[i]?.let { polyline!!.addTapListener(it) }
            polyline!!.setStrokeColor(resources.getColor(R.color.orange))
        }
    }

    fun makeTrail(trail: Trail) {
        val startPoint = trail.coordinatesList?.get(0)
        mapView.map.move(
            CameraPosition(
                Point(startPoint!!.latitude, startPoint.longitude),
                8.0f,
                0.0f,
                0.0f
            )
        )
        val points = trail.coordinatesList!!.map { Point(it.latitude, it.longitude) }
        val polyline = mapObjects.addPolyline(Polyline(points))
        mapTrailListener = createTapListener(trail, polyline)
        polyline.addTapListener(mapTrailListener!!)
        polyline.setStrokeColor(resources.getColor(R.color.orange))
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    private fun createTapListener(trail: Trail, polyline: PolylineMapObject): MapObjectTapListener {
        return MapObjectTapListener { _, _ ->
            polyline.setStrokeColor(resources.getColor(R.color.dark_blue))
            showTrailInfoDialog(trail, polyline)
            true
        }
    }
}
