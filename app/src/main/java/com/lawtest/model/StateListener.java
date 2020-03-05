package com.lawtest.model;

// интерфейс для контроля состояния процесса извне
public interface StateListener {
    void onStartProcessing();
    void onCompleteLocal();
    void onCompleteWeb();
    void onComplete();
    void onFailure(Exception exception);
}
