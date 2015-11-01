package info.androidhive.speechtotext;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements
        TextToSpeech.OnInitListener, android.widget.AdapterView.OnItemClickListener{

    private TextToSpeech tts;
    private TextView txtSpeechInput, textowner, txtname;
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

    ParseUser user;

    private DrawerLayout drawerLayout;
    ListView listView;
    private String[] titles;
    MyAdapterAppDrawer appdraweradapter;

    ActionBarDrawerToggle drawerListener;

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
        setContentView(R.layout.activity_drawer);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "QvGvOSa39nBizQdYGEcFzMNeTBT9bCb2FqJuXogh", "ox3KyIhaYtrGUm9oYDTci71664orUTCkkiMULpBn");

        ParseInstallation installation = ParseInstallation
                .getCurrentInstallation();

        installation.put("userclass",
                ParseObject.createWithoutData("userclass", "TUrmJkzcta"));
        installation.saveInBackground();

        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        startActivityForResult(builder.build(), 0);

        tcpServer = TCPServer.getInstance(MainActivity.this);
        tts = new TextToSpeech(this, this);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        textowner = (TextView) findViewById(R.id.txtowner);
        txtname = (TextView) findViewById(R.id.txtname);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        titles = getResources().getStringArray(R.array.navlist);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        listView = (ListView) findViewById(R.id.drawerListView);
        appdraweradapter = new MyAdapterAppDrawer(this);
        listView.setAdapter(appdraweradapter);
        listView.setOnItemClickListener(this);
        drawerListener = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.drawable.ic_menu_dark, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerListener);

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

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
            case 0: {

               user =  ParseUser.getCurrentUser();
                Log.d("user", user.getEmail() + user.getUsername());
                ParseObject accessLog = new ParseObject("AccessLocks");
                accessLog.put("UserName", user.getEmail());
                accessLog.put("House","ajmera");
                accessLog.saveInBackground();
                txtname.setText(user.getString("Name"));
                if (user.getBoolean("isOwner")){
                    textowner.setText("Owner id");
                }else{
                    textowner.setText("Visitor id");
                    listView.setVisibility(View.INVISIBLE);
                    listView.setAdapter(null);
                }
                break;
            }

        }
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onPostCreate(savedInstanceState);
        drawerListener.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        drawerListener.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(drawerListener.onOptionsItemSelected(item)){
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

        if (arg2 == 0) {
            /*FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.relativelayout_activity_help, new ContactListFragment());
			trans.commit();*/
//			startActivity(new Intent(this, ContactListActivity.class));

//            Intent i = new Intent(MainActivity.this, LogActivity.class);
//            startActivity(i);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("AccessLocks");
//            try {
//                query.get("ObjectId");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> accessList, ParseException e) {
                    if (e == null) {
                        Log.d("id", "mailid " + accessList.toString() + accessList.size());
                        Toast.makeText(MainActivity.this,accessList.toString(),Toast.LENGTH_SHORT).show();
                        //update the ui
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }
            });
            drawerLayout.closeDrawers();


        } else if (arg2 == 1) {
            //preset locations
			/*FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.relativelayout_activity_help, new PresetLocationsFragment());
			trans.commit();
			drawerLayout.closeDrawers();*/
            createGrantKeyDialog();
            drawerLayout.closeDrawers();

        } else if (arg2 == 2) {
            //startActivity(new Intent(this,SearchViewActivity.class));
        } else if (arg2 == 3) {
            //lalunchsettings();
        } else if (arg2 == 4) {

        }
//        createCustomDialog();

    }

    /***************************************** adapter for nav drawer ******************************************************/

    class MyAdapterAppDrawer extends BaseAdapter {

        String[] items;

        public MyAdapterAppDrawer(Context context) {
            items = context.getResources().getStringArray(R.array.navlist);

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return titles[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            View v = getLayoutInflater().inflate(R.layout.custom_row, parent,
                    false);
            TextView textview_title = (TextView) v.findViewById(R.id.textview_customrow);
            textview_title.setText(titles[position]);
            return v;
        }

    }


    public void  createCustomDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText("Android custom dialog example!");
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_launcher);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void createGrantKeyDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_grant_key);
        dialog.setTitle("Create Access key");
        final String objId;
        // set the custom dialog components - text, image and button
        final EditText email = (EditText) dialog.findViewById(R.id.email);
        final EditText key = (EditText) dialog.findViewById(R.id.access);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject modify = new ParseObject("User");
                Toast.makeText(MainActivity.this, "creating key", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                query.whereEqualTo("username", "htrapdev@gmail.com");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                          Log.d("size", list.size() + "  "+ list.get(0).getObjectId());

                            ParseQuery<ParseObject> queryone = ParseQuery.getQuery("_User");
                            queryone.whereEqualTo("objectId",list.get(0).getObjectId());
// Retrieve the object by id
                            queryone.findInBackground(new FindCallback<ParseObject>() {
                                public void done(List<ParseObject> scoreList, ParseException e) {
                                    if (e == null) {
                                        Log.d("score", "Retrieved " + scoreList.size() + scoreList.get(0).get("Name") +" scores");
                                        ParseUser send = (ParseUser) scoreList.get(0);
                                        send.put("name", key.getText().toString());
                                        send.saveInBackground();
                                    } else {
                                        Log.d("score", "Error: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
