/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.types;

/**
 * Interface class to be extended by all Ballerina Types which holds indexed values.
 * <br/>
 * Known implemented types:
 * <ul>
 * <li>{@link BArrayType}}</li>
 * <li>{@link BMapType}}</li>
 * </ul>
 */
public interface BIndexedType {
    
    /**
     * Get the type of a child elements of this type
     * 
     * @return  Type of the child elements
     */
    public BType getElementType();

}
