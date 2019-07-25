/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina;

import org.ballerinalang.packerina.task.Task;

import java.util.LinkedList;
import java.util.List;

/**
 * Tasks executor class.
 */
public class TaskExecutor {
    private List<Task> tasks;
    public TaskExecutor(List<Task> tasks) {
        this.tasks = tasks;
    }
    
    public void executeTasks(BuildContext buildContext) {
        for (Task task : tasks) {
            task.execute(buildContext);
        }
    }
    
    public static class TaskBuilder {
        private List<Task> tasks = new LinkedList<>();
        
        public TaskBuilder addTask(Task task) {
            this.tasks.add(task);
            return this;
        }
        
        public TaskExecutor build() {
            return new TaskExecutor(this.tasks);
        }
    }
}
