package com.lawtest.ui.user.specialists.show;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.model.AgencyService;
import com.lawtest.model.Review;
import com.lawtest.model.ReviewForList;
import com.lawtest.model.SpecialistForList;
import com.lawtest.util.MultiTaskCompleteWatcher;

import java.util.ArrayList;

// ViewModel, предоставляющая доступ к услугам и отзывам специалиста
public class SpecServicesViewModel extends ViewModel {
    private ArrayList<AgencyService> services;
    private MutableLiveData<ArrayList<AgencyService>> servicesData;
    private MutableLiveData<ArrayList<ReviewForList>> reviewsData;

    SpecServicesViewModel(SpecialistForList specialist) {
        services = new ArrayList<>();
        servicesData = new MutableLiveData<>();
        reviewsData = new MutableLiveData<>();

        final DatabaseReference database = MainActivity.getInstance().getViewModel().getDatabase()
                .child(AgencyService.DATABASE_ENTRY);

        specialist.getSpecialist().observeForever(new Observer<SpecialistForList>() {
            @Override
            public void onChanged(SpecialistForList specialist) {
                // наблюдение за изменениями в услугах специалиста
                services.clear();
                MultiTaskCompleteWatcher servicesWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        servicesData.postValue(services);
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        // TODO
                    }
                };
                for (String service: specialist.services) {
                    final MultiTaskCompleteWatcher.Task task = servicesWatcher.newTask();
                    database.child(service)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            AgencyService newService = dataSnapshot.getValue(AgencyService.class);
                            services.add(newService);
                            task.complete();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            task.fail(databaseError.toException());
                        }
                    });
                }

                // наблюдение за изменениями в отзывах
                final ArrayList<ReviewForList> newReviews = new ArrayList<>();
                MultiTaskCompleteWatcher reviewsWatcher = new MultiTaskCompleteWatcher() {
                    @Override
                    public void allComplete() {
                        reviewsData.postValue(newReviews);
                    }

                    @Override
                    public void onTaskFailed(Task task, Exception exception) {
                        //todo
                    }
                };

                for ( final String review: specialist.reviews ) {
                    final MultiTaskCompleteWatcher.Task task = reviewsWatcher.newTask();
                    MainActivity.getInstance().getViewModel().getDatabase()
                            .child(Review.DATABASE_REF)
                            .child(review)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Review reviewTemp = dataSnapshot.getValue(Review.class);
                                    newReviews.add(new ReviewForList(reviewTemp, review));
                                    task.complete();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    //todo
                                }
                            });
                }
            }
        });
    }

    public LiveData<ArrayList<AgencyService>> getServicesData() {
        return servicesData;
    }

    public LiveData<ArrayList<ReviewForList>> getReviews() {
        return reviewsData;
    }
}
