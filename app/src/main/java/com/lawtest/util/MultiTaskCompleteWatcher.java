package com.lawtest.util;

import java.util.ArrayList;

// класс, вызывающий allComplete, когда завершатся все задачи, которые в него добавлены.
// Удобен, чтобы знать, когда выполнены все параллельные процессы
public abstract class MultiTaskCompleteWatcher{
    private ArrayList<Task> tasks;
    public MultiTaskCompleteWatcher() {
        tasks = new ArrayList<>();
    }
    public Task newTask(){
        Task task = new Task();
        tasks.add(task);
        return task;
    }

    public abstract void allComplete();
    public abstract void onTaskFailed(Task task, Exception exception);

    public class Task {
        private boolean complete;
        public void complete() {
            complete = true;
            boolean allComplete = true;
            for (Task task: tasks) allComplete = allComplete && task.isComplete();
            if (allComplete) allComplete();
        }
        public void fail(Exception exception) {
            onTaskFailed(this, exception);
        }
        boolean isComplete() { return complete; }
    }
}