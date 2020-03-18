package com.lawtest.ui.specialist.appointments;

import android.net.Uri;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

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
import com.lawtest.model.Specialist;
import com.lawtest.model.User;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpecAppointmentsViewModel extends ViewModel {
    private ArrayList<Appointment> appointments;
    private MutableLiveData<ArrayList<Appointment>> data;
    private Map<Appointment, LiveData<AppointmentData>> map;
    private Appointment current;

    public SpecAppointmentsViewModel() {
        appointments = new ArrayList<>();
        data = new MutableLiveData<>();
        map = new HashMap<>();

        MainActivity.getInstance().getViewModel().getSpecialist().observeForever(new Observer<Specialist>() {
            @Override
            public void onChanged(Specialist specialist) {
                appointments.clear();
                for (String appointmentId: specialist.appointments) {
                    MainActivity.getInstance().getViewModel().getDatabase()
                            .child(Appointment.DATABASE_REF)
                            .child(appointmentId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                                    appointments.add(appointment);
                                    data.postValue(appointments);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // todo
                                }
                            });
                }
            }
        });
    }

    public LiveData<AppointmentData> getByAppointment(Appointment appointment) {
        LiveData<AppointmentData> appointmentData = map.get(appointment);
        if (appointmentData == null) {
            final MutableLiveData<AppointmentData> newData = new MutableLiveData<>();
            final AppointmentData postData = new AppointmentData();

            final StringBuilder builder = new StringBuilder();

            MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
                @Override
                public void allComplete() {
                    postData.services = builder.substring(0,builder.length()-2);
                    newData.postValue(postData);
                }

                @Override
                public void onTaskFailed(Task task, Exception exception) {
                    //todo
                }
            };

            final MultiTaskCompleteWatcher.Task statusTask = watcher.newTask();
            final MultiTaskCompleteWatcher.Task nameTask = watcher.newTask();
            final MultiTaskCompleteWatcher.Task avaTask = watcher.newTask();

            MainActivity.getInstance().getViewModel().getDatabase()
                    .child(Appointment.DATABASE_REF)
                    .child(appointment.id)
                    .child("status")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            postData.status = dataSnapshot.getValue(String.class);
                            statusTask.complete();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // todo
                        }
                    });

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
                            postData.name = user.fName + " " + user.surName;
                            nameTask.complete();
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
                                                    postData.ava = user.getAvatarUri();
                                                    avaTask.complete();
                                                }
                                            }
                                        });
                            } else {
                                postData.ava = null;
                                avaTask.complete();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //todo
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

            map.put(appointment, newData);

            return newData;
        } else {
            return appointmentData;
        }
    }

    public void setCurrent(Appointment appointment) {
        this.current = appointment;
    }

    public Appointment getCurrent() {
        return current;
    }

    public LiveData<ArrayList<Appointment>> getAppointments() {
        return data;
    }

    public class AppointmentData {
        public Uri ava;
        public String name;
        public String services;
        public String status;
    }
}
