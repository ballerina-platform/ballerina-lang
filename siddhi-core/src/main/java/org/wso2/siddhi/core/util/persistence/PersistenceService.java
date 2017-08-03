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
package org.wso2.siddhi.core.util.persistence;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.NoPersistenceStoreException;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;

/**
 * Persistence Service is the service layer to handle state persistence tasks such as persisting current state and
 * restoring previous states.
 */
public class PersistenceService {

    private static final Logger log = Logger.getLogger(PersistenceService.class);
    private String siddhiAppName;
    private PersistenceStore persistenceStore;
    private SnapshotService snapshotService;

    public PersistenceService(SiddhiAppContext siddhiAppContext) {
        this.snapshotService = siddhiAppContext.getSnapshotService();
        this.persistenceStore = siddhiAppContext.getSiddhiContext().getPersistenceStore();
        this.siddhiAppName = siddhiAppContext.getName();
    }


    public String persist() {

        if (persistenceStore != null) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting...");
            }
            byte[] snapshot = snapshotService.snapshot();
            String revision = System.currentTimeMillis() + "_" + siddhiAppName;
            persistenceStore.save(siddhiAppName, revision, snapshot);
            if (log.isDebugEnabled()) {
                log.debug("Persisted.");
            }
            return revision;
        } else {
            throw new NoPersistenceStoreException("No persistence store assigned for siddhi app " +
                    siddhiAppName);
        }

    }

    public void restoreRevision(String revision) {
        if (persistenceStore != null) {
            if (log.isDebugEnabled()) {
                log.debug("Restoring revision: " + revision + " ...");
            }
            byte[] snapshot = persistenceStore.load(siddhiAppName, revision);
            snapshotService.restore(snapshot);
            if (log.isDebugEnabled()) {
                log.debug("Restored revision: " + revision);
            }
        } else {
            throw new NoPersistenceStoreException("No persistence store assigned for siddhi app " +
                    siddhiAppName);
        }
    }

    public void restoreLastRevision() {
        if (persistenceStore != null) {
            String revision = persistenceStore.getLastRevision(siddhiAppName);
            if (revision != null) {
                restoreRevision(revision);
            }
        } else {
            throw new NoPersistenceStoreException("No persistence store assigned for siddhi app " +
                    siddhiAppName);
        }
    }

    public void restore(byte[] snapshot) {
        snapshotService.restore(snapshot);
    }
}
