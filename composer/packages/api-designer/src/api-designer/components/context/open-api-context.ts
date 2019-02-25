/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import * as Swagger from "openapi3-ts";
import * as React from "react";

export interface OpenApiContext {
    openApiJson: Swagger.OpenAPIObject;
    onAddOpenApiPath: (path: Swagger.PathItemObject, onAdd: (state: boolean) => void) => void;
    onAddOpenApiOperation: (operation: Swagger.OperationObject) => void;
    onAddOpenApiParameter: (operation: Swagger.ParameterObject) => void;
    onAddOpenApiResponse: (response: Swagger.ResponseObject) => void;
    onInlineValueChange: (openApiJson: Swagger.OpenAPIObject) => void;
    expandMode: ExpandMode;
}

export interface ExpandMode {
    type: string;
    isEdit: boolean;
}

const context = React.createContext<OpenApiContext>({
    expandMode: {
        isEdit: false,
        type: ""
    },
    onAddOpenApiOperation: () => {/* tslint:disable:no-empty */},
    onAddOpenApiParameter: () => {/* tslint:disable:no-empty */},
    onAddOpenApiPath: () => {/* tslint:disable:no-empty */},
    onAddOpenApiResponse: () => {/* tslint:disable:no-empty */},
    onInlineValueChange: () => {/* tslint:disable:no-empty */},
    openApiJson: {
        info: {
            title: "",
            version: ""
        },
        openapi: "",
        paths: {}
    }
});

export const OpenApiContextProvider = context.Provider;
export const OpenApiContextConsumer = context.Consumer;
