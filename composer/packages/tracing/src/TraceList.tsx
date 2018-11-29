/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the ''License''); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * ''AS IS'' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import * as _ from "lodash";
import * as React from "react";
import { Icon, Label, Table } from "semantic-ui-react";

const directionToIcon: any = {
    INBOUND: {
        "http.tracelog.downstream": "fw fw-downstream-inbound direction-icon inbound downstream",
        "http.tracelog.upstream": "fw fw-upstream-inbound direction-icon inbound upstream",
    },
    OUTBOUND: {
        "http.tracelog.downstream": "fw fw-downstream-outbound direction-icon outbound downstream",
        "http.tracelog.upstream": "fw fw-upstream-outbound direction-icon outbound upstream",
    },
};

export interface TraceListProps {
    traces: any[];
    selected?: string;
    onSelected: (record: any) => void;
}

/**
 *
 * @extends React.Component
 */
class TraceList extends React.Component<TraceListProps> {
    constructor(props: any, context: any) {
        super(props);
    }

    public getDirectionIcon(logger: string, direction: string) {
        directionToIcon[direction] = directionToIcon[direction] || {};
        return directionToIcon[direction][logger];
    }

    /**
     * @inheritdoc
     */
    public render() {
        return (
            <div id="logs-console">
                <Table celled inverted unstackable compact striped>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell></Table.HeaderCell>
                            <Table.HeaderCell className="method">Method</Table.HeaderCell>
                            <Table.HeaderCell>Path</Table.HeaderCell>
                            <Table.HeaderCell>Direction</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>

                    <Table.Body>
                        {this.props.traces.map((record: any) => {
                            return (
                                <Table.Row
                                    className={ this.props.selected === record.id ? "active clickable" : "clickable"}
                                    key={record.id}
                                    onClick={() => this.props.onSelected(record)}
                                >
                                    <Table.Cell className="direction-icon">
                                        <Icon
                                            name={this.getDirectionIcon(record.logger,
                                                record.message.direction)}
                                            title={`${record.logger} - ${record.message.direction}`}
                                        />
                                    </Table.Cell>
                                    <Table.Cell className="method">
                                        {
                                            record.message.httpMethod &&
                                            <Label horizontal className={record.message.httpMethod.toLowerCase()}>
                                                {record.message.httpMethod}
                                            </Label>
                                        }

                                    </Table.Cell>
                                    <Table.Cell className="path">{record.message.path}</Table.Cell>
                                    <Table.Cell className="direction">{record.message.direction}</Table.Cell>
                                </Table.Row>
                            );
                        })}
                    </Table.Body>
                </Table>
            </div>
        );
    }
}

export default TraceList;
