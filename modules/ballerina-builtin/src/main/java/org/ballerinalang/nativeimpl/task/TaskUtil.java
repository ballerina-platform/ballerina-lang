/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.bre.Context;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility methods for ballerina task.
 */
public class TaskUtil {
    private static final Log log = LogFactory.getLog(TaskUtil.class.getName());

    protected static synchronized int generateTaskId(Context ctx) {
        if (log.isDebugEnabled()) {
            log.info("Generating the task id");
        }
        String process = ManagementFactory.getRuntimeMXBean().getName();
        int pid = Integer.parseInt(process.substring(0, process.indexOf('@')));
        AtomicInteger id = ctx.getProperty(Constant.ID) != null ?
                new AtomicInteger(Integer.parseInt(ctx.getProperty(Constant.ID).toString())) :
                new AtomicInteger();
        ctx.setProperty(Constant.ID, id);
        int taskId = Integer.parseInt(pid + "" + id);
        if (log.isDebugEnabled()) {
            log.info("ID " + taskId + " is generated for this appointment");
        }
        return taskId;
    }
}
