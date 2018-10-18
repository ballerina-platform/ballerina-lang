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

import * as React from "react";
import * as _ from 'lodash';
import moment from 'moment';
import { Grid, Icon } from 'semantic-ui-react';
import './index.scss';

const directionToIcon = {
    INBOUND: {
        'http.tracelog.downstream': 'fw fw-downstream-inbound direction-icon inbound downstream',
        'http.tracelog.upstream': 'fw fw-upstream-inbound direction-icon inbound upstream',
    },
    OUTBOUND: {
        'http.tracelog.downstream': 'fw fw-downstream-outbound direction-icon outbound downstream',
        'http.tracelog.upstream': 'fw fw-upstream-outbound direction-icon outbound upstream',
    },
};

export interface TraceListState {
    selected: string | undefined
}

export interface TraceListProps {
    traces: Array<any>;
    selected: string | undefined;
}

/**
 *
 * @extends React.Component
 */
class TraceList extends React.Component<TraceListProps, TraceListState> {
    // static defaultProps = {
    //     traces: []
    // }
    constructor(props: any, context: any) {
        super(props);
        this.state = {
            selected: undefined,
        };
        this.toggleDetails = this.toggleDetails.bind(this);
    }

    getDirectionIcon(logger:string, direction: string) {
        directionToIcon[direction] = directionToIcon[direction] || {};
        return directionToIcon[direction][logger];
    }

    toggleDetails(id: string) {
        if (id === this.state.selected){
            this.setState({
                selected: undefined,
            });
        } else {
            this.setState({
                selected: id,
            });
        }

    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div id='logs-console'>
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
                >
                    {this.props.traces.map((record: any) => {
                        const timeString = moment(parseInt(record.millis)).format('HH:mm:ss.SSS');
                        return (
                            <Grid.Row
                                className={ this.props.selected === record.id ? 'active clickable' : 'clickable'}
                                key={record.id}
                                // onClick={() => this.props.onToggleDetails(record.id)}
                            >
                                <Grid.Column
                                    className='wrap-text summary'
                                >
                                    <Icon
                                        name={this.getDirectionIcon(record.logger,
                                            record.message.direction)}
                                        title={record.message.direction}
                                    />
                                </Grid.Column>
                                <Grid.Column className='wrap-text activity'>
                                    {record.message.id}
                                </Grid.Column>
                                <Grid.Column className='wrap-text time'>
                                    {timeString}
                                </Grid.Column>
                                <Grid.Column className='wrap-text path'>
                                    {record.message.httpMethod}
                                    &nbsp;
                                    {record.message.path}
                                </Grid.Column>
                            </Grid.Row>
                        );
                    })}
                </Grid>
            </div>
        );
    }
}

export default TraceList;
