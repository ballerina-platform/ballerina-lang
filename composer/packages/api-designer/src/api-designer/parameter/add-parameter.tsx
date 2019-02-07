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
import { Button, Checkbox, Form, Header, Select } from "semantic-ui-react";

export interface OpenApiAddParameterProps {
    openApiJson: Swagger.OpenAPIObject;
    onAddParameter: (parameter: Swagger.ParameterObject) => void;
    operation: string;
    resourcePath: string;
    index: number;
    onClose: (index: number) => void;
}

export interface OpenApiAddParameterState {
    parameterIn: OpenApiParameterIn[];
    parameterType: OpenApiParameterType[];
    parameterObj: Swagger.ParameterObject;
}

export interface OpenApiParameterIn {
    key: string;
    text: string;
    value: string;
}

export interface OpenApiParameterType {
    key: string;
    text: string;
    value: string;
}

class OpenApiAddParameter extends React.Component<OpenApiAddParameterProps, OpenApiAddParameterState> {
    constructor(props: OpenApiAddParameterProps) {
        super(props);

        this.state = {
            parameterIn: [],
            parameterObj: {
                allowedEmptyValues: false,
                description: "",
                in: "path",
                isRequired: false,
                name: "",
                operation: this.props.operation,
                parameterIn: "",
                resourcePath: this.props.resourcePath,
                type: "",
            },
            parameterType: []
        };
    }

    public componentDidMount() {
        this.populateParameterInField();
        this.populateParamTypeField();
    }

    public render() {
        const { parameterIn, parameterType } = this.state;
        const { onAddParameter, onClose, index } = this.props;

        return (
            <Form size="mini" className="add-operation">
                <div className="form-box">
                    <Header floated="left" as="h3">Add Parameter</Header>
                    <Button size="mini" floated="right" onClick={() => {
                        onClose(index);
                    }}>
                        <i className="fw fw-close btn-close"></i>
                    </Button>
                </div>
                <Form.Field>
                    <label>Name</label>
                    <input placeholder="Parameter Name" onChange={(e) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            name: e.target.value
                        }
                    })} />
                </Form.Field>
                <Form.Field>
                    <Form.Field
                        control={Select}
                        label="Parameter In"
                        options={parameterIn}
                        placeholder="Where parameter appears"
                        onChange={(e: any, data: any) => {
                        this.setState({
                            parameterObj: {
                                ...this.state.parameterObj,
                                parameterIn: data.value
                            }
                        });
                    }}/>
                </Form.Field>
                <Form.Field>
                    <Form.Field
                        control={Select}
                        label="Parameter Type"
                        options={parameterType}
                        onChange={(e: any, data: any) => {
                        this.setState({
                            parameterObj: {
                                ...this.state.parameterObj,
                                type: data.value
                            }
                        });
                    }}/>
                </Form.Field>
                <Form.Field>
                    <Form.TextArea
                        label="Description"
                        placeholder="Tell us more about..."
                        onChange={(e) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            description: e.currentTarget.value
                        }
                    })}/>
                </Form.Field>
                <Form.Field>
                    <Checkbox label="Property is required" onChange={(e, { checked }) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            isRequired: checked ? true : false
                        }
                    })} />
                </Form.Field>
                <Form.Field>
                    <Checkbox label="Allowed empty values" onChange={(e, { checked }) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            allowedEmptyValues: checked ? true : false
                        }
                    })} />
                </Form.Field>
                <Button size="mini" onClick={() => {
                    onAddParameter(this.state.parameterObj);
                }}>Save</Button>
            </Form>
        );
    }

    private populateParameterInField() {
        const paramInDefaults = ["Path", "Query"];
        const paramInList: OpenApiParameterIn[] = [];

        paramInDefaults.forEach((response) => {
            paramInList.push({
                key: response.toLowerCase(),
                text: response,
                value: response.toLowerCase(),
            });
        });

        this.setState({
            parameterIn: paramInList
        });
    }

    private populateParamTypeField() {
        const typeInDefaults = ["String", "Number", "Boolean"];
        const typeList: OpenApiParameterType[] = [];

        typeInDefaults.forEach((response) => {
            typeList.push({
                key: response.toLowerCase(),
                text: response,
                value: response.toLowerCase(),
            });
        });

        this.setState({
            parameterType: typeList
        });
    }
}

export default OpenApiAddParameter;
