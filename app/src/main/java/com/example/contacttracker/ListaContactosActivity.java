package com.example.contacttracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListaContactosActivity extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private ArrayList<Contacto> listaContactos;
    private ArrayList<String> nombresContactos;
    private ArrayList<String> contactosFiltrados;
    private ArrayAdapter<Contacto> adapter;
    private Contacto contactoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_activivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        listaContactos = new ArrayList<>();
        nombresContactos = new ArrayList<>();
        contactosFiltrados = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaContactos);
        listView.setAdapter(adapter);

        cargarContactos();
        configurarSearchView();

        Button btnActualizar = findViewById(R.id.btnActualizar);
        btnActualizar.setEnabled(false);
        Button btnMapa = findViewById(R.id.btnmapa);
        btnMapa.setEnabled(false);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setEnabled(false);

        // Evento de clic en la lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            contactoSeleccionado = listaContactos.get(position);
            Toast.makeText(this, "Seleccionado: " + contactoSeleccionado.getNombre() + " (ID: " + contactoSeleccionado.getId() + ")", Toast.LENGTH_SHORT).show();
            btnActualizar.setEnabled(true);
            btnMapa.setEnabled(true);
            btnEliminar.setEnabled(true);
        });

        btnMapa.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                abrirMapa(contactoSeleccionado.getUbicacion());
            } else {
                Toast.makeText(this, "Selecciona un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });

        btnActualizar.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                Intent intent = new Intent(this, EditarContactoActivity.class);
                intent.putExtra("id", contactoSeleccionado.getId());
                intent.putExtra("nombres", contactoSeleccionado.getNombre());
                intent.putExtra("telefono", contactoSeleccionado.getTelefono());
                intent.putExtra("ubicacion", contactoSeleccionado.getUbicacion());
                intent.putExtra("firma", contactoSeleccionado.getFirma());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Selecciona un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });


        btnEliminar.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                mostrarDialogoConfirmacion();
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirMapa(String ubicacion) {
        if (ubicacion != null && !ubicacion.isEmpty()) {
            Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(ubicacion));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
        }
    }


    private void mostrarDialogoConfirmacion() {
        Log.d("Eliminar", "ID seleccionado: " + contactoSeleccionado.getId()); // Verificar ID en logs

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Seguro que quieres eliminar a " + contactoSeleccionado.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarPersona(contactoSeleccionado.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPersona(int id) {
        String url = "http://192.168.96.4/api/DELETEpersonas.php";

        // Crear el objeto JSON con el ID
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Enviar la solicitud con JSON
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Toast.makeText(this, "Persona eliminada", Toast.LENGTH_SHORT).show();
                            contactoSeleccionado = null;
                            cargarContactos(); // Recargar lista
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error en la eliminación: " + error.toString());
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                });

        // Agregar la solicitud a la cola
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }





    private void mostrarOpciones(Contacto contactoSeleccionado) {
        // Implementación si la necesitas
    }

    private void configurarSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No hacemos nada cuando se presiona "Enter"
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarContactos(newText);
                return true;
            }
        });
    }

    private void filtrarContactos(String texto) {
        contactosFiltrados.clear();

        if (texto.isEmpty()) {
            contactosFiltrados.addAll(nombresContactos);
        } else {
            for (Contacto contacto : listaContactos) {
                if (contacto.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    contactosFiltrados.add(contacto.getNombre());
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void cargarContactos() {
        String url = "http://192.168.96.4/api/GETpersonas.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Volley", "Respuesta de la API: " + response); // Verificar respuesta

                    try {
                        JSONObject jsonObject = new JSONObject(response); // Convertir la respuesta en objeto JSON
                        JSONArray jsonArray = jsonObject.getJSONArray("persona"); // Obtener el array "persona"

                        listaContactos.clear();
                        nombresContactos.clear();
                        contactosFiltrados.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject contactoJSON = jsonArray.getJSONObject(i);
                            Contacto contacto = new Contacto(
                                    contactoJSON.getInt("id"),
                                    contactoJSON.getString("nombres"),
                                    contactoJSON.getString("telefono"),
                                    contactoJSON.getString("ubicacion"),
                                    contactoJSON.getString("firma")
                            );
                            listaContactos.add(contacto);

                            nombresContactos.add(contacto.getNombre());
                        }

                        contactosFiltrados.addAll(nombresContactos);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("Volley", "Error en JSON: " + e.getMessage()); // Log del error JSON
                    }
                },
                error -> Log.e("Volley", "Error en la solicitud: " + error.toString())); // Log del error Volley

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


}
