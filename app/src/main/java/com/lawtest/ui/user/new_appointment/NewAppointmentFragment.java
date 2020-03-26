package com.lawtest.ui.user.new_appointment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Appointment;
import com.lawtest.model.Specialist;
import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.ServicesArrayAdapter;
import com.lawtest.ui.login.LogInActivity;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

// фрагмент записи на встречу
public class NewAppointmentFragment extends Fragment {
    private NewAppointmentViewModel viewModel;
    private ArrayList<SpecialistForList> specialists;
    private ServicesArrayAdapter arrayAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(NewAppointmentViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_new_appointment, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        specialists = new ArrayList<>();

        final Spinner spinner = view.findViewById(R.id.newAppointmentSpinner);

        // текст в spinner, предлагающий выбрать нужные услуги
        TextView textView = new TextView(this.getActivity());
        textView.setText(R.string.new_specialist_services);
        textView.setGravity(Gravity.CENTER);
        textView.setEms(10);
        textView.setTextSize(18);
        textView.setPadding(8, 0 ,0, 0);
        textView.setTextColor(Color.BLACK);

        // инициализация spinner для выбора услуг и адаптера к нему
        arrayAdapter = new ServicesArrayAdapter(this.getActivity(), textView);
        spinner.setAdapter(arrayAdapter);
        spinner.setEnabled(!arrayAdapter.isEmpty());
        // получение информации о доступных услугах и обновление ListAdapter в соответствии с ними
        viewModel.getServices().observe(this.getActivity(), new Observer<ArrayList<AgencyService>>() {
            @Override
            public void onChanged(ArrayList<AgencyService> agencyServices) {
                arrayAdapter.clear();
                for (AgencyService service: agencyServices) {
                    arrayAdapter.add(service);
                }
                arrayAdapter.notifyDataSetChanged();
                spinner.setEnabled(!arrayAdapter.isEmpty());
            }
        });

        // инициализация списка специалистов, доступных для выбранных услуг и ListAdapter для него
        final ListView listView = view.findViewById(R.id.specList);
        final NewAppointmentSpecListAdapter listAdapter = new NewAppointmentSpecListAdapter(this.getActivity());
        listView.setAdapter(listAdapter);
        // наблюдение за изменениями списка всех специалистов на сервере
        viewModel.getSpecialists().observe(getViewLifecycleOwner(), new Observer<ArrayList<SpecialistForList>>() {
            @Override
            public void onChanged(ArrayList<SpecialistForList> specialist) {
                specialists = specialist;
            }
        });

        // коллбак на изменение списка выбранных услуг
        arrayAdapter.addOnCheckedChangeListener(new ServicesArrayAdapter.mOnCheckedChangeListener() {
            @Override
            public void onChanged() {
                // фильтрация специалистов в соответствии с выбранными услугами
                ArrayList<SpecialistForList> filtered = filterByServices(arrayAdapter.getSelected());
                listAdapter.updateSpecialists(filtered);
            }
        });

        // запись к специалисту, выбранному из списка
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                generateAppointment( listAdapter.getById(id) );
            }
        });
    }

    // фильтрует специалистов по переданному списку услуг
    private ArrayList<SpecialistForList> filterByServices(ArrayList<AgencyService> services) {
        ArrayList<SpecialistForList> filtered = new ArrayList<>();
        ArrayList<String> serviceNames = new ArrayList<>();
        for ( AgencyService service: services ) serviceNames.add(service.id);
        if ( serviceNames.isEmpty() ) return filtered;

        for (SpecialistForList specialist: specialists) {
            if ( specialist.services.containsAll(serviceNames) ) {
                filtered.add(specialist);
            }
        }
        return filtered;
    }

    // показ цепочки диалогов для создания встречи
    private void generateAppointment(final SpecialistForList specialist) {
        // выбор даты
        datePicker(new mDateSetListener() {
            @Override
            public void onDateSet(Appointment.DateTime dateTime) {
                getAppointmentData(specialist, dateTime);
            }
        });
    }

    // получение информации о выборе пользователя
    private void getAppointmentData(final SpecialistForList specialist, final Appointment.DateTime day) {
        final ArrayList<Appointment.DateTime> available = Appointment.getAvailableOnDay(day);
        if (specialist.appointments.isEmpty()) {
            getTimeAndComment(specialist, available);
            return;
        }

        // сообщение о загрузки доступных времен
        final ProgressDialog progress = new ProgressDialog(this.getActivity());
        progress.setMessage(getString(R.string.appointment_loading_time));
        progress.show();

        final ArrayList<Appointment.DateTime> occupied = new ArrayList<>();
        MultiTaskCompleteWatcher occupiedWatcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                // удаление из списка доступных времен занятые
                for ( Appointment.DateTime unavailableTime: occupied ) {
                    if (    unavailableTime.year >= day.year &&
                            unavailableTime.month >= day.month &&
                            unavailableTime.day >= day.day
                    ) {
                        int ind = available.indexOf(unavailableTime);
                        if ( ind >= 0 ) available.remove( ind );
                    }
                }

                progress.dismiss();
                if (available.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewAppointmentFragment.this.getActivity());
                    builder.setMessage(R.string.appointment_no_available_time);
                    builder.setPositiveButton("Ok", null);
                    builder.create().show();
                }else {
                    getTimeAndComment(specialist, available);
                }
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                // todo
            }
        };

        getOccupiedTimes(occupied, specialist, occupiedWatcher); // получение занятых времен
    }

    // диалог, позволяющий выбрать время и добавить комментарий
    private void getTimeAndComment(final SpecialistForList specialist, ArrayList<Appointment.DateTime> available) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_appointment,null);
        final EditText text = view.findViewById(R.id.dialogAppointmentComment);
        builder.setView(view);

        // элементы списка доступных времен
        String[] times = new String[available.size()];
        int i = 0;
        for (Appointment.DateTime dateTime: available) {
            times[i] = String.format("%02d:%02d", dateTime.hour, dateTime.minute);
            i++;
        }

        // инициализация spinner со списком доступныз времен
        final Appointment.DateTime selectedTime = available.get(0);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_list_item_1, times);
        final Spinner spinner = view.findViewById(R.id.dialogAppointmentSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // получение выбранного времени
                String selected = adapter.getItem(position);
                selectedTime.hour = Integer.parseInt(selected.substring(0,2));
                selectedTime.minute = Integer.parseInt(selected.substring(3));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String servicesNames = "[";

                // заполнение данных модели встречи
                final Appointment appointment = new Appointment();
                appointment.dateTime = selectedTime;
                appointment.status = Appointment.STATUS_SENT;
                appointment.userId = MainActivity.getInstance().getViewModel().getAuth().getUid();
                appointment.specialistId = specialist.getUid();
                appointment.userComment = text.getText().toString();
                appointment.ServiceIds = new ArrayList<>();
                for (AgencyService service: arrayAdapter.getSelected()) {
                    appointment.ServiceIds.add(service.id);
                    servicesNames = servicesNames + service.name + ", ";
                }
                servicesNames = servicesNames.substring(0, servicesNames.length() -2) + "] ";
                appointment.id = UUID.randomUUID().toString();

                // окно подтверждения записи
                final ProgressDialog progress = new ProgressDialog(NewAppointmentFragment.this.getContext());
                progress.setMessage(getString(R.string.progress_loading_appointment));

                // уведомление об успехе/провале регистрации встречи
                final MultiTaskCompleteWatcher appointmentWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        progress.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewAppointmentFragment.this.getContext());
                        builder.setTitle("Success");
                        builder.setMessage(getString(R.string.appointment_success));
                        builder.setPositiveButton("Ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        progress.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewAppointmentFragment.this.getContext());
                        builder.setTitle("Error");
                        builder.setMessage(exception.getMessage());
                        builder.setPositiveButton("Ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                };
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(NewAppointmentFragment.this.getActivity());
                builder.setMessage(String.format(getString(R.string.appointment_confirm),
                        specialist.fName, specialist.surName,
                        servicesNames,
                        appointment.dateTime.hour, appointment.dateTime.minute,
                        appointment.dateTime.day, appointment.dateTime.month, appointment.dateTime.year
                ));
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard(NewAppointmentFragment.this.getActivity());
                        progress.show();
                        specialist.addAppointment(appointment, appointmentWatcher);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });

        builder.create().show();
    }

    // получает времена, недоступные для записи
    private void getOccupiedTimes(final ArrayList<Appointment.DateTime> occupied,
                                  SpecialistForList specialist,
                                  MultiTaskCompleteWatcher watcher
    ) {
        occupied.clear();
        // запрос ко всем записям к специалиста
        for ( String appointmentId: specialist.appointments ) {
            final MultiTaskCompleteWatcher.Task task = watcher.newTask();
            MainActivity.getInstance().getViewModel().getDatabase()
                    .child(Appointment.DATABASE_REF)
                    .child(appointmentId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Appointment appointmentTemp = dataSnapshot.getValue(Appointment.class);
                            if ( !appointmentTemp.status.equals(Appointment.STATUS_REJECTED) ) {
                                occupied.add(appointmentTemp.dateTime);
                            }
                            task.complete();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            task.fail(databaseError.toException());
                        }
                    });
        }
    }

    // диалог, позволяющий выббрать дату встречи
    private void datePicker(final mDateSetListener listener){
        final Appointment.DateTime dateTime = new Appointment.DateTime();

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this.getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        dateTime.day = dayOfMonth;
                        dateTime.month = monthOfYear;
                        dateTime.year = year;

                        listener.onDateSet(dateTime);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private interface mDateSetListener {
        void onDateSet(Appointment.DateTime dateTime);
    }

    // функция, прячущая клавиатуру
    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
