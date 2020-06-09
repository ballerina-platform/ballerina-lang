/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.types;

import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;

/**
 * {@code SelectivelyImmutableReferenceType} represents selectively immutable reference types in Ballerina.
 * Namely XML, lists, mappings, and tables.
 *
 * @since 1.3.0
 */
public interface SelectivelyImmutableReferenceType extends ReferenceType {

    BIntersectionType getImmutableType();
}
