/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.JSONDataSource;
import io.ballerina.runtime.JSONGenerator;

import java.io.Writer;

/**
 * <p>
 * {@link BStreamingJson} represent a JSON array generated from a {@link JSONDataSource}.
 * </p>
 *
 * @since 1.1.0
 */
public interface BStreamingJson extends BArray {

    /**
     * Serialize to the given {@code JSONGenerator}.
     * @param gen {@code JSONGenerator} to use
     */
    void serialize(JSONGenerator gen);

    /**
     * Serialize the value to given {@code Writer}.
     * @param writer {@code Writer} to be used
     */
    void serialize(Writer writer);
}
