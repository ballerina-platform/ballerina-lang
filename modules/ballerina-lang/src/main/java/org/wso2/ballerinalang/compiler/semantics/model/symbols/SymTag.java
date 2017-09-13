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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

/**
 * @since 0.94
 */
public class SymTag {

    public static final int NIL = 0;
    public static final int PACKAGE = 1;
    public static final int TYPE = 2;
    public static final int STRUCT = 6;
    public static final int CONNECTOR = 10;
    public static final int SERVICE = 16;
    public static final int VARIABLE = 32;
    public static final int VALUE = 64;
    public static final int INVOKABLE = 128;
    public static final int FUNCTION = 384;
    public static final int ACTION = 630;
    public static final int RESOURCE = 1152;
    public static final int ANNOTATION = 2048;
    public static final int ANNOTATION_ATTRIBUTE = 4096;
    public static final int ERROR = 8192;
    public static final int ENUM = 16384;
}
