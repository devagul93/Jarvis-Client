package info.androidhive.speechtotext;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Codestation on 01/11/15.
 */
public class LogActivity extends Activity{

    public ArrayList<ParseObject> list = new ArrayList<ParseObject>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AccessLocks");
        try {
            query.get("UserName");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> accessList, ParseException e) {
                if (e == null) {
                    Log.d("id", "mailid " + accessList.toString());
                    Toast.makeText(LogActivity.this,accessList.toString(),Toast.LENGTH_SHORT).show();
                    //update the ui
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }
}
