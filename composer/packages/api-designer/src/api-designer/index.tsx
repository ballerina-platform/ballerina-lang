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
 *
 */

import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Divider } from "semantic-ui-react";

import { ExpandMode, OpenApiContext, OpenApiContextProvider } from "./components/context/open-api-context";

import { EVENTS } from "./components/utils/constants";

import OpenApiInfo from "./components/info/info";
import AddOpenApiPath from "./paths/add-path";
import OpenApiPathList from "./paths/paths-list";

export interface OpenApiProps {
    openApiJson: Swagger.OpenAPIObject;
    onDidAddResource?: (resourse: Swagger.PathItemObject | string, swagger: Swagger.OpenAPIObject) => void;
    onDidAddOperation?: (operation: Swagger.OperationObject, swagger: Swagger.OpenAPIObject) => void;
    onDidAddParameter?: (parameter: Swagger.ParameterObject, swagger: Swagger.OpenAPIObject) => void;
    onDidChange?: (event: string, swaggerJson: Swagger.OpenAPIObject) => void;
}

export interface OpenApiState {
    showOpenApiAddPath: boolean;
    openApiJson: Swagger.OpenAPIObject;
    expandMode: ExpandMode;
}

class OpenApiVisualizer extends React.Component<OpenApiProps, OpenApiState> {
    constructor(props: OpenApiProps) {
        super(props);

        this.state = {
            expandMode: {
                isEdit: false,
                type: ""
            },
            openApiJson: {
                info: {
                    title: this.props.openApiJson.info.title,
                    version: this.props.openApiJson.info.version
                },
                openapi: this.props.openApiJson.openapi,
                paths: this.props.openApiJson.paths
            },
            showOpenApiAddPath: false
        };

        this.handleShowOpenApiAddPath = this.handleShowOpenApiAddPath.bind(this);
        this.onAddOpenApiPath = this.onAddOpenApiPath.bind(this);
        this.onAddOpenApiOperation = this.onAddOpenApiOperation.bind(this);
        this.onAddOpenApiParameter = this.onAddOpenApiParameter.bind(this);
        this.onInlineValueChange = this.onInlineValueChange.bind(this);
        this.onExpandAll = this.onExpandAll.bind(this);
        this.handlePostAddPath = this.handlePostAddPath.bind(this);
    }

    public componentDidMount() {
        let { openApiJson } = this.props;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        this.setState({
            openApiJson
        });
    }

    public componentWillReceiveProps(nextProps: OpenApiProps) {
        let { openApiJson } = nextProps;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        if (openApiJson !== this.state.openApiJson) {
            this.setState({
                openApiJson
            });
        }
    }

    public render() {
        const { openApiJson } = this.props;
        const { showOpenApiAddPath, expandMode } = this.state;

        const appContext: OpenApiContext = {
            expandMode,
            onAddOpenApiOperation: this.onAddOpenApiOperation,
            onAddOpenApiParameter: this.onAddOpenApiParameter,
            onAddOpenApiPath: this.onAddOpenApiPath,
            onAddOpenApiResponse: this.onAddOpenApiResponse,
            onInlineValueChange: this.onInlineValueChange,
            openApiJson,
        };

        return (
            <OpenApiContextProvider value={appContext}>
                <OpenApiInfo info={openApiJson.info} />
                <Divider />
                <Button size="mini" primary icon labelPosition="left" onClick={this.handleShowOpenApiAddPath}>
                    <i className="fw fw-add icon"></i>
                    Add Resource
                </Button>
                <Button.Group floated="right" size="mini">
                    <Button type="all" onClick={this.onExpandAll}>List Resources</Button>
                    <Button type="resources" onClick={this.onExpandAll}>List Operations</Button>
                    <Button type="operations" onClick={this.onExpandAll}>Expand All</Button>
                </Button.Group>
                {showOpenApiAddPath &&
                    <AddOpenApiPath openApiJson={openApiJson}  onClose={this.handleShowOpenApiAddPath}
                        onAddOpenApiPath={appContext.onAddOpenApiPath} />
                }
                <OpenApiPathList expandMode={expandMode} paths={openApiJson.paths} />
            </OpenApiContextProvider>
        );
    }

