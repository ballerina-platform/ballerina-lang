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
 * @param <DataType> Type of data which should be stored in the node.
 * @param <CheckerType> Additional checker for node which can be used for additional checks before returning the <ITEM>.
 */
public interface DataElement<DataType, CheckerType> {

    /**
     * Set the node item.
     *
     * @param data data to be stored.
     */
    void setData(DataType data);

    /**
     * Get the stored data.
     *
     * @param checker Checker for extra checks of the item.
     * @return the item stored in the Node Item.
     */
    DataType getData(CheckerType checker);

    /**
     * Remove the data of the data element.
     */
    void clearData();

}
