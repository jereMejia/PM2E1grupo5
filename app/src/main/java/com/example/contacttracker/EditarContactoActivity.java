package com.example.contacttracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditarContactoActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etUbicacion;
    private ImageView ivFirma;
    private Button btnActualizar;
    private int contactoId;
    private String firmaBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_contacto);

        etNombre = findViewById(R.id.nombre);
        etTelefono = findViewById(R.id.telefono);
        etUbicacion = findViewById(R.id.ubicacion);
        ivFirma = findViewById(R.id.imageView);
        btnActualizar = findViewById(R.id.btnput);

        Intent intent = getIntent();
        if (intent != null) {
            contactoId = intent.getIntExtra("id", -1);
            etNombre.setText(intent.getStringExtra("nombres"));
            etTelefono.setText(intent.getStringExtra("telefono"));
            etUbicacion.setText(intent.getStringExtra("ubicacion"));
            firmaBase64 = intent.getStringExtra("firma");

            if (firmaBase64 != null && !firmaBase64.isEmpty()) {
                ivFirma.setImageBitmap(convertirBase64ABitmap(firmaBase64));
            }
        }

        btnActualizar.setOnClickListener(v -> actualizarContacto());
    }

    private void actualizarContacto() {
        String url = "http://192.168.96.4/api/PUTpersonas.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", contactoId);
            jsonBody.put("nombres", etNombre.getText().toString().trim());
            jsonBody.put("telefono", etTelefono.getText().toString().trim());
            jsonBody.put("ubicacion", etUbicacion.getText().toString().trim());
            jsonBody.put("firma", firmaBase64);

            Log.d("JSON Enviado", jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    Log.d("Respuesta Servidor", response.toString());
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            // Enviar resultado a MainActivity para actualizar la lista
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("actualizado", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Error Volley", "Error en la actualización: " + error.toString());
                    Toast.makeText(this, "Error en la actualización", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private Bitmap convertirBase64ABitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
