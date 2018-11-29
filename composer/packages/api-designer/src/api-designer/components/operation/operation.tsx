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
import { Accordion, AccordionTitleProps, Icon } from "semantic-ui-react";

import InlineEdit from "../../util-components/inline-edit";
import OpenApiAddParameter from "../parameter/add-parameter";
import OpenApiAddResponse from "../parameter/add-response";
import OpenApiParameterList from "../parameter/parameters";

import { OpenApiContext, OpenApiContextConsumer } from "../../context/open-api-context";

export interface OpenApiResourceProps {
    resourcePath: string;
    operationType: string;
    operationObject: any;
    activeIndex: number;
    currIndex: number;
    handleExpand: (event: React.MouseEvent<HTMLDivElement>, data: AccordionTitleProps) => void;
}

export interface OpenApiResourceState {
    showAddResponse: boolean;
    showAddParameter: boolean;
}

class OpenApiResource extends React.Component<OpenApiResourceProps, OpenApiResourceState> {
    constructor(props: OpenApiResourceProps) {
        super(props);

        this.state = {
            showAddParameter: false,
            showAddResponse: false
        };

        this.handleShowAddParameter = this.handleShowAddParameter.bind(this);
        this.handleShowAddResponse = this.handleShowAddResponse.bind(this);
    }

    public handleShowAddParameter(show?: boolean) {
        const { showAddParameter } = this.state;
        if (show !== undefined && !show) {
            this.setState({
                showAddParameter: false,
            });
        } else {
            this.setState({
                showAddParameter: !showAddParameter,
            });
        }

    }

    public handleShowAddResponse(show?: boolean) {
        const { showAddResponse } = this.state;
        if (show !== undefined && !show) {
            this.setState({
                showAddResponse: false,
            });
        } else {
            this.setState({
                showAddResponse: !showAddResponse,
            });
        }
    }

    public render() {
        const {
            resourcePath, operationType, operationObject, activeIndex, currIndex, handleExpand
        } = this.props;
        const { showAddResponse, showAddParameter } = this.state;

        return (
            <div className={"operation "  + operationType}>
                <Accordion.Title className="op-title " index={currIndex} onClick={handleExpand}>
                    <span className="op-method">{operationType}</span>
                    <InlineEdit
                        model={operationObject}
                        attribute={operationType}
                        customClass="op-summary"
                        isEditable
                        text={operationObject.summary}
                        placeholderText="Add a summary" />
                    <Icon
                        className="delete-op"
                        name="trash alternate"
                    />
                </Accordion.Title>
                <Accordion.Content active={activeIndex === currIndex}>
                    <InlineEdit
                        model={operationObject}
                        attribute={operationType}
                        isEditable
                        text={operationObject.description}
                        placeholderText="Add a description" />

                    <div className="op-section">
                        <div className="title">
                            <p>Parameters</p>
                            <a onClick={() => {
                                this.handleShowAddParameter();
                            }} >Add Parameter</a>
                        </div>
                        {showAddParameter &&
                            <OpenApiContextConsumer>
                                {(appContext: OpenApiContext | null) => {
                                    return (
                                        <OpenApiAddParameter
                                            openApiJson={appContext!.openApiJson}
                                            onAddParameter={appContext!.onDidAddParameter}
                                            operation={operationType}
                                            resourcePath={resourcePath}
                                            handleClose={this.handleShowAddParameter}
                                        />
                                    );
                                }}
                            </OpenApiContextConsumer>

                        }
                        {operationObject.parameters && Object.keys(operationObject.parameters).length !== 0 &&
                            <OpenApiParameterList
                                parameterType="parameter"
                                parameterList={operationObject.parameters} />
                        }
                    </div>

                    <div className="op-section ">
                        <div className="title">
                            <p>Responses</p>
                            <a onClick={() => {
                                this.handleShowAddResponse();
                            }} >Add Response</a>
                        </div>
                        {showAddResponse && OpenApiContextConsumer &&
                            <OpenApiContextConsumer>
                                {(appContext: OpenApiContext | null) => {
                                    return (
                                        <OpenApiAddResponse
                                            openApiJson={appContext!.openApiJson}
                                            onAddResponse={appContext!.onDidAddResponse}
                                            operation={operationType}
                                            resourcePath={resourcePath}
                                            handleClose={this.handleShowAddResponse}
                                        />
                                    );
                                }}
                            </OpenApiContextConsumer>
                        }
                        {operationObject.responses && Object.keys(operationObject.responses).length !== 0 &&
                            <OpenApiParameterList parameterType="response" parameterList={operationObject.responses} />
                        }
                    </div>

                </Accordion.Content>
            </div>
        );
    }
}

export default OpenApiResource;
