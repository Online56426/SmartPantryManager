package com.example.smartpantry.ui;

import android.os.Bundle;
import android.widget.RadioGroup;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppPreferences;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Screen 5: Settings. Every change is saved to SharedPreferences the moment the
 * user makes it, the same approach as the settings screen in the study guide.
 *
 *  - Highlight items that are expiring soon (pantry list)
 *  - Ignore expired items when suggesting recipes (matching)
 *  - Sort order of the pantry list
 */
public class SettingsActivity extends BaseNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchMaterial swAlerts = findViewById(R.id.swExpiryAlerts);
        SwitchMaterial swExclude = findViewById(R.id.swExcludeExpired);
        RadioGroup rgSort = findViewById(R.id.rgSort);

        // Show the saved values first, then start listening for changes.
        swAlerts.setChecked(AppPreferences.isExpiryAlertsOn(this));
        swExclude.setChecked(AppPreferences.isExcludeExpired(this));
        rgSort.check(AppPreferences.isSortByExpiry(this) ? R.id.rbSortExpiry : R.id.rbSortName);

        swAlerts.setOnCheckedChangeListener((button, isChecked) ->
                AppPreferences.setExpiryAlertsOn(this, isChecked));
        swExclude.setOnCheckedChangeListener((button, isChecked) ->
                AppPreferences.setExcludeExpired(this, isChecked));
        rgSort.setOnCheckedChangeListener((group, checkedId) ->
                AppPreferences.setSortByExpiry(this, checkedId == R.id.rbSortExpiry));

        setupBottomNav(R.id.nav_settings);
    }
}
