/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.ballerina.tooling.service.workspace.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-01-27T07:45:46.625Z")
public abstract class PackagesApiService {
    public abstract Response packagesGet(Integer limit
            , Integer offset
            , String query
            , String accept
            , String ifNoneMatch
    ) throws NotFoundException;

    public abstract Response packagesPackageNameGet(String packageName
            , String accept
            , String ifNoneMatch
            , String ifModifiedSince
    ) throws NotFoundException;

    public abstract Response packagesPost(String contentType
    ) throws NotFoundException;

    public abstract Response packagesSendCORS();
}
