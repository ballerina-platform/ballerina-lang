/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Description from './Description';
import { Table } from 'semantic-ui-react';

const Documentation = ({ docDetails }) => {
    const { title, kind, description, parameters, returnParameter, valueType } = docDetails;
    const paramNames = Object.keys(parameters);

    const icon = {
        'Function': 'fw-function',
        'Service': 'fw-service',
        'ObjectType': 'fw-struct',
        'RecordType': 'fw-record'
    }[kind];

    return (
        <div className='documentation-block' id={title}>
            <div className='title'>
                { icon && (<i className={`fw fw-fw icon ${icon}`}></i>) }{title}
                { kind === 'Function' && '()' }
                { valueType !== '' &&
                    (<span className='value-type'>[<span className='type'>{valueType}</span>]</span>)
                }
                { kind === 'Service' && (<span className='service-type'><span className='type'>service</span></span>)}
                { kind === 'ObjectType' && (<span className='object-type'>{'{'}<span className='type'>object</span>{'}'}</span>)}
            </div>
            <Description source={description} className='description' />
            {returnParameter && returnParameter.type !== 'nil' && (
                <div className='return-details'>
                    <div>----------------------------------</div>
                    {returnParameter.type && <div><strong>return:</strong> <span className='type'>{returnParameter.type}</span></div>}
                    <div className='return-description'>{<Description source={returnParameter.description || ''} />}</div>
                </div>
            )}
            {paramNames.length > 0 && (
                <Table celled compact className='parameters'>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell>Parameter Name</Table.HeaderCell>
                            <Table.HeaderCell>Data Type</Table.HeaderCell>
                            <Table.HeaderCell>Default Value</Table.HeaderCell>
                            <Table.HeaderCell>Description</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {
                            paramNames.map((param) => {
                                const { name, type, defaultValue, description } = parameters[param];
                                return (
                                    <Table.Row>
                                        <Table.Cell>
                                            <span className='stacked-content'>
                                                <strong className='label'>Parameter Name:</strong>
                                            </span>
                                            {name}
                                        </Table.Cell>
                                        <Table.Cell>
                                            <span className='stacked-content'>
                                                <strong className='label'>Data Type:</strong>
                                            </span>
                                            <span className='type'>{type}</span>
                                        </Table.Cell>
                                        <Table.Cell>
                                            {defaultValue && (
                                                <div>
                                                    <span className='stacked-content'>
                                                        <strong className='label'>Default Value:</strong>
                                                    </span> {defaultValue}
                                                </div>
                                            )}
                                        </Table.Cell>
                                        <Table.Cell>
                                            <span className='stacked-content'>
                                                <strong className='label'>Description:</strong>
                                            </span>
                                            {<Description source={description} />}
                                        </Table.Cell>
                                    </Table.Row>
                                );
                            })
                        }
                    </Table.Body>
                </Table>
            )}
        </div>
    );
};

export default Documentation;
