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
import OpenApiAddResource from "./add-resource";
import OpenApiResource from "./resource";

export interface OasResourceListProps {
    openApiResources: any;
}

export interface OpenApiResourceListState {
    expandAll: boolean;
    showAddResource: boolean;
    activeIndex: number[];
    collapseOnExpandAll: number[];
}

class OpenApiResourceList extends React.Component<OasResourceListProps, OpenApiResourceListState> {
    constructor(props: OasResourceListProps) {
        super(props);

        this.state = {
            activeIndex: [],
            collapseOnExpandAll: [],
            expandAll: false,
            showAddResource: false
        };

        this.showOpenApiAddResource = this.showOpenApiAddResource.bind(this);
        this.expandResource = this.expandResource.bind(this);
        this.expandAllResources = this.expandAllResources.bind(this);
    }

    public showOpenApiAddResource() {
        const { showAddResource } = this.state;
        this.setState({
            showAddResource: !showAddResource
        });
    }

    public expandAllResources() {
        const { expandAll } = this.state;

        if (expandAll) {
            this.setState({
                activeIndex: [],
                collapseOnExpandAll: [],
                expandAll: false
            });
        } else {
            this.setState({
                expandAll: true
            });
        }

    }

    public expandResource(event: React.MouseEvent<HTMLDivElement>, data: AccordionTitleProps): void {
        const { expandAll, activeIndex, collapseOnExpandAll } = this.state;
        const openApiResourcesLength = Object.keys(this.props.openApiResources).length;

        if (expandAll) {
            if (!collapseOnExpandAll.includes(Number(data.index))) {
                this.setState({
                    collapseOnExpandAll: [...this.state.collapseOnExpandAll, Number(data.index)]
                });
            } else {
                this.setState((prevState) => ({
                    collapseOnExpandAll: prevState.collapseOnExpandAll.filter((index) =>
                        index !==  Number(data.index))
                    }));
            }
        } else {
            if (!activeIndex.includes(Number(data.index))) {
                const indexArray = [...this.state.activeIndex, Number(data.index)];
                this.setState({
                    activeIndex: indexArray,
                    expandAll: indexArray.length === openApiResourcesLength
                });
            } else {
                this.setState((prevState) => ({
                    activeIndex: prevState.activeIndex.filter((index) => index !==  Number(data.index))
                }));
            }

        }
    }

    public render() {
        const { openApiResources } = this.props;
        const { expandAll, showAddResource, activeIndex, collapseOnExpandAll } = this.state;

        if (!openApiResources) {
            return "";
        }

        return (
            <div className="open-api-resource-list-container">
                <div className="action-container">
                    <Button size="mini" icon labelPosition="left" onClick={this.showOpenApiAddResource}>
                        <Icon name="plus" />
                        Add Resource
                    </Button>
                    <Button size="mini" icon labelPosition="left" floated="right" onClick={this.expandAllResources}>
                        <Icon name={expandAll ? "compress" : "expand"} />
                        {expandAll || activeIndex.length === Object.keys(openApiResources).length ?
                            "Collapse All" : "Expand All"
                        }
                    </Button>
                </div>
                {showAddResource &&
                    <OpenApiContextConsumer>
                        {(context: OpenApiContext | null) => {
                            return (
                                <OpenApiAddResource onDidAddResource={context!.onDidAddResource} />
                            );
                        }}
                    </OpenApiContextConsumer>
                }
                {openApiResources && Object.keys(openApiResources).length > 0 &&
                    <Accordion exclusive={false} className="open-api-resource-list">
                        {Object.keys(openApiResources).map((openApiResource, index) => {
                            return (
                                <OpenApiResource
                                    openApiResource = { openApiResource }
                                    openApiOperations = { openApiResources[openApiResource] }
                                    currentIndex = { index }
                                    onExpandEvent = { this.expandResource }
                                    isExpandAll = { !collapseOnExpandAll.includes(index) && expandAll ? true : false }
                                    active={ activeIndex.includes(index) }
                                />
                            );
                        })}
                    </Accordion>
                }
            </div>
        );
    }
}

export default OpenApiResourceList;
