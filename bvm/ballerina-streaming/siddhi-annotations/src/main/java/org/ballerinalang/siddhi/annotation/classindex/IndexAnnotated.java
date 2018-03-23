/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.ballerinalang.siddhi.annotation.classindex;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Index all classes annotated by annotation annotated by this meta-annotation.
 * <p>
 * During compilation {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}
 * creates a resource file listing all classes
 * annotated by annotation annotated by this meta-annotation.
 * </p>
 * <p>
 * You can retrieve the list at runtime using
 * {@link org.ballerinalang.siddhi.annotation.classindex.ClassIndex#getAnnotated(Class)}.
 * If the classes also have a zero-argument constructor you can use {@link java.util.ServiceLoader} facility.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface IndexAnnotated {
    /**
     * Specifies whether to store Javadoc for runtime retrieval.
     * <p>
     * You can retrieve the stored Javadoc summary using
     * {@link org.ballerinalang.siddhi.annotation.classindex.ClassIndex#getClassSummary(Class)}.
     * </p>
     * @return true or false
     */
    boolean storeJavadoc() default false;
}
