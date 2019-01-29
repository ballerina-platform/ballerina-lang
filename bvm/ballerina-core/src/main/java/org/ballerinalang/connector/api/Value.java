/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.connector.api;

import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BValue;

/**
 * Wrapper for values in BVM.
 *
 * @since 0.965.0
 */
public interface Value {

    /**
     * Returns the type of the value.
     * @return type of the value.
     */
    Type getType();

    /**
     * Returns int value.
     *
     * @return int value.
     */
    long getIntValue();

    /**
     * Returns float value.
     *
     * @return float value.
     */
    double getFloatValue();

    /**
     * Returns string value.
     *
     * @return string value.
     */
    String getStringValue();

    /**
     * Returns boolean value.
     *
     * @return boolean value.
     */
    boolean getBooleanValue();

    /**
     * Returns struct value.
     *
     * @return struct value.
     */
    Struct getStructValue();

    BValue getVMValue();

    // TODO Implement XML and JSON

    /**
     * Represent FieldType of a struct.
     *
     * @since 0.965.0
     */
    enum Type {

        INT(TypeTags.INT_TAG),
        FLOAT(TypeTags.FLOAT_TAG),
        STRING(TypeTags.STRING_TAG),
        BOOLEAN(TypeTags.BOOLEAN_TAG),
        ARRAY(TypeTags.ARRAY_TAG),
        MAP(TypeTags.MAP_TAG),
        OBJECT(TypeTags.OBJECT_TYPE_TAG),
        RECORD(TypeTags.RECORD_TYPE_TAG),
        JSON(TypeTags.JSON_TAG),
        XML(TypeTags.XML_TAG),
        TYPEDESC(TypeTags.TYPEDESC_TAG),
        NULL(TypeTags.NULL_TAG),
        OTHER(-1);

        int tag;

        Type(int i) {
            tag = i;
        }

        public int getTag() {
            return tag;
        }

        public static Type getType(int tag) {
            for (Type type : Type.values()) {
                if (type.tag == tag) {
                    return type;
                }
            }
            return OTHER;
        }

        public static Type getType(BValue value) {
            if (value == null) {
                return NULL;
            }
            for (Type type : Type.values()) {
                if (type.tag == value.getType().getTag()) {
                    return type;
                }
            }
            return OTHER;
        }
    }
}
