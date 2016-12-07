package kai.search.karaokebook.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kai.search.karaokebook.R;
import kai.search.karaokebook.activities.Main;
import kai.search.karaokebook.adapters.CategoryAdapter;
import kai.search.karaokebook.db.DbAdapter;
import kai.search.karaokebook.db.FavouriteCategory;

public class FavouriteCategoriesFragment extends ListFragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ArrayList<FavouriteCategory> list;
    private CategoryAdapter adapter;

    private DbAdapter dbAdapter;

    private OnFragmentInteractionListener mListener;

    private ListView mListView;


    public FavouriteCategoriesFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadCategories();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();
        adapter = new CategoryAdapter(getActivity(), list);
        dbAdapter = new DbAdapter(getActivity());
        reloadCategories();
    }

    private void reloadCategories() {

        list.clear();
        for (FavouriteCategory category : dbAdapter.getFavoriteCategories()) {
            list.add(category);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Main) context).onSectionAttached(getString(R.string.title_favourites));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FavouriteCategory category = adapter.getItem(position);

        // TODO: start fragment with category
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        FavouriteCategory category = adapter.getItem(position);
                        // FIXME: implement this
                        dbAdapter.removeFavoriteCategory(category);
                        reloadCategories();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.msg_remove_favourite);
        builder.setPositiveButton(android.R.string.yes, clickListener);
        builder.setNegativeButton(android.R.string.no, clickListener);
        builder.show();
        return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
