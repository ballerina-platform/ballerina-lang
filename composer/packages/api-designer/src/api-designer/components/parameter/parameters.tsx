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
import { Table } from "semantic-ui-react";

import OpenApiParameter from "./parameter";

export interface OpenApiParameterListProps {
    parameterType: string;
    parameterList: any;
}

class OpenApiParameterList extends React.Component<OpenApiParameterListProps, any> {
    constructor(props: OpenApiParameterListProps) {
        super(props);
    }

    public render() {
        const { parameterList, parameterType } = this.props;

        return (
            <Table celled>
                <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>Name</Table.HeaderCell>
                    <Table.HeaderCell>Description</Table.HeaderCell>
                </Table.Row>
                </Table.Header>
                <Table.Body>
                    {parameterList && Object.keys(parameterList).map((param) => {
                        return (
                            <OpenApiParameter
                                responseCode={parameterType === "response" ? param : ""}
                                parameterObject={parameterList[param]}
                            />
                        );
                    })}
                </Table.Body>
            </Table>
        );
    }
}

export default OpenApiParameterList;
