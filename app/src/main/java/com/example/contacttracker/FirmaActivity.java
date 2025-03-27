package com.example.contacttracker;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;

public class FirmaActivity extends AppCompatActivity {

    private SignatureView signatureView;
    private Button btnBorrar, btnGuardarFirma;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firma);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        signatureView = findViewById(R.id.signatureView);
        btnBorrar = findViewById(R.id.btnBorrar);
        btnGuardarFirma = findViewById(R.id.btnGuardarFirma);

        btnBorrar.setOnClickListener(v -> signatureView.clear());

        btnGuardarFirma.setOnClickListener(v -> {
            Bitmap firma = signatureView.getSignatureBitmap();
            guardarFirma(firma);
        });
    }

    private void guardarFirma(Bitmap bitmap) {
        try {
            File file = new File(getExternalFilesDir(null), "firma.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();

            Intent intent = new Intent();
            intent.putExtra("firma_path", file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}