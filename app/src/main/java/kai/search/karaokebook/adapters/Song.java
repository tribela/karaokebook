package kai.search.karaokebook.adapters;

/**
 * Created by kjwon15 on 2014. 7. 16..
 */
public class Song {
    private String vendor;
    private String number;
    private String title;

    private String singer;

    public Song(String vendor, String number, String title, String singer) {
        this.vendor = vendor;
        this.number = number;
        this.title = title;
        this.singer = singer;
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
