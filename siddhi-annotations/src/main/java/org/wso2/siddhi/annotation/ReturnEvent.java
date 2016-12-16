/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for storing additional attributes in the return event of stream processors.
 * Additional attributes can be defined by passing an array of org.wso2.siddhi.core.util.docs.annotation.AdditionalAttribute
 * Can be applied to stream processors
 *
 * <pre><code>
 * eg:-
 *      {@literal @}ReturnEvent({
 *          {@literal @}AdditionalAttribute(name = "attribute1", type = {DataType.INT, DataType.LONG}, description="description about the addition return attributes"),
 *          {@literal @}AdditionalAttribute(name = "attribute2", type = {DataType.INT, DataType.LONG})
 *      })
 *      public CustomStreamProcessor extends StreamProcessor {
 *          ...
 *      }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReturnEvent {
    AdditionalAttribute[] value();
}
