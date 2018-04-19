/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile.attributes;

/**
 * {@code AttributeInfo} contains metadata of a construct in Ballerina.
 * <p>
 * // TODO Improve this explanation
 *
 * @since 0.87
 */
public interface AttributeInfo {

    Kind getKind();

    int getAttributeNameIndex();

    /**
     * Represents the kind of the attribute.
     */
    enum Kind {
        CODE_ATTRIBUTE("Code"),
        PARAMETER_DEFAULTS_ATTRIBUTE("ParameterDefaults"),
        LOCAL_VARIABLES_ATTRIBUTE("LocalVariables"),
        VARIABLE_TYPE_COUNT_ATTRIBUTE("VariableTypeCount"),
        ERROR_TABLE("ErrorTable"),
        LINE_NUMBER_TABLE_ATTRIBUTE("LineNumberTable"),
        DEFAULT_VALUE_ATTRIBUTE("DefaultValue"),
        TAINT_TABLE("TaintTable");

        private String value;

        Kind(String value) {
            this.value = value;
        }

        public static Kind fromString(String name) {
            for (Kind kind : Kind.values()) {
                if (kind.value.equalsIgnoreCase(name)) {
                    return kind;
                }
            }
            return null;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
