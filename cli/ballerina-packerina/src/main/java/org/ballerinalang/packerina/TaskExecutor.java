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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.task.Task;

import java.util.LinkedList;
import java.util.List;

/**
 * Tasks executor class.
 */
public class TaskExecutor {
    private final List<Task> tasks = new LinkedList<>();

    private TaskExecutor() {
    }

    public void executeTasks(BuildContext buildContext) {
        for (Task task : tasks) {
            task.execute(buildContext);
        }
    }

    /**
     * Task executor builder class.
     */
    public static class TaskBuilder {
        private TaskExecutor taskExecutor = new TaskExecutor();

        public TaskBuilder addTask(Task task) {
            this.taskExecutor.tasks.add(task);
            return this;
        }

        /**
         *  Add a task to the build schedule.
         *
         * @param task Task to execute during the build.
         * @param skip If true task will be skipped.
         * @return TaskBuilder instance.
         */
        public TaskBuilder addTask(Task task, boolean skip) {
            if (!skip) {
                this.taskExecutor.tasks.add(task);
            }
            return this;
        }

        public TaskExecutor build() {
            return this.taskExecutor;
        }
    }
}
