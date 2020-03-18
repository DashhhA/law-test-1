package com.lawtest.ui.specialist.appointments;

import android.app.Activity;
import android.content.res.ColorStateList;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Appointment;
import com.lawtest.model.User;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.Map;

public class AppointmentsListAdapter extends ArrayAdapter<Appointment> {

    private FragmentActivity context;
    private SpecAppointmentsViewModel viewModel;

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView task;
        ImageView status;
    }

    public AppointmentsListAdapter(FragmentActivity context, SpecAppointmentsViewModel viewModel) {
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
        /*switch (appointment.status) {
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

        MainActivity.getInstance().getViewModel().getDatabase()
                .child(User.DATABASE_TAG)
                .child(appointment.userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<Map<String, Object>> typeIndicator =
                                new GenericTypeIndicator<Map<String, Object> >() {};
                        Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                        final User user = new User(map);
                        holder.name.setText(user.fName + " " + user.surName);
                        if (user.getAvatarUri() != null) {
                            MainActivity.getInstance().getViewModel().getStorage()
                                    .child(User.DATABASE_AVA_FOLDER)
                                    .child(user.getAvatarUri().getLastPathSegment())
                                    .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                    .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {
                                            if (task.isSuccessful()) {
                                                utils.saveBytesToFile(user.getAvatarUri(), task.getResult());
                                                holder.image.setImageURI(user.getAvatarUri());
                                            }
                                        }
                                    });
                        } else {
                            holder.image.setImageResource(R.drawable.ic_user_default);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //todo
                    }
                });

        final StringBuilder builder = new StringBuilder();
        MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                holder.task.setText(builder.substring(0,builder.length()-2));
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                // todo
            }
        };
        for (String serviceId: appointment.ServiceIds) {
            final MultiTaskCompleteWatcher.Task task = watcher.newTask();
            MainActivity.getInstance().getViewModel().getDatabase()
                    .child(AgencyService.DATABASE_ENTRY)
                    .child(serviceId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            AgencyService service = dataSnapshot.getValue(AgencyService.class);
                            builder.append(service.name + ", ");
                            task.complete();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            task.fail(databaseError.toException());
                        }
                    });
        }*/

        viewModel.getByAppointment(appointment).observe(context, new Observer<SpecAppointmentsViewModel.AppointmentData>() {
            @Override
            public void onChanged(SpecAppointmentsViewModel.AppointmentData appointmentData) {
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
