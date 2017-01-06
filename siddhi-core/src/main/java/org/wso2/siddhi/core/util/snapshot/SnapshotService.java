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
import org.wso2.siddhi.core.debugger.QueryState;

import java.util.*;

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
        HashMap<String, Object[]> snapshots = new HashMap<String, Object[]>(snapshotableMap.size());
        List<Snapshotable> snapshotableList = new ArrayList<Snapshotable>();
        log.debug("Taking snapshot ...");
        try {
            executionPlanContext.getThreadBarrier().lock();
            for (Map.Entry<String, List<Snapshotable>> entry : snapshotableMap.entrySet()) {
                snapshotableList = entry.getValue();
                List<Object> snaps = new ArrayList<Object>();
                for (Snapshotable snapshotableElement : snapshotableList) {
                    snapshots.put(snapshotableElement.getElementId(), snapshotableElement.currentState());
                }
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

    public QueryState queryState(String queryName) {
        QueryState queryState = new QueryState();
        try {
            // Lock the threads in Siddhi
            executionPlanContext.getThreadBarrier().lock();
            List<Snapshotable> list = snapshotableMap.get(queryName);
            if (list != null) {
                for (Snapshotable element : list) {
                    Map<String, Object> subMap = new HashMap<String, Object>();
                    List<Object> unknownFieldList = new ArrayList<Object>();
                    Object[] currentState = element.currentState();
                    String elementId = element.getElementId();
                    for (Object state : currentState) {
                        if (state instanceof Map.Entry) {
                            subMap.put((String) ((Map.Entry) state).getKey(), ((Map.Entry) state).getValue());
                        } else {
                            unknownFieldList.add(state);
                        }
                    }
                    if (subMap.size() != 0) {
                        queryState.addKnownFields(new AbstractMap.SimpleEntry<String, Map<String, Object>>
                                (elementId, subMap));
                    }
                    if (unknownFieldList.size() != 0) {
                        queryState.addUnknownFields(new AbstractMap.SimpleEntry<String, Object[]>(elementId,
                                unknownFieldList.toArray()));
                    }

                }
            }

        } finally {
            executionPlanContext.getThreadBarrier().unlock();
        }
        log.debug("Taking snapshot finished.");

        return queryState;

    }


    public void restore(byte[] snapshot) {
        HashMap<String, Object[]> snapshots = (HashMap<String, Object[]>) ByteSerializer.BToO(snapshot);
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
