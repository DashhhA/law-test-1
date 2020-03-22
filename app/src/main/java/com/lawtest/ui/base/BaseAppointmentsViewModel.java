package com.lawtest.ui.base;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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
import com.lawtest.model.AgencyService;
import com.lawtest.model.Appointment;
import com.lawtest.model.BasePerson;
import com.lawtest.model.Specialist;
import com.lawtest.model.User;
import com.lawtest.util.MultiTaskCompleteWatcher;
import com.lawtest.util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BaseAppointmentsViewModel extends ViewModel {
    private ArrayList<Appointment> appointments;
    private MediatorLiveData<ArrayList<Appointment>> data;
    private Map<Appointment, LiveData<AppointmentData>> map;
    private Appointment current;
    private Class tClass;
    LiveData<BasePerson> person;

    public BaseAppointmentsViewModel(LiveData<BasePerson> person, Class tClass) {
        appointments = new ArrayList<>();
        data = new MediatorLiveData<>();
        map = new HashMap<>();
        this.tClass = tClass;
        this.person = person;

        data.addSource(person, new Observer<BasePerson>() {
            @Override
            public void onChanged(BasePerson person) {
                final ArrayList<Appointment> appointmentsNew = new ArrayList<>();
                MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        appointments = appointmentsNew;
                        data.postValue(appointments);
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        // todo
                    }
                };
                for (String appointmentId: person.appointments) {
                    final MultiTaskCompleteWatcher.Task task = watcher.newTask();
                    MainActivity.getInstance().getViewModel().getDatabase()
                            .child(Appointment.DATABASE_REF)
                            .child(appointmentId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                                    appointmentsNew.add(appointment);
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

            /*MainActivity.getInstance().getViewModel().getDatabase()
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
                    });*/

            MainActivity.getInstance().getViewModel().getDatabase()
                    .child(getDatabaseTag(tClass))
                    .child(getPersonId(tClass, appointment))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            GenericTypeIndicator<Map<String, Object>> typeIndicator =
                                    new GenericTypeIndicator<Map<String, Object> >() {};
                            Map<String, Object> map = dataSnapshot.getValue(typeIndicator);
                            try {
                                final BasePerson person = (BasePerson) tClass.newInstance();
                                person.fromMap(map);
                                postData.name = person.fName + " " + person.surName;
                                nameTask.complete();
                                if (person.getAvatarUri() != null) {
                                    MainActivity.getInstance().getViewModel().getStorage()
                                            .child(getDatabaseAvaFolder(tClass))
                                            .child(person.getAvatarUri().getLastPathSegment())
                                            .getBytes(utils.MAX_DOWNLOAD_BYTES)
                                            .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                                @Override
                                                public void onComplete(@NonNull Task<byte[]> task) {
                                                    if (task.isSuccessful()) {
                                                        utils.saveBytesToFile(person.getAvatarUri(), task.getResult());
                                                        postData.ava = person.getAvatarUri();
                                                        avaTask.complete();
                                                    } else {
                                                        avaTask.fail(task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    postData.ava = null;
                                    avaTask.complete();
                                }
                            } catch (Exception e) {
                                // TODO
                                avaTask.fail(e);
                                nameTask.fail(e);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            nameTask.fail(databaseError.toException());
                            avaTask.fail(databaseError.toException());
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

    private String getDatabaseAvaFolder(Class c) {
        if (c.equals(User.class)) return User.DATABASE_AVA_FOLDER;
        if (c.equals(Specialist.class)) return Specialist.DATABASE_AVA_FOLDER;
        return null;
    }

    private String getDatabaseTag(Class c) {
        if (c.equals(User.class)) return User.DATABASE_TAG;
        if (c.equals(Specialist.class)) return Specialist.DATABASE_TAG;
        return null;
    }

    private String getPersonId(Class c, Appointment appointment) {
        if (c.equals(User.class)) return appointment.userId;
        if (c.equals(Specialist.class)) return appointment.specialistId;
        return null;
    }
}
