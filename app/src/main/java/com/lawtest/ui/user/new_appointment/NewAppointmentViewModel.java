package com.lawtest.ui.user.new_appointment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lawtest.MainActivity;
import com.lawtest.model.AgencyService;
import com.lawtest.ui.base.BaseSpecialistsListViewModel;

import java.util.ArrayList;

// ViewModel, предоставляющая доступ к списку услуг и специалистов
public class NewAppointmentViewModel extends BaseSpecialistsListViewModel {
    private MutableLiveData<ArrayList<AgencyService>> services;

    private ValueEventListener servicesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<AgencyService> newServices = new ArrayList<>();
            for (DataSnapshot data: dataSnapshot.getChildren()) {
                AgencyService service = data.getValue(AgencyService.class);
                newServices.add(service);
            }
            services.postValue(newServices);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // TODO
        }
    };

    public NewAppointmentViewModel() {
        super();
        services = new MutableLiveData<>();
        MainActivity.getInstance().getViewModel().getDatabase()
                .child(AgencyService.DATABASE_ENTRY)
                .addValueEventListener(servicesEventListener);
    }

    public LiveData<ArrayList<AgencyService>> getServices() {
        if (services ==  null) services = new MutableLiveData<>();
        return services;
    }


}
