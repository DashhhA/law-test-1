package com.lawtest.ui.base;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.lawtest.R;
import com.lawtest.model.AgencyService;

import java.util.ArrayList;


public class ServicesArrayAdapter extends ArrayAdapter<ServicesArrayAdapter.ListItm> {

    private Activity context;
    private boolean isFromView = false;
    private TextView textView;
    private ArrayList<mOnCheckedChangeListener> checkedChangeListeners;

    public interface mOnCheckedChangeListener {
        public void onChanged();
    }

    public class ListItm {
        private AgencyService service;
        private boolean selected;

        ListItm(AgencyService service) {
            this.service = service;
            this.selected = false;
        }

        String getTitle() {
            return service.name;
        }

        AgencyService getService() {
            return service;
        }

        boolean isSelected() {
            return selected;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public ServicesArrayAdapter(Activity context, int resource){
        super(context, resource);

        textView = new TextView(context);
        textView.setText(R.string.new_specialist_services);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setEms(10);
        textView.setTextSize(18);
        textView.setPadding(8, 0 ,0, 0);
        textView.setTextColor(Color.BLACK);

        this.context = context;
    }

    public ServicesArrayAdapter(Activity activity, TextView textView) {
        super(activity, 0);

        this.textView = textView;
        this.context = activity;
        checkedChangeListeners = new ArrayList<>();
    }

    public ArrayList<AgencyService> getSelected() {
        ArrayList<AgencyService> selected = new ArrayList<>();
        if (isEmpty()) return selected;
        for (int i = 0; i < getCount(); i++) {
            ListItm itm = getItem(i);
            if (itm.isSelected()) selected.add(itm.getService());
        }

        return selected;
    }

    public void add(AgencyService service) {
        super.add(new ListItm(service));
    }

    public void addOnCheckedChangeListener( mOnCheckedChangeListener listener ) {
        checkedChangeListeners.add(listener);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return textView;
    }


    private View getCustomView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.spinner_item, null, true);
            holder = new ViewHolder();
            holder.textView = rowView.findViewById(R.id.spinner_text);
            holder.checkBox = rowView.findViewById(R.id.spinner_checkbox);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        // To check weather checked event fire from getview() or user input
        isFromView = true;
        holder.checkBox.setChecked(getItem(position).isSelected());
        isFromView = false;
        holder.textView.setText(getItem(position).getTitle());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isFromView) {
                    getItem(position).setSelected(isChecked);
                    notifyCheckedChanged();
                }
            }
        });

        return rowView;
    }

    private void notifyCheckedChanged() {
        for (mOnCheckedChangeListener listener: checkedChangeListeners) {
            listener.onChanged();
        }
    }

    private class ViewHolder {
        private TextView textView;
        private CheckBox checkBox;
    }

}
