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

import org.ballerinalang.core.model.values.BValue;

/**
 * Convert Java object into {@link BValue} representation.
 *
 * @since 0.982.0
 */
public interface BValueSerializer {
    /**
     * Convert {@code src} Java object into {@link BValue} representation.
     *
     * @param src          Java object to be converted.
     * @param leftSideType Type of the field {@code src} is assigned to, or null if doesn't apply.
     * @return {@link BValue} representation of Java object.
     */
    BValue toBValue(Object src, Class<?> leftSideType);
}
