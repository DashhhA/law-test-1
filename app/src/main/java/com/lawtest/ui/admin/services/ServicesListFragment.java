package com.lawtest.ui.admin.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.ui.admin.AdminActivity;
import com.lawtest.ui.base.TasksOnActivity;
import com.lawtest.ui.base.task;

import java.util.ArrayList;

public class ServicesListFragment extends ListFragment {

    private ServicesListViewModel viewModel;
    private AdminActivity activity;
    private TasksOnActivity tasks = new TasksOnActivity();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(ServicesListViewModel.class);
        activity = (AdminActivity) requireActivity();
        tasks.applyTasks();
        return inflater.inflate(R.layout.fragment_admin_services_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_list_item_1);
        setListAdapter(adapter);

        viewModel.getServices().observe(this.getViewLifecycleOwner(), new Observer<ArrayList<AgencyService>>() {
            @Override
            public void onChanged(ArrayList<AgencyService> agencyServices) {
                adapter.clear();
                String[] names = new String[agencyServices.size()];
                for (int i=0; i<names.length; i++) names[i] = agencyServices.get(i).name;
                adapter.addAll( names );
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (activity == null) {
                tasks.addTask(new task() {
                    @Override
                    public void apply() {
                        activity.hideSpec();
                        activity.showService();
                    }
                });
            } else {
                activity.hideSpec();
                activity.showService();
            }
        }
    }
}
