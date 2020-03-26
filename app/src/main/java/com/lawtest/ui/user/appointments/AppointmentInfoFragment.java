package com.lawtest.ui.user.appointments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Appointment;
import com.lawtest.model.Specialist;
import com.lawtest.model.User;
import com.lawtest.ui.base.BaseAppointmentsViewModel;
import com.lawtest.ui.base.BaseAppointmentsViewModelFactory;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.Map;

// фрагмент с информацией о встрече
public class AppointmentInfoFragment extends Fragment {
    private Appointment appointment;
    BaseAppointmentsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_user_appointment, container, false);
        // получение ViewModel для списка встречь, привязянного к UserActivity
        viewModel = new ViewModelProvider(requireActivity(),
                new BaseAppointmentsViewModelFactory(MainActivity.getInstance().getViewModel().getUser(), Specialist.class)
        ).get(BaseAppointmentsViewModel.class);
        appointment = viewModel.getCurrent();   // полусение выбранной встречи

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // получение ссылок на элементы gui
        final ImageView avaView = view.findViewById(R.id.userSpecAvaView);
        final TextView userName = view.findViewById(R.id.userSpecName);
        final TextView userEmail = view.findViewById(R.id.userSpecEmail);
        final TextView appointmentDesc = view.findViewById(R.id.userAppointmentDescription);
        final TextView status = view.findViewById(R.id.userAppointmentStatus);
        final TextView specComment = view.findViewById(R.id.specCommentTextView);
        final TextView comment = view.findViewById(R.id.userAppointmentSpecComment);
        specComment.setVisibility(View.INVISIBLE);
        comment.setVisibility(View.INVISIBLE);

        // прогресс загрузки с уведомлением
        final ProgressDialog progress = new ProgressDialog(this.getContext());
        progress.setMessage(getString(R.string.appointment_loading));
        progress.show();

        // StringBuilder, в который будут присоединятся названия услуг
        final StringBuilder builder = new StringBuilder();

        // получение полных AppointmentForList по Appointment и "подписка" на ее изменение
        viewModel.getByAppointment(viewModel.getCurrent()).observe(this.getViewLifecycleOwner(),
                new Observer<BaseAppointmentsViewModel.AppointmentData>() {
                    @Override
                    public void onChanged(final BaseAppointmentsViewModel.AppointmentData appointmentData) {

                        MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
                            @Override
                            public void allComplete() {
                                // заполнение элементов интерфейса по получении всех данных
                                if (appointmentData.ava == null) avaView.setImageResource(R.drawable.ic_user_default);
                                else avaView.setImageURI(appointmentData.ava);
                                String services = builder.substring(0,builder.length()-2);
                                String desc = String.format(getString(R.string.appointment_desc),
                                        services,
                                        appointment.dateTime.hour, appointment.dateTime.minute,
                                        appointment.dateTime.day, appointment.dateTime.month, appointment.dateTime.year
                                );
                                appointmentDesc.setText(desc);
                                comment.setText(appointment.specialistComment);

                                switch (appointmentData.status) {
                                    case Appointment.STATUS_SENT:
                                        specComment.setVisibility(View.INVISIBLE);
                                        break;
                                    case Appointment.STATUS_ACCEPTED:
                                        specComment.setVisibility(View.VISIBLE);
                                        status.setText(R.string.appointment_status_accepted);
                                        status.setTextColor(Color.GREEN);
                                        break;
                                    case Appointment.STATUS_REJECTED:
                                        specComment.setVisibility(View.VISIBLE);
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

                        // получение данных специалиста
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

                        // получение услуг, требуемых пользователем
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
}
