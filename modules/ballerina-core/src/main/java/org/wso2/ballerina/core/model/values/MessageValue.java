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

import org.wso2.ballerina.core.message.BallerinaMessageDataSource;
import org.wso2.ballerina.core.model.util.MessageUtils;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;

import java.util.List;

/**
 * {@code MessageValue} represents a Carbon Message in Ballerina.
 *
 * @since 1.0.0
 */
public class MessageValue implements BValue<CarbonMessage> {
    private CarbonMessage value;
    private BValue<?> builtPayload;
    private Headers headers = new Headers();

    /**
     * Create a message in ballerina using a Carbon Message.
     * 
     * @param value     Carbon Message
     */
    public MessageValue(CarbonMessage value) {
        this.value = (value != null) ? value : new DefaultCarbonMessage();
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
        // Set the message data source once the message is built
        if (builtMsg instanceof XMLValue || builtMsg instanceof JSONValue || builtMsg instanceof StringValue) {
            BallerinaMessageDataSource ballerinaMessageDataSource = (BallerinaMessageDataSource) builtMsg;
            ballerinaMessageDataSource.setOutputStream(this.value.getOutputStream());
            this.value.setMessageDataSource(ballerinaMessageDataSource);
        }
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
        headers.set(headerName, headerValue);
    }

    /**
     * Get the header value.
     *
     * @return  header name
     */
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    /**
     * Get header values for the given header name.
     *
     * @param headerName header name
     * @return String array that contains all header values
     */
    public String[] getHeaders(String headerName) {
        List<String> allHeaderValues = headers.getAllBy(headerName);
        return allHeaderValues.stream().toArray(String[]::new);
    }

    /**
     * Remove the header.
     *
     * @param headerName Header name
     */
    public void removeHeader(String headerName) {
        headers.remove(headerName);
    }

    /**
     * Set header value.
     *
     * @param headerName  header name
     * @param headerValue header value
     */
    public void setHeader(String headerName, String headerValue) {
        headers.set(headerName, headerValue);
    }

    /**
     * Get all headers.
     *
     * @return List that contains the headers
     */
    public List<Header> getHeaders() {
        return headers.getAll();
    }

    /**
     * Set given header list.
     *
     * @param list List of Headers
     */
    public void setHeaderList(List<Header> list) {
        headers.set(list);
    }

    @Override
    public StringValue getString() {
        StringValue value;
        if (this.isAlreadyRead()) {
            value = new StringValue(getBuiltPayload().getString().getValue());
        } else {
            String payload = MessageUtils.getStringFromInputStream(this.getValue().getInputStream());
            value = new StringValue(payload);
        }
        return value;
    }
}
