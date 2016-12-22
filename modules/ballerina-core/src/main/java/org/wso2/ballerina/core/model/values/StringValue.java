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

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code StringValue} represents a string value in Ballerina
 *
 * @since 1.0.0
 */
public class StringValue extends BallerinaMessageDataSource {
    private String value;
    private OutputStream outputStream;

    public StringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void serializeData() {
        try {
            this.outputStream.write(this.value.getBytes());
        } catch (IOException e) {
            throw new BallerinaException("Error occurred during writing the message to the output stream", e);
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public StringValue getString() {
        return this;
    }
}
