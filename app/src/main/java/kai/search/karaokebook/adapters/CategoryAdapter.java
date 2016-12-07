package kai.search.karaokebook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kai.search.karaokebook.R;
import kai.search.karaokebook.db.FavouriteCategory;
import kai.search.karaokebook.db.Song;

/**
 * Created by kjwon15 on 2014. 7. 16..
 */
public class CategoryAdapter extends ArrayAdapter<FavouriteCategory> {
    private List<FavouriteCategory> list;
    private LayoutInflater inflater;

    public CategoryAdapter(Context context, List<FavouriteCategory> list) {
        super(context, android.R.layout.simple_list_item_1, list);
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public FavouriteCategory getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        FavouriteCategory category = list.get(position);
        if (category != null) {
            TextView name = (TextView) view.findViewById(android.R.id.text1);

            name.setText(category.getCategoryName());
        }
        return view;
    }
}
