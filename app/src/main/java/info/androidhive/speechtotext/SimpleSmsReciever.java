package info.androidhive.speechtotext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Codestation on 01/11/15.
 */
public class SimpleSmsReciever extends BroadcastReceiver{
//        implements
//        TextToSpeech.OnInitListener{

    private static final String TAG = "Message recieved";
    private TextToSpeech tts;
    String callMessage;
    String sender;
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
//        tts = new TextToSpeech(context, this);
        c = context;
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i(TAG, messages.getMessageBody());
        callMessage = messages.getMessageBody();
        sender = messages.getDisplayOriginatingAddress();
        Uri personUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, messages.getOriginatingAddress());

        Cursor cur = context.getContentResolver().query(personUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if( cur.moveToFirst() ) {
            int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

            sender = cur.getString(nameIndex);
        }
        cur.close();
        Toast.makeText(context, "SMS Received : "+messages.getMessageBody(),
                Toast.LENGTH_LONG) .show();
        Intent i = new Intent(context,MainActivity.class);
        i.putExtra("sms", callMessage);
        i.putExtra("sender", sender);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

//    @Override
//    public void onInit(int status) {
//        if (status == TextToSpeech.SUCCESS) {
//
//            int result = tts.setLanguage(Locale.US);
//
//            // tts.setPitch(5); // set pitch level
//
//            // tts.setSpeechRate(2); // set speech speed rate
//
//            if (result == TextToSpeech.LANG_MISSING_DATA
//                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "Language is not supported");
//            } else {
////				btnSpeak.setEnabled(true);
//				speakOut(callMessage);
//            }
//
//        } else {
//            Log.e("TTS", "Initilization Failed");
//        }
//    }

//    public void speakOut(String message) {
//
//        if (null == message){
//        } else {
//            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
//            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
//        }
//
//    }
}