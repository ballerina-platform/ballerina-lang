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

package org.ballerinalang.net.uri.parser;

/**
 * Interface to hold various node items.
 *
 * @param <ITEM> Item which should be stored in the node.
 * @param <CHECKER> Additional checker for node. Which can be used for additional checks before returning the <ITEM>.
 */
public interface NodeItem<ITEM, CHECKER> {

    /**
     * Set the node item.
     *
     * @param item item to be stored.
     */
    void setItem(ITEM item);

    /**
     * Get the item stored.
     *
     * @param checker Checker for extra checks of the item.
     * @return the item stored in the Node Item.
     */
    ITEM getItem(CHECKER checker);

}
