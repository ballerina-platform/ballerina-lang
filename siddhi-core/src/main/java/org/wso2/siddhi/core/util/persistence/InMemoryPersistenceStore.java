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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link PersistenceStore} which will store the state in-memory.
 */
public class InMemoryPersistenceStore implements PersistenceStore {

    private static final Logger log = Logger.getLogger(InMemoryPersistenceStore.class);

    Map<String, Map<String, byte[]>> persistenceMap = new HashMap<String, Map<String, byte[]>>();
    Map<String, List<String>> revisionMap = new HashMap<String, List<String>>();


    @Override
    public void save(String siddhiAppId, String revision, byte[] data) {
        Map<String, byte[]> executionPersistenceMap = persistenceMap.get(siddhiAppId);
        if (executionPersistenceMap == null) {
            executionPersistenceMap = new HashMap<String, byte[]>();
        }

        executionPersistenceMap.put(revision, data);


        List<String> revisionList = revisionMap.get(siddhiAppId);
        if (revisionList == null) {
            revisionList = new ArrayList<String>();
            revisionMap.put(siddhiAppId, revisionList);
        }
        if (revisionList.size() == 0 || (revisionList.size() > 0 && !revision.equals(revisionList.get(
                revisionList.size() - 1)))) {
            revisionList.add(revision);
            revisionMap.put(siddhiAppId, revisionList);
        }
        persistenceMap.put(siddhiAppId, executionPersistenceMap);


    }

    @Override
    public byte[] load(String siddhiAppId, String revision) {


        Map<String, byte[]> executionPersistenceMap = persistenceMap.get(siddhiAppId);
        if (executionPersistenceMap == null) {
            log.warn("Data not found for the siddhi app " + siddhiAppId);
            return null;
        }
        return executionPersistenceMap.get(revision);
    }

    @Override
    public String getLastRevision(String siddhiAppIdentifier) {
        List<String> revisionList = revisionMap.get(siddhiAppIdentifier);
        if (revisionList == null) {
            return null;
        }
        if (revisionList.size() > 0) {
            return revisionList.get(revisionList.size() - 1);
        }
        return null;
    }

    @Override
    public void setProperties(Map properties) {
        //no properties to add
    }

    public void shutdown() {
        persistenceMap.clear();
        revisionMap.clear();
    }
}
