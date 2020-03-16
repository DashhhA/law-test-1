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

public class SpecialistsFragment extends ListFragment {

    private SpecialistsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SpecialistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_specialists, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SpecialistsListAdapter adapter = new SpecialistsListAdapter(this.getActivity());
        viewModel.getSpecialists().observe(getViewLifecycleOwner(),
                new Observer<ArrayList<SpecialistForList>>() {
                    @Override
                    public void onChanged(ArrayList<SpecialistForList> specialist) {
                        adapter.updateSpecialists(specialist);
                    }
                });
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setPosition(position);
                Navigation.findNavController(view).navigate(
                        R.id.action_nav_specialists_to_showSpecialistFragment
                );
            }
        });
    }
}
