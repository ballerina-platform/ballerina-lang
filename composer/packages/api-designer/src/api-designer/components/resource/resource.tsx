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
import { Accordion, AccordionTitleProps, Button, Icon } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextConsumer } from "../../context/open-api-context";
import InlineEdit from "../../util-components/inline-edit";
import OpenApiOperationsList from "../operation/operations";

export interface OpenApiResourceProps {
    openApiResource: string;
    openApiOperations: any;
    activeIndex?: number;
    currentIndex: number;
    onExpandEvent: (event: React.MouseEvent<HTMLDivElement>, data: AccordionTitleProps) => void;
    isExpandAll: boolean;
    active: boolean;
}

export interface OpenApiResourceState {
    showAddOperation: boolean;
}

class OpenApiResource extends React.Component<OpenApiResourceProps, OpenApiResourceState> {
    constructor(props: OpenApiResourceProps) {
        super(props);

        this.state = {
            showAddOperation: false,
        };

        this.handleShowAddOperation = this.handleShowAddOperation.bind(this);
    }

    public handleShowAddOperation(event: React.MouseEvent<HTMLButtonElement>) {
        event.stopPropagation();
        const { showAddOperation } = this.state;
        this.setState({
            showAddOperation: !showAddOperation,
        });
    }

    public render() {
        const {
            openApiResource, openApiOperations, currentIndex, onExpandEvent, isExpandAll, active
        } = this.props;

        const { showAddOperation } = this.state;

        return (
            <div className="resource">
                <Accordion.Title
                    index={currentIndex}
                    className="res-title"
                    active={isExpandAll || active}
                    onClick={onExpandEvent} >
                    <Icon className={isExpandAll || active ? "fw fw-down" : "fw fw-right"} />
                    <OpenApiContextConsumer>
                        {(appContext: OpenApiContext) => {
                            return (
                                <InlineEdit
                                    changeModel={appContext.openApiJson}
                                    changeAttribute={{key: "resource.name", changeValue: openApiResource}}
                                    inlineEditString={openApiResource}
                                    placeholderString="Add a description"
                                    onInlineValueChange={appContext.onInlineEditChange}
                                />
                            );
                        }}
                    </OpenApiContextConsumer>
                    {isExpandAll || active ?
                        <Button
                        title="Add operation to resource."
                        size="mini"
                        compact
                        className="add-operation-action"
                        circular
                        onClick={(e) => {
                            this.handleShowAddOperation(e); }
                        }><i className="fw fw-add"></i></Button> : ""
                    }
                </Accordion.Title>
                <Accordion.Content className="resource-content" active={isExpandAll || active} >
                    <OpenApiOperationsList
                        openApiOperations={openApiOperations}
                        resourcePath={openApiResource}
                        showAddOperation={showAddOperation}
                    />
                </Accordion.Content>
            </div>
        );
    }
}

export default OpenApiResource;
