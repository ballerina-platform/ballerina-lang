/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.dispatchers.Session;
import org.ballerinalang.services.dispatchers.SessionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTPSession represents a session
 */
public class HTTPSession implements Session {

    private String id;
    private Long createTime;
    private Long lastAccessedTime;
    private Map<String, BValue> attributeMap = new ConcurrentHashMap<>();

    public HTTPSession(String id) {
        this.id = id;
        createTime = System.currentTimeMillis();
        lastAccessedTime = createTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setAttribute(String attributeKey, BValue attributeValue) {
        attributeMap.put(attributeKey, attributeValue);
    }

    @Override
    public BValue getAttributeValue(String attributeKey) {
        return attributeMap.get(attributeKey);
    }

    public Long getCreateTime() {
        return createTime;
    }

    public Long getLastAccessed() {
        return lastAccessedTime;
    }



    public void setManager(SessionManager sessionManager) {
    }
}
