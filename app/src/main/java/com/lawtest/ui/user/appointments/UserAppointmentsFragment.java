package com.lawtest.ui.user.appointments;

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

import com.lawtest.MainActivity;
import com.lawtest.R;
import com.lawtest.model.Appointment;
import com.lawtest.model.Specialist;
import com.lawtest.ui.base.BaseAppointmentsListAdapter;
import com.lawtest.ui.base.BaseAppointmentsViewModel;
import com.lawtest.ui.base.BaseAppointmentsViewModelFactory;

import java.util.ArrayList;

// фрагмент со списком встречь пользователя
public class UserAppointmentsFragment extends ListFragment {
    private BaseAppointmentsViewModel viewModel;
    private BaseAppointmentsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_user_appointments, container, false);
        // получение ViewModel для списка встречь, привязянного к UserActivity,
        // инициированной для пользователя
        viewModel = new ViewModelProvider(requireActivity(),
                new BaseAppointmentsViewModelFactory(MainActivity.getInstance().getViewModel().getUser(), Specialist.class)
        ).get(BaseAppointmentsViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // инициализация ListAdapter и передача его в ListView
        adapter = new BaseAppointmentsListAdapter(this.getActivity(), viewModel);
        setListAdapter(adapter);

        // "подписка" на изменения в списке встреч и обновление ListAdapter в соответствии с ним
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

        // сохранение выбранной встречи в ViewModel и переход к фрагменту, показывающему встречу
        viewModel.setCurrent(adapter.getItem(position));
        Navigation.findNavController(v).navigate(
                R.id.action_userAppointmentsFragment_to_appointmentInfoFragment
        );
    }
}
