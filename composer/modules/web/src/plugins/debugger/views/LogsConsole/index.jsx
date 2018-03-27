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
import moment from 'moment';
import { Table, Rail, Sticky, Grid } from 'semantic-ui-react';
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
            filteredMessages: [],
        };
        this.onFilteredMessages = this.onFilteredMessages.bind(this);
        this.debouncedSetState = _.debounce(nextState => this.setState({ messages: nextState }), 400);
    }

    /**
     * React.Component lifecycle hook
     *
     * @memberof LogsConsole
     */
    componentDidMount() {
        const messages = [
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }, {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }
            ,
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }, {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }
            ,
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "Inbound",
                "stream": "tracelog.http.downstream", "headers": ""
            },
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }, {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.upstream", "headers": ""
            }
            ,
            {
                "timestamp": "2018-03-16 19:37:45,292", "id": "0xbef1f7aa", "direction": "OutBound",
                "stream": "tracelog.http.downstream", "headers": ""
            }
        ];
        
        this.setState({
            messages,
            filteredMessages: messages,
        });
        let messageCache = [];
        this.props.LaunchManager.on('execution-started', () => {
            messageCache = [];
            this.setState({
                messages: [],
            });
        });
        this.props.LaunchManager.on('print-trace-message', (message) => {
            messageCache.push(JSON.parse(message.message));
            const newMessage = messageCache.map((msg) => {
                message.timestamp = moment(msg.timestamp).unix();
                message.dateString = moment(msg.timestamp).toString();
                return msg;
            }).sort((msg1, msg2) => {
                return msg1.timestamp - msg2.timestamp;
            });

            this.debouncedSetState(newMessage);
        });
    }

    onFilteredMessages(filteredMessages) {
        this.setState({
            filteredMessages,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { height } = this.props;
        return (
            <div id='logs-console' ref={(stickyContext) => { this.stickyContext = stickyContext; }}>
                {
                    this.state.messages.length > 0 &&
                    <div>
                        <ToolBar
                            messages={this.state.messages}
                            filters={{ id: 'Activity Id', direction: 'Direction', stream: 'Stream' }}
                            onFilteredMessages={this.onFilteredMessages}
                        />
                        <div >
                            <Grid columns={3} stretched style={{ width: '100%', margin: 0 }}>
                                <Grid.Row className='table-heading'>
                                    <Grid.Column>
                                        Activity Id
                                    </Grid.Column>
                                    <Grid.Column>
                                        Stream
                                    </Grid.Column>
                                    <Grid.Column>
                                        Headers
                                    </Grid.Column>
                                </Grid.Row>
                            </Grid>
                            <Grid
                                columns={3}
                                className='table-content'
                                style={{ maxHeight: height }} 
                            >
                                {this.state.filteredMessages.map((message) => {
                                    return (
                                        <Grid.Row
                                        >
                                            <Grid.Column>{message.id}</Grid.Column>
                                            <Grid.Column>{message.stream}</Grid.Column>
                                            <Grid.Column>{message.headers}</Grid.Column>
                                        </Grid.Row>
                                    );
                                })}
                            </Grid>
                        </div>
                    </div>
                }

            </div>
        );
    }
}

LogsConsole.propTypes = {

};

export default LogsConsole;
