package info.androidhive.speechtotext;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements
        TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private TextView txtSpeechInput;
    public String callmess;
    public String sender;
    public String prevMess= ""; public String currMess =  "";
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Thread thread;
    TCPServer tcpServer = null;
    public static String message = null;

    boolean started = false;

    private static MainActivity mInstance = null;

    public MainActivity(){
    }

    public static MainActivity getmInstance() {
        if (mInstance == null) {
            mInstance = new MainActivity();
        }
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tcpServer = TCPServer.getInstance(MainActivity.this);
        tts = new TextToSpeech(this, this);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
        getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                TCPServer.createServer(message);
            }
        });

        if (null == getIntent()){

        }else {
            if (null == getIntent().getStringExtra("sms")){

            }else {
                callmess = getIntent().getStringExtra("sms");
                sender = getIntent().getStringExtra("sender");
                speakOut(sender,callmess);
            }
            if (null == getIntent().getStringExtra("incomingcall")){

            }else {
                callmess = getIntent().getStringExtra("incomingcall");
                sender = getIntent().getStringExtra("sender");
                speakOut(sender,callmess);
            }

        }


    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    TCPServer.mess = result.get(0);
                    if (!started) {
                        thread.start();
                        started = true;

                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            // tts.setPitch(5); // set pitch level

            // tts.setSpeechRate(2); // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
//				btnSpeak.setEnabled(true);
                if (null == callmess){

                }else
				speakOut(sender,callmess);
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    public void speakOut() {
        if (null == message) {

        } else {
            prevMess = currMess;
            currMess = message;
            if (prevMess.equalsIgnoreCase(currMess)){

            }else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                tts.speak(message, TextToSpeech.QUEUE_ADD, null);
            }

        }

    }
    public void speakOut(String send, String mess) {
        if (null == mess) {

        } else {

            Toast.makeText(this, mess + send, Toast.LENGTH_SHORT).show();

            if(mess.equalsIgnoreCase("incoming Call from ")){
                tts.speak(mess + send, TextToSpeech.QUEUE_FLUSH, null);
            }else  tts.speak("message from :" + send + "    "+ mess, TextToSpeech.QUEUE_FLUSH, null);


        }

    }
}
