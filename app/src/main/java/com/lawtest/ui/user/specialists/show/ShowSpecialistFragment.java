package com.lawtest.ui.user.specialists.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.lawtest.model.Review;
import com.lawtest.model.ReviewForList;
import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.BaseReviewListAdapter;
import com.lawtest.ui.base.ServicesArrayAdapter;
import com.lawtest.ui.user.specialists.SpecialistsViewModel;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

// экран специалиста с информацией о нем и функциями записи и оставления отзыва
public class ShowSpecialistFragment extends Fragment {
    private SpecialistsViewModel viewModel;
    private SpecServicesViewModel servicesViewModel;
    private SpecialistForList specialist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // получение SpecialistsViewModel, связянной с UserActivity
        viewModel = new ViewModelProvider(requireActivity()).get(SpecialistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_specialist, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // получение ссылок на элементы интерфейса
        final ImageView avaView = view.findViewById(R.id.showSpecAvaView);
        final TextView nameText = view.findViewById(R.id.showSpecName);
        final TextView emailText = view.findViewById(R.id.showSpecEmail);
        final ListView reviewsList = view.findViewById(R.id.showComments);
        final Button reviewBtn = view.findViewById(R.id.btnReview);
        final Button appointmentBtn = view.findViewById(R.id.btnAppointment);
        final Button moreBtn = view.findViewById(R.id.btnMore);

        reviewBtn.setEnabled(false);
        appointmentBtn.setEnabled(false);
        moreBtn.setEnabled(false);
        final BaseReviewListAdapter reviewsAdapter = new BaseReviewListAdapter(requireActivity());
        reviewsList.setAdapter(reviewsAdapter);

        // получение данных о специалисте
        viewModel.getSpecialists().observe(this.getViewLifecycleOwner(), new Observer<ArrayList<SpecialistForList>>() {
            @Override
            public void onChanged(ArrayList<SpecialistForList> specialists) {
                SpecialistForList specialist = specialists.get(viewModel.getPosition());
                // инициализация ViewModel для услуг, связянной со специалистом
                servicesViewModel = new ViewModelProvider(
                        ShowSpecialistFragment.this,
                        new SpecServiceViewModelFactory(specialist)).get(SpecServicesViewModel.class);

                // отслеживание изменений в услугах специалиста
                servicesViewModel.getReviews().observe(
                        ShowSpecialistFragment.this.getViewLifecycleOwner(),
                        new Observer<ArrayList<ReviewForList>>() {
                            @Override
                            public void onChanged(ArrayList<ReviewForList> reviewForLists) {
                                reviewsAdapter.updateReviews(reviewForLists);
                            }
                        });

                // отслеживание изменений в данных специалиста и заполнение элементов
                // интерфейса по изменении данных
                specialist.getSpecialist().observe(
                        ShowSpecialistFragment.this.getViewLifecycleOwner(),
                        new Observer<SpecialistForList>() {
                    @Override
                    public void onChanged(final SpecialistForList specialist) {
                        ShowSpecialistFragment.this.specialist = specialist;
                        reviewBtn.setEnabled(true);
                        appointmentBtn.setEnabled(true);
                        moreBtn.setEnabled(true);

                        // заполнение элементов ui
                        String name;
                        if (specialist.sName != null) {
                            name = String.format("%s %s %s",
                                    specialist.fName, specialist.sName, specialist.surName);
                        } else {
                            name = String.format("%s %s",
                                    specialist.fName, specialist.surName);
                        }
                        nameText.setText(name);
                        emailText.setText(specialist.email);
                        if ( specialist.getAvatarUri() != null ) avaView.setImageURI(specialist.getAvatarUri());
                        else avaView.setImageResource(R.drawable.ic_user_default); // default image

                        // new on click listener, for this snapshot of a specialist
                        reviewBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showReviewDialog(specialist);
                            }
                        });
                    }
                });
            }
        });

        appointmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAppointment();
            }
        });
    }

    // создание заявки на встречу
    private void makeAppointment() {

        final ProgressDialog progress = new ProgressDialog(ShowSpecialistFragment.this.getContext());
        progress.setMessage(getString(R.string.progress_loading_appointment));

        // уведомление о успехе/провале регистрации всречи
        final MultiTaskCompleteWatcher appointmentWatcher = new MultiTaskCompleteWatcher() {
            @Override
            public void allComplete() {
                progress.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowSpecialistFragment.this.getContext());
                builder.setTitle("Success");
                builder.setMessage(getString(R.string.appointment_success));
                builder.setPositiveButton("Ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                progress.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowSpecialistFragment.this.getContext());
                builder.setTitle("Error");
                builder.setMessage(exception.getMessage());
                builder.setPositiveButton("Ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        // подбо даты, затем нахождение доступных премен, затем показ диалога с
        // выбором времени и услуг
        datePicker(new mDateSetListener() {
            @Override
            public void onDateSet(final Appointment.DateTime dateTime) {
                getAvailableTimes(dateTime, new mAvailableTimesFoundListener() {
                    @Override
                    public void onAvailableTimesFoundListener(ArrayList<Appointment.DateTime> available) {
                        getServiceDialog(available, new mServiceSetListener() {
                            @Override
                            public void onServiceSet(Appointment.DateTime selectedTime, ArrayList<AgencyService> services, String comment) {
                                // заполнение модели записи
                                String servicesNames = "[";

                                final Appointment appointment = new Appointment();
                                appointment.dateTime = selectedTime;
                                appointment.status = Appointment.STATUS_SENT;
                                appointment.userId = MainActivity.getInstance().getViewModel().getAuth().getUid();
                                appointment.specialistId = specialist.getUid();
                                appointment.userComment = comment;
                                appointment.ServiceIds = new ArrayList<>();
                                for (AgencyService service: services) {
                                    appointment.ServiceIds.add(service.id);
                                    servicesNames = servicesNames + service.name + ", ";
                                }
                                servicesNames = servicesNames.substring(0, servicesNames.length() -2) + "] ";
                                appointment.id = UUID.randomUUID().toString();

                                // окно подтверждения записи
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(ShowSpecialistFragment.this.getActivity());
                                builder.setMessage(String.format(getString(R.string.appointment_confirm),
                                        specialist.fName, specialist.surName,
                                        servicesNames,
                                        appointment.dateTime.hour, appointment.dateTime.minute,
                                        appointment.dateTime.day, appointment.dateTime.month, appointment.dateTime.year
                                ));
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progress.show();
                                        specialist.addAppointment(appointment, appointmentWatcher);
                                    }
                                });
                                builder.setNegativeButton("Cancel", null);
                                builder.create().show();
                            }
                        });
                    }
                });
            }
        });
    }

    // показывает диалог выбора даты
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

    // получает времена, недоступные для записи
    private void getOccupiedTimes(final ArrayList<Appointment.DateTime> occupied,
                                  MultiTaskCompleteWatcher watcher
    ) {
        occupied.clear();
        MultiTaskCompleteWatcher.Task wholeTask = watcher.newTask();
        if ( specialist.appointments.isEmpty() ) {
            wholeTask.complete();
            return;
        }
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
        wholeTask.complete();
    }

    // нахождение доступных для записи времен
    private void getAvailableTimes( final Appointment.DateTime date,
                                    final mAvailableTimesFoundListener listener
    ) {
        final ArrayList<Appointment.DateTime> available = Appointment.getAvailableOnDay(date);

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
                    if (    unavailableTime.year >= date.year &&
                            unavailableTime.month >= date.month &&
                            unavailableTime.day >= date.day
                    ) {
                        int ind = available.indexOf(unavailableTime);
                        if ( ind >= 0 ) available.remove( ind );
                    }
                }

                progress.dismiss();
                if (available.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowSpecialistFragment.this.getActivity());
                    builder.setMessage(R.string.appointment_no_available_time);
                    builder.setPositiveButton("Ok", null);
                    builder.create().show();
                }else {
                    listener.onAvailableTimesFoundListener(available);
                }
            }

            @Override
            public void onTaskFailed(Task task, Exception exception) {
                // todo
            }
        };
        getOccupiedTimes(occupied, occupiedWatcher);
    }

    // диалог, позволяющий выбрать время, услуги и добавить комментарий
    private void getServiceDialog(ArrayList<Appointment.DateTime> available,
                                  final mServiceSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_appointment_old,null);
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
        final Spinner timeSpinner = view.findViewById(R.id.timeSelect);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // инициализация spinner со списком доступных услуг и ArrayAdapter для него
        final ServicesArrayAdapter arrayAdapter = new ServicesArrayAdapter(this.getActivity(), 0);
        final Spinner spinner = view.findViewById(R.id.dialogAppointmentSpinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setEnabled(!arrayAdapter.isEmpty());
        // наблюдение за услугами специалиста и обновление ArrayAdapter
        servicesViewModel.getServicesData().observe(this, new Observer<ArrayList<AgencyService>>() {
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

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", null);

        final AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (arrayAdapter.getSelected().isEmpty()) {
                            // сообщение о том, что надо выбрать хотя бы одну услугу
                            AlertDialog.Builder innerBuilder =
                                    new AlertDialog.Builder(ShowSpecialistFragment.this.getActivity());
                            innerBuilder.setMessage(R.string.appointment_warning);
                            innerBuilder.setPositiveButton("Ok", null);
                            innerBuilder.create().show();
                        } else {
                            alert.dismiss();
                            hideKeyboard(ShowSpecialistFragment.this.getActivity());
                            listener.onServiceSet(selectedTime, arrayAdapter.getSelected(), text.getText().toString());
                        }
                    }
                });
    }

    // показывает диалог для отзыва
    private void showReviewDialog(final SpecialistForList specialist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setHint(R.string.review_review_hint);
        builder.setView(editText);
        builder.setPositiveButton("Ok", null);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if ( !text.trim().isEmpty() ) {                       // проверка, что отзыв не пуст
                    // прогресс с сообщением о регистрации отзыва
                    final ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setMessage(getString(R.string.review_registering));
                    progress.show();

                    MultiTaskCompleteWatcher watcher = new MultiTaskCompleteWatcher() {
                        @Override
                        public void allComplete() {
                            progress.dismiss();
                            dialog.dismiss();
                        }

                        @Override
                        public void onTaskFailed(Task task, Exception exception) {
                            progress.dismiss();
                            dialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error");
                            builder.setMessage(exception.getMessage());
                            builder.setPositiveButton("Ok", null);
                            builder.create().show();
                        }
                    };

                    // заполнение модели отзыва
                    Review review = new Review();
                    review.body = text;
                    review.specialistId = specialist.getUid();
                    review.userId = MainActivity.getInstance().getViewModel().getAuth().getUid();
                    specialist.addReview(review, watcher); // сохранение отзыва
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage(R.string.review_must_not_be_empty);
                    builder.setPositiveButton("Ok", null);
                    builder.create().show();
                }
            }
        });
    }

    // интерфейс для получения результата выбора даты
    private interface mDateSetListener {
        void onDateSet(Appointment.DateTime dateTime);
    }

    // интерфейс для получения результата выбора времени и сервисов
    private interface mServiceSetListener {
        void onServiceSet(Appointment.DateTime selectedTime, ArrayList<AgencyService> services, String comment);
    }

    // интерфейс для получения доступных времен
    private interface mAvailableTimesFoundListener {
        void onAvailableTimesFoundListener(ArrayList<Appointment.DateTime> available);
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
