package com.lawtest.ui.admin.services;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.R;
import com.lawtest.model.AgencyService;
import com.lawtest.ui.admin.AdminActivity;
import com.lawtest.ui.base.ServicesArrayAdapter;
import com.lawtest.ui.base.TasksOnActivity;
import com.lawtest.ui.base.task;

import java.util.ArrayList;

public class ServicesListFragment extends ListFragment {

    private ServicesListViewModel viewModel;
    private AdminActivity activity;
    private TasksOnActivity tasks = new TasksOnActivity();
    private ServicesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ServicesListViewModel.class);
        activity = (AdminActivity) requireActivity();
        tasks.applyTasks();
        return inflater.inflate(R.layout.fragment_admin_services_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ServicesListAdapter(getActivity());
        setListAdapter(adapter);

        viewModel.getServices().observe(this.getViewLifecycleOwner(), new Observer<ArrayList<AgencyService>>() {
            @Override
            public void onChanged(ArrayList<AgencyService> agencyServices) {
                adapter.clear();
                adapter.addAll( agencyServices );
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(ServicesListFragment.this.getActivity(), ShowServiceActivity.class);
        intent.putExtra("serviceId", adapter.getItem(position).id);
        startActivity(intent);
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
