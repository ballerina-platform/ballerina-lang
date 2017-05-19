/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for storing the system parameters of a Siddhi Extension.
 * <pre><code>
 * eg:-
 *      {@literal @}Extension(
 *                      ...
 *                      SystemParameters = {
 *                          {@literal @}SystemParameters(
 *                          name = "systemParameterName",
 *                      description = "Description of the system parameter.",
 *                      defaultValue = "defaultValue1",
 *                      possibleParameters = {"defaultValue1", "defaultValue2", "defaultValue3"}
 *                      ),
 *                      },
 *                      ...
 *      )
 *      public CustomExtension extends ExtensionSuperClass {
 *          ...
 *      }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface SystemParameter {
    String name() default "";

    String description() default "";

    String defaultValue() default "";

    String[] possibleParameters() default {}; //Optional
}
