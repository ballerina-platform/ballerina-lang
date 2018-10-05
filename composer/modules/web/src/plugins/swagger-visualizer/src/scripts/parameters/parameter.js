/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Table } from 'semantic-ui-react';

/* eslint-disable */
class OasParameter extends React.Component {
    constructor(props){
        super(props);
    }
    render() {
        const { param, responseCode } = this.props;
        return (
            <Table.Row>
                <Table.Cell className='parameter-name-cell'>
                    <div className='parameter__name required'>
                        {responseCode === '' ? param.name : responseCode }
                    </div>
                    <div className='parameter__type'>
                        {param.type}
                    </div>
                </Table.Cell>
                <Table.Cell className='parameter-desc-cell'>
                    <div className='markdown'>
                        {param.description}
                    </div>
                </Table.Cell>
            </Table.Row>
        );
    }
}

OasParameter.proptypes = {
    param: PropTypes.object.isRequired,
    responseCode: PropTypes.string.isRequired
}

export default OasParameter;