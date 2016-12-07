package kai.search.karaokebook.db;

/**
 * Created by kjwon15 on 16. 12. 7.
 */

public class FavouriteCategory {
    private long rowId = -1;
    private String categoryName;

    public FavouriteCategory(long rowId, String categoryName) {
        this.rowId = rowId;
        this.categoryName = categoryName;
    }
}
