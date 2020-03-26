package com.lawtest.ui.base;

import java.util.ArrayList;

// класс, нужный для вызова методов в OnCreateView
public class TasksOnActivity {

    private ArrayList<task> tasks = new ArrayList<>();

    public void applyTasks() {
        for (task task: tasks) task.apply();
    }

    public void addTask( task task ) {
        tasks.add(task);
    }
}
