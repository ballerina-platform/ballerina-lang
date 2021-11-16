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
package io.ballerina.types;

/**
 * Each bit represents a XML singleton subtype or a empty sequence.
 * `primitives` fields are composed of allowed values from this singleton set.
 * Atom of the `Bdd` represented by `sequence` field is also composed of these to indicate
 * non-empty sequence of allowed singletons.
 *
 * @since 3.0.0
 */
public class XmlPrimitive {
    public static final int XML_PRIMITIVE_NEVER      = 1;
    public static final int XML_PRIMITIVE_TEXT       = 1 << 1;
    public static final int XML_PRIMITIVE_ELEMENT_RO = 1 << 2;
    public static final int XML_PRIMITIVE_PI_RO      = 1 << 3;
    public static final int XML_PRIMITIVE_COMMENT_RO = 1 << 4;
    public static final int XML_PRIMITIVE_ELEMENT_RW = 1 << 5;
    public static final int XML_PRIMITIVE_PI_RW      = 1 << 6;
    public static final int XML_PRIMITIVE_COMMENT_RW = 1 << 7;

    public static final int XML_PRIMITIVE_RO_SINGLETON = XML_PRIMITIVE_TEXT | XML_PRIMITIVE_ELEMENT_RO
            | XML_PRIMITIVE_PI_RO | XML_PRIMITIVE_COMMENT_RO;
    public static final int XML_PRIMITIVE_RO_MASK = XML_PRIMITIVE_NEVER | XML_PRIMITIVE_RO_SINGLETON;
    public static final int XML_PRIMITIVE_RW_MASK = XML_PRIMITIVE_ELEMENT_RW | XML_PRIMITIVE_PI_RW
            | XML_PRIMITIVE_COMMENT_RW;
    public static final int XML_PRIMITIVE_SINGLETON = XML_PRIMITIVE_RO_SINGLETON | XML_PRIMITIVE_RW_MASK;
}
