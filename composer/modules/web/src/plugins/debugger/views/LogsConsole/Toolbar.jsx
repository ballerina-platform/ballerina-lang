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

/**
 *
 * @extends React.Component
 */
class ToolBar extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className='logs-console-toolbar'>
                <div className='filter-service pull-right'>
                    <label>Service</label>
                    <select title='Dropdown' bsStyle='default' bsSize='small'>
                        <option>All</option>
                        <option>service 1</option>
                        <option>service 2</option>
                    </select>
                </div>
                <div className='filter-service pull-right'>
                    <label>Channel</label>
                    <select title='Dropdown' bsStyle='default' bsSize='small'>
                        <option>All</option>
                        <option>Inbound</option>
                        <option>OutBound</option>
                    </select>
                </div>
                <div className='filter-service pull-right'>
                    <label>Activity Id</label>
                    <select title='Dropdown' bsStyle='default' bsSize='small'>
                        <option>All</option>
                        <option>0x163f1842</option>
                        <option>0x8fa952f4</option>
                    </select>
                </div>
            </div>
        );
    }
}

ToolBar.propTypes = {

};

export default ToolBar;
