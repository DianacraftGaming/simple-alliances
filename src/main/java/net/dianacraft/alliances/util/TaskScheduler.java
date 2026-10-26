package net.dianacraft.alliances.util;

import net.dianacraft.alliances.Main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskScheduler {

    private static final List<Task> tasks = new ArrayList<>();
    private static final List<Task> newTasks = new ArrayList<>();
    private static boolean clearTasks = false;

    public static void scheduleTask(int ticks, Runnable goal) {
        Task task = new Task(ticks, goal);
        newTasks.add(task);
    }

    public static void schedulePriorityTask(int ticks, Runnable goal) {
        Task task = new Task(ticks, goal);
        newTasks.add(task);
    }

    public static void clearTasks() {
        clearTasks = true;
        newTasks.clear();
    }

    public static void onTick(boolean gameFrozen) {
        try {
            if (clearTasks) {
                clearTasks = false;
                tasks.clear();
                return;
            }

            Iterator<Task> iterator = tasks.iterator();

            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (!gameFrozen) {
                    task.tickCount--;
                    //if (task.tickCount % 20 == 0) Main.LOGGER.info(String.valueOf(task.tickCount));
                    if (task.tickCount <= 0) {
                        try {
                            //Inner try-catch to prevent errors from preventing the task from being removed
                            //Main.LOGGER.info("running...");
                            task.goal.run();
                        }catch(Exception e) {
                            Main.LOGGER.error("Fatal error while running task " + task);
                            e.printStackTrace();
                        }
                        iterator.remove();
                    }
                }
            }

            tasks.addAll(newTasks);
            newTasks.clear();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static class Task {
        private int tickCount;
        private final Runnable goal;

        public Task(int tickCount, Runnable goal) {
            this.tickCount = tickCount;
            this.goal = goal;
        }
    }
}