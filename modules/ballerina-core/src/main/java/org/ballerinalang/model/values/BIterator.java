/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.model.values;

/**
 * Iterator for Ballerina Collections.
 *
 * @since 0.96.0
 */
public interface BIterator {

    /**
     * Get ID of the iterator.
     *
     * @return iterator ID
     */
    String getID();

    /**
     * Get next value.
     *
     * @return next value
     */
    BValue getNext();

    /**
     * Get current cursor value.
     *
     * @return current cursor
     */
    BValue getCursor();

    /**
     * Checks collection has a next value.
     *
     * @return true, if has a next value, false otherwise
     */
    boolean hasNext();
}
