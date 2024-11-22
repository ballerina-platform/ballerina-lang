/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.langserver.apispec;

import java.util.List;
import java.util.Map;

/**
 * Represents a type schema for an LS API.
 *
 * @since 2201.12.0
 */
public abstract class TypeSchema {

    final String type;
    final String className;

    protected TypeSchema(String type, String className) {
        this.type = type;
        this.className = className;
    }

    public static TypeSchema string(String className) {
        return new PrimitiveTypeSchema("string", className);
    }

    public static TypeSchema number(String className) {
        return new PrimitiveTypeSchema("number", className);
    }

    public static TypeSchema integer(String className) {
        return new PrimitiveTypeSchema("integer", className);
    }

    public static TypeSchema bool(String className) {
        return new PrimitiveTypeSchema("boolean", className);
    }

    public static TypeSchema array(TypeSchema item, String className) {
        return new CollectionTypeSchema("array", className, item);
    }

    public static TypeSchema map(TypeSchema item, String className) {
        return new CollectionTypeSchema("map", className, item);
    }

    public static TypeSchema object(Map<String, TypeSchema> properties, String className) {
        return new ObjectTypeSchema(properties, className);
    }

    public static TypeSchema enumSchema(List<String> values, String className) {
        return new EnumTypeSchema(values, className);
    }

    static class PrimitiveTypeSchema extends TypeSchema {

        PrimitiveTypeSchema(String type, String className) {
            super(type, className);
        }
    }

    static class CollectionTypeSchema extends TypeSchema {

        final TypeSchema item;

        CollectionTypeSchema(String type, String className, TypeSchema item) {
            super(type, className);
            this.item = item;
        }
    }

    static class ObjectTypeSchema extends TypeSchema {

        final Map<String, TypeSchema> properties;

        ObjectTypeSchema(Map<String, TypeSchema> properties, String className) {
            super("object", className);
            this.properties = properties;
        }
    }

    static class EnumTypeSchema extends TypeSchema {

        final List<String> enumValues;

        EnumTypeSchema(List<String> enumValues, String className) {
            super("string", className);
            this.enumValues = enumValues;
        }
    }
}
