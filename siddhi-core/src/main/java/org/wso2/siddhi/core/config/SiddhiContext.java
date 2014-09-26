/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.util.SiddhiExtensionLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class SiddhiContext {


    private int defaultEventBufferSize;
    private Executor executorService;
    private List<EternalReferencedHolder> eternalReferencedHolders;
    private Map<String, Class> siddhiExtensions;

    public SiddhiContext() {
        this.eternalReferencedHolders = new ArrayList<EternalReferencedHolder>();
        setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());
    }


    public int getDefaultEventBufferSize() {
        return defaultEventBufferSize;
    }

    public Executor getExecutorService() {
        return executorService;
    }

    public void setExecutorService(Executor executorService) {
        this.executorService = executorService;
    }

    public void addEternalReferencedHolder(EternalReferencedHolder eternalReferencedHolder) {
        eternalReferencedHolders.add(eternalReferencedHolder);
    }

    public Map<String, Class> getSiddhiExtensions() {
        return siddhiExtensions;
    }

    public void setSiddhiExtensions(Map<String, Class> siddhiExtensions) {
        this.siddhiExtensions = siddhiExtensions;
    }
}
