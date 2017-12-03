/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Map;

/**
 * An interface which represents the message after loading in to memory (After first built).
 */
public interface MessageDataSource {

    public String getValueAsString(String path);

    public String getValueAsString(String path, Map<String, String> properties);

    public Object getValue(String path);

    public Object getDataObject();

    public String getContentType();

    public void setContentType(String type);

    public void serializeData();

    public String getMessageAsString();

}
