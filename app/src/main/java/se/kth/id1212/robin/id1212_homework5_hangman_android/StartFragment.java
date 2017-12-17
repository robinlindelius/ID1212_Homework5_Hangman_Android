package se.kth.id1212.robin.id1212_homework5_hangman_android;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class StartFragment extends android.support.v4.app.Fragment {

    private OnStartFragmentListener callback;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        final Button button = view.findViewById(R.id.btn_start_game);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onStartButtonClick();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStartFragmentListener) {
            callback = (OnStartFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStartFragmentListener");
        }
    }

    public interface OnStartFragmentListener {
        void onStartButtonClick();
    }
}
