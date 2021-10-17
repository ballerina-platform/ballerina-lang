/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.task;

import io.ballerina.projects.PackageId;
import org.ballerinalang.langserver.commons.LanguageServerContext;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to track background services being run within a language server instance.
 *
 * @since 2.0.0
 */
public class BackgroundTaskService {

    private static final LanguageServerContext.Key<BackgroundTaskService> TASK_SERVICE_KEY =
            new LanguageServerContext.Key<>();

    private final Map<TaskKey, CompletableFuture<Void>> tasks = new ConcurrentHashMap<>();

    private BackgroundTaskService(LanguageServerContext context) {
        context.put(TASK_SERVICE_KEY, this);
    }

    public static BackgroundTaskService getInstance(LanguageServerContext context) {
        BackgroundTaskService taskService = context.get(TASK_SERVICE_KEY);
        if (taskService == null) {
            taskService = new BackgroundTaskService(context);
        }

        return taskService;
    }

    /**
     * Submit a new task to be executed in the background. For a given package, only one task with the given name can
     * be running at a given time.
     *
     * @param packageId Package with which a background task is associated
     * @param task      The task to be executed
     */
    public void submit(PackageId packageId, Task task) {
        TaskKey taskKey = TaskKey.from(packageId, task.name());

        if (isRunning(packageId, task.name())) {
            throw new IllegalStateException("A task is already running: " + taskKey);
        }

        task.setTaskId(taskKey.taskId());
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            task.onStart();
            try {
                task.execute();
            } catch (Exception e) {
                throw new TaskExecutionException(e);
            }
        });
        tasks.put(taskKey, future);

        future.thenAccept(v -> {
            task.onSuccess();
        }).exceptionally(t -> {
            if (t instanceof CompletionException) {
                t = t.getCause();
            }
            
            if (t instanceof TaskExecutionException) {
                task.onFail(t.getCause());
            } else {
                task.onFail(t);
            }
            return null;
        }).thenApply(v -> {
            tasks.remove(taskKey);
            return null;
        });
    }

    /**
     * Check if a task with the given name is running for the provided package.
     *
     * @param packageId Package ID
     * @param taskName  Name of the task
     * @return true if a task with the given name is running for the provided package
     */
    public boolean isRunning(PackageId packageId, String taskName) {
        TaskKey key = TaskKey.from(packageId, taskName);
        return tasks.containsKey(key) && !tasks.get(key).isDone();
    }

    /**
     * Cancels all running tasks and clear tasks map.
     */
    public void shutdown() {
        this.tasks.forEach(((taskKey, voidCompletableFuture) -> {
            if (!voidCompletableFuture.isDone()) {
                voidCompletableFuture.cancel(true);
            }
        }));
        this.tasks.clear();
    }

    /**
     * Represents the ID of a task.
     */
    static class TaskKey {

        private final PackageId packageId;
        private final String taskName;

        private TaskKey(PackageId packageId, String taskName) {
            this.packageId = packageId;
            this.taskName = taskName;
        }

        static TaskKey from(PackageId packageId, String taskName) {
            return new TaskKey(packageId, taskName);
        }

        public String taskId() {
            return packageId.id() + "." + taskName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaskKey taskKey = (TaskKey) o;
            return Objects.equals(packageId, taskKey.packageId) && Objects.equals(taskName, taskKey.taskName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageId, taskName);
        }

        @Override
        public String toString() {
            return "TaskKey{" +
                    "packageId=" + packageId +
                    ", taskName='" + taskName + '\'' +
                    '}';
        }
    }
}
