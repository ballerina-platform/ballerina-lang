/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.types;

import java.util.List;
import java.util.Optional;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public interface RecordTypeDescriptor extends BallerinaTypeDescriptor {

    /**
     * Get the list of field descriptors.
     *
     * @return {@link List} of ballerina field
     */
    List<FieldDescriptor> fieldDescriptors();

    /**
     * Whether inclusive record ot not.
     *
     * @return {@link Boolean} inclusive or not
     */
    boolean inclusive();

    /**
     * Get the rest type descriptor.
     *
     * @return {@link Optional} rest type descriptor
     */
    Optional<BallerinaTypeDescriptor> restTypeDescriptor();
}
