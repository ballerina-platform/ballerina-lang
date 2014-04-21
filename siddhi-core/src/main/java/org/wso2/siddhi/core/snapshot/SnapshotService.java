/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.snapshot;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.util.ByteSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SnapshotService {

    static final Logger log = Logger.getLogger(SnapshotService.class);
    private List<Snapshotable> snapshotableList = new ArrayList<Snapshotable>();
    private SiddhiContext siddhiContext;

    public SnapshotService(
            SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
    }

    public void addSnapshotable(Snapshotable snapshotable) {
        snapshotableList.add(snapshotable);
    }

    public byte[] snapshot() {
        HashMap<String, SnapshotObject> snapshots = new HashMap<String, SnapshotObject>(snapshotableList.size());

        if (log.isDebugEnabled()) {
            log.debug("Taking snapshot ...");
        }
        try {
            siddhiContext.getThreadBarrier().close();
            for (Snapshotable snapshotable : snapshotableList) {
                snapshots.put(snapshotable.getElementId(), snapshotable.snapshot());
            }
        } finally {
            siddhiContext.getThreadBarrier().open();
        }
        if (log.isDebugEnabled()) {
            log.debug("Taking snapshot finished.");
        }

        return ByteSerializer.OToB(snapshots);

    }

    public void restore(byte[] snapshot) {
        HashMap<String, SnapshotObject> snapshots = (HashMap<String, SnapshotObject>) ByteSerializer.BToO(snapshot);
        try {
            this.siddhiContext.getThreadBarrier().close();
            for (Snapshotable snapshotable : snapshotableList) {
                snapshotable.restore(snapshots.get(snapshotable.getElementId()));
            }
        } finally {
            siddhiContext.getThreadBarrier().open();
        }
    }

}
