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
import { Accordion, AccordionTitleProps } from "semantic-ui-react";

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
                    <OpenApiContextConsumer>
                        {(appContext: OpenApiContext) => {
                            return (
                                <InlineEdit
                                    characterLimit={50}
                                    changeModel={appContext.openApiJson}
                                    changeAttribute={{
                                        changeValue: operationType,
                                        key: "operation.summary",
                                        path: resourcePath
                                    }}
                                    classDefinition="op-summary"
                                    inlineEditString={operationObject.summary}
                                    placeholderString="Add a summary"
                                    onInlineValueChange={appContext.onInlineEditChange}
                                />
                            );
                        }}
                    </OpenApiContextConsumer>
                </Accordion.Title>
                <Accordion.Content active={activeIndex === currIndex}>
                    <OpenApiContextConsumer>
                        {(appContext: OpenApiContext) => {
                            return (
                                <InlineEdit
                                    isMarkdown
                                    isParagraph
                                    changeModel={appContext.openApiJson}
                                    changeAttribute={{
                                        changeValue: operationType,
                                        key: "operation.description",
                                        path: resourcePath
                                    }}
                                    inlineEditString={operationObject.description}
                                    placeholderString="Add a description"
                                    onInlineValueChange={appContext.onInlineEditChange}
                                />
                            );
                        }}
                    </OpenApiContextConsumer>
                    <div className="op-section">
                        <div className="title">
                            <p>Parameters</p>
                            <a onClick={() => {
                                this.handleShowAddParameter();
                            }} >Add Parameter</a>
                        </div>
                        {showAddParameter &&
                            <OpenApiContextConsumer>
                                {(appContext: OpenApiContext) => {
                                    return (
                                        <OpenApiAddParameter
                                            openApiJson={appContext.openApiJson}
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
                                {(appContext: OpenApiContext) => {
                                    return (
                                        <OpenApiAddResponse
                                            openApiJson={appContext.openApiJson}
                                            onAddResponse={appContext.onDidAddResponse}
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
