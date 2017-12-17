package se.kth.id1212.robin.id1212_homework5_hangman_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import se.kth.id1212.robin.id1212_homework5_hangman_android.net.ServerConnection;

public class MainActivity extends AppCompatActivity implements
        StartFragment.OnStartFragmentListener, GameFragment.OnGameFragmentListener {
    private final String TAG = "HANGMAN";
    private final String SERVER_ADDRESS = "192.168.2.178";
    private final String PORT = "8080";

    private ServerConnection serverConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
