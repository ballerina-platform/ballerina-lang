/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.util.Map;

public class SnapshotService {


    private static final Logger log = Logger.getLogger(SnapshotService.class);
    private HashMap<String, List<Snapshotable>> snapshotableMap = new HashMap<String, List<Snapshotable>>();
    private ExecutionPlanContext executionPlanContext;

    public SnapshotService(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    public synchronized void addSnapshotable(String queryName, Snapshotable snapshotable) {

        List<Snapshotable> snapshotableList = snapshotableMap.get(queryName);

        // if List does not exist create it
        if (snapshotableList == null) {
            snapshotableList = new ArrayList<Snapshotable>();
            snapshotableList.add(snapshotable);
            snapshotableMap.put(queryName, snapshotableList);
        } else {
            // add if item is not already in list
            if (!snapshotableList.contains(snapshotable)) snapshotableList.add(snapshotable);
        }
    }

    public byte[] snapshot() {
        HashMap<String, Map<String, Object>> snapshots = new HashMap<>(snapshotableMap.size());
        List<Snapshotable> snapshotableList = new ArrayList<>();
        byte[] serializedSnapshots;
        if (log.isDebugEnabled()) {
            log.debug("Taking snapshot ...");
        }
        try {
            executionPlanContext.getThreadBarrier().lock();
            for (Map.Entry<String, List<Snapshotable>> entry : snapshotableMap.entrySet()) {
                snapshotableList = entry.getValue();
                snapshotableList.forEach(snapshotableElement -> snapshots.put(snapshotableElement.getElementId(),
                        snapshotableElement.currentState()));
            }
            if (log.isDebugEnabled()) {
                log.debug("Snapshot serialization started ...");
            }
            serializedSnapshots = ByteSerializer.OToB(snapshots);
            if (log.isDebugEnabled()) {
                log.debug("Snapshot serialization finished.");
            }
        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
        if (log.isDebugEnabled()) {
            log.debug("Snapshot taken for Execution Plan '" + executionPlanContext.getName() + "'");
        }

        return serializedSnapshots;
    }

    public Map<String, Object> queryState(String queryName) {
        Map<String, Object> state = new HashMap<>();
        try {
            // Lock the threads in Siddhi
            executionPlanContext.getThreadBarrier().lock();
            List<Snapshotable> list = snapshotableMap.get(queryName);

            if (list != null) {
                for (Snapshotable element : list) {
                    Map<String, Object> elementState = element.currentState();
                    String elementId = element.getElementId();
                    state.put(elementId, elementState);
                }
            }

        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
        log.debug("Taking snapshot finished.");

        return state;

    }


    public void restore(byte[] snapshot) {
        HashMap<String, Map<String, Object>> snapshots = (HashMap<String, Map<String, Object>>) ByteSerializer.BToO(snapshot);
        List<Snapshotable> snapshotableList;
        try {
            this.executionPlanContext.getThreadBarrier().lock();
            for (Map.Entry<String, List<Snapshotable>> entry : snapshotableMap.entrySet()) {
                snapshotableList = entry.getValue();
                for (Snapshotable snapshotable : snapshotableList) {
                    snapshotable.restoreState(snapshots.get(snapshotable.getElementId()));
                }
            }
        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
    }

}
