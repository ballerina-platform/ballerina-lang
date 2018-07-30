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

package org.ballerinalang.net.http.session;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.Serializable;

/**
 * HTTPSession represents the Java session object.
 *
 * @since 0.89
 */
public class HTTPSession implements Session, Serializable {

    private final String sessionPath;
    private String id;
    private Long createTime;
    private Long accessedTime;
    private Long lastAccessedTime;
    private int maxInactiveInterval;
    private BMap<String, BValue> attributeMap = new BMap<>();
    private SessionManager sessionManager;
    private boolean isValid = true;
    private boolean isNew = true;

    public HTTPSession(String id, int maxInactiveInterval, String path) {
        this.id = id;
        this.maxInactiveInterval = maxInactiveInterval;
        createTime = System.currentTimeMillis();
        accessedTime = createTime;
        lastAccessedTime = accessedTime;
        this.sessionPath = path;
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

    @Override
    public String[] getAttributeNames() {
        return attributeMap.keySet().toArray(new String[attributeMap.size()]);
    }

    @Override
    public BMap<String, BValue> getAttributes() {
        return attributeMap;
    }

    @Override
    public Session setNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    @Override
    public String getPath() {
        return sessionPath;
    }

    @Override
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }

    @Override
    public Long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public Long getCreationTime() {
        return createTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public void invalidate() {
        sessionManager.invalidateSession(this);
        attributeMap = new BMap<>();
        isValid = false;
    }

    @Override
    public Session setAccessed() {
        checkValidity();
        lastAccessedTime = this.accessedTime;
        accessedTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void generateSessionHeader(HttpCarbonMessage message, boolean isSecureRequest) {
        //Add set Cookie only for the first response after the creation
        if (this.isNew()) {
            message.setHeader(HttpConstants.RESPONSE_COOKIE_HEADER, HttpConstants.SESSION_ID + this.getId() + "; "
                    + HttpConstants.PATH + this.getPath() + ";" +
                    (isSecureRequest ? " " + HttpConstants.SECURE + ";" : "") + " " + HttpConstants.HTTP_ONLY + ";");
        }
    }

    public void setManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private void checkValidity() {
        if (!isValid) {
            throw new IllegalStateException("Failed to execute action: Invalid session");
        }
    }

    public boolean isNew() {
        return this.isNew;
    }

}
