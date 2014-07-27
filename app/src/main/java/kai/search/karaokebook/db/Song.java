package kai.search.karaokebook.db;

/**
 * Created by kjwon15 on 2014. 7. 16..
 */
public class Song {
    private long rowid = -1;
    private String vendor;
    private String number;
    private String title;
    private String singer;

    public Song(long rowid, String vendor, String number, String title, String singer) {
        this.rowid = rowid;
        this.vendor = vendor;
        this.number = number;
        this.title = title;
        this.singer = singer;
    }

    public long getRowid() {
        return rowid;
    }

    public String getSinger() {
        return singer;
    }

    public String getVendor() {
        return vendor;
    }

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }
}
