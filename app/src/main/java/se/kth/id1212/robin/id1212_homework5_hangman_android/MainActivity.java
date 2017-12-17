package se.kth.id1212.robin.id1212_homework5_hangman_android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import se.kth.id1212.robin.id1212_homework5_hangman_android.net.ServerConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        StartFragment.OnStartFragmentListener, GameFragment.OnGameFragmentListener {
    private final String TAG = "HANGMAN";
    private final String SERVER_ADDRESS = "192.168.2.178";
    private final String PORT = "8080";

    private ServerConnection serverConnection;
    private TextView outputTextView;
    private EditText inputEditText;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setupActivity();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            StartFragment startFragment = new StartFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, startFragment).commit();
        }
    }

    private void setupActivity() {
        //progress = findViewById(R.id.progressBar);

        // Initialize buttons
        final Button btnStart = findViewById(R.id.btn_start_game);
        final Button btnNewGame = findViewById(R.id.btn_new_game);
        final Button btnQuit = findViewById(R.id.btn_quit);
        final Button btnGuess = findViewById(R.id.btn_guess);

        // Set button onClick event listeners
        btnStart.setOnClickListener(this);
        btnNewGame.setOnClickListener(this);
        btnGuess.setOnClickListener(this);
        btnQuit.setOnClickListener(this);

        // Initialize the output TextView
        outputTextView = findViewById(R.id.tv_output_game);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        // Initialize the input
        inputEditText = findViewById(R.id.et_input_guess);

        // Prevent buttons and textView from being clicked before the game i started
        btnNewGame.setEnabled(false);
        btnQuit.setEnabled(false);
        btnGuess.setEnabled(false);

        inputEditText.setEnabled(false);
    }

    @Override
    public void onClick(View v) {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int id = v.getId();

            switch (id) {
                case R.id.btn_start_game:
                    new Connection().execute(SERVER_ADDRESS, PORT);
                    findViewById(R.id.btn_start_game).setEnabled(false);
                    findViewById(R.id.btn_new_game).setEnabled(true);
                    findViewById(R.id.btn_quit).setEnabled(true);
                    break;
                case R.id.btn_new_game:
                    new NewGame().execute();
                    findViewById(R.id.btn_guess).setEnabled(true);
                    inputEditText.setEnabled(true);
                    break;
                case R.id.btn_guess:
                    new Guess().execute(inputEditText.getText().toString());
                    inputEditText.getText().clear();
                    break;
                case R.id.btn_quit:
                    new Quit().execute();
                    break;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getString(R.string.no_internetConnection))
                    .setTitle(getString(R.string.no_internet));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onStartButtonClick() {


        new Connection().execute(SERVER_ADDRESS, PORT);

        GameFragment gameFragment = new GameFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, gameFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onNewGameButtonClick() {
        new NewGame().execute();
    }

    @Override
    public void onGuessButtonClick() {
        EditText inputEditText = findViewById(R.id.et_input_guess);
        new Guess().execute(inputEditText.getText().toString());
        inputEditText.getText().clear();
    }

    @Override
    public void onQuitButtonClick() {
        new Quit().execute();
    }

    private class Connection extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... serverAddress) {
            serverConnection = new ServerConnection();
            try {
                serverConnection.connect(serverAddress[0], Integer.valueOf(serverAddress[1]),new ViewOutput());
            }
            catch (IOException ioe) {
                Log.e(TAG,ioe.getMessage());
                cancel(true);
            }
            return null;
        }

        @Override
        public void onCancelled() {
            //progress.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getString(R.string.no_serverConnection))
                    .setTitle(getString(R.string.no_connection));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private class NewGame extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            serverConnection.startNewGame();

            return null;
        }
    }

    private class Guess extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... guess) {
            serverConnection.sendGuess(guess[0]);
            return null;
        }
    }

    private class Quit extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                serverConnection.disconnect();
                finish();
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }

    private class Rules extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            serverConnection.getRules();
            return null;
        }
    }

    private class ViewOutput implements OutputHandler{

        @Override
        public void handleOutput(String output) {
            final String out = output;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView outputGame = findViewById(R.id.tv_output_game);
                    outputGame.append(out + "\n");
                    Editable editable = outputGame.getEditableText();
                    Selection.setSelection(editable, editable.length());
                }
            });
        }
    }
}
