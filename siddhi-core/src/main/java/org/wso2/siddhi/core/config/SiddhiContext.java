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

package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.SiddhiExtensionLoader;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.wso2.siddhi.core.util.statistics.StatManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SiddhiContext {

    private int eventBufferSize;
    private Map<String, Class> siddhiExtensions;
    private PersistenceStore persistenceStore = null;
    private ConcurrentHashMap<String, DataSource> siddhiDataSources;
    private StatManager statManager;

    public SiddhiContext() {
        setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());
        eventBufferSize = SiddhiConstants.DEFAULT_EVENT_BUFFER_SIZE;
        this.siddhiDataSources = new ConcurrentHashMap<String, DataSource>();
    }

    public int getEventBufferSize() {
        return eventBufferSize;
    }

    public Map<String, Class> getSiddhiExtensions() {
        return siddhiExtensions;
    }

    public void setSiddhiExtensions(Map<String, Class> siddhiExtensions) {
        this.siddhiExtensions = siddhiExtensions;
    }

    public void setEventBufferSize(int eventBufferSize) {
        this.eventBufferSize = eventBufferSize;
    }

    public PersistenceStore getPersistenceStore() {
        return persistenceStore;
    }

    public void setPersistenceStore(PersistenceStore persistenceStore) {
        this.persistenceStore = persistenceStore;
    }

    public DataSource getSiddhiDataSource(String dataSourceName) {
        if (dataSourceName != null) {
            return siddhiDataSources.get(dataSourceName);
        }

        return null;
    }

    public void addSiddhiDataSource(String dataSourceName, DataSource dataSource) {
        this.siddhiDataSources.put(dataSourceName, dataSource);
    }

    public StatManager getStatManager() {
        return statManager;
    }

    public void setStatManager(StatManager statManager) {
        this.statManager = statManager;
    }

}
