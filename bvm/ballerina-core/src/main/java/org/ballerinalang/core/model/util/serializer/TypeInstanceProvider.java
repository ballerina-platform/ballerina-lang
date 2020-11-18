/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util.serializer;

/**
 * Implementors of this interface provide plugable mechanisms to create instance of some specified type of objects.
 * These instances are then used to populate that object's fields reflectively.
 *
 * @since 0.982.0
 */
public interface TypeInstanceProvider {
    /**
     * Get type name supported by this {@link TypeInstanceProvider}.
     *
     * @return Type name as a string
     */
    String getTypeName();

    /**
     * New empty object instance, this instance's fields will be overwritten later in the deserialization process.
     * Therefore the instance doesn't have to be in valid state at this point of time.
     *
     * @return Empty instance
     */
    Object newInstance();

    /**
     * Instance of type class, supported by this implementation.
     *
     * @return Class object instance
     */
    Class getTypeClass();
}
