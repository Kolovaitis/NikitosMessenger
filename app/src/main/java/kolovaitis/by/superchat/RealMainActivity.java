package kolovaitis.by.superchat;


import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


import io.socket.client.IO;
import io.socket.client.Socket;

import io.socket.emitter.Emitter;

public class RealMainActivity extends AppCompatActivity {
    ArrayList<User> users = new ArrayList<User>();
    String SOCKET_HTTP = "http://46.101.96.234";
    String TAG = "test";
    public Socket mSocket;
    int currentColor;

    {
        try {
            mSocket = IO.socket(SOCKET_HTTP);
        } catch (URISyntaxException e) {
        }

    }

    boolean isTyping = false;
    public EditText message;
    public LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
setContentView(R.layout.loading);
        ((AnimationDrawable)((ImageView) findViewById(R.id.imageView)).getBackground()).start();


        mSocket.connect();

        mSocket.on("login", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
RealMainActivity.this.runOnUiThread(new Runnable() {
    @Override
    public void run() {
        setContentView(R.layout.activity_main_real);


                mainLayout = (LinearLayout) findViewById(R.id.linearLayout);
                mainLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                        ((ScrollView) findViewById(R.id.scrollView)).scrollTo(mainLayout.getScrollX(), mainLayout.getHeight());
                    }
                });

                message = (EditText) findViewById(R.id.message);
                JSONObject data = (JSONObject) args[0];
                String numUs;

                try {
                    numUs = data.getString("numUsers");

                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());

                    return;
                }
                addSimpleText(getString(R.string.welcome));
                addSimpleText(getString(R.string.there) + numUs + getString(R.string.participants));
                message.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (isTyping == false) {
                            isTyping = true;
                            mSocket.emit("typing");
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (isTyping) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mSocket.emit("stop typing");
                                    isTyping = false;
                                }
                            });
                            thread.start();

                        }
                    }
                });
                message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        send(textView);
                        return true;
                    }
                }); }
}); }
        });

        mSocket.on("new message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());

                    return;
                }
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    Log.d(TAG, keys.next());
                }

                addMessage(message, username, Color.BLACK);

            }
        });
        mSocket.on("user joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("numUsers");
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());

                    return;
                }
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    Log.d(TAG, keys.next());
                }

                addSimpleText(username + getString(R.string.joined));
                addSimpleText(getString(R.string.there) + message + getString(R.string.there));
            }
        });


        mSocket.on("user left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("numUsers");
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());

                    return;
                }
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    Log.d(TAG, keys.next());
                }

                addSimpleText(username + getString(R.string.left));
                addSimpleText(getString(R.string.there) + message + getString(R.string.there));
            }
        });
        mSocket.on("typing", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String username;

                try {
                    username = data.getString("username");
                    Log.d(TAG, username);
                } catch (JSONException e) {


                    return;
                }
                onTyping(username, "add");

            }
        });


        mSocket.on("stop typing", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String username;

                try {
                    username = data.getString("username");
                    Log.d(TAG, username);
                } catch (JSONException e) {


                    return;
                }
                onTyping(username, "stop");

            }
        });

        mSocket.emit("login", getIntent().getStringExtra("userName"));
        mSocket.emit("add user", getIntent().getStringExtra("userName"));

    }


    private void addMessage(final String message, final String username, final int textColor) {
        boolean was = false;

        for (int i = 0; i < users.size(); i++) {
            if (username.equals(users.get(i).name)) {
                was = true;
                currentColor = users.get(i).color;
                break;
            }
        }
        if (was == false) {
            Random rand = new Random();
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            int randomColor = Color.rgb(r, g, b);
            users.add(new User(username, randomColor));
            currentColor = users.get(users.size() - 1).color;
            Log.d(TAG, currentColor + "");
        }

        RealMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(RealMainActivity.this);
                TextView user = new TextView(RealMainActivity.this);
                textView.setText(message);
                textView.setTextSize(24);

                textView.setTextColor(textColor);
                user.setText(username);
                user.setTextSize(16);
                user.setTextColor(currentColor);
                LinearLayout linearLayout = new LinearLayout(RealMainActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                textView.setPadding(0, 0, 0, 40);
                LinearLayout userLayout = new LinearLayout(RealMainActivity.this);
                userLayout.setOrientation(LinearLayout.HORIZONTAL);

                userLayout.addView(user);
                TextView userView = new TextView(RealMainActivity.this);
                userView.setText(":");
                userLayout.addView(userView);
                linearLayout.addView(userLayout);
                linearLayout.addView(textView);

                if (RealMainActivity.this.getIntent().getStringExtra("userName").toString().equals(username.toString())) {
                    userLayout.setGravity(Gravity.RIGHT);
                    textView.setGravity(Gravity.RIGHT);
                }
                mainLayout.addView(linearLayout);
                Log.d(TAG, RealMainActivity.this.getIntent().getStringExtra("userName"));
                Log.d(TAG, user.getText() + "");
                Log.d(TAG, textView.getText().toString());
            }
        });

    }

    public void addSimpleText(final String text) {
        RealMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("it", text);
                TextView tv = new TextView(RealMainActivity.this);
                tv.setTextColor(Color.GRAY);
                tv.setTextSize(16);
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(0, 0, 0, 40);
                tv.setText(text);

                mainLayout.addView(tv);
            }
        });
    }


    public void send(View v) {
        String s = message.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            return;
        }

        message.setText("");
        mSocket.emit("new message", s);
        addMessage(s, getIntent().getStringExtra("userName"), Color.BLACK);
    }

    public void onTyping(final String username, final String action) {
        boolean was = false;

        for (int i = 0; i < users.size(); i++) {
            if (username.equals(users.get(i).name)) {
                was = true;
                currentColor = users.get(i).color;
                break;
            }
        }
        if (was == false) {
            Random rand = new Random();
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            int randomColor = Color.rgb(r, g, b);
            users.add(new User(username, randomColor));
            currentColor = users.get(users.size() - 1).color;
            Log.d(TAG, currentColor + "");
        }
        if (!username.equals("")) RealMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int color = 0;
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).name.equals(username)) color = users.get(i).color;

                }
                LinearLayout TypLayout = (LinearLayout) findViewById(R.id.linearLayout2);
                if (TypLayout.getChildCount() == 0) {
                    ImageView pencel = new ImageView(RealMainActivity.this);
                    pencel.setBackgroundResource(R.drawable.animation);
                    ((AnimationDrawable) pencel.getBackground()).start();
                    LinearLayout linear = new LinearLayout(RealMainActivity.this);
                    linear.setId(R.id.linear);
                    TextView userName = new TextView(RealMainActivity.this);
                    userName.setText(username);
                    userName.setTextSize(10);
                    userName.setTextColor(color);
                    userName.setPadding(0, 5, 10, 0);
                    linear.addView(userName);
                    TypLayout.addView(pencel);
                    TypLayout.addView(linear);
                    linear.setGravity(Gravity.CENTER_VERTICAL);
                } else {
                    if (action.equals("stop")) {
                        LinearLayout linear = (LinearLayout) findViewById(R.id.linear);
                        for (int i = 0; i < linear.getChildCount(); i++) {
                            if (((TextView) linear.getChildAt(i)).getText().toString().equals(username)) {
                                linear.removeViewAt(i);
                                if (linear.getChildCount() == 0) {
                                    TypLayout.removeAllViews();
                                }
                                break;
                            }
                        }

                    } else {

                        LinearLayout linear = (LinearLayout) findViewById(R.id.linear);
                        for (int i = 0; i < linear.getChildCount(); i++) {
                            if (((TextView) linear.getChildAt(i)).getText().toString().equals(username)) {
                                linear.removeViewAt(i);

                                break;
                            }
                        }
                        TextView userName = new TextView(RealMainActivity.this);
                        userName.setText(username);
                        userName.setTextSize(10);
                        userName.setTextColor(color);
                        userName.setPadding(0, 5, 10, 0);
                        linear.addView(userName);

                    }
                }
            }
        });
    }

}
