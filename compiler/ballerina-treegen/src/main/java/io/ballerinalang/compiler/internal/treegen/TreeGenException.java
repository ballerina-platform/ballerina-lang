/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.treegen;

/**
 * Thrown for any exceptions in this tool.
 *
 * @since 1.3.0
 */
public class TreeGenException extends RuntimeException {

    public TreeGenException(String msg) {
        super(msg);
    }

    public TreeGenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TreeGenException(Throwable cause) {
        super(cause);
    }
}
