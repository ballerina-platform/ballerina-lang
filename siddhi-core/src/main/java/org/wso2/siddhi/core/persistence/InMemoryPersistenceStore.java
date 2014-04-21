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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryPersistenceStore implements PersistenceStore {

    private static final Logger log = Logger.getLogger(InMemoryPersistenceStore.class);
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(new Config().setInstanceName(UUID.randomUUID().toString()));
    IMap<String, Map<String, byte[]>> persistenceMap = hazelcastInstance.getMap("persistenceMap");
    IMap<String, List<String>> revisionMap = hazelcastInstance.getMap("revisionMap");


    @Override
    public void save(String queryPlanIdentifier, String revision, byte[] data) {
        Map<String, byte[]> executionPersistenceMap = persistenceMap.get(queryPlanIdentifier);
        if (executionPersistenceMap == null) {
            executionPersistenceMap = new HashMap<String, byte[]>();
        }

        executionPersistenceMap.put(revision, data);


        List<String> revisionList = revisionMap.get(queryPlanIdentifier);
        if (revisionList == null) {
            revisionList = new ArrayList<String>();
            revisionMap.put(queryPlanIdentifier, revisionList);
        }
        if (revisionList.size() == 0 || (revisionList.size() > 0 && !revision.equals(revisionList.get(revisionList.size() - 1)))) {
            revisionList.add(revision);
            revisionMap.put(queryPlanIdentifier, revisionList);
        }
        persistenceMap.put(queryPlanIdentifier, executionPersistenceMap);


    }

    @Override
    public byte[] load(String queryPlanIdentifier, String revision) {


        Map<String, byte[]> executionPersistenceMap = persistenceMap.get(queryPlanIdentifier);
        if (executionPersistenceMap == null) {
            log.warn("Data not found for the execution plan " + queryPlanIdentifier);
            return null;
        }
        return executionPersistenceMap.get(revision);
    }

    @Override
    public String getLastRevision(String executionPlanIdentifier) {
        List<String> revisionList = revisionMap.get(executionPlanIdentifier);
        if (revisionList == null) {
            return null;
        }
        if (revisionList.size() > 0) {
            return revisionList.get(revisionList.size() - 1);
        }
        return null;
    }

    public void shutdown() {
        hazelcastInstance.getLifecycleService().shutdown();
    }
}
