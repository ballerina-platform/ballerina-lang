/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.jvm;

import java.util.concurrent.Future;

/**
 * Strand base class used with jvm code generation for functions.
 *
 * @since 0.955.0
 */

public class Strand {
    public boolean yield;
    public Object[] frames;
    public int resumeIndex;
    public Future future;
    public boolean blocked;
    public Strand blockedOn;
    public Scheduler scheduler;

    public Strand(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void block() {

    }

    public void resume(Object returnValue) {

    }
}
