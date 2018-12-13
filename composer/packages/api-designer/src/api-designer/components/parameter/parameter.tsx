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

export interface OpenApiParameterProps {
    parameterObject: any;
    responseCode: string;
}

class OpenApiParameter extends React.Component<OpenApiParameterProps, any> {
    constructor(props: OpenApiParameterProps) {
        super(props);
    }

    public render() {
        const { parameterObject, responseCode } = this.props;

        return (
            <Table.Row>
                <Table.Cell className="parameter-name-cell">
                    <div className="parameter__name required">
                        {responseCode === "" ? parameterObject.name : responseCode }
                    </div>
                    <div className="parameter__type">
                        <strong>{parameterObject.schema && parameterObject.schema.type}</strong>
                        {parameterObject.in &&
                            <p><em>({parameterObject.in})</em></p>
                        }
                    </div>
                </Table.Cell>
                <Table.Cell className="parameter-desc-cell">
                    <div className="markdown">
                        {parameterObject.description}
                    </div>
                </Table.Cell>
            </Table.Row>
        );
    }
}

export default OpenApiParameter;
