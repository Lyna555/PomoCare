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
import android.widget.TextView;

import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    AppCompatButton start;
    TextView start_text;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.start_page);

        start = findViewById(R.id.start);
        start_text = findViewById(R.id.start_text);

        Locale currentLocale = getResources().getConfiguration().locale;
        String languageCode = currentLocale.getLanguage();

        if (languageCode.equals("fr")) {
            start_text.setText("Identifiez la santé de votre tomate en un seul clin d'œil");
            start.setText("Commencez maintenant");
        } else if (languageCode.equals("ar")) {
            Typeface typeface = getResources().getFont(R.font.hayah);
            start_text.setTypeface(typeface);
            start_text.setTextSize(23);
            start_text.setText("تفقد صحة أوراق الطماطم برمشة عين");
            start.setTypeface(typeface);
            start.setText("ابدأ الآن");
            start.setTextSize(18);
        }

        start.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, PredictActivity.class);
            startActivity(intent);
        });
    }
}