    private onAddOpenApiPath(path: Swagger.PathItemObject, onAdd: (state: boolean) => void) {
        const resourceName = path.name.replace(" ", "");
        const operations: { [index: string]: Swagger.OperationObject } = {};

        if (resourceName === "") {
            onAdd(false);
            return;
        }

        path.methods.forEach((method: string, index: number) => {
            let opName = resourceName;
            opName = opName.replace(/[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/gi, "");

            if (resourceName.match(/\d+/g) !== null) {
                opName = "resource" + opName;
            }

            if (!this.state.openApiJson.paths["/" + resourceName]) {
                operations[method.toLowerCase()] = {
                    operationId: index === 0 ? opName : "resource" + index,
                    responses: {},
                };
            } else {
                operations[method.toLowerCase()] = {
                    operationId: opName,
                    responses: {},
                };
            }
        });

        if (this.state.openApiJson.paths["/" + resourceName]) {
            this.setState((prevState) => ({
                ...prevState,
                openApiJson: {
                    ...prevState.openApiJson,
                    paths: {
                        ...prevState.openApiJson.paths,
                        ["/" + resourceName] : {
                            ...prevState.openApiJson.paths["/" + resourceName],
                            ...operations
                        }
                    }
                }
            }), () => {
                this.handlePostAddPath(resourceName, onAdd);
            });
        } else {
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
                this.handlePostAddPath(resourceName, onAdd);
            });
        }

    }

    private handlePostAddPath(resourceName: string, onAdd: (state: boolean) => void) {
        const { onDidAddResource, onDidChange } = this.props;

        if (this.state.openApiJson.paths["/" + resourceName]) {

            onAdd(true);

            if (onDidAddResource) {
                onDidAddResource(resourceName, this.state.openApiJson);
            }

            if (onDidChange) {
                onDidChange(EVENTS.ADD_RESOURCE, this.state.openApiJson);
            }

        } else {
            onAdd(false);
        }
    }

    private onAddOpenApiOperation(operation: Swagger.OperationObject) {
        const { onDidAddOperation, onDidChange } = this.props;
        const path = operation.path;
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

        operation.method.forEach((method: string, index: number) => {
            operations[method.toLowerCase()] = {
                description: "",
                operationId: Object.keys(operations).length === 0 ? path : "resource" + resourceIndex,
                parameters: [],
                responses: {
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
                    [path]: operations
                }
            }
        }), () => {

            if (onDidAddOperation) {
                onDidAddOperation(operation, this.state.openApiJson);
            }

            if (onDidChange) {
                onDidChange(EVENTS.ADD_OPERATION, this.state.openApiJson);
            }

        });
    }

    private onExpandAll(e: any) {
        const type = e.target.attributes.getNamedItem("type").value;

        switch (type) {
            case "resources":
                this.setState({
                    expandMode: {
                        ...this.state.expandMode,
                        isEdit: false,
                        type: "resources",
                    }
                });
                break;
            case "operations":
                this.setState({
                    expandMode: {
                        ...this.state.expandMode,
                        isEdit: false,
                        type: "operations"
                    }
                });
                break;
            case "all":
                this.setState({
                    expandMode: {
                        ...this.state.expandMode,
                        isEdit: false,
                        type: "collapse"
                    }
                });
                break;
            default:
                break;
        }
    }

    private onAddOpenApiParameter(parameter: Swagger.ParameterObject) {
        const { onDidAddParameter, onDidChange } = this.props;
        const { openApiJson } = this.state;
        const path = parameter.resourcePath;
        const method = parameter.operation;

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
                                        allowEmptyValue: parameter.allowedEmptyValues,
                                        description: parameter.description,
                                        in: parameter.parameterIn,
                                        isRequired: parameter.isRequired,
                                        name: parameter.name,
                                        schema: {
                                            type: parameter.type
                                        }
                                    }
                                ]
                            }
                        }
                    }
                }
            }), () => {

                if (onDidAddParameter) {
                    onDidAddParameter(parameter, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_PARAMETER, this.state.openApiJson);
                }

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
                                        allowEmptyValue: parameter.allowedEmptyValues,
                                        description: parameter.description,
                                        in: parameter.parameterIn,
                                        isRequired: parameter.isRequired,
                                        name: parameter.name,
                                        schema: {
                                            type: parameter.type
                                        }
                                    }
                                ]
                            }
                        }
                    }
                }
            }), () => {

                if (onDidAddParameter) {
                    onDidAddParameter(parameter, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_PARAMETER, this.state.openApiJson);
                }

            });
        }
    }

    private onAddOpenApiResponse(response: Swagger.ResponseObject) {
        // console.log('add-response');
    }

    private onInlineValueChange(openApiJson: Swagger.OpenAPIObject) {
        const { onDidChange } = this.props;

        this.setState((prevState) => ({
            ...prevState,
            expandMode: {
                ...this.state.expandMode,
                isEdit: true
            },
            openApiJson,
        }), () => {
            if (onDidChange) {
                onDidChange(EVENTS.ON_INLINE_CHANGE, this.state.openApiJson);
            }
        });

    }

    private handleShowOpenApiAddPath() {
        this.setState({
            showOpenApiAddPath: !this.state.showOpenApiAddPath
        });
    }
}

export default OpenApiVisualizer;
