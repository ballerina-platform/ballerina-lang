/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semtype;

/**
 * Represent bit field that indicate which uniform type a semType belongs to.
 * Regular types are divided longo mutable part and immutable part and these parts are called an uniform type.
 * 5th bit indicate mutability; 0 immutable, 1 mutable.
 *
 * @since 2.0.0
 */
public class UniformTypeCode {
    // Inherently immutable
    public static final int UT_NIL = 0x00;
    public static final int UT_BOOLEAN = 0x01;

    // Selectively immutable; immutable half
    public static final int UT_LIST_RO = 0x02;
    public static final int UT_MAPPING_RO = 0x03;
    public static final int UT_TABLE_RO = 0x04;
    public static final int UT_XML_RO = 0x05;
    public static final int UT_OBJECT_RO = 0x06;

    // Rest of inherently immutable
    public static final int UT_INT = 0x07;
    public static final int UT_FLOAT = 0x08;
    public static final int UT_DECIMAL = 0x09;
    public static final int UT_STRING = 0x0A;
    public static final int UT_ERROR = 0x0B;
    public static final int UT_FUNCTION = 0x0C;
    public static final int UT_TYPEDESC = 0x0D;
    public static final int UT_HANDLE = 0x0E;

    // Inherently mutable
    public static final int UT_FUTURE = 0x10;
    public static final int UT_STREAM = 0x11;

    // Selectively immutable; mutable half
    public static final int UT_LIST_RW = 0x12;
    public static final int UT_MAPPING_RW = 0x13;
    public static final int UT_TABLE_RW = 0x14;
    public static final int UT_XML_RW = 0x15;
    public static final int UT_OBJECT_RW = 0x16;

    // Helper bit fields (does not represent uniform type tag)
    static final int UT_COUNT = UT_OBJECT_RW + 1;
    static final int UT_MASK = (1 << UT_COUNT) - 1;

    static final int UT_COUNT_RO = 0x10;
    static final int UT_READONLY = (1 << UT_COUNT_RO) - 1;

    static final int UT_RW_MASK = UT_MASK ^ ~UT_READONLY;
}
