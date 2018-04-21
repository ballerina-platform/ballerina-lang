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

import org.ballerinalang.model.values.BValue;

import java.util.List;
import java.util.Map;

/**
 * Represents A Struct value in runtime.
 *
 * @since 0.965.0
 */
public interface Struct {

    /**
     * Returns the struct name.
     *
     * @return name of the struct.
     */
    String getName();

    /**
     * Returns the package of the struct.
     *
     * @return package of the struct.
     */
    String getPackage();

    /**
     * Returns all the struct fields.
     *
     * @return struct fields of the struct.
     */
    StructField[] getFields();

    /**
     * Returns value of an int field.
     *
     * @param fieldName given field name
     * @return value
     */
    long getIntField(String fieldName);

    /**
     * Returns value of a float field.
     *
     * @param fieldName given field name
     * @return value
     */
    double getFloatField(String fieldName);

    /**
     * Returns value of a string field.
     *
     * @param fieldName given field name
     * @return value
     */
    String getStringField(String fieldName);

    /**
     * Returns value of a boolean field.
     *
     * @param fieldName given field name
     * @return value
     */
    boolean getBooleanField(String fieldName);

    /**
     * Returns value of a struct field.
     *
     * @param fieldName given field name
     * @return value
     */
    Struct getStructField(String fieldName);

    /**
     * Returns value of an array field.
     *
     * @param fieldName given field name
     * @return value
     */
    Value[] getArrayField(String fieldName);

    /**
     * Returns value of a map field.
     *
     * @param fieldName given field name
     * @return value
     */
    Map<String, Value> getMapField(String fieldName);

    /**
     * Returns value of an Type field.
     *
     * @param fieldName given field name
     * @return value
     */
    Value getTypeField(String fieldName);

    /**
     * Returns value of an Ref field.
     *
     * @param fieldName given field name
     * @return value
     */
    Value getRefField(String fieldName);

    // TODO Implement XML and JSON

    /**
     * This method will return the list of annotations for the given package path and annotation name.
     *
     * @param pkgPath of the annotation.
     * @param name    of the annotation.
     * @return matching annotations list.
     */
    List<Annotation> getAnnotationList(String pkgPath, String name);

    /**
     * returns VM value.
     *
     * @return BValue instance.
     */
    BValue getVMValue();

    /**
     * Add native data to the struct.
     *
     * @param key key to identify native value.
     * @param data value to be added.
     */
    void addNativeData(String key, Object data);

    /**
     * Get native data.

     * @param key key to identify native value.
     * @return value for the given key.
     */
    Object getNativeData(String key);
}
