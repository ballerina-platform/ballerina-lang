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

import React from 'react';
import _ from 'lodash';
import { Dropdown, Button } from 'semantic-ui-react';
/**
 *
 * @extends React.Component
 */
class ToolBar extends React.Component {

    constructor(props) {
        super(props);
        _.forEach(props.filters, (label, filter) => {
            this.state = {
                filterValue: {},
            };
            this.state.filterValue[filter] = 'all';
        });
    }

    onChangeFilter(key, filterBy) {
        this.setState({
            filterValue: {
                ...this.state.filterValue,
                [key]: filterBy,
            },
        });

        setTimeout(() => {
            const filtered = this.props.messages.filter((item) => {
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

            this.props.onFilteredMessages(filtered);
        }, 0);
    }
    /**
     * @inheritdoc
     */
    render() {
        const { filters, messages } = this.props;
        const keys = _.keys(filters) || [];
        const groupedMessages = {};
        messages.forEach((message) => {
            keys.forEach((key) => {
                groupedMessages[key] = groupedMessages[key] || [];
                if (groupedMessages[key].indexOf(_.get(message, key)) === -1) {
                    groupedMessages[key].push(_.get(message, key));
                }
            });
        });

        return (
            <div className='logs-console-toolbar'>
                <div>
                    <Button
                        icon='fw fw-clear'
                        className='pull-left clear-button'
                        onClick={() => this.props.clearLogs()}
                    />
                </div>
                {
                    keys.map((key) => {
                        groupedMessages[key] = groupedMessages[key] || [];
                        const options = groupedMessages[key].map((option) => {
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
                            <div className={`filter-${key} pull-right`}>
                                <label htmlFor='dropdown'>{filters[key]}</label>
                                <Dropdown
                                    direction='left'
                                    value={this.state.filterValue[key]}
                                    options={options}
                                    onChange={(e, data) => this.onChangeFilter(key, data.value)}
                                />
                            </div>);
                    })
                }

            </div>
        );
    }
}

ToolBar.propTypes = {

};

ToolBar.defaultProps = {
    messages: [],
    filters: {},
};

export default ToolBar;
