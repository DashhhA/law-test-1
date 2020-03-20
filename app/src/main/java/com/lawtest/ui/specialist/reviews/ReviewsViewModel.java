package com.lawtest.ui.specialist.reviews;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.model.Review;
import com.lawtest.model.ReviewForList;
import com.lawtest.model.Specialist;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.ArrayList;

public class ReviewsViewModel extends ViewModel {
    private MutableLiveData<ArrayList<ReviewForList>> data;

    public ReviewsViewModel() {
        data = new MutableLiveData<>();

        MainActivity.getInstance().getViewModel().getSpecialist().observeForever(
                new Observer<Specialist>() {
            @Override
            public void onChanged(Specialist specialist) {
                final ArrayList<ReviewForList> reviews = new ArrayList<>();
                MultiTaskCompleteWatcher reviewsWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        data.postValue(reviews);
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        //todo
                    }
                };
                for ( final String reviewId: specialist.reviews ) {
                    final MultiTaskCompleteWatcher.Task task = reviewsWatcher.newTask();
                    MainActivity.getInstance().getViewModel().getDatabase()
                            .child(Review.DATABASE_REF)
                            .child(reviewId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Review reviewTemp = dataSnapshot.getValue(Review.class);
                                    reviews.add(new ReviewForList(reviewTemp, reviewId));
                                    task.complete();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    task.fail(databaseError.toException());
                                }
                            });
                }
            }
        });
    }

    LiveData<ArrayList<ReviewForList>> getReviews(){
        return data;
    }
}
