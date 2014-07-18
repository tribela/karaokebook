package kai.search.karaokebook;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String KEY_UPDATE = "update";
    private UpdateChecker updateChecker;


    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        updateChecker = new UpdateChecker(getActivity());

        Preference update = findPreference(KEY_UPDATE);
        update.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_UPDATE)) {
            String msg;
            if (updateChecker.updateAvailable()) {
                updateChecker.doUpdate(getActivity());
            }
        }
        return false;
    }
}
