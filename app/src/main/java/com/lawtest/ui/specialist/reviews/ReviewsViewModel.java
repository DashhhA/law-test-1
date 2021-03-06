package com.lawtest.ui.specialist.reviews;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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

// ViewModel, предоставляющая доступ к данным о списке отзывов
public class ReviewsViewModel extends ViewModel {
    private MediatorLiveData<ArrayList<ReviewForList>> data;

    public ReviewsViewModel() {
        data = new MediatorLiveData<>();

        // получение специалиста
        data.addSource(MainActivity.getInstance().getViewModel().getSpecialist(),
                new Observer<Specialist>() {
            @Override
            public void onChanged(Specialist specialist) {
                if (specialist.reviews == null) return; // dont continue, if there is no reviews
                final ArrayList<ReviewForList> reviews = new ArrayList<>();
                MultiTaskCompleteWatcher reviewsWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        // "публикация" списка отзывов
                        data.postValue(reviews);
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        //todo
                    }
                };
                // формирование списка отзывов специалиста
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
