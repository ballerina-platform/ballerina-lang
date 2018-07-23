/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model;

import org.ballerinalang.model.util.JsonGenerator;

import java.io.IOException;

/**
 * This represents a JSON data source implementation, which should be used for custom JSON
 * streaming implementations.
 * 
 * @since 0.981.0
 */
public interface JSONDataSource {

    /**
     * Serializes the current representation of the JSON data source to the given {@link JSONDataSource}.
     * 
     * @param gen The {@link JsonGenerator} object to write the data to
     * @throws IOException Error occurs while serializing
     */
    void serialize(JsonGenerator gen) throws IOException;

}
