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
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code Session} represents the session interface.
 * <p>
 * Need to have a protocol specific sessions
 *
 * @since 0.89
 */
public interface Session {
    /**
     * Retrieve a session attribute value.
     *
     * @param attributeKey key of the session attribute
     * @return attribute values from map if it exists, and null otherwise.
     */
    BValue getAttributeValue(String attributeKey);

    /**
     * Retrieve a session id.
     *
     * @return String id of the session.
     */
    String getId();

    /**
     * Add attribute key and value to session attribute map.
     *
     * @param attributeKey key of the session attribute
     * @param attributeValue value of the session attribute
     */
    void setAttribute(String attributeKey, BValue attributeValue);

    /**
     * Invalidate a session.
     *
     */
    void invalidate();

    /**
     * Update lastaccesed time and return Session.
     *
     * @return current session.
     */
    Session setAccessed();

    /**
     * Get last accessed time.
     *
     * @return Time in milliseconds.
     */
    Long getLastAccessedTime();

    /**
     * Get session creation time.
     *
     * @return Time in milliseconds.
     */
    Long getCreationTime();

    /**
     * Get attribute name list.
     *
     * @return array of attribute names.
     */
    String[]  getAttributeNames();

    /**
     * Get attribute key value map.
     *
     * @return map of key value pairs.
     */
    BMap<String, BValue> getAttributes();

    /**
     * Remove attribute from session.
     *
     * @param name which needs to delete
     */
    void removeAttribute(String name);

    /**
     * Get max inactive time duration.
     *
     * @return Time duration in seconds.
     */
    int getMaxInactiveInterval();

    /**
     * Set max inactive time duration.
     *
     * @param maxInactiveInterval is the session max inactive time in seconds
     */
    void setMaxInactiveInterval(int maxInactiveInterval);

    /**
     * check validity.
     *
     * @return boolean depend on validity
     */
    boolean isValid();

    /**
     * Add header to response message.
     *
     * @param message which send to client
     */
    void generateSessionHeader(HTTPCarbonMessage message, boolean isSecureRequest);

    /**
     * Update session stage whether new or already used.
     *
     * @param isNew is the stage of session
     * @return  session
     */
    Session setNew(boolean isNew);

    /**
     * Get path of session.
     *
     * @return  session path
     */
    String getPath();

    /**
     * Get session status.
     *
     * @return  session status
     */
    boolean isNew();
}
