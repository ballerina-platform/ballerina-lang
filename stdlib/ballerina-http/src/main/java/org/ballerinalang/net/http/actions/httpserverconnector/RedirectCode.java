/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.actions.httpserverconnector;

/**
 * Status codes for HTTP redirect.
 *
 * @since 0.965.0
 */
public enum RedirectCode {
    MULTIPLE_CHOICES_300,
    MOVED_PERMANENTLY_301,
    FOUND_302,
    SEE_OTHER_303,
    NOT_MODIFIED_304,
    USE_PROXY_305,
    TEMPORARY_REDIRECT_307
}
