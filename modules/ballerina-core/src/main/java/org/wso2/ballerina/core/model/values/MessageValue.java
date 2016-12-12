/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.values;

import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code MessageValue} represents a Carbon Message in Ballerina.
 *
 * @since 1.0.0
 */
public class MessageValue implements BValue<CarbonMessage> {
    private CarbonMessage value;
    private BValue<?> builtPayload;
    private Map<String, String> headers = new HashMap<>();

    /**
     * Create a message in ballerina using a Carbon Message.
     * 
     * @param value     Carbon Message
     */
    public MessageValue(CarbonMessage value) {
        this.value = value;
    }

    public CarbonMessage getValue() {
        return value;
    }

    /**
     * Set carbon message associated with this ballerina Message instance.
     * 
     * @param value     Carbon Message
     */
    public void setValue(CarbonMessage value) {
        this.value = value;
    }

    /**
     * Check whether the payload of this message is already read and built.
     * 
     * @return  Flag indicating whether the payload of this message is already read and built.
     */
    public boolean isAlreadyRead() {
        return this.value.isAlreadyRead();
    }

    /**
     * Set the flag indicating the payload of this message is already read and built.
     * 
     * @param isRead    Flag indicating whether the payload of this message is already read and built.
     */
    public void setAlreadyRead(Boolean isRead) {
        this.value.setAlreadyRead(isRead);
    }

    /**
     * Set the built payload of this message.
     * 
     * @param builtMsg  Built payload of this message
     */
    public void setBuiltPayload(BValue<?> builtMsg) {
        this.builtPayload = builtMsg;
    }

    /**
     * Get the built payload of this message.
     * 
     * @return  Built payload of this message
     */
    public BValue<?> getBuiltPayload() {
        return this.builtPayload;
    }

    /**
     * Add  message header.
     *  @param headerName  Headers Name
     *  @param headerValue  Headers Value
     */
    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    /**
     * Get the header value.
     *
     * @return  header name
     */
    public String getHeaderValue(String headerName) {
        return headers.get(headerName);
    }

}
