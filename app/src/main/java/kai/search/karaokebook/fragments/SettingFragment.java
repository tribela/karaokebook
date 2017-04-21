package kai.search.karaokebook.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference;

import kai.search.karaokebook.R;
import kai.search.karaokebook.UpdateChecker;
import kai.search.karaokebook.activities.Main;

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

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
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.title_setting));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_UPDATE)) {
            updateChecker.checkUpdate();
        }
        return false;
    }
}
