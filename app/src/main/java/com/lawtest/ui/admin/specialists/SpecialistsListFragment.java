package com.lawtest.ui.admin.specialists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.R;
import com.lawtest.model.SpecialistForList;
import com.lawtest.ui.admin.AdminActivity;
import com.lawtest.ui.base.TasksOnActivity;
import com.lawtest.ui.base.task;

import java.util.ArrayList;

// фрагмент, содержащий список специалистов
public class SpecialistsListFragment extends ListFragment {

    private SpecialistsListViewModel viewModel;
    private AdminActivity activity;
    private TasksOnActivity tasks = new TasksOnActivity(); // задачи, которые будут выполнены в onCreateView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(SpecialistsListViewModel.class);
        activity = (AdminActivity) requireActivity();
        tasks.applyTasks(); // исполнение отложенных задач
        View root = inflater.inflate(R.layout.fragment_admin_specialists, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // установка ListAdapter
        final SpecialistsListAdapter adapter = new SpecialistsListAdapter(this.getActivity());
        setListAdapter(adapter);

        // получение данных об изменениях списка специалистов и обновление ListView
        viewModel.getSpecialists().observe(getViewLifecycleOwner(),
                new Observer<ArrayList<SpecialistForList>>() {
            @Override
            public void onChanged(ArrayList<SpecialistForList> specialist) {
                adapter.updateSpecialists(specialist);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // выполняется когда пользователь видит фрагмент
            if (activity == null) {
                // если фрагмент еще не привязан к активити
                tasks.addTask(new task() {
                    @Override
                    public void apply() {
                        activity.showSpec();
                        activity.hideService();
                    }
                });
            } else {
                activity.showSpec();
                activity.hideService();
            }
        }
    }
}
