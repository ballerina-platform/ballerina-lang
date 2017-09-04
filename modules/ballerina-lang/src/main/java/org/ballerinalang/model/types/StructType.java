/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.types;

import org.ballerinalang.model.Name;

import java.util.List;

/**
 * {@code StructType} represents the type of a struct in Ballerina.
 *
 * @since 0.94
 */
public interface StructType extends ReferenceType {

    /**
     * @since 0.94
     */
    interface Field {
        Name getName();

        Type getType();
    }

    List<? extends Field> getFields();
}
