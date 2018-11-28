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
import { Message } from "semantic-ui-react";
import { validate } from "swagger-parser";
import * as Swagger from "swagger-schema-official";
import uniqid from "uniqid";

import { OpenApiOperation } from "./components/operation/add-operation";
import { OpenApiParameter } from "./components/parameter/add-parameter";
import { OpenApiResponse } from "./components/parameter/add-response";
import { OpenApiResource } from "./components/resource/add-resource";
import OpenApiResourceList from "./components/resource/resources";
import { OpenApiContext, OpenApiContextProvider } from "./context/open-api-context";

import HideComponent from "./util-components/hider";
import InlineEdit from "./util-components/inline-edit";

import "./components/style/main.less";

export interface OasProps {
    openApiJson: any;
    onDidAddResource?: (resourse: OpenApiResponse | string, swagger: Swagger.Spec) => void;
    onDidAddOperation?: (operation: OpenApiOperation, swagger: Swagger.Spec) => void;
    onDidAddParameter?: (parameter: OpenApiParameter, swagger: Swagger.Spec) => void;
    onDidChange?: (event: string, element: any, swaggerJson: Swagger.Spec) => void;
}

export interface OpenApiState {
    openApiJson: any;
    isError: OpenApiError;
    actionState: OpenApiActionState;
}

export interface OpenApiActionState {
    state: string;
    message: string;
}

export interface OpenApiError {
    status: boolean;
    inline: boolean;
    message: string;
}

enum EVENTS {
    ADD_RESOURCE = "add_resource",
    ADD_OPERATION  = "add_operation",
    ADD_PARAMETER  = "add_parameter",
    ADD_RESPONSE  = "add_response",
    DELETE_OPERATION  = "del_operation",
    DELETE_RESOURCE  = "del_resource",
    DELETE_PARAMETER  = "del_parameter",
    DELETE_RESPONSE  = "del_response"
}

/**
 * Component which will visualize a given OAS Json
 * Compatible with OAS 3.x versions
 */
class OpenApiVisualizer extends React.Component<OasProps, OpenApiState> {
    constructor(props: OasProps) {
        super(props);
        this.state = {
            actionState: {
                message: "",
                state: ""
            },
            isError: {
                inline: false,
                message: "",
                status: false
            },
            openApiJson: {}
        };

        this.onDidAddResource = this.onDidAddResource.bind(this);
        this.onDidAddOperation = this.onDidAddOperation.bind(this);
        this.onDidAddParameter = this.onDidAddParameter.bind(this);
        this.onDidAddResponse = this.onDidAddResponse.bind(this);
        this.handleMessageHide = this.handleMessageHide.bind(this);
    }

    public componentDidMount() {
        let { openApiJson } = this.props;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        this.setState({
            openApiJson
        });

        this.validateJsonProp(openApiJson);
        this.validateOpenApiJson(openApiJson);
    }

    public componentWillReceiveProps(nextProps: OasProps) {
        let { openApiJson } = nextProps;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        this.setState({
            openApiJson
        });

        this.validateJsonProp(openApiJson);
        this.validateOpenApiJson(openApiJson);
    }

    /**
     * Event which will get triggered when a resource is added.
     *
     * @param addedResource Resource object which is added
     */
    public onDidAddResource(addedResource: OpenApiResource) {
        const { onDidAddResource, onDidChange } = this.props;
        const resourceName = addedResource.name.replace(" ", "");
        const operations: { [index: string]: Swagger.Operation } = {};

        addedResource.methods.forEach((method, index) => {
            operations[method.toLowerCase()] = {
                description: "",
                operationId: index === 0 ? resourceName : "resource" + index,
                parameters: [],
                responses : {
                        200: {
                            description: "OK"
                        }
                    },
                security: [],
                summary: "",
                tags: []
            };
        });

        this.setState((prevState) => ({
            ...prevState,
            openApiJson: {
                ...prevState.openApiJson,
                paths: {
                    ...prevState.openApiJson.paths,
                    ["/" + resourceName]: operations
                }
            }
        }), () => {

            if (this.state.openApiJson.paths["/" + resourceName]) {
                if (onDidAddResource) {
                    onDidAddResource(resourceName, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_RESOURCE, resourceName, this.state.openApiJson);
                }

                this.setState({
                    actionState: {
                        message: "Resource : " + resourceName + " added successfully to the definition.",
                        state: "success"
                    }
                });
            }
        });
    }

