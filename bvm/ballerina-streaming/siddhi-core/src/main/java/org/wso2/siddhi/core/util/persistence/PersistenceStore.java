/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.persistence;

import java.util.Map;

/**
 * Interface class for Persistence Stores. Persistence Stores are used to store the current state of Siddhi Engine so
 * that it can be restored in-case of an issue.
 */
public interface PersistenceStore {

    void save(String siddhiAppId, String revision, byte[] snapshot);

    void setProperties(Map properties);

    byte[] load(String siddhiAppId, String revision);

    String getLastRevision(String siddhiAppId);

}
