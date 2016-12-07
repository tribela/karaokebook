package kai.search.karaokebook.fragments;


import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import kai.search.karaokebook.R;
import kai.search.karaokebook.UpdateChecker;
import kai.search.karaokebook.activities.Main;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Main) context).onSectionAttached(getString(R.string.title_setting));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_UPDATE)) {
            updateChecker.checkUpdate();
        }
        return false;
    }
}
