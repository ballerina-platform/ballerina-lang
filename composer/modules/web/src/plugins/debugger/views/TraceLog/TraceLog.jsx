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
import Proptypes from 'prop-types';
import _ from 'lodash';
import moment from 'moment';
import { Grid, Icon } from 'semantic-ui-react';
import SplitPane from 'react-split-pane';
import ErrorBoundary from 'core/editor/views/ErrorBoundary';
import ToolBar from './Toolbar';
import DetailView from './DetailView';
import './index.scss';

const directionToIcon = {
    INBOUND: {
        'http.tracelog.downstream': 'fw fw-downstream-inbound direction-icon',
        'http.tracelog.upstream': 'fw fw-upstream-inbound direction-icon',
    },
    OUTBOUND: {
        'http.tracelog.downstream': 'fw fw-downstream-outbound direction-icon',
        'http.tracelog.upstream': 'fw fw-upstream-outbound direction-icon',
    },
};

/**
 *
 * @extends React.Component
 */
class LogsConsole extends React.Component {

    /**
     * Creates an instance of LogsConsole.
     * @memberof LogsConsole
     */
    constructor(props) {
        super(props);
        this.state = {
            messages: [],
            filteredMessages: [],
            details: false,
        };
        this.onFilteredMessages = this.onFilteredMessages.bind(this);
        this.debouncedSetState = _.debounce(nextState => this.setState({ messages: nextState }), 400);
        this.toggleDetails = this.toggleDetails.bind(this);

        props.LaunchManager.on('execution-started', () => {
            this.setState({
                messages: [],
                filteredMessages: [],
            });
        });

        props.LaunchManager.on('print-trace-message', (message) => {
            if (!message.message.meta.direction) {
                return;
            }
            this.setState({
                messages: [...this.state.messages, message],
                filteredMessages: this.mergeRelatedMessages([...this.state.filteredMessages, message]),
            });
        });
    }

    componentDidMount() {
        const messages = this.props.LaunchManager.messages.filter( (message) => {
            return message.type === 'TRACE' && message.message.meta.direction;
        });
        this.setState({
            messages,
            filteredMessages: this.mergeRelatedMessages(messages),
        });
    }

    onFilteredMessages(filteredMessages) {
        this.setState({
            filteredMessages: this.mergeRelatedMessages(filteredMessages),
        });
    }

    getDirectionIcon(logger, direction) {
        directionToIcon[direction] = directionToIcon[direction] || {};
        return directionToIcon[direction][logger];
    }

    mergeRelatedMessages(messages) {
        const newMessages = [];
        for (let index = 0; index < messages.length; index++) {
            let message1 = messages[index];
            let message2 = messages[index + 1];
            if (message1.message.meta.headers.startsWith('DefaultHttpRequest')
                && message2
                && message1.message.record.thread === message2.message.record.thread
                && (message2.message.meta.headers.startsWith('DefaultLastHttpContent')
                    || message2.message.meta.headers.startsWith('EmptyLastHttpContent'))) {

                message1.message.meta.payload = message2.message.meta.payload;
                newMessages.push(message1);
            } else if (message1.message.meta.headers.startsWith('DefaultLastHttpContent') ||
            message1.message.meta.headers.startsWith('EmptyLastHttpContent')) {
                // do nothing
            } else {
                newMessages.push(message1);
            }
        }
        return newMessages;
    }

    toggleDetails(message) {
        this.setState({
            details: message,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { height } = this.props;
        const { details } = this.state;
        return (
            <div id='logs-console' ref={(stickyContext) => { this.stickyContext = stickyContext; }} style={{ height }}>
                <ErrorBoundary>
                    <div>
                        <ToolBar
                            messages={this.state.messages}
                            filters={{
                                'message.meta.id': 'Activity Id',
                                'message.record.logger': 'Logger',
                                'message.meta.path': 'Path',
                                'message.meta.direction': 'Inbound/Outbound',
                                'message.meta.httpMethod': 'Method',
                            }}
                            onFilteredMessages={this.onFilteredMessages}
                            clearLogs={() => {
                                this.setState({
                                    messages: [],
                                    filteredMessages: [],
                                });
                            }}
                        />
                        {
                            this.state.messages.length > 0 &&
                            <div >
                                <SplitPane
                                    split='vertical'
                                    size={details ? 450 : '100%'}
                                    allowResize={details ? true : false}
                                >
                                    <div>
                                        <Grid style={{ margin: 0 }}>
                                            <Grid.Row className='table-heading'>
                                                <Grid.Column className='summary'>
                                                    &nbsp;
                                                </Grid.Column>
                                                <Grid.Column className='activity'>
                                                    Activity Id
                                                </Grid.Column>
                                                <Grid.Column className='time'>
                                                    Time
                                                </Grid.Column>
                                                <Grid.Column className='path'>
                                                    Path
                                                </Grid.Column>
                                            </Grid.Row>
                                        </Grid>
                                        <Grid
                                            className='table-content'
                                            style={{ maxHeight: height }}
                                        >
                                            {this.state.filteredMessages.map((message) => {
                                                const timeString = moment(parseInt(message.message.record.millis)).format('HH:mm:ss.SSS');
                                                return (
                                                    <Grid.Row
                                                        className={(details && details.id === message.id) ? 'active clickable' : 'clickable'}
                                                        onClick={() => this.toggleDetails(message)}
                                                    >
                                                        <Grid.Column
                                                            className='wrap-text summary'
                                                        >
                                                            <Icon
                                                                name={this.getDirectionIcon(message.message.record.logger,
                                                                    message.message.meta.direction)}
                                                                title={message.message.meta.direction}
                                                            />
                                                        </Grid.Column>
                                                        <Grid.Column className='wrap-text activity'>
                                                            {message.message.meta.id}
                                                        </Grid.Column>
                                                        <Grid.Column className='wrap-text time'>
                                                            {timeString}
                                                        </Grid.Column>
                                                        <Grid.Column className='wrap-text path'>
                                                            {message.message.meta.httpMethod}
                                                            &nbsp;
                                                            {message.message.meta.path}
                                                        </Grid.Column>
                                                    </Grid.Row>
                                                );
                                            })}
                                        </Grid>
                                    </div>
                                    {details &&
                                        <div width={12} style={{ height, overflow: 'auto' }}>
                                            <DetailView
                                                rawLog={details.message.record}
                                                meta={details.message.meta}
                                                hideDetailView={() => { this.setState({ details: null }); }}
                                            />
                                        </div>
                                    }
                                </SplitPane>
                            </div>
                        }
                    </div>
                </ErrorBoundary>
            </div>
        );
    }
}

LogsConsole.propTypes = {
    height: Proptypes.number,
};

LogsConsole.defaultProps = {
    height: 0,
};

export default LogsConsole;
