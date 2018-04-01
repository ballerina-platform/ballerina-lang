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
import { Grid, Icon } from 'semantic-ui-react';
import SplitPane from 'react-split-pane';
import ToolBar from './Toolbar';
import DetailView from './DetailView';
import './index.scss';

const loggerToIcon = {
    'tracelog.http.downstream': 'fw fw-down-arrow',
    'tracelog.http.upstream': 'fw fw-up-arrow',
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
    constructor() {
        super();
        const messages = [
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336393",
                        "sequence": "1",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f] REGISTERED"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "a4d56efe-ccab-4598-9efe-b4976686b013"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336394",
                        "sequence": "2",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] ACTIVE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "98cfe3ec-77ad-4298-9a2b-fe173164e8c0"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336431",
                        "sequence": "3",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1) , GET /passthru HTTP/1.1 , Host: localhost:9090 , Connection: keep-alive , Cache-Control: no-cache , User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36 , Postman-Token: ffa3dc65-4aa5-aa9a-13b1-5597b99d82ba , Accept: */* , Accept-Encoding: gzip, deflate, br , Accept-Language: en-US,en;q=0.9,si;q=0.8"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "6853053d-ab48-41f2-ab7e-59af7b02f0c4"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336458",
                        "sequence": "4",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] INBOUND: EmptyLastHttpContent, 0B"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "cca11237-a02c-40ed-a495-17caaef31b50"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336459",
                        "sequence": "5",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] READ COMPLETE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "f8ca6aa0-b5b3-4c2a-97f2-f6de17a721ee"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336594",
                        "sequence": "6",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e] REGISTERED"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "603b3774-cf67-4e80-a7b4-410cc21c83e3"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336596",
                        "sequence": "7",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e] CONNECT: localhost/127.0.0.1:8080, null"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "d8cb7695-4196-4744-b42e-15e52e1a20e0"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336606",
                        "sequence": "8",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] ACTIVE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "1143a5fe-276d-4616-9233-4edf2c99b803"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336608",
                        "sequence": "9",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] OUTBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1) , GET /hello HTTP/1.1 , Cache-Control: no-cache , Postman-Token: ffa3dc65-4aa5-aa9a-13b1-5597b99d82ba , Accept: */* , Accept-Encoding: gzip, deflate, br , Accept-Language: en-US,en;q=0.9,si;q=0.8 , host: localhost:8080 , user-agent: ballerina/0.970.0-alpha1-SNAPSHOT"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "41879197-d27d-408b-bd51-caaf0b96c4d0"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336610",
                        "sequence": "10",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] OUTBOUND: EmptyLastHttpContent, 0B"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "e5ba935d-2b3a-479e-8a0c-79dd0b3ea648"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336610",
                        "sequence": "11",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] FLUSH"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "29b4a120-f540-440e-b86a-3a503a50c090"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336613",
                        "sequence": "12",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] INBOUND: DefaultHttpResponse(decodeResult: success, version: HTTP/1.1) , HTTP/1.1 404 Not Found , X-Powered-By: Express , Content-Security-Policy: default-src 'self' , X-Content-Type-Options: nosniff , Content-Type: text/html; charset=utf-8 , Content-Length: 144 , Date: Wed, 28 Mar 2018 06:08:56 GMT , Connection: keep-alive"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "cbe3c8e6-6221-4a04-b7b7-f943fc560880"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336803",
                        "sequence": "13",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] INBOUND: DefaultLastHttpContent(data: PooledSlicedByteBuf(ridx: 0, widx: 144, cap: 144/144, unwrapped: PooledUnsafeDirectByteBuf(ridx: 393, widx: 393, cap: 1024)), decoderResult: success), 144B , <!DOCTYPE html> , <html lang=\"en\"> , <head> , <meta charset=\"utf-8\"> , <title>Error</title> , </head> , <body> , <pre>Cannot GET /hello</pre> , </body> , </html> , "
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "3c59c7c7-2d09-4a79-8498-f50a5b7bffc5"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336804",
                        "sequence": "14",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] READ COMPLETE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "b45b61f4-9135-4f06-a661-0dcefba2b0ba"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336814",
                        "sequence": "15",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 144, cap: 144, components=1)) , HTTP/1.1 404 Not Found , X-Powered-By: Express , Content-Security-Policy: default-src 'self' , X-Content-Type-Options: nosniff , Content-Type: text/html; charset=utf-8 , Date: Wed, 28 Mar 2018 06:08:56 GMT , content-length: 144 , server: ballerina/0.970.0-alpha1-SNAPSHOT, 144B , <!DOCTYPE html> , <html lang=\"en\"> , <head> , <meta charset=\"utf-8\"> , <title>Error</title> , </head> , <body> , <pre>Cannot GET /hello</pre> , </body> , </html> , "
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "f5278db1-ddc8-4807-b1cc-1c3b5d573480"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:38Z",
                        "millis": "1522217336817",
                        "sequence": "16",
                        "logger": "tracelog.http.downstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x6a83720f, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:48678] FLUSH"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "aee89cab-01d0-4ee1-acc2-e1bd6a43ada7"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:39Z",
                        "millis": "1522217341613",
                        "sequence": "17",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] READ COMPLETE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "9406a7e4-50da-4738-892b-639e3618d18d"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:39Z",
                        "millis": "1522217341614",
                        "sequence": "18",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] INACTIVE"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "72e71b45-b037-4691-86d2-cdbb82dcbde6"
            },
            {
                "code": "OUTPUT",
                "message": {
                    "record": {
                        "logDate": "2018-03-28T11:39Z",
                        "millis": "1522217341614",
                        "sequence": "19",
                        "logger": "tracelog.http.upstream",
                        "level": "FINEST",
                        "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
                        "sourceMethod": "trace",
                        "thread": "14",
                        "message": "[id: 0x73495d7e, correlatedSource: 0x6a83720f, host:/127.0.0.1:50516 - remote:localhost/127.0.0.1:8080] UNREGISTERED"
                    }
                },
                "type": "TRACE",
                "port": 0,
                "id": "3900d326-90d4-40be-9a33-6874cbac3818"
            }
        ];
        this.state = {
            messages: messages,
            filteredMessages: messages,
            details: false,
        };
        this.onFilteredMessages = this.onFilteredMessages.bind(this);
        this.debouncedSetState = _.debounce(nextState => this.setState({ messages: nextState }), 400);
        this.toggleDetails = this.toggleDetails.bind(this);
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
                filteredMessages: [],
            });
        });
        this.props.LaunchManager.on('print-trace-message', (message) => {
            // const newMessage = messageCache.map((msg) => {
            //     message.timestamp = moment(msg.timestamp).unix();
            //     message.dateString = moment(msg.timestamp).toString();
            //     return msg;
            // }).sort((msg1, msg2) => {
            //     return msg1.timestamp - msg2.timestamp;
            // });

            // this.debouncedSetState(newMessage);
            this.setState({
                messages: [...this.state.messages, message],
                filteredMessages: [...this.state.messages, message],
            });
        });
    }

    onFilteredMessages(filteredMessages) {
        this.setState({
            filteredMessages,
        });
    }

    toggleDetails(message) {
        this.setState({
            details: message,
        });
    }

    getLoggercon(logger) {
        return loggerToIcon[logger];
    }

    /**
     * @inheritdoc
     */
    render() {
        const { height } = this.props;
        const { details } = this.state;
        return (
            <div id='logs-console' ref={(stickyContext) => { this.stickyContext = stickyContext; }} style={{ height }}>
                {
                    this.state.messages.length > 0 &&
                    <div>
                        <ToolBar
                            messages={this.state.messages}
                            filters={{ id: 'Activity Id', 'message.record.logger': 'Logger' }}
                            onFilteredMessages={this.onFilteredMessages}
                        />
                        <div >
                        {/* allowResize={details ? true : false} */}
                            <SplitPane 
                                split="vertical"
                                size={details ? '25%' : '100%'} 
                                allowResize={details? true: false}
                            >
                                    <div>
                                        <Grid style={{ margin: 0 }}>
                                            <Grid.Row className='table-heading'>
                                                <Grid.Column width={details ? 16 : 4}>
                                                    Activity Id
                                                </Grid.Column>
                                                {!details && [
                                                    <Grid.Column width={4}>
                                                        Time
                                                    </Grid.Column>,
                                                    <Grid.Column width={4}>
                                                        Stream
                                                    </Grid.Column>,
                                                    <Grid.Column width={4}>
                                                        Headers
                                                    </Grid.Column>
                                                ]}

                                            </Grid.Row>
                                        </Grid>
                                        <Grid
                                            className='table-content'
                                            style={{ maxHeight: height }}
                                        >
                                            {this.state.filteredMessages.map((message) => {
                                                
                                                const timeString =  moment(parseInt(message.message.record.millis)).format('HH:mm:ss.SSS');
                                                return (
                                                    <Grid.Row className={(details && details.id === message.id ) ? 'active': ''}>
                                                        <Grid.Column 
                                                            width={details ? 16 : 4} 
                                                            onClick={() => this.toggleDetails(message)}
                                                            className='wrap-text clickable'
                                                        >
                                                            <Icon 
                                                                name={this.getLoggercon(message.message.record.logger)} 
                                                            />
                                                            {message.id}
                                                        </Grid.Column>
                                                        {!details && [
                                                            <Grid.Column width={4} className='wrap-text'>
                                                                {timeString}
                                                            </Grid.Column>,
                                                            <Grid.Column width={4} className='wrap-text'>
                                                                {message.message.record.logger}
                                                            </Grid.Column>,
                                                            <Grid.Column width={4} className='wrap-text'>
                                                                {message.message.record.message}
                                                            </Grid.Column>,
                                                        ]}

                                                    </Grid.Row>
                                                );
                                            })}
                                        </Grid>
                                    </div>
                                    {details &&
                                        <div width={12} style={{ height, overflow: 'auto' }}>
                                            <DetailView 
                                                details={details.message.record.message} 
                                                hideDetailView={() => { this.setState({ details: null }) }}
                                            />
                                        </div>
                                    }
                             </SplitPane>
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
