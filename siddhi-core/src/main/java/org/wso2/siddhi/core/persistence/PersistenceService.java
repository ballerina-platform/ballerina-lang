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
package org.wso2.siddhi.core.persistence;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.NoPersistenceStoreAssignedException;

import java.util.ArrayList;
import java.util.List;

public class PersistenceService {

    static final Logger log = Logger.getLogger(PersistenceService.class);
    private String queryPlanIdentifier;
    private PersistenceStore persistenceStore;
    private SiddhiContext siddhiContext;

    public PersistenceService(
            SiddhiContext siddhiContext) {
        this.queryPlanIdentifier = siddhiContext.getQueryPlanIdentifier();
        this.siddhiContext = siddhiContext;
    }


    public String persist() {

        if (persistenceStore != null) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting...");
            }
            byte[] snapshot = siddhiContext.getSnapshotService().snapshot();
            String revision = System.currentTimeMillis() + "_" + queryPlanIdentifier;
            persistenceStore.save(queryPlanIdentifier, revision, snapshot);
            if (log.isDebugEnabled()) {
                log.debug("Persisted.");
            }
            return revision;
        } else {
            throw new NoPersistenceStoreAssignedException("No persistence store assigned for execution plan " + queryPlanIdentifier);
        }

    }

    public void restoreRevision(String revision) {

        if (persistenceStore != null) {
            if (log.isDebugEnabled()) {
                log.debug("Restoring revision: " + revision + " ...");
            }
            byte[] snapshot = persistenceStore.load(queryPlanIdentifier, revision);
            siddhiContext.getSnapshotService().restore(snapshot);
            if (log.isDebugEnabled()) {
                log.debug("Restored revision: " + revision);
            }
        } else {
            throw new NoPersistenceStoreAssignedException("No persistence store assigned for execution plan " + queryPlanIdentifier);
        }

    }

//    public PersistenceStore getPersistenceStore() {
//        return persistenceStore;
//    }

    public void setPersistenceStore(PersistenceStore persistenceStore) {
        this.persistenceStore = persistenceStore;
    }


    public void restoreLastRevision() {
        try {
            this.siddhiContext.getThreadBarrier().close();
            if (persistenceStore != null) {
                String revision = persistenceStore.getLastRevision(queryPlanIdentifier);
                if (revision != null) {
                    restoreRevision(revision);
                }
            } else {
                throw new NoPersistenceStoreAssignedException("No persistence store assigned for execution plan " + queryPlanIdentifier);
            }
        } finally {
            siddhiContext.getThreadBarrier().open();
        }
    }
}
