package kai.search.karaokebook.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kai.search.karaokebook.R;
import kai.search.karaokebook.activities.Main;
import kai.search.karaokebook.adapters.SongAdapter;
import kai.search.karaokebook.db.DbAdapter;
import kai.search.karaokebook.db.Song;

public class FavouritesFragment extends ListFragment implements AdapterView.OnItemLongClickListener {

    private ArrayList<Song> list;
    private SongAdapter adapter;

    private DbAdapter dbAdapter;

    private OnFragmentInteractionListener mListener;

    private ListView mListView;

    private long categoryId;


    public FavouritesFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadFavourites();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();
        adapter = new SongAdapter(getActivity(), list);
        dbAdapter = new DbAdapter(getActivity());
        categoryId = getArguments().getLong("RowId");
        reloadFavourites();
    }

    private void reloadFavourites() {

        list.clear();
        for (Song song : dbAdapter.getFavouriteSongs(categoryId)) {
            list.add(song);
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Song song = adapter.getItem(position);
                        dbAdapter.removeFavouriteSong(categoryId, song);
                        reloadFavourites();
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
