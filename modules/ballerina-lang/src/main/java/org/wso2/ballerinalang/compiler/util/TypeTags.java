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
    public static final int FLOAT = 2;
    public static final int STRING = 3;
    public static final int BOOLEAN = 4;
    public static final int BLOB = 5;
    public static final int JSON = 6;
    public static final int XML = 7;
    public static final int DATATABLE = 8;
    public static final int STRUCT = 9;
    public static final int CONNECTOR = 10;
    public static final int ENUM = 11;
    public static final int ARRAY = 12;
    public static final int PACKAGE = 13;
    public static final int INVOKABLE = 14;
    public static final int NULL = 15;
    public static final int NONE = 16;
    public static final int VOID = 17;
    public static final int ERROR = 18;

    private TypeTags() {
    }
}
