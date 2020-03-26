package com.lawtest.ui.specialist.reviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lawtest.R;
import com.lawtest.model.ReviewForList;
import com.lawtest.ui.base.BaseReviewListAdapter;

import java.util.ArrayList;

// фрагмент со списком отзывов
public class ReviewsFragment extends ListFragment {
    private ReviewsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(ReviewsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_specialist_reviews, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // инициализация ListAdapter и передача его в ListView
        final BaseReviewListAdapter reviewsAdapter = new BaseReviewListAdapter(requireActivity());
        setListAdapter(reviewsAdapter);

        // "подписка" на изменения в списке отзывов и обновление ListAdapter в соответствии с ним
        viewModel.getReviews().observe(requireActivity(), new Observer<ArrayList<ReviewForList>>() {
            @Override
            public void onChanged(ArrayList<ReviewForList> review) {
                reviewsAdapter.updateReviews(review);
            }
        });
    }
}
