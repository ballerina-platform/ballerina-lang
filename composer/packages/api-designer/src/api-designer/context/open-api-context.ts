/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import * as React from "react";

import { OpenApiOperation } from "../components/operation/add-operation";
import { OpenApiParameter } from "../components/parameter/add-parameter";
import { OpenApiResponse } from "../components/parameter/add-response";
import { OpenApiResource } from "../components/resource/add-resource";

export interface OpenApiContext {
    openApiJson: any;
    onDidAddResource: (resource: OpenApiResource) => void;
    onDidAddOperation: (operation: OpenApiOperation) => void;
    onDidAddParameter: (operation: OpenApiParameter) => void;
    onDidAddResponse: (response: OpenApiResponse) => void;
}

const context = React.createContext<OpenApiContext | null>(null);

export const OpenApiContextProvider = context.Provider;
export const OpenApiContextConsumer = context.Consumer;
