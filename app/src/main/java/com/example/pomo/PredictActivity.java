package com.example.pomo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.Manifest;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomo.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public class PredictActivity extends AppCompatActivity {

    AppCompatButton predict;
    ImageView gallery;
    ImageView capture;
    ImageView leaf;
    TextView upload;
    TextView choose;
    Bitmap image;
    Locale currentLocale;
    String languageCode;
    Typeface typeface;
    int imageSize = 224;
    String alert = "Image required for prediction";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.predict_page);

        upload = findViewById(R.id.upload);
        choose = findViewById(R.id.choose);
        leaf = findViewById(R.id.leaf);
        capture = findViewById(R.id.capture);
        gallery = findViewById(R.id.gallery);
        predict = findViewById(R.id.predict);

        currentLocale = getResources().getConfiguration().locale;
        languageCode = currentLocale.getLanguage();

        typeface = getResources().getFont(R.font.hayah);

        if (languageCode.equals("fr")) {
            upload.setText("Charger une photo de feuille de tomate");
            upload.setTextSize(18.5f);

            choose.setText("Choisissez ou capturez une photo de feuille pour l'analyse");

            predict.setText("Prédire");

            gallery.setImageResource(R.drawable.gallery_fr);
            capture.setImageResource(R.drawable.capture_fr);

            alert = "Image requise pour la prédiction";
        } else if (languageCode.equals("ar")) {

            upload.setTypeface(typeface);
            upload.setTextSize(28);
            upload.setText("قم بتحميل صورة لورقة طماطم");

            choose.setTypeface(typeface);
            choose.setText("الـتـقـط أو اخـتـر ورقـة لـيـتـم تـحـلـيـلـها");
            choose.setTextSize(15);

            predict.setTypeface(typeface);
            predict.setTextSize(22);
            predict.setText("تـوقـع");

            gallery.setImageResource(R.drawable.gallery_ar);
            capture.setImageResource(R.drawable.capture_ar);

            alert = "تحميل الصورة مطلوب للتوقع";
        }

        capture.setOnClickListener(view -> {

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 3);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }

            AnimationSet animationSet = new AnimationSet(true);

            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.8f, 1, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(100);

            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
            alphaAnimation.setDuration(100);

            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);

            capture.startAnimation(animationSet);

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    capture.clearAnimation();
                    capture.setScaleX(1.0f);
                    capture.setScaleY(1.0f);
                    capture.setAlpha(1.0f);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        });

        gallery.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(cameraIntent, 4);

            AnimationSet animationSet = new AnimationSet(true);

            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.8f, 1, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(100);

            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
            alphaAnimation.setDuration(100);

            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);

            gallery.startAnimation(animationSet);

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gallery.clearAnimation();
                    gallery.setScaleX(1.0f);
                    gallery.setScaleY(1.0f);
                    gallery.setAlpha(1.0f);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        });

        predict.setOnClickListener(v -> {
            if (image != null) {
                String result = classifyImage(image)[1] + "% " + classifyImage(image)[0];

                if (result.toLowerCase().contains("healthy") || result.toLowerCase().contains("saine") || result.contains("سليمة")) {
                    showPopup(R.layout.healthy_page, result);
                } else {
                    showPopup(R.layout.diseased_page, result);
                }
            } else {
                Toast.makeText(this, alert, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopup(int layoutId, String predictedDisease) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(layoutId);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.round_corners));

        if (layoutId == R.layout.healthy_page) {
            TextView prediction = dialog.findViewById(R.id.prediction);
            TextView healthyText = dialog.findViewById(R.id.healthy_text);
            Button healthyButton = dialog.findViewById(R.id.healthy);

            if (languageCode.equals("fr")) {
                prediction.setText("Prédiction Réussite");
                healthyText.setText("Votre tomate est saine!");
                healthyButton.setText("Retour");
            } else if (languageCode.equals("ar")) {
                prediction.setTypeface(typeface);
                healthyText.setTypeface(typeface);
                healthyButton.setTypeface(typeface);

                prediction.setText("تمت عملية التوقع بنجاح");
                prediction.setTextSize(20);
                healthyText.setText("الطماطم سليمة!");
                healthyText.setTextSize(50);
                healthyButton.setText("عودة");
                healthyButton.setTextSize(20);
            }

            healthyButton.setOnClickListener(v -> {
                dialog.dismiss();
            });
        } else if (layoutId == R.layout.diseased_page) {
            Button diseasedButton = dialog.findViewById(R.id.diseased);
            LinearLayout diseaseDetails = dialog.findViewById(R.id.disease_details);
            LinearLayout diseased_layout = dialog.findViewById(R.id.diseased_layout);
            TextView prediction = dialog.findViewById(R.id.prediction);
            TextView disease = dialog.findViewById(R.id.disease_name);
            TextView description = dialog.findViewById(R.id.description);
            TextView treatment = dialog.findViewById(R.id.treatment);
            TextView diseasedText = dialog.findViewById(R.id.diseased_text);
            TextView diseaseHeader = dialog.findViewById(R.id.disease_header);
            TextView descriptionHeader = dialog.findViewById(R.id.description_header);
            TextView treatmentHeader = dialog.findViewById(R.id.treatment_header);

            if (languageCode.equals("fr")) {
                prediction.setText("Prédiction Réussite");
                diseasedText.setText("Votre tomate est malade!");
                diseasedButton.setText("Voir Plus");

                diseaseHeader.setText("Maladie");
                treatmentHeader.setText("Traitement");
            } else if (languageCode.equals("ar")) {
                prediction.setTypeface(typeface);
                diseasedText.setTypeface(typeface);
                diseasedButton.setTypeface(typeface);

                diseaseHeader.setTypeface(typeface);
                descriptionHeader.setTypeface(typeface);
                treatmentHeader.setTypeface(typeface);

                prediction.setText("تمت عملية التوقع بنجاح");
                prediction.setTextSize(20);
                diseasedText.setText("الطماطم مريضة!");
                diseasedText.setTextSize(50);
                diseasedButton.setText("اطلع على المزيد");
                diseasedButton.setTextSize(20);

                diseaseHeader.setText("نوع المرض");
                diseaseHeader.setTextSize(18);
                descriptionHeader.setText("وصف المرض");
                descriptionHeader.setTextSize(18);
                treatmentHeader.setText("علاج المرض");
                treatmentHeader.setTextSize(18);

                disease.setTextSize(16);
                description.setTextSize(16);
                treatment.setTextSize(16);
            }

            String predictedDiseaseName = String.join(" ",
                    Arrays.copyOfRange(predictedDisease.split(" "), 1,
                            predictedDisease.split(" ").length));

            diseasedButton.setOnClickListener(v -> {
                diseased_layout.setVisibility(View.GONE);
                diseaseDetails.setVisibility(View.VISIBLE);

                disease.setText(predictedDisease);

                if (languageCode.equals("fr")) {
                    prediction.setText("Détails de la maladie");
                    diseasedButton.setText("Terminé");

                    switch (predictedDiseaseName) {
                        case "Flétrissure tardive":
                            description.setText("La tache tardive est une maladie destructrice des tomates caractérisée par des lésions imbibées d'eau sur les feuilles, les tiges et les fruits.");
                            treatment.setText("Utilisez des fongicides tels que le mancozèbe, les fongicides à base de cuivre et évitez l'irrigation par le dessus.");
                            break;
                        case "Tache bactérienne":
                            description.setText("La tache bactérienne est une maladie bactérienne courante affectant les tomates, provoquant des lésions sombres et enfoncées sur les feuilles et les fruits.");
                            treatment.setText("Utilisez des bactéricides tels que la streptomycine, les fongicides à base de cuivre et maintenez une surveillance continue de l'humidité et une bonne ventilation.");
                            break;
                        case "Moisissure des feuilles":
                            description.setText("La moisissure des feuilles est une maladie fongique qui affecte principalement les feuilles de tomate, provoquant un jaunissement et des taches floues caractéristiques sur l'envers.");
                            treatment.setText("Évitez l'arrosage par le dessus et augmentez la ventilation, utilisez des fongicides tels que le benomyl et l'huile de neem.");
                            break;
                        case "Taches ciblées":
                            description.setText("Les taches ciblées sont une maladie fongique qui provoque des lésions sombres avec des anneaux concentriques sur les feuilles de tomate, finissant par entraîner la défoliation.");
                            treatment.setText("Utilisez des fongicides tels que le benomyl et les fongicides à base de cuivre.");
                            break;
                        case "Acariens":
                            description.setText("Les acariens sont des ravageurs communs qui infestent les tomates, provoquant des piqûres sur les feuilles et des toiles entre les feuilles et les tiges.");
                            treatment.setText("Utilisez des huiles végétales telles que l'huile de neem ou des insecticides ciblés contre les acariens.");
                            break;
                        case "Tache septorienne":
                            description.setText("La tache septorienne est une maladie fongique qui provoque de petites taches sombres avec des centres clairs sur les feuilles de tomate, entraînant un jaunissement des feuilles et une défoliation.");
                            treatment.setText("Utilisez des fongicides tels que le mancozèbe et les fongicides à base de cuivre, augmentez la ventilation et évitez l'irrigation par le dessus.");
                            break;
                        case "Flétrissure précoce":
                            description.setText("La flétrissure précoce est une maladie fongique affectant les tomates, caractérisée par des anneaux concentriques sombres sur les feuilles et les tiges, menant à une réduction du rendement.");
                            treatment.setText("Utilisez des fongicides tels que le benomyl et le mancozèbe, et évitez l'irrigation par le dessus.");
                            break;
                        case "Virus de mosaïque":
                            description.setText("Le virus de la mosaïque de la tomate est une maladie virale qui provoque des mosaïques, des boucles et des déformations des feuilles de tomate, réduisant la vigueur et le rendement de la plante.");
                            treatment.setText("Évitez l'infection par des outils contaminés, contrôlez les insectes vecteurs et retirez les plantes infectées.");
                            break;
                        case "Virus de boucle jaune":
                            description.setText("Le virus de la boucle jaune de la tomate est une maladie virale transmise par les mouches blanches, provoquant un jaunissement, une boucle et un arrêt de croissance des plants de tomates.");
                            treatment.setText("Utilisez des variétés résistantes, contrôlez les insectes vecteurs, augmentez l'humidité et couvrez les plantes pour prévenir l'infection.");
                            break;
                        default:
                            description.setText("Maladie inconnue. Veuillez consulter un pathologiste végétal pour obtenir de l'aide supplémentaire.");
                            treatment.setText("Veuillez consulter un pathologiste végétal pour des solutions appropriées.");
                            break;
                    }
                } else if (languageCode.equals("ar")) {
                    disease.setTypeface(typeface);
                    description.setTypeface(typeface);
                    treatment.setTypeface(typeface);

                    prediction.setText("تفاصيل المرض");
                    diseasedButton.setText("تم");

                    prediction.setText("تفاصيل المرض");
                    diseasedButton.setText("تم");

                    switch (predictedDiseaseName) {
                        case "الذبول المتأخر":
                            description.setText("الذبول المتأخر هو مرض مدمر للطماطم يتميز بالآفات المشبعة بالماء على الأوراق والسيقان والثمار.");
                            treatment.setText("استخدم مبيدات فطرية مثل المانكوزيب، ومبيدات الفطريات التي تحتوي على النحاس، وتجنب الري العلوي.");
                            break;
                        case "البقع البكتيرية":
                            description.setText("البقع البكتيرية هي مرض بكتيري شائع يؤثر على الطماطم، مما يسبب آفات داكنة ومغمورة على الأوراق والثمار.");
                            treatment.setText("استخدم مبيدات البكتيريا مثل الستربتوميسين، ومبيدات الفطريات التي تحتوي على النحاس، وحافظ على مراقبة مستمرة للرطوبة والتهوية الجيدة.");
                            break;
                        case "عفن الأوراق":
                            description.setText("عفن الأوراق هو مرض فطري يؤثر بشكل أساسي على أوراق الطماطم، مما يسبب الإصفرار والبقع الواضحة على الجانب السفلي.");
                            treatment.setText("تجنب الري العلوي وزيادة التهوية، استخدم مبيدات فطرية مثل البينوميل وزيت النيم.");
                            break;
                        case "بقع السبتوريا":
                            description.setText("بقعة سبتوريا هي مرض فطري يسبب بقعًا صغيرة وداكنة مع مراكز خفيفة على أوراق الطماطم، مما يؤدي إلى الإصفرار وتساقط الأوراق.");
                            treatment.setText("استخدم مبيدات فطرية مثل المانكوزيب ومبيدات الفطريات التي تحتوي على النحاس، وزيادة التهوية، وتجنب الري العلوي.");
                            break;
                        case "الحلم العنكبوتي":
                            description.setText("الحلم العنكبوتي هو آفة شائعة تصيب الطماطم، مما يسبب التنقيط على الأوراق والشباك بين الأوراق والسيقان.");
                            treatment.setText("استخدم الزيوت النباتية مثل زيت النيم أو المبيدات الحشرية المستهدفة ضد العناكب العنكبوتية.");
                            break;
                        case "البقع المستهدفة":
                            description.setText("البقعة المستهدفة هي مرض فطري يسبب آفات داكنة بحلقات متركزة على أوراق الطماطم، مما يؤدي في النهاية إلى تساقط الأوراق.");
                            treatment.setText("استخدم مبيدات فطرية مثل البينوميل ومبيدات الفطريات التي تحتوي على النحاس.");
                            break;
                        case "الذبول المبكر":
                            description.setText("الذبول المبكر هو مرض فطري يؤثر على الطماطم، يتميز بالحلقات المتراكمة الداكنة على الأوراق والسيقان، مما يؤدي إلى تقليل الإنتاج.");
                            treatment.setText("استخدم مبيدات فطرية مثل البينوميل والمانكوزيب، وتجنب الري العلوي.");
                            break;
                        case "فيروس اللفافة الصفراء":
                            description.setText("فيروس اللفافة الصفراء للطماطم هو مرض فيروسي ينتقل عن طريق الذباب الأبيض، مما يسبب الإصفرار والتجعد وتوقف نمو النباتات.");
                            treatment.setText("استخدم أصناف مقاومة، وتحكم في الحشرات الناقلة، وزيادة الرطوبة، وغطاء النباتات لمنع الإصابة.");
                            break;
                        case "فيروس الفسيفساء":
                            description.setText("فيروس الفسيفساء للطماطم هو مرض فيروسي يسبب التشققات والتجعد والتشوه في أوراق الطماطم، مما يقلل من قوة النبات والإنتاج.");
                            treatment.setText("تجنب الإصابة عبر الأدوات الملوثة، وبالتحكم في الحشرات الناقلة، وإزالة النباتات المصابة");
                        default:
                            description.setText("مرض غير معروف. يرجى استشارة أخصائي علم النبات للمساعدة الإضافية.");
                            treatment.setText("يرجى استشارة أخصائي علم النبات للحصول على حلول مناسبة.");
                            break;
                    }

                } else {
                    prediction.setText("Disease Details");

                    diseasedButton.setText("Done");

                    switch (predictedDiseaseName) {
                        case "Late blight":
                            description.setText("Late blight is a destructive disease of tomatoes characterized by water-soaked lesions on leaves, stems, and fruits.");
                            treatment.setText("Use fungicides such as mancozeb, copper-based fungicides, and avoid overhead irrigation.");

                            break;
                        case "Bacterial spot":
                            description.setText("Bacterial spot is a common bacterial disease affecting tomatoes, causing dark, sunken lesions on leaves and fruit.");
                            treatment.setText("Use bactericides such as streptomycin, copper-based fungicides, and maintain continuous monitoring for humidity and good ventilation.");

                            break;
                        case "Leaf Mold":
                            description.setText("Leaf mold is a fungal disease that primarily affects tomato leaves, causing yellowing and distinctive fuzzy patches on the underside.");
                            treatment.setText("Avoid overhead watering and increase ventilation, use fungicides such as benomyl and neem oil.");

                            break;
                        case "Target Spot":
                            description.setText("Target spot is a fungal disease that causes dark lesions with concentric rings on tomato leaves, eventually leading to defoliation.");
                            treatment.setText("Use fungicides such as benomyl and copper-based fungicides.");

                            break;
                        case "Spider mites":
                            description.setText("Spider mites are common pests that infest tomatoes, causing stippling on leaves and webbing between leaves and stems.");
                            treatment.setText("Use plant oils such as neem oil or insecticides targeted against spider mites.");

                            break;
                        case "Septoria leaf spot":
                            description.setText("Septoria leaf spot is a fungal disease that causes small, dark spots with light centers on tomato leaves, leading to leaf yellowing and defoliation.");
                            treatment.setText("Use fungicides such as mancozeb and copper-based fungicides, increase ventilation, and avoid overhead irrigation.");

                            break;
                        case "Early blight":
                            description.setText("Early blight is a fungal disease affecting tomatoes, characterized by dark concentric rings on leaves and stems, leading to reduced yield.");
                            treatment.setText("Use fungicides such as benomyl and mancozeb, and avoid overhead irrigation.");

                            break;
                        case "Tomato mosaic virus":
                            description.setText("Tomato mosaic virus is a viral disease that causes mottling, curling, and distortion of tomato leaves, reducing plant vigor and yield.");
                            treatment.setText("Avoid infection through contaminated tools, control vector insects, and remove infected plants.");

                            break;
                        case "Yellow Leaf Curl Virus":
                            description.setText("Tomato yellow leaf curl virus is a viral disease transmitted by whiteflies, causing yellowing, curling, and stunting of tomato plants.");
                            treatment.setText("Use resistant varieties, control vector insects, increase humidity, and cover plants to prevent infection.");

                            break;
                        default:
                            description.setText("Unknown disease. Please consult a plant pathologist for further assistance.");
                            treatment.setText("Please consult a plant pathologist for appropriate solutions.");
                            break;
                    }
                }

                diseasedButton.setOnClickListener(v1 -> {
                    dialog.dismiss();
                });
            });
        }
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public String[] classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;

            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val & 0xFF) * (1.f / 255)));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;

            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes;

            if (languageCode.equals("fr")) {
                classes = new String[]{"Tache bactérienne", "Flétrissure précoce", "Flétrissure tardive",
                        "Moisissure des feuilles", "Tache septorienne", "Acariens", "Taches ciblées",
                        "Virus de boucle jaune", "Virus de mosaïque", "Saine"};
            } else if (languageCode.equals("ar")) {
                classes = new String[]{"البقع البكتيرية", "الذبول المبكر", "الذبول المتأخر", "عفن الأوراق", "بقع السبتوريا",
                        "الحلم العنكبوتي", "البقع المستهدفة", "فيروس اللفافة الصفراء", "فيروس الفسيفساء", "سليمة"};
            } else {
                classes = new String[]{"Bacterial spot", "Early blight", "Late blight", "Leaf Mold", "Septoria leaf spot",
                        "Spider mites", "Target Spot", "Yellow Leaf Curl Virus", "Mosaic virus", "Healthy"};
            }

            String[] result = {classes[maxPos], String.format("%.2f", maxConfidence * 100)};

            model.close();

            return result;

        } catch (IOException e) {
            return new String[]{e.toString()};
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    image = (Bitmap) extras.get("data");
                    if (image != null) {
                        int dimension = Math.min(image.getWidth(), image.getHeight());
                        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                        leaf.setImageBitmap(image);

                        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    }
                }
            } else if (requestCode == 4) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        if (image != null) {
                            int dimension = Math.min(image.getWidth(), image.getHeight());
                            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                            leaf.setImageBitmap(image);

                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}