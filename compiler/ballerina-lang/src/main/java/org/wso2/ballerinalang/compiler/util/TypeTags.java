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
package org.wso2.ballerinalang.compiler.util;

/**
 * @since 0.94
 */
public class TypeTags {

    public static final int INT = 1;
    public static final int FLOAT = INT + 1;
    public static final int STRING = FLOAT + 1;
    public static final int BOOLEAN = STRING + 1;
    public static final int BLOB = BOOLEAN + 1;
    public static final int TYPE = BLOB + 1;
    public static final int JSON = TYPE + 1;
    public static final int XML = JSON + 1;
    public static final int TABLE = XML + 1;
    public static final int STREAM = TABLE + 1;
    public static final int MAP = STREAM + 1;
    public static final int ANY = MAP + 1;
    public static final int STRUCT = ANY + 1;
    public static final int CONNECTOR = STRUCT + 1;
    public static final int SERVICE = CONNECTOR + 1;
    public static final int STREAMLET = SERVICE + 1;
    public static final int ENUM = STREAMLET + 1;
    public static final int ARRAY = ENUM + 1;
    public static final int UNION = ARRAY + 1;
    public static final int NULL = UNION + 1;
    public static final int PACKAGE = NULL + 1;
    public static final int INVOKABLE = PACKAGE + 1;
    public static final int NONE = INVOKABLE + 1;
    public static final int VOID = NONE + 1;
    public static final int XMLNS = VOID + 1;
    public static final int ANNOTATION = XMLNS + 1;
    public static final int XML_ATTRIBUTES = ANNOTATION + 1;
    public static final int ERROR = XML_ATTRIBUTES + 1;
    public static final int ITERATOR = ERROR + 1;
    public static final int TUPLE_COLLECTION = ITERATOR + 1;
    public static final int FUTURE = TUPLE_COLLECTION + 1;

    private TypeTags() {
    }
}
