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
import proptypes from 'prop-types';
import { Table } from 'semantic-ui-react';

import OasParameter from './parameter'

/* eslint-disable */
class OasParameters extends React.Component {
    constructor(props){
        super(props);
    }
    render(){
        const { parameterObj, paramType } = this.props;
        return (
            <Table celled>
                <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>Name</Table.HeaderCell>
                    <Table.HeaderCell>Description</Table.HeaderCell>
                </Table.Row>
                </Table.Header>
                <Table.Body>
                    {parameterObj && Object.keys(parameterObj).map((param)=>{
                        return (
                            <OasParameter responseCode={paramType === 'response' ? param : ''} param={parameterObj[param]} />
                        )  
                    })}
                </Table.Body>
            </Table>
        )
    }
}

OasParameters.proptypes = {
    parameterObj : proptypes.object.isRequired,
    paramType: proptypes.string.isRequired
}

export default OasParameters;