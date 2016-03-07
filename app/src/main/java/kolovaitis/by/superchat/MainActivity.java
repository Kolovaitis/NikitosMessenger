package kolovaitis.by.superchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.net.URISyntaxException;
import java.util.prefs.Preferences;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    private EditText mInputMessageView;

public EditText userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName=(EditText)findViewById(R.id.editText);

        Button addUser=(Button)findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                Intent toReal=new Intent(MainActivity.this, RealMainActivity.class);
                toReal.putExtra("userName",userName.getText().toString());
                startActivity(toReal);
                finish();
            }
        });
AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("No internet connection");
        ad.setMessage("Please connect your device to internet");
        ad.setPositiveButton("Rerun", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }
        });
        ad.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                finish();
            }
        });
        ad.setCancelable(false);
if(isNetworkAvailable()==false){ad.show();}
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;}
}
