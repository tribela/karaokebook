package kai.search.karaokebook.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import kai.search.karaokebook.R;
import kai.search.karaokebook.activities.Main;

/**
 * Created by kjwon15 on 16. 12. 7.
 */

public class AboutFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Main) context).onSectionAttached(getString(R.string.title_about));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView headerView = (TextView) view.findViewById(R.id.licensesHeaderView);
        TextView mainView = (TextView) view.findViewById(R.id.licensesMainView);

        readText("notes.txt", headerView);
        readText("APACHE_LICENSE_2.txt", mainView);

        return view;
    }

    private void readText(String filename, TextView headerView) {
        try {
            InputStream noteIn = getActivity().getAssets().open(filename);
            int size = noteIn.available();
            byte[] buffer = new byte[size];
            noteIn.read(buffer);
            noteIn.close();
            String text = new String(buffer);
            headerView.setText(text);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("READ", e.getMessage());
        }
    }
}
