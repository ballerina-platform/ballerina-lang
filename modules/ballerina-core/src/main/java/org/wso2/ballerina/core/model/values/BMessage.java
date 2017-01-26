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
import org.wso2.ballerina.core.message.StringDataSource;
import org.wso2.ballerina.core.model.Null;
import org.wso2.ballerina.core.model.util.MessageUtils;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BMessage} represents a Carbon Message in Ballerina.
 *
 * @since 0.8.0
 */
public final class BMessage implements BRefType<CarbonMessage> {
    private CarbonMessage value;
    private BValue builtPayload;
    private Headers headers;

    /**
     * Create a message value in ballerina.
     */
    public BMessage() {
        this(new DefaultCarbonMessage());
        this.builtPayload = new BString("");
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
     * Set the built payload of this message.
     *
     * @param builtMsg Built payload of this message
     */
    public void setBuiltPayload(BValue builtMsg) {
        // Set the message data source once the message is built
        BallerinaMessageDataSource ballerinaMessageDataSource = null;
        if (builtMsg instanceof BXML || builtMsg instanceof BJSON) {
            ballerinaMessageDataSource = (BallerinaMessageDataSource) builtMsg;
            ballerinaMessageDataSource.setOutputStream(this.value.getOutputStream());
        } else {
            ballerinaMessageDataSource = new StringDataSource(builtMsg.stringValue(), this.value.getOutputStream());
        }
        ballerinaMessageDataSource.setOutputStream(this.value.getOutputStream());
        this.value.setMessageDataSource(ballerinaMessageDataSource);
        this.builtPayload = builtMsg;
        setAlreadyRead(true);
    }

    /**
     * Get the built payload of this message.
     *
     * @return Built payload of this message
     */
    public BValue getBuiltPayload() {
        return this.builtPayload;
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
     * @return String array that contains all header values
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
            return this.builtPayload.stringValue();
        }

        return MessageUtils.getStringFromInputStream(this.value.getInputStream());
    }

    @Override
    public Null nullValue() {
        return null;
    }
}
