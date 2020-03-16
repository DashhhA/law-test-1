package com.lawtest.ui.base;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lawtest.R;
import com.lawtest.model.SpecialistForList;

import java.util.ArrayList;

public abstract class BaseSpecialistListAdapter extends ArrayAdapter<SpecialistForList> {
    private Activity context;

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView email;
        ImageButton button;
    }

    public BaseSpecialistListAdapter(Activity context) {
        super (context, R.layout.person_list_row);

        this.context = context;
    }

    public abstract void onButtonClicked(SpecialistForList specialist);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.person_list_row, null, true);
            holder = new ViewHolder();
            holder.image = rowView.findViewById(R.id.person_row_ava);
            holder.name = rowView.findViewById(R.id.person_row_name);
            holder.email = rowView.findViewById(R.id.person_row_email);
            holder.button = rowView.findViewById(R.id.person_row_button);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        final SpecialistForList specialist = getItem(position);
        specialist.getSpecialist().observe((FragmentActivity)context, new Observer<SpecialistForList>() {
            @Override
            public void onChanged(SpecialistForList specialist) {
                if (specialist.getAvatarUri() != null) holder.image.setImageURI(specialist.getAvatarUri());
                else holder.image.setImageResource(R.drawable.ic_user_default);
                String name = specialist.fName + " "+ specialist.surName;
                holder.name.setText(name);
                holder.email.setText(specialist.email);
            }
        });
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked(specialist);
            }
        });

        return rowView;
    }

    public void updateSpecialists( ArrayList<SpecialistForList> specialists ) {
        clear();
        addAll(specialists);
        notifyDataSetChanged();
    }
}
