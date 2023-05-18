/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.symbols;

import io.ballerina.compiler.api.values.ConstantValue;

import java.util.Optional;

/**
 * Represents an Annotation attachment.
 *
 * @since 2201.3.0
 */
public interface AnnotationAttachmentSymbol extends Symbol {

    /**
     * Check whether this is a constant annotation-attachment.
     *
     * @return true if the annotation-attachment is constant annotation-attachment
     */
    boolean isConstAnnotation();

    /**
     * Returns the attachment value symbol.
     *
     * @return The constant value associated with the annotation-attachment
     */
    Optional<ConstantValue> attachmentValue();

    /**
     * Returns the annotation symbol associated with this annotation-attachment symbol.
     *
     * @return The annotation symbol associated with this annotation-attachment symbol
     */
    AnnotationSymbol typeDescriptor();
}
