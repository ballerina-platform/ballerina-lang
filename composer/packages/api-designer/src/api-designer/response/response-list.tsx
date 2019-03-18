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
import { Table } from "semantic-ui-react";

export interface OpenApiResponseListProps {
    responsesList: Swagger.ResponsesObject[];
}

class OpenApiResponseList extends React.Component<OpenApiResponseListProps, any> {
    constructor(props: OpenApiResponseListProps) {
        super(props);
    }

    public render() {
        const { responsesList } = this.props;

        return (
            <Table single line>
                <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>Name</Table.HeaderCell>
                    <Table.HeaderCell>Description</Table.HeaderCell>
                </Table.Row>
                </Table.Header>
                <Table.Body>
                    {Object.keys(responsesList).map((param: any, index: number) => {
                        return (
                            <Table.Row>
                                <Table.Cell className="parameter-name-cell">
                                    <div className="parameter__name required">
                                        {param}
                                    </div>
                                </Table.Cell>
                                <Table.Cell className="parameter-desc-cell">
                                    <div className="markdown">
                                        {responsesList[param].description}
                                    </div>
                                </Table.Cell>
                            </Table.Row>
                        );
                    })}
                </Table.Body>
            </Table>
        );
    }
}

export default OpenApiResponseList;
