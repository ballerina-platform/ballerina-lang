/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.SiddhiExtensionLoader;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.util.Map;

public class SiddhiContext {

    private int eventBufferSize;
    private Map<String, Class> siddhiExtensions;
    private PersistenceStore persistenceStore = null;

    public SiddhiContext() {
        setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());
        eventBufferSize = SiddhiConstants.DEFAULT_EVENT_BUFFER_SIZE;
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
}
