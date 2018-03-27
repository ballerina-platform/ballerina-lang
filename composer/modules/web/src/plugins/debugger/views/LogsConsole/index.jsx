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

import React from 'react';
import _ from 'lodash';
import { Table } from 'semantic-ui-react';
import ToolBar from './Toolbar';
import './index.scss';

/**
 *
 * @extends React.Component
 */
class LogsConsole extends React.Component {

    /**
     * Creates an instance of LogsConsole.
     * @memberof LogsConsole
     */
    constructor() {
        super();
        this.state = {
            messages: [],
        };
        this.debouncedSetState = _.debounce(nextState => this.setState({ messages: nextState }), 400);
    }

    /**
     * React.Component lifecycle hook
     *
     * @memberof LogsConsole
     */
    componentDidMount() {
        let messageCache = [];
        this.props.LaunchManager.on('execution-started', () => {
            messageCache = [];
            this.setState({
                messages: [],
            });
        });
        this.props.LaunchManager.on('print-trace-message', (message) => {
            messageCache.push(JSON.parse(message.message));
            this.debouncedSetState(messageCache);
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div id='logs-console'>
                <ToolBar activity='' channel='' service='' />
                <Table celled inverted>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell>Activity Id</Table.HeaderCell>
                            <Table.HeaderCell>stream</Table.HeaderCell>
                            <Table.HeaderCell>headers</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>

                    <Table.Body>
                        {this.state.messages.map((message) => {
                            return (
                                <Table.Row>
                                    <Table.Cell>{message.id}</Table.Cell>
                                    <Table.Cell>{message.stream}</Table.Cell>
                                    <Table.Cell>{message.headers}</Table.Cell>
                                </Table.Row>
                            );
                        })}
                    </Table.Body>
                </Table>
            </div>
        );
    }
}

LogsConsole.propTypes = {

};

export default LogsConsole;
