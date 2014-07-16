package kai.search.karaokebook;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import kai.search.karaokebook.adapters.Song;
import kai.search.karaokebook.adapters.SongAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;

    private Spinner spinnerVendor;
    private Spinner spinnerSearchCategory;
    private EditText editSearchString;
    private Button searchButton;
    private ListView listSearchResult;

    private ArrayList<Song> list;
    private SongAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<Song>();
        adapter = new SongAdapter(getActivity(), list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        spinnerVendor = (Spinner) view.findViewById(R.id.spinnerVendor);
        spinnerSearchCategory = (Spinner) view.findViewById(R.id.spinnerSearchCategory);
        editSearchString = (EditText) view.findViewById(R.id.editSearchString);
        searchButton = (Button) view.findViewById(R.id.buttonSearch);
        listSearchResult = (ListView) view.findViewById(R.id.listSearchResult);

        setupSpinner(spinnerVendor, R.array.vendor);
        setupSpinner(spinnerSearchCategory, R.array.searchCategory);
        setupButton(searchButton);
        listSearchResult.setAdapter(adapter);

        return view;
    }

    private void setupButton(Button button) {
        button.setOnClickListener(this);
    }

    private void setupSpinner(Spinner spinner, int array) {
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(getActivity(), array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((Main) activity).onSectionAttached(getString(R.string.title_search));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSearch:
                search();
        }
    }

    private void search() {
        String query = editSearchString.getText().toString();
        String vendor = spinnerVendor.getSelectedItem().toString();
        int category = spinnerSearchCategory.getSelectedItemPosition();

        String queryTitle = null;
        String querySinger = null;
        String queryNumber = null;

        switch (category) {
            case 0:
                queryTitle = query;
                break;
            case 1:
                querySinger = query;
                break;
            case 2:
                queryNumber = query;
                break;
        }

        // TODO: query to database and show it.
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
