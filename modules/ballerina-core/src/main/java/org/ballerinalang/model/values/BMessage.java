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
package org.ballerinalang.model.values;

import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BMessage} represents a Carbon Message in Ballerina.
 *
 * @since 0.8.0
 */
public final class BMessage implements BRefType<CarbonMessage> {
    private CarbonMessage value;
    private Headers headers;

    /**
     * Create a message value in ballerina.
     */
    public BMessage() {
        this(new DefaultCarbonMessage());
        // Set an empty string to the message
        this.setMessageDataSource("");
        setAlreadyRead(true);
    }

    /**
     * Create a message in ballerina using a Carbon Message.
     *
     * @param value Carbon Message
     */
    public BMessage(CarbonMessage value) {
        this.value = (value != null) ? value : new DefaultCarbonMessage();
        this.headers = new Headers();
        this.value.setProperty(Constants.INTERMEDIATE_HEADERS, this.headers);
    }


    /**
     * Set carbon message associated with this ballerina Message instance.
     *
     * @param value Carbon Message
     */
    public void setValue(CarbonMessage value) {
        this.value = value;
    }

    /**
     * Check whether the payload of this message is already read and built.
     *
     * @return Flag indicating whether the payload of this message is already read and built.
     */
    public boolean isAlreadyRead() {
        return this.value.isAlreadyRead();
    }

    /**
     * Set the flag indicating the payload of this message is already read and built.
     *
     * @param isRead Flag indicating whether the payload of this message is already read and built.
     */
    public void setAlreadyRead(Boolean isRead) {
        this.value.setAlreadyRead(isRead);
    }

    /**
     * This method returns the already built content of this ballerina message
     * @return BallerinaMessageDataSource content of this message
     */
    public BallerinaMessageDataSource getMessageDataSource() {
        if (this.value.getMessageDataSource() != null &&
                this.value.getMessageDataSource() instanceof BallerinaMessageDataSource) {
            // this means that message value has been set from within ballerina.
            return (BallerinaMessageDataSource) this.value.getMessageDataSource();
        } else if (!(this.value.isEmpty()) && this.value.getMessageBody() != null) {
            // value can be set from outside ballerina. Then we read the content from carbon message and return
                return new StringDataSource(this.value.getMessageBody().toString());
        } else {
            // This means an empty message and we return a message datasource with empty string
            return new StringDataSource("");
        }
    }

    /**
     * Set the built payload of this message.
     *
     * @param messageDataSource Built payload of this message
     */
    public void setMessageDataSource(BallerinaMessageDataSource messageDataSource) {
        // Set the message data source once the message is built
        //this.ballerinaMessageDataSource = messageDataSource;
        messageDataSource.setOutputStream(this.value.getOutputStream());
        this.value.setMessageDataSource(messageDataSource);
        setAlreadyRead(true);
    }

    /**
     * Set the built payload of this message.
     *
     * @param message String payload of this message
     */
    public void setMessageDataSource(String message) {
        this.value.setMessageDataSource(new StringDataSource(message, this.value.getOutputStream()));
        setAlreadyRead(true);
    }

    /**
     * Add  message header.
     *
     * @param headerName  Headers Name
     * @param headerValue Headers Value
     */
    public void addHeader(String headerName, String headerValue) {
        //todo make the org.wso2.carbon.messaging.Headers.add to public and use that stead of creating a list
        List<Header> headerList = new ArrayList<>();
        headerList.add(new Header(headerName, headerValue));
        // Add to intermediate headers
        headers.set(headerList);
        // Add to carbon message headers
        value.getHeaders().set(headerList);
    }

    /**
     * Get the header value.
     *
     * @return header name
     */
    public String getHeader(String headerName) {
        return this.value.getHeader(headerName);
    }

    /**
     * Get header values for the given header name.
     *
     * @param headerName header name
     * @return String arrays that contains all header values
     */
    public String[] getHeaders(String headerName) {
        List<String> allHeaderValues = value.getHeaders().getAllBy(headerName);
        return allHeaderValues.stream().toArray(String[]::new);
    }

    /**
     * Remove the header.
     *
     * @param headerName Header name
     */
    public void removeHeader(String headerName) {
        // Remove from carbon message
        value.getHeaders().remove(headerName);
        // Remove from intermediate headers
        headers.remove(headerName);
    }

    /**
     * Set header value.
     *
     * @param headerName  header name
     * @param headerValue header value
     */
    public void setHeader(String headerName, String headerValue) {
        // Set value in intermediate headers list
        headers.set(headerName, headerValue);
        // Set value in carbon message headers
        value.getHeaders().set(headerName, headerValue);
    }

    /**
     * Get all headers.
     *
     * @return List that contains the headers
     */
    public List<Header> getHeaders() {
        return value.getHeaders().getAll();
    }

    /**
     * Set given header list.
     *
     * @param list List of Headers
     */
    public void setHeaderList(List<Header> list) {
        // Set intermediate headers list
        headers.set(list);
        // Set carbon message headers
        value.getHeaders().set(list);
    }

    @Override
    public CarbonMessage value() {
        return value;
    }

    @Override
    public String stringValue() {
        if (this.isAlreadyRead()) {
            return this.value.getMessageDataSource().getMessageAsString();
        }
        return MessageUtils.getStringFromInputStream(this.value.getInputStream());
    }

    public BMessage clone() {
        BMessage clonedMessage = new BMessage();
        // Clone the carbon message
        if (this.value != null && !this.value.isEmpty()) {
            clonedMessage.value = MessageUtil.cloneCarbonMessageWithData(this.value());
        } else {
            clonedMessage.setValue(MessageUtil.cloneCarbonMessageWithOutData(this.value()));
            clonedMessage.setHeaderList(this.getHeaders());

        }
        // Clone the already built content
        if (this.value.getMessageDataSource() != null &&
                this.value.getMessageDataSource() instanceof BallerinaMessageDataSource) {
            clonedMessage.setMessageDataSource(((BallerinaMessageDataSource) this.value.
                    getMessageDataSource()).clone());
        }
        return clonedMessage;
    }
}
