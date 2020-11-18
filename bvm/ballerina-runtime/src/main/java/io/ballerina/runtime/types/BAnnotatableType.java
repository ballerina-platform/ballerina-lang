/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.AnnotatableType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * {@code AnnotatableType} represents a type description which contains annotations.
 *
 * @since 0.995.0
 */
public abstract class BAnnotatableType extends BType implements AnnotatableType {

    protected BMap<BString, Object> annotations = ValueCreator.createMapValue();

    BAnnotatableType(String typeName, Module pkg, Class<?> valueClass) {
        super(typeName, pkg, valueClass);
    }

    public void setAnnotations(BMap<BString, Object> annotations) {
        this.annotations = annotations;
    }

    public Object getAnnotation(BString key) {
        return this.annotations.get(key);
    }

    public Object getAnnotation(String pkg, String annotName) {
        return this.annotations.get(StringUtils.fromString(pkg + ":" + annotName));
    }
}
