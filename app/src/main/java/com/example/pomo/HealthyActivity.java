package com.example.pomo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class HealthyActivity extends AppCompatActivity {
    AppCompatButton healthy;
    TextView prediction;
    TextView healthyText;
    Locale currentLocale;
    String languageCode;
    Typeface typeface;;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.healthy_page);

        healthy = findViewById(R.id.healthy);
        prediction = findViewById(R.id.prediction);
        healthyText = findViewById(R.id.healthy_text);

        currentLocale = getResources().getConfiguration().locale;
        languageCode = currentLocale.getLanguage();

        typeface = getResources().getFont(R.font.hayah);

        if (languageCode.equals("fr")) {
            prediction.setText("Prédiction Réussite");
            healthyText.setText("Votre tomate est saine!");
            healthy.setText("Retour");
        } else if (languageCode.equals("ar")) {
            prediction.setTypeface(typeface);
            healthyText.setTypeface(typeface);
            healthy.setTypeface(typeface);

            prediction.setText("تمت عملية التوقع بنجاح");
            prediction.setTextSize(20);
            healthyText.setText("الطماطم سليمة!");
            healthyText.setTextSize(50);
            healthy.setText("عودة");
            healthy.setTextSize(20);
        }

        healthy.setOnClickListener(v -> {
            Intent intent = new Intent(HealthyActivity.this, PredictActivity.class);
            startActivity(intent);
        });
    }
}