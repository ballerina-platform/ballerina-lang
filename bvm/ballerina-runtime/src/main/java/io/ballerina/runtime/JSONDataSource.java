/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime;

import java.io.IOException;

/**
 * This represents a JSON data source implementation, which should be used for custom JSON
 * streaming implementations.
 * 
 * @since 0.995.0
 */
public interface JSONDataSource {

    /**
     * Serializes the current representation of the JSON data source to the given {@link JSONDataSource}.
     * 
     * @param gen The {@link JSONGenerator} object to write the data to
     * @throws IOException Error occurs while serializing
     */
    void serialize(JSONGenerator gen) throws IOException;;

    /**
     * Returns {@code true} if the data-source has more JSON elements.
     * i.e: returns {@code true} if {@link #next} would
     * return an element rather than throwing an error.
     *
     * @return {@code true} if the data-source has more elements
     */
    boolean hasNext();

    /**
     * Returns the next JSON element in the data-source.
     * 
     * @return The next JSON element in the data-source.
     */
    Object next();

    /**
     * Build the entire data-source in to memory, and returns the JSON.
     * 
     * @return JSON represented by the data-source
     */
    Object build();
}
