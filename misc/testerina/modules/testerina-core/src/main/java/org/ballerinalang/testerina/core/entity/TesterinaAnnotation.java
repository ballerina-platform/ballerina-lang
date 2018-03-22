/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.core.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class to hold annotation information.
 *
 * @since 0.963.0
 */
public class TesterinaAnnotation {

    private boolean disabled = false;
    private List<String> groups = new ArrayList<>();
    private List<String[]> valueSet = new ArrayList<>();

    /**
     * Testerina annotation types.
     */
    public enum Annotations {
        TEST_CONFIG("config"),
        BEFORE_TEST_CONFIG("beforeTest"),
        AFTER_TEST_CONFIG("afterTest");

        private final String annotationName;

        Annotations(String annotationName) {
            this.annotationName = annotationName;
        }

        public String getName() {
            return annotationName;
        }
    }

    /**
     * Testerina config annotation properties.
     */
    public enum ConfigAnnotationProps {
        TEST_GROUP("groups"),
        TEST_VALUE_SET("valueSets"),
        TEST_DISABLED("disabled");

        String annotationName;

        ConfigAnnotationProps(String annotationName) {
            this.annotationName = annotationName;
        }

        public String getName() {
            return annotationName;
        }
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String[]> getValueSet() {
        return valueSet;
    }

    public void setValueSet(List<String[]> valueSet) {
        this.valueSet = valueSet;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
