/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.exception;

/**
 * This Exception is thrown when there is an error in standard library cache in language server.
 * 
 * @since 1.2.0
 */
public class LSStdlibCacheException extends Exception {
    public LSStdlibCacheException(String message) {
        super(message);
    }

    public LSStdlibCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
