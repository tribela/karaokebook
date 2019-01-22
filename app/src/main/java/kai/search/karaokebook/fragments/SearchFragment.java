package kai.search.karaokebook.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kai.search.karaokebook.R;
import kai.search.karaokebook.adapters.SongAdapter;
import kai.search.karaokebook.db.DbAdapter;
import kai.search.karaokebook.db.FavouriteCategory;
import kai.search.karaokebook.db.Song;


public class SearchFragment extends Fragment implements
        TextView.OnEditorActionListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener {
    private OnFragmentInteractionListener mListener;

    private Spinner spinnerVendor;
    private Spinner spinnerSearchCategory;
    private EditText editSearchString;
    private ListView listSearchResult;

    private ArrayList<Song> list;
    private SongAdapter adapter;

    private DbAdapter dbAdapter;

    private SharedPreferences sharedPreference;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<Song>();
        adapter = new SongAdapter(getActivity(), list);
        dbAdapter = new DbAdapter(getActivity());

        sharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());

        dbAdapter.updateIndices(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_search);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        spinnerVendor = (Spinner) view.findViewById(R.id.spinnerVendor);
        spinnerSearchCategory = (Spinner) view.findViewById(R.id.spinnerSearchCategory);
        editSearchString = (EditText) view.findViewById(R.id.editSearchString);
        listSearchResult = (ListView) view.findViewById(R.id.listSearchResult);

        editSearchString.setOnEditorActionListener(this);
        spinnerSearchCategory.setOnItemSelectedListener(this);
        spinnerVendor.setOnItemSelectedListener(this);

        setupSpinner(spinnerVendor, R.array.vendor);
        setupSpinner(spinnerSearchCategory, R.array.searchCategory);
        listSearchResult.setAdapter(adapter);
        listSearchResult.setOnItemLongClickListener(this);

        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void search() {
        String query = editSearchString.getText().toString();
        int vendorPosition = spinnerVendor.getSelectedItemPosition();
        String vendor = spinnerVendor.getSelectedItem().toString();
        int category = spinnerSearchCategory.getSelectedItemPosition();
        boolean searchFromMiddle = sharedPreference.getBoolean(
                getString(R.string.key_search_middle), false);


        String queryVendor = null;
        String queryTitle = null;
        String querySinger = null;
        String queryNumber = null;

        if (vendorPosition > 0) {
            queryVendor = vendor;
        }

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

        List<Song> songs = dbAdapter.getSongs(
                queryVendor, queryTitle, queryNumber, querySinger, searchFromMiddle);
        list.clear();
        list.addAll(songs);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            search();
            return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        search();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final Song song = adapter.getItem(position);
        final List<FavouriteCategory> categories = dbAdapter.getFavoriteCategories();
        CharSequence[] items = aggregateCategoryNames(categories);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            int categoryPosition;

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (categoryPosition < categories.size()) {
                            // Existing category
                            long categoryId = categories.get(categoryPosition).getRowId();
                            dbAdapter.addFavouriteSong(categoryId, song);
                        } else {
                            makeCategoryAndFav(song);
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        categoryPosition = which;
                }
            }
        };
        builder.setSingleChoiceItems(items, 0, clickListener);
        builder.setPositiveButton(android.R.string.yes, clickListener);
        builder.setNegativeButton(android.R.string.no, clickListener);
        builder.setTitle(R.string.msg_select_category);
        builder.show();

        return false;
    }

    private void makeCategoryAndFav(final Song song) {
        final EditText input = new EditText(getActivity());
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String categoryName = input.getText().toString();
                        long categoryId = dbAdapter.createOrGetFavoriteCategory(categoryName);
                        dbAdapter.addFavouriteSong(categoryId, song);
                }
            }
        };

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.msg_new_category_name)
                .setView(input)
                .setPositiveButton(android.R.string.yes, clickListener)
                .setNegativeButton(android.R.string.no, clickListener)
                .show();
    }

    private CharSequence[] aggregateCategoryNames(List<FavouriteCategory> categories) {
        CharSequence[] results = new CharSequence[categories.size() + 1];
        for (int i = 0; i < categories.size(); i += 1) {
            results[i] = categories.get(i).getCategoryName();
        }

        results[results.length - 1] = getString(R.string.msg_add_new);

        return results;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
