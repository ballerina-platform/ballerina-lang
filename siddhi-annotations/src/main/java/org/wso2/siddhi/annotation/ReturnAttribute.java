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

import org.wso2.siddhi.annotation.util.DataType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for storing additional attributes returned by a stream processor.
 * This should not be directly applied to any classes.
 * <pre><code>
 * eg:-
 *      {@literal @}Extension(
 *                     ...
 *                      returnAttributes = {{@literal @}ReturnAttribute( name = "attribute1",
 *                                                                      type = {DataType.INT, DataType.LONG},
 *                                                                      description="Description of attribute1"),
 *                                          {{@literal @}ReturnAttribute( name = "attribute2",
 *                                                                      type = {DataType.INT},
 *                                                                      description="Description of attribute2")},
 *                     ...
 *      )
 *      public CustomExtension extends ExtensionSuperClass {
 *          ...
 *      }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ReturnAttribute {
    String name() default "";

    DataType[] type() default {};

    String description() default "";
}
