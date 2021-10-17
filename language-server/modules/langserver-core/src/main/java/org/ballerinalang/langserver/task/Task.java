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

/**
 * Represents a task which can be executed in the background for a given language server instance.
 *
 * @since 2.0.0
 */
public abstract class Task {

    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * Invoked before starting the task execution.
     */
    public abstract void onStart();

    /**
     * Actual task is performed within this method.
     *
     * @throws Exception Due to issues in execution.
     */
    public abstract void execute() throws Exception;

    /**
     * Invoked when the task is successfully executed.
     */
    public abstract void onSuccess();

    /**
     * Invoked when the task execution fails due to an exception.
     *
     * @param t Exception occurred.
     */
    public abstract void onFail(Throwable t);

    /**
     * A name for the task. Each implementation class should have a unique name.
     *
     * @return Name
     */
    public abstract String name();
}
