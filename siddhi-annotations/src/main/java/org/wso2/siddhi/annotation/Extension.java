/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.annotation;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying it as a Siddhi Extension.
 * <pre><code>
 * eg:-
 *      {@literal @}Extension(
 *                      name = "customExtensionName",
 *                      namespace = "customExtensionNamespace",
 *                      description = "Description of the custom extension.",
 *                      parameters = {
 *                          {@literal @}Parameter(name = "firstParameterName", type = {DataType.INT, DataType.LONG}),
 *                          {@literal @}Parameter(name = "SecondParameterName", type = {DataType.STRING})
 *                      },
 *                      returnAttributes = {@literal @}ReturnAttribute(type = {DataType.INT, DataType.LONG}),
 *                      examples = {{@literal @}Example({"Example of the CustomExtension usage 1"}),
 *                                  {@literal @}Example({"Example of the CustomExtension usage 2"})}
 *      )
 *      public CustomExtension extends ExtensionSuperClass {
 *          ...
 *      }
 * </code></pre>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface Extension {
    String name() default "";

    String namespace() default "";

    String description() default "";

    Parameter[] parameters() default {};

    SystemParameter[] systemParameter() default {};

    ReturnAttribute[] returnAttributes() default {};

    Example[] examples() default {};
}
