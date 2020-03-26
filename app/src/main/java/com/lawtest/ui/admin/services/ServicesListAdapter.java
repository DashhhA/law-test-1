package com.lawtest.ui.admin.services;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lawtest.R;
import com.lawtest.model.AgencyService;

// ArrayAdapter для списка услуг
public class ServicesListAdapter extends ArrayAdapter<AgencyService> {
    private Activity activity;
    ServicesListAdapter(Activity activity) {
        super(activity, android.R.layout.simple_list_item_1);
        this.activity = activity;
    }

    // класс, содержащий ссылки на элементы итерфейса
    private class ViewHolder {
        TextView text;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        // если строка не была сохранена в convertView, создается новый ViewHolder и сохраняется
        // в rowView. Иначе ViewHolder берется из rowView.
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(android.R.layout.simple_list_item_1, null, true);
            holder = new ViewHolder();
            holder.text = rowView.findViewById(android.R.id.text1);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        final AgencyService service = getItem(position);
        holder.text.setText(service.name);
        return rowView;
    }
}
