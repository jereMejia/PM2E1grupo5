package com.example.contacttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView txtUbicacion;
    private EditText txtNombre, TextTelefono;
    private String rutaFirma = "";
    private double latitud = 0.0, longitud = 0.0;

    private Button btnGuardarUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtUbicacion = findViewById(R.id.txtUbicacion);
        btnGuardarUbicacion = findViewById(R.id.btnObtenerUbicacion);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGuardarUbicacion.setOnClickListener(v -> obtenerUbicacion());

        Button btnCaptureFirma = findViewById(R.id.btnCapturarFirma);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnVerContactos = findViewById(R.id.btnVerContactos);
        txtNombre = findViewById(R.id.txtNombre);
        TextTelefono = findViewById(R.id.txtTelefono);
        txtUbicacion = findViewById(R.id.txtUbicacion);

        btnCaptureFirma.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FirmaActivity.class);
            startActivityForResult(intent, 1);
        });

        btnVerContactos = findViewById(R.id.btnVerContactos);
        btnVerContactos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaContactosActivity.class);
            startActivity(intent);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obtenerUbicacion();

        btnGuardar.setOnClickListener(view -> guardarContactoEnServidor());
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();

                        String ubicacionTexto = "Latitud: " + latitud + "\nLongitud: " + longitud;
                        txtUbicacion.setText(ubicacionTexto);
                        Log.d("Ubicación", ubicacionTexto);

                        // Aquí podrías guardar en SharedPreferences o en una BD si lo necesitas
                        guardarUbicacion(latitud, longitud);
                    } else {
                        txtUbicacion.setText("No se pudo obtener la ubicación.");
                        Log.e("Ubicación", "No se pudo obtener la ubicación.");
                    }
                });
    }

    // Ejemplo de función para guardar la ubicación en SharedPreferences
    private void guardarUbicacion(double lat, double lon) {
        SharedPreferences preferences = getSharedPreferences("UbicacionPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("latitud", String.valueOf(lat));
        editor.putString("longitud", String.valueOf(lon));
        editor.apply();
        Toast.makeText(this, "Ubicación guardada correctamente", Toast.LENGTH_SHORT).show();
    }

    private void guardarContactoEnServidor() {
        String nombre = txtNombre.getText().toString().trim();
        String telefono = TextTelefono.getText().toString().trim();
        String firmaPath = rutaFirma;
        String ubicacion = latitud + "," + longitud;

        Log.d("Depuración", "Nombre: " + nombre);
        Log.d("Depuración", "Teléfono: " + telefono);
        Log.d("Depuración", "Firma Path: " + firmaPath);
        Log.d("Depuración", "Ubicación: " + ubicacion);

        if (nombre.isEmpty() || telefono.isEmpty() || firmaPath.isEmpty() ||
                (latitud == 0.0 && longitud == 0.0 && !ubicacion.contains(","))) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            Log.e("Validación", "Faltan datos");
            return;
        }

        String url = "http://192.168.96.4/api/POSTpersonas.php";

        // Crear el objeto JSON con los parámetros
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("nombres", nombre);
            jsonBody.put("telefono", telefono);
            jsonBody.put("ubicacion", ubicacion);
            jsonBody.put("firma", firmaPath); // Asegúrate de que 'firma' esté en el formato adecuado

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            // Comprobamos si la respuesta es un JSON y tiene las claves correctas
                            if (response != null && response.has("success") && response.has("message")) {
                                boolean success = response.getBoolean("success");
                                String message = response.getString("message");
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                // Si la respuesta no es un JSON esperado
                                Log.e("Error", "Respuesta del servidor no válida: " + response.toString());
                                Toast.makeText(MainActivity.this, "Respuesta no válida del servidor", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON Error", "Error al parsear JSON: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("Volley Error", "Error en la solicitud: " + (error != null ? error.toString() : "Desconocido") +
                                " Código de estado: " + (error.networkResponse != null ? error.networkResponse.statusCode : "N/A"), error);
                        Toast.makeText(MainActivity.this, "Error de conexión. Verifique el servidor.", Toast.LENGTH_SHORT).show();
                    });

            // Configura el tiempo de espera
            int socketTimeout = 30000;  // 30 segundos de timeout
            RetryPolicy policy = new DefaultRetryPolicy(
                    socketTimeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            );
            jsonRequest.setRetryPolicy(policy);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear JSON", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String firmaPath = data.getStringExtra("firma_path");

            rutaFirma = firmaPath;

            ImageView imgFirma = findViewById(R.id.imgFirma);
            Bitmap bitmap = BitmapFactory.decodeFile(firmaPath);
            imgFirma.setImageBitmap(bitmap);

            Log.d("Firma", "Ruta de la firma guardada: " + rutaFirma);
            Toast.makeText(this, "Firma guardada", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

