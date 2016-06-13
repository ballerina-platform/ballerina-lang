/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.snapshot;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SnapshotService {


    private static final Logger log = Logger.getLogger(SnapshotService.class);
    private List<Snapshotable> snapshotableList = new ArrayList<Snapshotable>();
    private ExecutionPlanContext executionPlanContext;

    public SnapshotService(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    public void addSnapshotable(Snapshotable snapshotable) {
        snapshotableList.add(snapshotable);
    }

    public byte[] snapshot() {
        HashMap<String, Object[]> snapshots = new HashMap<String, Object[]>(snapshotableList.size());
        log.debug("Taking snapshot ...");
        try {
            executionPlanContext.getThreadBarrier().lock();
            for (Snapshotable snapshotable : snapshotableList) {
                snapshots.put(snapshotable.getElementId(), snapshotable.currentState());
            }
        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
        log.info("Snapshot taken of Execution Plan '" + executionPlanContext.getName() + "'");

        log.debug("Snapshot serialization started ...");
        byte[] serializedSnapshots = ByteSerializer.OToB(snapshots);
        log.debug("Snapshot serialization finished.");
        return serializedSnapshots;

    }

    public void restore(byte[] snapshot) {
        HashMap<String, Object[]> snapshots = (HashMap<String, Object[]>) ByteSerializer.BToO(snapshot);
        try {
            this.executionPlanContext.getThreadBarrier().lock();
            for (Snapshotable snapshotable : snapshotableList) {
                snapshotable.restoreState(snapshots.get(snapshotable.getElementId()));
            }
        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
    }

}
