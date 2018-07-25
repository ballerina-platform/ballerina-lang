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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.runtime.message;

import java.io.OutputStream;
import java.util.Map;

/**
 * An interface which represents the message after loading in to memory (After first built).
 */
public interface MessageDataSource {

    /**
     * Returns the string value of the given path.
     * @param path value path
     * @return value
     */
    public String getValueAsString(String path);

    /**
     * Returns the string value of the path.
     * @param path value path
     * @param properties meta info
     * @return value
     */
    public String getValueAsString(String path, Map<String, String> properties);

    /**
     * Returns the value.
     * @param path value path
     * @return value
     */
    public Object getValue(String path);

    /**
     * Returns the dataExpr-object of the dataExpr source.
     * @return dataExpr-object
     */
    public Object getDataObject();

    /**
     * Reruns the content-type of the dataExpr.
     * @return content-type
     */
    public String getContentType();

    /**
     * Set the content type of the dataExpr.
     * @param type content-type
     */
    public void setContentType(String type);

    /**
     * Serialize dataExpr in to a byte stream.
     *
     * @param outputStream Represent the outputstream that the dataExpr will be written to
     */
    public void serializeData(OutputStream outputStream);

    /**
     * Returns the entire message as string.
     * @return message
     */
    public String getMessageAsString();
}
