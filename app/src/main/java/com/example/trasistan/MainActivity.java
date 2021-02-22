package com.example.trasistan;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.SearchManager;
import android.content.Context;
import android.os.Vibrator;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import java.text.DateFormat;
import java.time.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private static int REQUEST_IMAGE_CAPTURE ;
    private SpeechRecognizer speechRecognizer;
    private static final Uri ALARM_URI = Uri.parse("android-app://com.myclockapp/set_alarm_page");
    public static final String ACTION_VIDEO_CAPTURE = null;
    static final int REQUEST_SELECT_CONTACT = 1;
    private EditText editText;
    private ImageView micButton;
    String text;
    TextToSpeech tts;
    LocalTime time = LocalTime.now();
    Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        final Vibrator vbr=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        final AudioManager VoiceControl = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //speechRecognizerIntent.putExtra(RecognizerIntent.ACTION_WEB_SEARCH);
       // speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT)




        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }


            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.mic_green);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                if(editText.getText().toString().contains("saat kaç")){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    text = "Şu an saat: "+time.format(formatter);
                } else if(editText.getText().toString().contains("günlerden ne")){
                    LocalDate localDate = LocalDate.now();
                    DayOfWeek dayOfWeek = DayOfWeek.from(localDate);
                    text = "Bugün günlerden: "+ dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
                } else if(editText.getText().toString().contains("tarih")){
                    String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                    text = "Bugünün tarihi: "+currentDate;
                }else if (editText.getText().toString().contains("titreşim")){
                    VoiceControl.setRingerMode(RINGER_MODE_VIBRATE);
                    text="Telefon titreşime alındı";
                }else if (editText.getText().toString().contains("sessiz")) {
                    VoiceControl.setRingerMode(RINGER_MODE_SILENT);
                    text="telefonun sessize alındı";
                }else if (editText.getText().toString().contains("normal")) {
                    VoiceControl.setRingerMode(RINGER_MODE_NORMAL);
                    text="telefonun normal alındı";
                }else if (editText.getText().toString().contains("kapat")) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    text="Uygulama kapatıldı";
                }else if (editText.getText().toString().contains("Telefonu kapat")){
                    Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    i.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    text="Telefonunu kapat";
                }else if (editText.getText().toString().contains("kamera")) {
                    Intent camera =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    text="Kamera Açıldı";
                }else if (editText.getText().toString().contains("WEB")) {
                    String q = editText.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
                    intent.putExtra(SearchManager.QUERY, q);
                    startActivity(intent);
                    text="Web arama";
                }


                ConvertTextToSpeech();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.getDefault());
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        //ConvertTextToSpeech();
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.mic_red);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    public void onClick(View v){

    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        //text = editText.getText().toString();
        if(text==null||"".equals(text))
        {
            text = "Bilgi bulunamadı";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}