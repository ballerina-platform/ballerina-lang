/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.types;

import java.util.Optional;

/**
 * {@code IntersectableReferenceType} represents runtime reference types that can be used in intersection types,
 * that result in the creation of effective types.
 *
 * @since 2.0.0
 */
public interface IntersectableReferenceType extends ReferenceType {

    /**
     * Retrieve the intersection type if this type was created as the effective type of a {@link IntersectionType}.
     *
     * @return the intersection type.
     */
    Optional<IntersectionType> getIntersectionType();

    /**
     * Set the intersection type if this type was created as the effective type of a {@link IntersectionType}.
     *
     * @param intersectionType the intersection type.
     */
    void setIntersectionType(IntersectionType intersectionType);
}
