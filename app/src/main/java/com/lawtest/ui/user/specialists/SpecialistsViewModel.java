package com.lawtest.ui.user.specialists;

import com.lawtest.ui.base.BaseSpecialistsListViewModel;

public class SpecialistsViewModel extends BaseSpecialistsListViewModel {
    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
