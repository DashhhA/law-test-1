package com.lawtest.ui.user.specialists.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Appointment;
import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.base.ServicesArrayAdapter;
import com.lawtest.ui.user.specialists.SpecialistsViewModel;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class ShowSpecialistFragment extends Fragment {
    private SpecialistsViewModel viewModel;
    private SpecServicesViewModel servicesViewModel;
    SpecialistForList specialist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SpecialistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_specialist, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageView avaView = view.findViewById(R.id.showSpecAvaView);
        final TextView nameText = view.findViewById(R.id.showSpecName);
        final TextView emailText = view.findViewById(R.id.showSpecEmail);
        final Button reviewBtn = view.findViewById(R.id.btnReview);
        final Button appointmentBtn = view.findViewById(R.id.btnAppointment);
        final Button moreBtn = view.findViewById(R.id.btnMore);

        reviewBtn.setEnabled(false);
        appointmentBtn.setEnabled(false);
        moreBtn.setEnabled(false);

        viewModel.getSpecialists().observe(this.getViewLifecycleOwner(), new Observer<ArrayList<SpecialistForList>>() {
            @Override
            public void onChanged(ArrayList<SpecialistForList> specialists) {
                SpecialistForList specialist = specialists.get(viewModel.getPosition());
                servicesViewModel = new ViewModelProvider(
                        ShowSpecialistFragment.this, new SpecServiceViewModelFactory(specialist))
                        .get(SpecServicesViewModel.class);

                specialist.getSpecialist().observe(
                        ShowSpecialistFragment.this.getViewLifecycleOwner(),
                        new Observer<SpecialistForList>() {
                    @Override
                    public void onChanged(SpecialistForList specialist) {
                        ShowSpecialistFragment.this.specialist = specialist;
                        reviewBtn.setEnabled(true);
                        appointmentBtn.setEnabled(true);
                        moreBtn.setEnabled(true);

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

    private void makeAppointment() {

        final ProgressDialog progress = new ProgressDialog(ShowSpecialistFragment.this.getContext());
        progress.setMessage(getString(R.string.progress_loading_appointment));

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

        getServiceDialog(new mServiceSetListener() {
            @Override
            public void onServiceSet(final ArrayList<AgencyService> services, final String comment) {
                datePicker(new mDateSetListener() {
                    @Override
                    public void onDateSet(Appointment.DateTime dateTime) {
                        String servicesNames = "[";

                        final Appointment appointment = new Appointment();
                        appointment.dateTime = dateTime;
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
                                specialist.addAppointment(appointment, appointmentWatcher, ShowSpecialistFragment.this.getActivity());
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();
                    }
                });
            }
        });
    }

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

                        timePicker(dateTime, listener);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void timePicker(final Appointment.DateTime dateTime, final mDateSetListener listener){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this.getContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        dateTime.hour = hourOfDay;
                        dateTime.minute = minute;

                        listener.onDateSet(dateTime);
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    private void getServiceDialog(final mServiceSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_appointment_old,null);
        final EditText text = view.findViewById(R.id.dialogAppointmentComment);
        builder.setView(view);

        final ServicesArrayAdapter arrayAdapter = new ServicesArrayAdapter(this.getActivity(), 0);
        final Spinner spinner = view.findViewById(R.id.dialogAppointmentSpinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setEnabled(!arrayAdapter.isEmpty());
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

        builder.setPositiveButton("Ok", null);

        final AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (arrayAdapter.getSelected().isEmpty()) {
                            AlertDialog.Builder innerBuilder =
                                    new AlertDialog.Builder(ShowSpecialistFragment.this.getActivity());
                            innerBuilder.setMessage(R.string.appointment_warning);
                            innerBuilder.setPositiveButton("Ok", null);
                            innerBuilder.create().show();
                        } else {
                            alert.dismiss();
                            hideKeyboard(ShowSpecialistFragment.this.getActivity());
                            listener.onServiceSet(arrayAdapter.getSelected(), text.getText().toString());
                        }
                    }
                });
    }

    private interface mDateSetListener {
        void onDateSet(Appointment.DateTime dateTime);
    }

    private interface mServiceSetListener {
        void onServiceSet(ArrayList<AgencyService> services, String comment);
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
