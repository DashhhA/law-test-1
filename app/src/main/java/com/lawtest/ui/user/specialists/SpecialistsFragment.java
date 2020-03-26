package com.lawtest.ui.user.specialists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.lawtest.R;
import com.lawtest.model.SpecialistForList;

import java.util.ArrayList;

// фрагмент со списком специалистов
public class SpecialistsFragment extends ListFragment {

    private SpecialistsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // получение SpecialistsViewModel, связанной с UserActivity
        viewModel = new ViewModelProvider(requireActivity()).get(SpecialistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_specialists, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // инициализация ListAdapter и передача его в ListView
        final SpecialistsListAdapter adapter = new SpecialistsListAdapter(this.getActivity());
        setListAdapter(adapter);

        // "подписка" на изменения в списке списке специалистов и обновление ListAdapter в соответствии с ним
        viewModel.getSpecialists().observe(getViewLifecycleOwner(),
                new Observer<ArrayList<SpecialistForList>>() {
                    @Override
                    public void onChanged(ArrayList<SpecialistForList> specialist) {
                        adapter.updateSpecialists(specialist);
                    }
                });

        // по нажатию на элемент списка
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setPosition(position); // сохранение выбранной позиции в ViewModel
                // переход на фрагмент, показывающий специалиста
                Navigation.findNavController(view).navigate(
                        R.id.action_nav_specialists_to_showSpecialistFragment
                );
            }
        });
    }
}
