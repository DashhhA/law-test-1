package com.lawtest.ui.user.specialists;

import com.lawtest.ui.base.BaseSpecialistsListViewModel;

// ViewModel, предоставляющая доступ к списку специалистов и позиции, выбранной в списке
public class SpecialistsViewModel extends BaseSpecialistsListViewModel {
    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
