package kai.search.karaokebook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kai.search.karaokebook.R;

/**
 * Created by kjwon15 on 2014. 7. 16..
 */
public class SongAdapter extends ArrayAdapter<Song> {
    private ArrayList<Song> list;
    private LayoutInflater inflater;

    public SongAdapter(Context context, ArrayList<Song> list) {
        super(context, R.layout.listitem_song, list);
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.listitem_song, null);
        }
        Song song = list.get(position);
        if (song != null) {
            TextView vendor = (TextView) view.findViewById(R.id.vendor);
            TextView number = (TextView) view.findViewById(R.id.number);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView singer = (TextView) view.findViewById(R.id.singer);

            vendor.setText(song.getVendor());
            number.setText(song.getNumber());
            title.setText(song.getTitle());
            singer.setText(song.getSinger());
        }
        return view;
    }
}
