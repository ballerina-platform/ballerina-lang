/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
import * as React from "react";
import * as _ from 'lodash';
import { Dropdown, Button } from 'semantic-ui-react';

export interface ToolBarState {
    filterValue: any,
}

export interface ToolBarProps {
    traces: Array<any>
    onFilteredTraces: Function,
    filters: any,
    clearLogs: Function,
}

/**
 *
 * @extends React.Component
 */
export default class ToolBar extends React.Component<ToolBarProps, ToolBarState> {
    constructor(props: ToolBarProps, context: ToolBarState) {
        super(props);
        _.forEach(props.filters, (label: string, filter: string) => {
            this.state = {
                filterValue: {},
            };
            this.state.filterValue[filter] = 'all';
        });
    }

    onChangeFilter(key: string, filterBy: any) {
        this.setState({
            filterValue: {
                ...this.state.filterValue,
                [key]: filterBy,
            },
        });

        setTimeout(() => {
            const filtered = this.props.traces.filter((item) => {
                let isIncluded = true;
                _.forEach(this.state.filterValue, (value, k) => {
                    if (value === 'all') {
                        isIncluded = isIncluded && true;
                    } else {
                        isIncluded = isIncluded && (_.get(item, k) === value);
                    }
                });
                return isIncluded;
            });
            this.props.onFilteredTraces(filtered);
        }, 0);
    }
    /**
     * @inheritdoc
     */
    render() {
        const { filters, traces } = this.props;
        const keys = _.keys(filters) || [];
        const groupedTraces = {};

        traces.forEach((message) => {
            keys.forEach((key) => {
                groupedTraces[key] = groupedTraces[key] || [];
                if (groupedTraces[key].indexOf(_.get(message, key)) === -1) {
                    groupedTraces[key].push(_.get(message, key));
                }
            });
        });

        return (
            <div className='logs-console-toolbar'>
                <Button
                    icon='trash alternate outline'
                    className='pull-left clear-button'
                    onClick={() => this.props.clearLogs()}
                />
                {
                    keys.map((key) => {
                        groupedTraces[key] = groupedTraces[key] || [];
                        const options = groupedTraces[key].map((option: string) => {
                            return {
                                key: option,
                                text: option,
                                value: option,
                            };
                        });
                        options.unshift({
                            key: 'all',
                            text: 'All',
                            value: 'all',
                        });
                        return (
                            <span className={`filter-${key} pull-right`} key={key}>
                                <label htmlFor='dropdown'>{filters[key]}</label>
                                <Dropdown
                                    direction='left'
                                    value={this.state.filterValue[key]}
                                    options={options}
                                    onChange={(e, data) => this.onChangeFilter(key, data.value)}
                                />
                            </span>);
                    })
                }

            </div>
        );
    }
}