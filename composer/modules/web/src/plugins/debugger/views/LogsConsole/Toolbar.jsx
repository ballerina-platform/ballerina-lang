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
import { Dropdown, Menu, Button } from 'semantic-ui-react';
/**
 *
 * @extends React.Component
 */
class ToolBar extends React.Component {

    onChangeFilter(key, filterBy) {
        console.log(key, filterBy);
        const filtered = this.props.messages.filter((item) => {
            if (filterBy === 'all') {
                return true;
            }
            return item[key] === filterBy;
        });
        this.props.onFilteredMessages(filtered);
    }

    /**
     * @inheritdoc
     */
    render() {
        const { filters, messages } = this.props;
        console.log(filters)
        const keys = _.keys(filters);
        const groupedMessages = {};
        messages.forEach((message) => {
            keys.forEach((key) => {
                groupedMessages[key] = groupedMessages[key] || [];
                if (groupedMessages[key].indexOf(message[key]) === -1) {
                    groupedMessages[key].push(message[key]);
                }
            });
        });

        return (
            <div className='logs-console-toolbar'>
                {
                    keys.map((key) => {
                        const options = groupedMessages[key].map((option) => {
                            return {
                                key: option,
                                text: option,
                                value: option,
                            }
                        });
                        options.unshift({
                            key: 'all',
                            text: 'All',
                            value: 'all',
                        });
                        return (
                            <div className={`filter-${key} pull-right`}>
                                <label>{filters[key]}</label>
                                <Button size='tiny' >
                                    <Dropdown
                                        placeholder='All'
                                        options={options}
                                        onChange={(e, data) => this.onChangeFilter(key, data.value)}
                                    />
                                </Button>
                            </div>);
                    })
                }

            </div>
        );
    }
}

ToolBar.propTypes = {

};

export default ToolBar;
