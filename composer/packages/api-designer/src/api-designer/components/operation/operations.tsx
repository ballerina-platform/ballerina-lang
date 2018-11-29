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

import { OpenApiContext, OpenApiContextConsumer } from "../../context/open-api-context";
import OpenApiAddOperation from "./add-operation";
import OpenApiOperation from "./operation";

export interface OasOperationsListProps {
    openApiOperations: any;
    resourcePath: string;
    showAddOperation: boolean;
}

export interface OasOperationsListState {
    activeIndex: number;
}

class OpenApiOperationsList extends React.Component<OasOperationsListProps, OasOperationsListState> {
    constructor(props: OasOperationsListProps) {
        super(props);

        this.state = {
            activeIndex: -1
        };

        this.expandOperation = this.expandOperation.bind(this);
    }

    public expandOperation(event: React.MouseEvent<HTMLDivElement>, titleProps: AccordionTitleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === Number(index) ? -1 : Number(index);

        this.setState({ activeIndex: newIndex });
    }

    public render() {
        const { openApiOperations, resourcePath, showAddOperation } = this.props;
        const { activeIndex } = this.state;

        return (
            <React.Fragment>
                {showAddOperation &&
                    <OpenApiContextConsumer>
                        {(appContext: OpenApiContext | null) => {
                            return (
                                <OpenApiAddOperation
                                    onAddOperation={appContext!.onDidAddOperation}
                                    openApiJson={appContext!.openApiJson}
                                    resourcePath={resourcePath}
                                />
                            );
                        }}
                    </OpenApiContextConsumer>
                }
                {openApiOperations && Object.keys(openApiOperations).length > 0 &&
                    <Accordion fluid>
                        {Object.keys(openApiOperations).map((operation, index) => {
                            return (
                                <OpenApiContextConsumer>
                                    {(appContext: OpenApiContext | null) => {
                                        return (
                                            <OpenApiOperation
                                                resourcePath={resourcePath}
                                                operationType={operation}
                                                operationObject={openApiOperations[operation]}
                                                activeIndex={activeIndex}
                                                currIndex={index}
                                                handleExpand={this.expandOperation}
                                            />
                                        );
                                    }}
                                </OpenApiContextConsumer>
                            );
                        })}
                    </Accordion>
                }
            </React.Fragment>
        );
    }
}

export default OpenApiOperationsList;
