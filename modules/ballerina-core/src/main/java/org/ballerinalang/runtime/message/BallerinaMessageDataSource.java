/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.runtime.message;

import org.wso2.carbon.messaging.MessageDataSource;

import java.io.OutputStream;
import java.util.Map;

/**
 * This class holds the data related to carbon message once it is built.
 */
public class BallerinaMessageDataSource implements MessageDataSource {

    @Override
    public String getValueAsString(String path) {
        return null;
    }

    @Override
    public String getValueAsString(String path, Map<String, String> properties) {
        return null;
    }

    @Override
    public Object getValue(String path) {
        return null;
    }

    @Override
    public Object getDataObject() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void serializeData() {
        // This is where we write to output stream
    }

//    @Override
//    public String getMessageAsString() {
//        return null;
//    }

    public void setOutputStream(OutputStream outputStream) {
    }

    @Override
    public String getMessageAsString() {
        return null;
    }

    public BallerinaMessageDataSource clone() {
        return null;
    }
}
