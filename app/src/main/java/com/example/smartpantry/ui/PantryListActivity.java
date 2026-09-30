package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppPreferences;
import com.example.smartpantry.data.PantryDataSource;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 1 (launcher): the pantry list. Shows every ingredient from the database
 * in a RecyclerView. Tapping a row opens the edit form, the bin icon deletes,
 * and the + button opens the same form empty to add a new ingredient.
 */
public class PantryListActivity extends BaseNavActivity implements PantryAdapter.ItemListener {

    private PantryAdapter adapter;
    private RecyclerView rvPantry;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new PantryAdapter(this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            // No item id extra means the form opens in "add" mode.
            startActivity(new Intent(this, PantryItemActivity.class));
        });

        setupBottomNav(R.id.nav_pantry);
    }

    /**
     * The list is reloaded every time the screen comes back to the front, so an item
     * that was just added, edited or deleted, or a changed setting, shows straight away.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = new ArrayList<>();
        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            items = dataSource.getItems(AppPreferences.isSortByExpiry(this));
        } catch (Exception e) {
            Toast.makeText(this, R.string.load_failed, Toast.LENGTH_LONG).show();
        } finally {
            dataSource.close();
        }

        adapter.setItems(items, AppPreferences.isExpiryAlertsOn(this));
        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = new Intent(this, PantryItemActivity.class);
        intent.putExtra(PantryItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(final PantryItem item) {
        // Ask first so a slip of the finger does not remove an ingredient.
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteItem(PantryItem item) {
        boolean deleted;
        PantryDataSource dataSource = new PantryDataSource(this);
        try {
            dataSource.open();
            deleted = dataSource.deleteItem(item.getId());
        } catch (Exception e) {
            deleted = false;
        } finally {
            dataSource.close();
        }

        if (deleted) {
            Toast.makeText(this, getString(R.string.item_deleted, item.getName()),
                    Toast.LENGTH_SHORT).show();
            loadItems();
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_LONG).show();
        }
    }
}
