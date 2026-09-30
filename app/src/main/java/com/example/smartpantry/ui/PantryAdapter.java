package com.example.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppPreferences;
import com.example.smartpantry.logic.ExpiryUtil;
import com.example.smartpantry.logic.QuantityFormat;
import com.example.smartpantry.model.PantryItem;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Feeds the pantry RecyclerView. The adapter only displays data and reports
 * taps. The Activity decides what a tap means, so no database code lives here.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Implemented by the Activity so it can react to taps on a row. */
    public interface ItemListener {
        void onItemClicked(PantryItem item);

        void onDeleteClicked(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private boolean expiryAlertsOn = true;
    private final ItemListener listener;

    public PantryAdapter(ItemListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems, boolean expiryAlertsOn) {
        this.items = newItems;
        this.expiryAlertsOn = expiryAlertsOn;
        notifyDataSetChanged();
    }

    /** Holds references to the widgets of one row so they are only looked up once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final ImageButton btnDelete;
        final int defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvExpiry = itemView.findViewById(R.id.tvItemExpiry);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
            defaultExpiryColor = tvExpiry.getCurrentTextColor();
        }
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(QuantityFormat.format(item.getQuantity()) + " " + item.getUnit());
        bindExpiry(holder, item, context);

        holder.btnDelete.setContentDescription(context.getString(R.string.cd_delete_item, item.getName()));
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Chooses the expiry wording and colour: expired (red), soon (orange), or plain. */
    private void bindExpiry(PantryViewHolder holder, PantryItem item, Context context) {
        TextView view = holder.tvExpiry;

        if (!item.hasExpiry()) {
            view.setText(R.string.no_expiry);
            view.setTextColor(holder.defaultExpiryColor);
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        long days = ExpiryUtil.daysUntil(item.getExpiryMillis(), System.currentTimeMillis(), zone);
        String date = ExpiryUtil.formatDate(item.getExpiryMillis(), zone, Locale.getDefault());

        if (days < 0) {
            view.setText(context.getString(R.string.expired_on, date));
            view.setTextColor(ContextCompat.getColor(context, R.color.status_expired));
        } else if (expiryAlertsOn && days <= AppPreferences.EXPIRING_SOON_DAYS) {
            if (days == 0) {
                view.setText(R.string.expires_today);
            } else if (days == 1) {
                view.setText(R.string.expires_tomorrow);
            } else {
                view.setText(context.getResources().getQuantityString(
                        R.plurals.expires_in_days, (int) days, (int) days));
            }
            view.setTextColor(ContextCompat.getColor(context, R.color.status_soon));
        } else {
            view.setText(context.getString(R.string.expires_on, date));
            view.setTextColor(holder.defaultExpiryColor);
        }
    }
}