    /**
     *
     * Event which will get triggered when a resource is added.
     *
     * @param operationsObj operations object which is added
     */
    public onDidAddOperation(operationsObj: OpenApiOperation) {
        const { onDidAddOperation, onDidChange } = this.props;
        const path = operationsObj.path;
        const operations = this.state.openApiJson.paths[path];
        const paths = this.state.openApiJson.paths;
        let resourceIndex: number = 0;

        Object.keys(paths).forEach((e) => {
            const op = paths[e];
            if (Object.keys(op).length > 0) {
                Object.keys(op).forEach((ea) => {
                    const lastValue: string = op[ea].operationId.split("-").pop();
                    if (lastValue.match(/\b(s|resource\w)\b/g) !== null) {
                        if (resourceIndex <= Number(lastValue.split("resource")[1])) {
                            resourceIndex = Number(lastValue.split("resource")[1]) + 1;
                        }
                    }
                });
            }
        });

        operationsObj.method.forEach((method, index) => {
            operations[method.toLowerCase()] = {
                description: "",
                operationId: Object.keys(operations).length === 0 ? path  : "resource" + resourceIndex,
                parameters: [],
                responses : {
                        200: {
                            description: "OK"
                        }
                    },
                security: [],
                summary: "",
                tags: []
            };
            resourceIndex++;
        });

        this.setState((prevState) => ({
            ...prevState,
            openApiJson: {
                ...prevState.openApiJson,
                paths: {
                    ...prevState.openApiJson.paths,
                    [path] : operations
                }
            }
        }), () => {

            if (onDidAddOperation) {
                onDidAddOperation(operationsObj, this.state.openApiJson);
            }

            if (onDidChange) {
                onDidChange(EVENTS.ADD_OPERATION, operationsObj, this.state.openApiJson);
            }

            const { openApiJson } = this.state;

            this.setState({
                actionState: {
                    message: "Added operation to " + path,
                    state: "success"
                },
                openApiJson
            });

        });
    }

    public onDidAddParameter(parameterObj: OpenApiParameter) {
        const { onDidAddParameter, onDidChange } = this.props;
        const { openApiJson } = this.state;
        const path = parameterObj.resourcePath;
        const method = parameterObj.operation;

        if (openApiJson.paths[path][method].parameters) {
            this.setState((prevState) => ({
                ...prevState,
                openApiJson: {
                    ...prevState.openApiJson,
                    paths: {
                        ...prevState.openApiJson.paths,
                        [path] : {
                            ...prevState.openApiJson.paths[path],
                            [method] : {
                                ...prevState.openApiJson.paths[path][method],
                                parameters : [
                                    ...prevState.openApiJson.paths[path][method].parameters,
                                    {
                                        allowEmptyValue: parameterObj.allowedEmptyValues,
                                        description: parameterObj.description,
                                        in: parameterObj.parameterIn,
                                        isRequired: parameterObj.isRequired,
                                        name: parameterObj.name,
                                        schema: {
                                            type: parameterObj.type
                                        }
                                    }
                                ]
                            }
                        }
                    }
                }
            }), () => {

                if (onDidAddParameter) {
                    onDidAddParameter(parameterObj, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_PARAMETER, parameterObj, this.state.openApiJson);
                }

                this.setState({
                    actionState: {
                        message: "Successfully added parameter to " + path + method,
                        state: "success"
                    }
                });

            });
        } else {
            this.setState((prevState) => ({
                ...prevState,
                openApiJson: {
                    ...prevState.openApiJson,
                    paths: {
                        ...prevState.openApiJson.paths,
                        [path] : {
                            ...prevState.openApiJson.paths[path],
                            [method] : {
                                ...prevState.openApiJson.paths[path][method],
                                parameters : [
                                    {
                                        allowEmptyValue: parameterObj.allowedEmptyValues,
                                        description: parameterObj.description,
                                        in: parameterObj.parameterIn,
                                        isRequired: parameterObj.isRequired,
                                        name: parameterObj.name,
                                        schema: {
                                            type: parameterObj.type
                                        }
                                    }
                                ]
                            }
                        }
                    }
                }
            }), () => {

                if (onDidAddParameter) {
                    onDidAddParameter(parameterObj, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_PARAMETER, parameterObj, this.state.openApiJson);
                }

                this.setState({
                    actionState: {
                        message: "Successfully added parameter to " + path + method,
                        state: "success"
                    }
                });

            });
        }

    }

    /**
     * Event which will get triggerred when a new response is added
     *
     * @param responseObj response object which is added
     */
    public onDidAddResponse(responseObj: OpenApiResponse) {
        const { onDidAddResource, onDidChange } = this.props;
        const path = responseObj.resourcePath;
        const method = responseObj.operation;

        this.setState((prevState) => ({
            ...prevState,
            openApiJson: {
                ...prevState.openApiJson,
                paths: {
                    ...prevState.openApiJson.paths,
                    [path] : {
                        ...prevState.openApiJson.paths[path],
                        [method] : {
                            ...prevState.openApiJson.paths[path][method],
                            responses : {
                                ...prevState.openApiJson.paths[path][method].responses,
                                [responseObj.status] : {
                                    description: responseObj.description
                                }
                            }
                        }
                    }
                }
            }
        }), () => {

            if (onDidAddResource) {
                onDidAddResource(responseObj, this.state.openApiJson);
            }

            if (onDidChange) {
                onDidChange(EVENTS.ADD_RESPONSE, responseObj, this.state.openApiJson);
            }

            this.setState({
                actionState: {
                    message: "Successfully added response to " + path + method,
                    state: "success"
                }
            });

        });
    }

