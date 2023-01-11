package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var selectedLocation: LatLng
    private var selectedLocationDescription: String? = null
    private var pointOfInterest: PointOfInterest? = null
    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    private var marker: Marker? = null
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.saveBtn.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }
    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        setMapLongCLick(map)
        setPoiCLick(map)
        setMapStyle(map)
        enableMyLocation()
    }
    private fun setMapLongCLick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            selectedLocation = latLng
            selectedLocationDescription = getString(R.string.dropped_pin)
        }
    }

    private fun setPoiCLick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            poiMarker!!.showInfoWindow()
            //Save the poi
            pointOfInterest = poi
        }
    }
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            getUserLoc()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun getUserLoc(){
        // check the location permission
        if (this.context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && this.context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //Get the user's location
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location.let {
                    if (it != null) {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        val snippet = String.format(
                            Locale.getDefault(),
                            "Lat: %1$.5f, Long: %2$.5f",
                            it.latitude,
                            it.longitude
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        map.addMarker(
                            MarkerOptions().position(userLocation).title("Your Location")
                                .snippet(snippet)
                        )
                    } else {
                        getUserLoc()
                    }
                }
            }
    }

    private fun onLocationSelected() {
        if (pointOfInterest != null) {
            // the user select location from poi
            _viewModel.selectedPOI.value = pointOfInterest
            _viewModel.reminderSelectedLocationStr.value = pointOfInterest!!.name
            _viewModel.longitude.value = pointOfInterest!!.latLng.longitude
            _viewModel.latitude.value = pointOfInterest!!.latLng.latitude
            // navigate back
            _viewModel.navigationCommand.value = NavigationCommand.Back

            Log.i(TAG, _viewModel.selectedPOI.value!!.name)
        } else if (selectedLocationDescription != null) {
            // when user select a random location
            _viewModel.reminderSelectedLocationStr.value = selectedLocationDescription
            _viewModel.latitude.value = selectedLocation.latitude
            _viewModel.longitude.value = selectedLocation.longitude
            _viewModel.navigationCommand.value = NavigationCommand.Back
            Log.i(TAG, _viewModel.reminderSelectedLocationStr.value.toString())
        } else {
            Toast.makeText(requireContext(), getString(R.string.select_poi), Toast.LENGTH_SHORT)
                .show()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType =GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType =GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType =GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType =GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
