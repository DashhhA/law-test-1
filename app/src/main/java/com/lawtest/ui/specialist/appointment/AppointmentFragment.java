package com.lawtest.ui.specialist.appointment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.lawtest.ui.base.BaseAppointmentsViewModel;
import com.lawtest.ui.base.BaseAppointmentsViewModelFactory;
import com.lawtest.ui.base.SpinnerProgress;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.HashMap;
import java.util.Map;

public class AppointmentFragment extends Fragment {
    private BaseAppointmentsViewModel viewModel;
    private Appointment appointment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_specialist_appointment, container, false);
        viewModel = new ViewModelProvider(requireActivity(),
                new BaseAppointmentsViewModelFactory(MainActivity.getInstance().getViewModel().getSpecialist(), User.class)
        ).get(BaseAppointmentsViewModel.class);
        appointment = viewModel.getCurrent();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageView avaView = view.findViewById(R.id.specUserAvaView);
        final TextView userName = view.findViewById(R.id.specUserName);
        final TextView userEmail = view.findViewById(R.id.specUserEmail);
        final TextView appointmentDesc = view.findViewById(R.id.specAppointmentDescription);
        final Button accept = view.findViewById(R.id.specAppointmentAccept);
        final Button decline = view.findViewById(R.id.specAppointmentDecline);
        final TextView status = view.findViewById(R.id.specAppointmentStatus);
        final TextView userComment = view.findViewById(R.id.specAppointmentUserComment);
        accept.setVisibility(View.GONE);
        decline.setVisibility(View.GONE);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpecialistComment(Appointment.STATUS_ACCEPTED);
            }
        });
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpecialistComment(Appointment.STATUS_REJECTED);
            }
        });

        final ProgressDialog progress = new ProgressDialog(this.getContext());
        progress.setMessage(getString(R.string.appointment_loading));
        progress.show();

        final StringBuilder builder = new StringBuilder();

        viewModel.getByAppointment(viewModel.getCurrent()).observe(this.getViewLifecycleOwner(),
                new Observer<BaseAppointmentsViewModel.AppointmentData>() {
            @Override
            public void onChanged(final BaseAppointmentsViewModel.AppointmentData appointmentData) {

                MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        if (appointmentData.ava == null) avaView.setImageResource(R.drawable.ic_user_default);
                        else avaView.setImageURI(appointmentData.ava);
                        String services = builder.substring(0,builder.length()-2);
                        String desc = String.format(
                                MainActivity.getInstance().getString(R.string.appointment_desc),
                                services,
                                appointment.dateTime.hour, appointment.dateTime.minute,
                                appointment.dateTime.day, appointment.dateTime.month, appointment.dateTime.year
                        );
                        appointmentDesc.setText(desc);
                        userComment.setText(appointment.userComment);

                        if (appointmentData.status.equals(Appointment.STATUS_SENT)) {
                            accept.setVisibility(View.VISIBLE);
                            decline.setVisibility(View.VISIBLE);
                        } else {

                        }
                        switch (appointmentData.status) {
                            case Appointment.STATUS_SENT:
                                accept.setVisibility(View.VISIBLE);
                                decline.setVisibility(View.VISIBLE);
                                break;
                            case Appointment.STATUS_ACCEPTED:
                                accept.setVisibility(View.GONE);
                                decline.setVisibility(View.GONE);
                                status.setText(R.string.appointment_status_accepted);
                                status.setTextColor(Color.GREEN);
                                break;
                            case Appointment.STATUS_REJECTED:
                                accept.setVisibility(View.GONE);
                                decline.setVisibility(View.GONE);
                                status.setText(R.string.appointment_status_rejected);
                                status.setTextColor(Color.RED);
                                break;
                        }

                        progress.dismiss();
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        // todo
                    }
                };


                final MultiTaskCompleteWatcher.Task userTask = watcher.newTask();

                MainActivity.getInstance().getViewModel().getDatabase()
                        .child(User.DATABASE_TAG)
                        .child(appointment.userId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                GenericTypeIndicator<Map<String, Object>> typeIndicator =
                                        new GenericTypeIndicator<Map<String, Object> >() {};
                                Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                                User user = new User(map);

                                String name;
                                if (user.sName != null) {
                                    name = String.format("%s %s %s",
                                            user.fName, user.sName, user.surName);
                                } else {
                                    name = String.format("%s %s",
                                            user.fName, user.surName);
                                }
                                userName.setText(name);
                                userEmail.setText(user.email);

                                userTask.complete();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                userTask.fail(databaseError.toException());
                            }
                        });

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
                }
            }
        });
    }

    private void changeStatus(String status, String specialistComment) {
        Appointment appointment = viewModel.getCurrent();
        final SpinnerProgress progress = new SpinnerProgress(this.getContext());
        progress.show();
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("specialistComment", specialistComment);
        MainActivity.getInstance().getViewModel().getDatabase()
                .child(Appointment.DATABASE_REF)
                .child(appointment.id)
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AppointmentFragment.this.getActivity());
                        if (task.isSuccessful()) {
                            builder.setTitle("Success");
                        } else {
                            builder.setTitle("Error");
                            builder.setMessage(task.getException().getMessage());
                        }
                        builder.setPositiveButton("Ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
    }

    private void getSpecialistComment(final String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(R.string.appointment_add_comment);
        final EditText editText = new EditText(this.getContext());
        editText.setHint(R.string.appointment_comment_hint);
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeStatus(status, editText.getText().toString());
            }
        });
        builder.create().show();
    }
}
