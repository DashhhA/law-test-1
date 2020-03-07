package com.lawtest.ui.user.specialists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.lawtest.R;

public class SpecialistsFragment extends Fragment {

    private SpecialistsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(SpecialistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_specialists, container, false);

        return root;
    }
}
