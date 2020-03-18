package com.lawtest.ui.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import com.lawtest.R;
import com.lawtest.model.Appointment;

import java.util.ArrayList;

public class BaseAppointmentsListAdapter extends ArrayAdapter<Appointment> {
    private FragmentActivity context;
    private BaseAppointmentsViewModel viewModel;

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView task;
        ImageView status;
    }

    public BaseAppointmentsListAdapter(FragmentActivity context, BaseAppointmentsViewModel viewModel) {
        super(context, R.layout.spec_user_list_row);
        this.context = context;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.spec_user_list_row, null, true);
            holder = new ViewHolder();
            holder.image = rowView.findViewById(R.id.specUserAva);
            holder.name = rowView.findViewById(R.id.specUserName);
            holder.task = rowView.findViewById(R.id.specUserTask);
            holder.status = rowView.findViewById(R.id.specUserStatus);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        final Appointment appointment = getItem(position);
        viewModel.getByAppointment(appointment).observe(context, new Observer<BaseAppointmentsViewModel.AppointmentData>() {
            @Override
            public void onChanged(BaseAppointmentsViewModel.AppointmentData appointmentData) {
                if (appointmentData.ava == null) holder.image.setImageResource(R.drawable.ic_user_default);
                else holder.image.setImageURI(appointmentData.ava);

                holder.name.setText(appointmentData.name);

                holder.task.setText(appointmentData.services);

                switch (appointmentData.status) {
                    case Appointment.STATUS_SENT:
                        holder.status.setImageResource(R.drawable.pending);
                        break;
                    case Appointment.STATUS_ACCEPTED:
                        holder.status.setImageResource(R.drawable.accepted);
                        break;
                    case Appointment.STATUS_REJECTED:
                        holder.status.setImageResource(R.drawable.rejected);
                        break;
                }
            }
        });

        return rowView;
    }

    public void UpdateAppointments(ArrayList<Appointment> appointments) {
        clear();
        for (Appointment appointment: appointments) {
            add(appointment);
        }
        notifyDataSetChanged();
    }
}
