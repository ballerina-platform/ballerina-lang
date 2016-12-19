/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.values;

/**
 * {@code BValue} represents any value in Ballerina
 * @see StringValue
 * @see DoubleValue
 * @see IntValue
 * @since 1.0.0
 * 
 * @param <T>   Java type associated with this Ballerina value 
 */
public interface BValue<T> {
    
    /**
     * Get the java value associated with this ballerina value.
     * 
     * @return  Java value associated with this ballerina value
     */
    public T getValue();

    /**
     * Return StringValue representation of the BValue. This will be a simple toString() type behaviour for primitive
     * types like StringValue, IntValue etc. Complex object types like MessageValue, XMLValue, JSONValue it will be
     * String XML, JSON representation.
     *
     * @return A StringValue that contains the value.
     */
    public default StringValue getString() {
        return new StringValue("Default Method Executed");
    }
}
