/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.util.exceptions;
// TODO: 4/28/20 rename
/**
 * Represents an error that occurs in the Ballerina runtime, while attempting freezing a value.
 *
 * @since 0.985.0
 */
public class BLangFreezeException extends BLangRuntimeException {

    private String detail = null;

    public BLangFreezeException(String message) {
        super(message);
    }

    public BLangFreezeException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public BLangFreezeException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getDetail() {
        return detail;
    }
}
