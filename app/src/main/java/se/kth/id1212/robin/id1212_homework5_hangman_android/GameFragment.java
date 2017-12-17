package se.kth.id1212.robin.id1212_homework5_hangman_android;


import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends android.support.v4.app.Fragment implements View.OnClickListener{
    private OnGameFragmentListener callback;

    private TextView outputTextView;
    private EditText inputEditText;

    private Button btnNewGame;
    private Button btnQuit;
    private Button btnGuess;

    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // Initialize buttons
        btnNewGame = view.findViewById(R.id.btn_new_game);
        btnQuit = view.findViewById(R.id.btn_quit);
        btnGuess = view.findViewById(R.id.btn_guess);

        // Set button onClick event listeners
        btnNewGame.setOnClickListener(this);
        btnGuess.setOnClickListener(this);
        btnQuit.setOnClickListener(this);

        // Initialize the output TextView
        outputTextView = view.findViewById(R.id.tv_output_game);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        // Initialize the input
        inputEditText = view.findViewById(R.id.et_input_guess);

        // Prevent buttons and textView from being clicked before the game i started
        btnGuess.setEnabled(false);

        inputEditText.setEnabled(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameFragmentListener) {
            callback = (OnGameFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGameFragmentListener");
        }
    }

    @Override
    public void onClick(View v) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int id = v.getId();

            switch (id) {
                case R.id.btn_new_game:
                    btnGuess.setEnabled(true);
                    inputEditText.setEnabled(true);
                    callback.onNewGameButtonClick();
                    break;
                case R.id.btn_guess:
                    callback.onGuessButtonClick();
                    break;
                case R.id.btn_quit:
                    callback.onQuitButtonClick();
                    break;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.no_internetConnection))
                    .setTitle(getString(R.string.no_internet));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public interface OnGameFragmentListener {
        void onNewGameButtonClick();
        void onGuessButtonClick();
        void onQuitButtonClick();
    }
}
