package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryDataSource;
import com.example.smartpantry.logic.ExpiryUtil;
import com.example.smartpantry.logic.InputValidator;
import com.example.smartpantry.logic.QuantityFormat;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Screen 2: the Add / Edit ingredient form.
 *
 * The same screen does both jobs. If the Intent that started it carries an item
 * id, the form loads that item and saving updates it. With no id it is a blank
 * form and saving inserts a new row.
 */
public class PantryItemActivity extends AppCompatActivity {

    /** Key for the item id passed in the Intent by PantryListActivity. */
    public static final String EXTRA_ITEM_ID = "com.example.smartpantry.EXTRA_ITEM_ID";

    private static final String STATE_EXPIRY = "state_expiry";

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private Spinner spUnit;
    private TextView tvExpiry;
    private Button btnClearDate;

    private long itemId = PantryItem.NO_ID;
    private long expiryMillis = PantryItem.NO_EXPIRY;
    private final ZoneId zone = ZoneId.systemDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_item);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        tvExpiry = findViewById(R.id.tvExpiry);
        btnClearDate = findViewById(R.id.btnClearDate);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NO_ID);
        setTitle(itemId == PantryItem.NO_ID ? R.string.title_add_item : R.string.title_edit_item);

        if (savedInstanceState != null) {
            // After a screen rotation the text fields restore themselves; the date does not.
            expiryMillis = savedInstanceState.getLong(STATE_EXPIRY, PantryItem.NO_EXPIRY);
        } else if (itemId != PantryItem.NO_ID) {
            loadExistingItem();
        }

        // Clear an error message as soon as the user starts fixing the field.
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilName.setError(null);
            }
        });
        etQuantity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilQuantity.setError(null);
            }
        });

        findViewById(R.id.btnPickDate).setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryMillis = PantryItem.NO_EXPIRY;
            updateExpiryText();
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());

        updateExpiryText();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EXPIRY, expiryMillis);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Edit mode: fill the form from the database row. */
    private void loadExistingItem() {
        PantryItem item = null;
        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            item = dataSource.getItem(itemId);
        } catch (Exception e) {
            item = null;
        } finally {
            dataSource.close();
        }

        if (item == null) {
            Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etName.setText(item.getName());
        etQuantity.setText(QuantityFormat.format(item.getQuantity()));
        expiryMillis = item.getExpiryMillis();

        // Select the saved unit in the drop down.
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(item.getUnit())) {
                spUnit.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        LocalDate initial = expiryMillis != PantryItem.NO_EXPIRY
                ? ExpiryUtil.toLocalDate(expiryMillis, zone)
                : LocalDate.now(zone);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // DatePicker months start at 0, LocalDate months start at 1.
                    expiryMillis = ExpiryUtil.startOfDayMillis(year, month + 1, dayOfMonth, zone);
                    updateExpiryText();
                },
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }

    private void updateExpiryText() {
        if (expiryMillis == PantryItem.NO_EXPIRY) {
            tvExpiry.setText(R.string.no_expiry);
            btnClearDate.setVisibility(View.GONE);
            return;
        }

        String date = ExpiryUtil.formatDate(expiryMillis, zone, Locale.getDefault());
        boolean passed = ExpiryUtil.isExpired(expiryMillis, System.currentTimeMillis(), zone);
        tvExpiry.setText(passed ? date + "\n" + getString(R.string.date_has_passed) : date);
        btnClearDate.setVisibility(View.VISIBLE);
    }

    /** Validates the form, then inserts or updates depending on whether we are editing. */
    private void saveItem() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String quantityText = etQuantity.getText() == null ? "" : etQuantity.getText().toString();

        if (!validate(name, quantityText)) {
            return;
        }

        PantryItem item = new PantryItem();
        item.setId(itemId);
        item.setName(name);
        item.setQuantity(QuantityFormat.parse(quantityText));
        item.setUnit(spUnit.getSelectedItem().toString());
        item.setExpiryMillis(expiryMillis);

        boolean saved;
        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            // An id of -1 means the item is new (the same convention as the study guide).
            saved = itemId == PantryItem.NO_ID
                    ? dataSource.insertItem(item)
                    : dataSource.updateItem(item);
        } catch (Exception e) {
            saved = false;
        } finally {
            dataSource.close();
        }

        if (saved) {
            Toast.makeText(this, getString(R.string.item_saved, name), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }

    /** Shows an error on each invalid field. Returns true only if everything is valid. */
    private boolean validate(String name, String quantityText) {
        boolean valid = true;

        switch (InputValidator.checkName(name)) {
            case EMPTY:
                tilName.setError(getString(R.string.error_name_empty));
                valid = false;
                break;
            case TOO_LONG:
                tilName.setError(getString(R.string.error_name_long, InputValidator.MAX_NAME_LENGTH));
                valid = false;
                break;
            case NO_LETTERS:
                tilName.setError(getString(R.string.error_name_letters));
                valid = false;
                break;
            default:
                tilName.setError(null);
        }

        switch (InputValidator.checkQuantity(quantityText)) {
            case EMPTY:
                tilQuantity.setError(getString(R.string.error_qty_empty));
                valid = false;
                break;
            case NOT_A_NUMBER:
                tilQuantity.setError(getString(R.string.error_qty_invalid));
                valid = false;
                break;
            case NOT_POSITIVE:
                tilQuantity.setError(getString(R.string.error_qty_positive));
                valid = false;
                break;
            case TOO_LARGE:
                tilQuantity.setError(getString(R.string.error_qty_large));
                valid = false;
                break;
            default:
                tilQuantity.setError(null);
        }

        if (!valid) {
            // Put the cursor on the first field that needs attention.
            if (tilName.getError() != null) {
                etName.requestFocus();
            } else {
                etQuantity.requestFocus();
            }
        }
        return valid;
    }
}