    /**
     * Util method to validate Open API JSON.
     *
     * @param json Open API JSON that needs to be validated
     * @param onvalidattion Function to be run as a callback after validation
     */
    public validateOpenApiJson(json: Swagger.Spec , onvalidattion?: (validJson: any) => void) {
        validate(json).then((validjson) => {
            if (onvalidattion) {
                onvalidattion(validjson);
            }
        }).catch((error) => {
            this.setState({
                isError: {
                    inline: true,
                    message: error.message,
                    status: true
                }
            });
        });
    }

    /**
     * Util method to check the prop contains a json to be processed.
     *
     * @param json JSON String which needs to be checked for undefined
     */
    public validateJsonProp(json: Swagger.Spec) {
        if (!json) {
            this.setState({
                isError: {
                    inline: false,
                    message: "Open API Specification complient JSON String is required.",
                    status: true,
                }
            });
        }
    }

    public handleMessageHide() {
        this.setState({
            actionState: {
                message: "",
                state: ""
            },
            isError: {
                inline: false,
                message: "",
                status: false
            }
        });
    }

    public render() {
        const { isError: { status, inline }, isError, openApiJson,
                openApiJson: { paths, info }, actionState } = this.state;
        const appContext: OpenApiContext = {
            onDidAddOperation: this.onDidAddOperation,
            onDidAddParameter: this.onDidAddParameter,
            onDidAddResource: this.onDidAddResource,
            onDidAddResponse: this.onDidAddResponse,
            openApiJson,
        };

        if (status && !inline) {
            return (
                <Message error content={isError.message} />
            );
        }

        return (
            <OpenApiContextProvider value={appContext}>
                {isError.status && isError.inline &&
                    <HideComponent hideOn={5000} callback={this.handleMessageHide}>
                        <Message error content={isError.message} />
                    </HideComponent>
                }
                {info &&
                    <React.Fragment>
                        <div className="oas-header">
                            <h1>
                                {info.title}
                                <span>{openApiJson.host}{openApiJson.basePath}</span>
                            </h1>
                            <span className="version">{info.version}</span>
                        </div>
                        <div className="oas-details">
                            <div className="description">
                                <InlineEdit
                                    model={openApiJson}
                                    attribute="description"
                                    isEditable
                                    isTextArea
                                    text={info.description}
                                    placeholderText="Add a description"
                                />
                            </div>
                            <div>
                                <InlineEdit
                                    model={openApiJson}
                                    attribute="description"
                                    isEditable
                                    isUrl
                                    urlLink={info.termsOfService}
                                    text=""
                                    placeholderText="Add terms of service link."
                                />
                            </div>
                            <div>
                                {info.license ?
                                    <InlineEdit
                                        model={openApiJson}
                                        attribute="description"
                                        isEditable
                                        isUrl
                                        text={info.license.name}
                                        urlLink={info.license.url}
                                        placeholderText="Add license link."
                                    />
                                :
                                    <InlineEdit
                                        model={openApiJson}
                                        attribute="description"
                                        isEditable
                                        isUrl
                                        text=""
                                        urlLink=""
                                        placeholderText="Add license link."
                                    />
                                }
                            </div>
                            <div>
                                {info.contact ?
                                    <InlineEdit
                                        model={openApiJson}
                                        attribute="description"
                                        isEditable
                                        isUrl
                                        text={info.contact.name}
                                        urlLink={info.contact.url}
                                        placeholderText="Add contact information"
                                    />
                                :
                                    <InlineEdit
                                        model={openApiJson}
                                        attribute="description"
                                        isEditable
                                        isUrl
                                        text=""
                                        urlLink=""
                                        placeholderText="Add contact information"
                                    />
                                }

                            </div>
                        </div>
                    </React.Fragment>
                }
                {(() => {
                    if (actionState.state === "success") {
                        return (
                            <HideComponent hideOn={5000} callback={this.handleMessageHide}>
                                <Message success content={actionState.message} />
                            </HideComponent>
                        );
                    } else if (actionState.state === "error") {
                        return (
                            <HideComponent hideOn={5000} callback={this.handleMessageHide}>
                                <Message error content={actionState.message} />
                            </HideComponent>
                        );
                    } else {
                        return "";
                    }
                })()}
                <OpenApiResourceList openApiResources={paths} />
            </OpenApiContextProvider>
        );
    }
}

export default OpenApiVisualizer;
