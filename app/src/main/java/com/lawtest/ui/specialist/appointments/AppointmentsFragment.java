package com.lawtest.ui.specialist.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lawtest.R;
import com.lawtest.model.Appointment;

import java.util.ArrayList;

public class AppointmentsFragment extends ListFragment {
    private SpecAppointmentsViewModel viewModel;
    private AppointmentsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_specialist_appointments, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SpecAppointmentsViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AppointmentsListAdapter(this.getActivity(), viewModel);
        setListAdapter(adapter);

        viewModel.getAppointments().observe(getViewLifecycleOwner(), new Observer<ArrayList<Appointment>>() {
            @Override
            public void onChanged(ArrayList<Appointment> appointments) {
                adapter.UpdateAppointments(appointments);
            }
        });
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        viewModel.setCurrent(adapter.getItem(position));
        Navigation.findNavController(v).navigate(
                R.id.action_nav_spec_appointments_to_appointmentFragment
        );
    }
}
