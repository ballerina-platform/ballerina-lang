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
import { Button, Form, Segment } from "semantic-ui-react";

export interface OpenApiAddOperationProps {
    openApiJson: Swagger.OpenAPIObject;
    onAddOperation: (operation: Swagger.OperationObject) => void ;
    resourcePath: string;
    handleOnClose: (id: number) => void;
    operationIndex: number;
}

export interface OpenApiAddOperationState {
    operationObject: OpenApiOperation;
    operationMethods: OpenApiOperationMethod[];
}

export interface OpenApiOperationMethod {
    text: string;
    value: string;
    checked: boolean;
}

export interface OpenApiOperation {
    id: string;
    name: string;
    description: string;
    method: string[];
    path: string;
    responses: string[];
}

class AddOpenApiOperation extends React.Component<OpenApiAddOperationProps, OpenApiAddOperationState> {

    constructor(props: any) {
        super(props);

        this.state = {
            operationMethods: [],
            operationObject: {
                description: "",
                id: "",
                method: [],
                name: "",
                path: this.props.resourcePath,
                responses: []
            }
        };

        this.handleCheckboxCheck = this.handleCheckboxCheck.bind(this);
    }

    public componentWillReceiveProps(nextProps: OpenApiAddOperationProps) {
        const { resourcePath, openApiJson } = nextProps;
        this.populateOperationMethods(resourcePath, openApiJson);
    }

    public componentDidMount() {
        const { resourcePath, openApiJson } = this.props;
        this.populateOperationMethods(resourcePath, openApiJson);
    }

    public render() {
        const { operationMethods } = this.state;
        const { onAddOperation, handleOnClose, operationIndex } = this.props;

        return (
            <Form className="add-operation">
                <Segment basic clearing>
                    <Button size="mini" className="btn-close" floated="right" onClick={(e: any) => {
                        handleOnClose(operationIndex);
                        this.setState({
                            operationMethods: [],
                            operationObject: {
                                description: "",
                                id: "",
                                method: [],
                                name: "",
                                path: this.props.resourcePath,
                                responses: []
                            }
                        });
                    }}>
                        <i className="fw fw-close icon"></i>
                    </Button>
                </Segment>
                <Form.Group inline>
                    <label>Methods</label>
                    {operationMethods.sort().map((method) => {
                        return (
                            <Form.Checkbox
                                size="mini"
                                label={method.text}
                                defaultChecked={method.checked}
                                disabled={method.checked}
                                onChange={this.handleCheckboxCheck}
                            />
                        );
                    })}
                </Form.Group>
                <Button size="mini" onClick={() => {
                    onAddOperation(this.state.operationObject);
                    handleOnClose(operationIndex);
                    this.setState({
                        operationMethods: [],
                        operationObject: {
                            description: "",
                            id: "",
                            method: [],
                            name: "",
                            path: this.props.resourcePath,
                            responses: []
                        }
                    });
                }}>Add</Button>
            </Form>
        );
    }

    private handleCheckboxCheck(e: React.SyntheticEvent, data: any) {
        if (data.checked) {
            this.setState({
                operationObject: {
                    ...this.state.operationObject,
                    method:  [...this.state.operationObject.method, data.label],
                }
            });
        } else {
            const methods = this.state.operationObject.method.filter((item) =>
                    item !== data.label);
            this.setState({
                operationObject: {
                    ...this.state.operationObject,
                    method: methods,
                }
            });
        }
    }

    private populateOperationMethods(resourcePath: string, openApiJson: Swagger.OpenAPIObject) {
        const methodOpts: OpenApiOperationMethod[] = [];

        const availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"];

        availableMethods.forEach((method) => {
            methodOpts.push({
                checked: Object.keys(openApiJson.paths[resourcePath]).includes(method.toLowerCase()) ? true : false,
                text: method,
                value: method.toLowerCase()
            });
        });

        this.setState({
            operationMethods: methodOpts,
        });
    }
}

export default AddOpenApiOperation;
