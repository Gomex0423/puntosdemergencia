package com.example.puntosdeemergencia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private lateinit var tvUbicacion: TextView

    private lateinit var btnMiUbicacion: Button

    private var marcadorUsuario: Marker? = null


    // ==========================================
    // PUNTOS DE EMERGENCIA
    // ==========================================

    private val puntosEmergencia = arrayListOf(

        PuntoEmergencia(
            "Policía",
            "Policía",
            "Estación de Policía",
            5.06889,
            -75.51738,
            "123"
        ),

        PuntoEmergencia(
            "Hospital",
            "Hospital",
            "Hospital Universitario",
            5.06700,
            -75.52000,
            "125"
        ),

        PuntoEmergencia(
            "Bomberos",
            "Bomberos",
            "Estación de Bomberos",
            5.07300,
            -75.51500,
            "119"
        )

    )


    // ==========================================
    // CREAR FUENTE DE MAPA
    // ==========================================

    private val fuenteOpenStreetMap = XYTileSource(

        "OpenStreetMap",

        0,

        19,

        256,

        ".png",

        arrayOf(
            "https://tile.openstreetmap.org/"
        )

    )


    // ==========================================
    // ON CREATE
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        // ======================================
        // CONFIGURACIÓN DE OSMDROID
        // ======================================

        Configuration.getInstance().load(
            applicationContext,

            getSharedPreferences(
                "osmdroid",
                MODE_PRIVATE
            )
        )


        // ======================================
        // USER AGENT
        // ======================================

        Configuration.getInstance()
            .setUserAgentValue(
                "PuntosDeEmergencia/1.0 (Android; com.example.puntosdeemergencia)"
            )


        // ======================================
        // CONECTAR MAPA
        // ======================================

        mapView =
            findViewById(R.id.mapView)


        // ======================================
        // USAR OPENSTREETMAP OFICIAL
        // ======================================

        mapView.setTileSource(
            fuenteOpenStreetMap
        )


        // ======================================
        // CONTROLES
        // ======================================

        mapView.setMultiTouchControls(true)


        // ======================================
        // ZOOM INICIAL
        // ======================================

        mapView.controller.setZoom(15.0)


        // ======================================
        // TEXTVIEW
        // ======================================

        tvUbicacion =
            findViewById(R.id.tvUbicacion)


        // ======================================
        // BOTÓN
        // ======================================

        btnMiUbicacion =
            findViewById(R.id.btnMiUbicacion)


        // ======================================
        // SERVICIO DE UBICACIÓN
        // ======================================

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)


        // ======================================
        // COLOCAR PUNTOS DE EMERGENCIA
        // ======================================

        colocarPuntosEmergencia()


        // ======================================
        // BOTÓN MI UBICACIÓN
        // ======================================

        btnMiUbicacion.setOnClickListener {

            obtenerUbicacion()

        }


        // ======================================
        // PEDIR PERMISO
        // ======================================

        solicitarPermisoUbicacion()

    }


    // ==========================================
    // SOLICITAR PERMISO DE UBICACIÓN
    // ==========================================

    private fun solicitarPermisoUbicacion() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(

                this,

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION

                ),

                100

            )

            return

        }


        obtenerUbicacion()

    }


    // ==========================================
    // OBTENER UBICACIÓN
    // ==========================================

    private fun obtenerUbicacion() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return

        }


        fusedLocationClient
            .lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val latitud =
                        location.latitude

                    val longitud =
                        location.longitude


                    // Mostrar coordenadas

                    tvUbicacion.text =
                        "Mi ubicación:\n" +
                                "Latitud: $latitud\n" +
                                "Longitud: $longitud"


                    // Crear ubicación

                    val ubicacion =
                        GeoPoint(
                            latitud,
                            longitud
                        )


                    // Centrar mapa

                    mapView.controller
                        .setCenter(ubicacion)


                    // Mostrar marcador

                    mostrarUsuario(
                        ubicacion
                    )

                }

            }

    }


    // ==========================================
    // MARCADOR DEL USUARIO
    // ==========================================

    private fun mostrarUsuario(
        ubicacion: GeoPoint
    ) {

        // Eliminar marcador anterior

        if (
            marcadorUsuario != null
        ) {

            mapView.overlays.remove(
                marcadorUsuario
            )

        }


        // Crear marcador

        marcadorUsuario =
            Marker(mapView)


        marcadorUsuario!!.position =
            ubicacion


        marcadorUsuario!!.title =
            "Mi ubicación"


        // Agregar al mapa

        mapView.overlays.add(
            marcadorUsuario
        )


        mapView.invalidate()

    }


    // ==========================================
    // COLOCAR PUNTOS DE EMERGENCIA
    // ==========================================

    private fun colocarPuntosEmergencia() {

        for (
        punto in puntosEmergencia
        ) {

            val ubicacion =
                GeoPoint(
                    punto.latitud,
                    punto.longitud
                )


            // Crear marcador

            val marcador =
                Marker(mapView)


            marcador.position =
                ubicacion


            marcador.title =
                punto.nombre


            marcador.snippet =
                punto.tipo +
                        "\n" +
                        punto.direccion


            // Click en marcador

            marcador.setOnMarkerClickListener {

                    _,
                    _ ->

                mostrarInformacion(
                    punto
                )

                true

            }


            // Agregar marcador

            mapView.overlays.add(
                marcador
            )

        }


        mapView.invalidate()

    }


    // ==========================================
    // INFORMACIÓN DEL PUNTO
    // ==========================================

    private fun mostrarInformacion(
        punto: PuntoEmergencia
    ) {

        tvUbicacion.text =
            "Punto de emergencia\n\n" +
                    "Nombre: ${punto.nombre}\n" +
                    "Tipo: ${punto.tipo}\n" +
                    "Dirección: ${punto.direccion}\n" +
                    "Teléfono: ${punto.telefono}"


        // Centrar mapa

        mapView.controller.setCenter(

            GeoPoint(
                punto.latitud,
                punto.longitud
            )

        )

    }


    // ==========================================
    // ABRIR GOOGLE MAPS
    // ==========================================

    private fun abrirGoogleMaps(
        punto: PuntoEmergencia
    ) {

        val uri =
            Uri.parse(

                "google.navigation:q=" +
                        "${punto.latitud}," +
                        "${punto.longitud}"

            )


        val intent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            )


        intent.setPackage(
            "com.google.android.apps.maps"
        )


        startActivity(intent)

    }


    // ==========================================
    // REANUDAR MAPA
    // ==========================================

    override fun onResume() {

        super.onResume()

        mapView.onResume()

    }


    // ==========================================
    // PAUSAR MAPA
    // ==========================================

    override fun onPause() {

        super.onPause()

        mapView.onPause()

    }

}