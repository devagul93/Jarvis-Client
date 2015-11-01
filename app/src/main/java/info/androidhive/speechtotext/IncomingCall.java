package info.androidhive.speechtotext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Codestation on 01/11/15.
 */
public class IncomingCall extends BroadcastReceiver {

    String sender;
    Context c;

    public void onReceive(Context context, Intent intent) {

        try {
            // TELEPHONY MANAGER class object to register one listner
            TelephonyManager tmgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            c = context;
            //Create Listner
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener();

            // Register listener for LISTEN_CALL_STATE
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);



        } catch (Exception e) {
            Log.e("Phone Receive Error", " " + e);
        }

    }

    private class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {

            Log.d("MyPhoneListener",state+"   incoming no:"+incomingNumber);

            if (state == 1) {

                String msg = "New Phone Call Event. Incomming Number : "+incomingNumber;
                int duration = Toast.LENGTH_LONG;
//                Toast toast = Toast.makeText(pcontext, msg, duration);
//                toast.show();

                Uri personUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,"+" + incomingNumber);

                Cursor cur = c.getContentResolver().query(personUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                if( cur.moveToFirst() ) {
                    int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                    sender = cur.getString(nameIndex);
                }
                cur.close();

                Intent i = new Intent(c,MainActivity.class);
                i.putExtra("incomingcall", "incoming Call from ");
                i.putExtra("sender", sender);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);

            }
        }
    }
}