package com.dh.justcalendar

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dh.justcalendar.R
import com.dh.justcalendar.model.Event
import android.util.Log
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private lateinit var eventNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var addCurrentLocationCheckBox: CheckBox

    private lateinit var fusedLocationClient: FusedLocationProviderClient



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventNameEditText = findViewById(R.id.editTextEventName)
        descriptionEditText = findViewById(R.id.editTextDescription)
        dateEditText = findViewById(R.id.editTextDate)
        addCurrentLocationCheckBox = findViewById(R.id.checkBox)

        // TODO: Add date picker for dateEditText
        // add date formater for dateEditText
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        dateEditText.setText(formatter.format(java.time.LocalDate.now()))

        // get addCurrentLocationCheckBox state from shared preferences
        val sharedPref = this.getPreferences(MODE_PRIVATE)
        val defaultValue = false
        val addCurrentLocation = sharedPref.getBoolean("add_current_location", defaultValue)
        addCurrentLocationCheckBox.isChecked = addCurrentLocation

        // addCurrentLocationCheckBox on click listener
        addCurrentLocationCheckBox.setOnClickListener {
            with (sharedPref.edit()) {
                putBoolean("add_current_location", addCurrentLocationCheckBox.isChecked)
                apply()
            }
        }




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val saveButton: Button = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            saveEventToCalendar()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }


    private fun saveEventToCalendar() {
        val eventName = eventNameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val date = dateEditText.text.toString()

        // Check if all fields are filled
        if (eventName.isBlank() || description.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        requestLocation()
    }

    private fun addToCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, event.name)
            .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getMillis())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getMillis() + 60 * 60 * 1000) // 1 hour event

        startActivity(intent)

        resetForm()
    }

    private fun resetForm() {
        Toast.makeText(this, "Event added to calendar", Toast.LENGTH_SHORT).show()

        // Reset form by clearing EditText fields
        eventNameEditText.text.clear()
        descriptionEditText.text.clear()
        dateEditText.text.clear()
        // You can also reset other UI elements as needed
    }

    private fun checkCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCalendarPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_CALENDAR),
            PERMISSION_REQUEST_WRITE_CALENDAR
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_WRITE_CALENDAR = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 2
    }

    // TODO: Move this to LocationHelper.kt
    private fun requestLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.d("MyApp", "1")
                    if (location != null) {
                        Log.d("MyApp", "Event Location: ${location.latitude}") // Log the event location
                        val latLng = LatLng(location.latitude, location.longitude)
                        // TODO: Add reverse geocoding to get address from latLng
                        var address = ""
                        if (addCurrentLocationCheckBox.isChecked) {
                            address = latLng.toString()
                        }

                        // Prepare event details
                        val event = Event(eventNameEditText.text.toString(), descriptionEditText.text.toString(), dateEditText.text.toString(), address)

                        // Save event to calendar
                        if (checkCalendarPermission()) {
                            addToCalendar(event)
                        } else {
                            requestCalendarPermission()

                            if (checkCalendarPermission()) {
                                addToCalendar(event)
                            } else {
                                Toast.makeText(this, "Calendar permission denied", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            val result = ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            if (checkLocationPermission()) {
                requestLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
