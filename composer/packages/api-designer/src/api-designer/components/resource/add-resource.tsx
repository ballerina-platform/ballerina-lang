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
import { Button, Form } from "semantic-ui-react";

export interface OpenApiAddResourceProps {
    onDidAddResource: (resource: OpenApiResource) => void;
}

export interface OpenApiAddResourceState {
    openApiResourceObj: OpenApiResource;
    operationMethods: OpenApiOperationMethod[];
}

export interface OpenApiResource {
    name: string;
    methods: string[];
}

export interface OpenApiOperationMethod {
    text: string;
    value: string;
}

class OpenApiAddResource extends React.Component<OpenApiAddResourceProps, OpenApiAddResourceState> {
    constructor(props: OpenApiAddResourceProps) {
        super(props);

        this.state = {
            openApiResourceObj: {
                methods: [],
                name: ""
            },
            operationMethods: []
        };
    }

    public componentDidMount() {
        this.populateOperationMethods();
    }

    public populateOperationMethods() {
        const methodOpts: OpenApiOperationMethod[] = [];

        const availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"];

        availableMethods.forEach((method) => {
            methodOpts.push({
                text: method,
                value: method.toLowerCase(),
            });
        });

        this.setState({
            operationMethods: methodOpts,
        });
    }

    public render() {
        const { onDidAddResource } = this.props;
        const { operationMethods } = this.state;

        return (
            <Form size="mini" className="add-resource">
                <Form.Field>
                    <label>Resource Name</label>
                    <input placeholder="Example: /users/{userId}" onChange={(e) => this.setState({
                        openApiResourceObj: {
                            ...this.state.openApiResourceObj,
                            name: e.target.value
                        }
                    })} />
                </Form.Field>
                <Form.Group inline>
                    <label>Methods</label>
                    {operationMethods.map((method) => {
                        return (
                            <Form.Checkbox
                                size="mini"
                                label={method.text}
                                value={method.value}
                                onChange={(e: React.SyntheticEvent, data: any) => {
                                    if (data.checked) {
                                        this.setState({
                                            openApiResourceObj: {
                                                ...this.state.openApiResourceObj,
                                                methods:  [...this.state.openApiResourceObj.methods, data.label],
                                            }
                                        });
                                    }
                                }}
                            />
                        );
                    })}
                </Form.Group>
                <Button size="tiny" onClick={() => {
                    onDidAddResource(this.state.openApiResourceObj);
                }}>Save Resource</Button>
            </Form>
        );
    }
}

export default OpenApiAddResource;
