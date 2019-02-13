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
import { Grid, List, Table } from 'semantic-ui-react';

const Documentation = ({ docDetails }) => {
    const { title, kind, description, parameters, returnParameter, valueType } = docDetails;
    const paramNames = Object.keys(parameters);

    const icon = {
        'Function': 'fw-function',
        'Service': 'fw-service',
        'ObjectType': 'fw-object',
        'RecordType': 'fw-record'
    }[kind];

    const renderfunctionHeader = (name, parameters) => {
        const params = parameters.map((param) => {
            return param.name;
        });

        return (
            <div className='title'>
                {name}
                ( <span className='parameters'>{ params.toString().replace(',', ', ') }</span> )
            </div>
        );
    }

    const renderReturnParameters = () => {
        return (
            <div>
                {returnParameter && returnParameter.type !== 'nil' && (
                    <div className='return-details'>
                        { returnParameter.type && 
                            <span>
                                <strong>return:</strong> <span className='type'>{returnParameter.type}</span>
                            </span>
                        }
                        { returnParameter.description && 
                            <span className='return-description inline-paragraph'>
                                ~ { <Description source={returnParameter.description || ''} /> }
                            </span>
                        }
                    </div>
                )}
            </div>
        );
    }

    const renderParameters = (name, type, defaultValue, description) => {
        return (
            <Grid.Row>
                <Grid.Column mobile={16} tablet={3} computer={3}>
                    <span className='parameter'>{name} </span>:
                </Grid.Column>
                <Grid.Column mobile={16} tablet={13} computer={13}>
                    <div><span className='type'>{type.trim()}</span>
                        { description && (
                            <span className='inline-paragraph'> ~ {<Description source={description} />}</span>
                        )}
                    </div>
                    { defaultValue && (<div>Default Value : {defaultValue}</div>)}
                </Grid.Column>
            </Grid.Row>
        )
    };

    return (
        <List.Item className='documentation-block' id={title}>
            { icon && (<i className={`fw fw-fw icon ${icon}`}></i>) }
            <List.Content>
                <List.Header>
                    { kind === 'Function' && renderfunctionHeader(title, Object.entries(parameters).map(
                            function(entry) {
                                return entry[1];
                            })
                        )
                    }
                    { kind !== 'Function' && (
                        <div className='title'>
                            {title}
                            { valueType !== '' &&
                                (<span className='value-type'>[<span className='type'>{valueType}</span>]</span>)
                            }
                            { kind === 'Service' && (
                                <span className='service-type'><span className='type'>service</span></span>
                            )}
                            { kind === 'ObjectType' && (
                                <span className='object-type'>{'{'}<span className='type'>object</span>{'}'}</span>
                            )}
                        </div>
                    )}
                </List.Header>
                <List.Description>
                    <Description source={description} className='description' />
                    { renderReturnParameters() }
                    {(kind !== 'RecordType' && kind !== 'Function' && paramNames.length > 0) && (
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

                                        return type !== 'Function' && (
                                            <Table.Row>
                                                <Table.Cell>
                                                    <span className='stacked-content'>
                                                        <strong className='label'>Parameter Name :</strong>
                                                    </span>
                                                    <span className='parameter'>{name}</span>
                                                </Table.Cell>
                                                <Table.Cell>
                                                    <span className='stacked-content'>
                                                        <strong className='label'>Data Type :</strong>
                                                    </span>
                                                    <span className='type'>{type.trim()}</span>
                                                </Table.Cell>
                                                <Table.Cell>
                                                    {defaultValue && (
                                                        <div>
                                                            <span className='stacked-content'>
                                                                <strong className='label'>Default Value :</strong>
                                                            </span> {defaultValue}
                                                        </div>
                                                    )}
                                                </Table.Cell>
                                                <Table.Cell>
                                                    <span className='stacked-content'>
                                                        <strong className='label'>Description :</strong>
                                                    </span>
                                                    {<Description source={description} />}
                                                </Table.Cell>
                                            </Table.Row>
                                        )
                                    })
                                }
                            </Table.Body>
                        </Table>
                    )}
                    {(kind === 'RecordType' && paramNames.length > 0) && (
                        <Grid className='params-list'>
                            {
                                paramNames.map((param) => {
                                    const { name, type, defaultValue, description } = parameters[param];
                                    return renderParameters(name, type, defaultValue, description);
                                })
                            }
                        </Grid>
                    )}
                    {(kind === 'Function' && paramNames.length > 0) && (
                        <Grid className='params-list'>
                            {
                                paramNames.map((param) => {
                                    const { name, type, description } = parameters[param];
                                    return renderParameters(name, type, '', description);
                                })
                            }
                        </Grid>
                    )}
                </List.Description>
                {(kind == 'ObjectType' && paramNames.length > 0) &&
                    paramNames.map((param) => {
                        const { name, type, functionParameters, description } = parameters[param];

                        return type == 'Function' && (
                            <List.List>
                                <List.Item id={name}>
                                    <i className='fw fw-fw icon fw-function'></i>
                                    <List.Content>
                                        <List.Header>
                                            { renderfunctionHeader(name, functionParameters) }
                                        </List.Header>
                                        <List.Description>
                                            <Description source={description} />
                                            {functionParameters.length > 0 && (
                                                <Grid className='params-list'>
                                                    {
                                                        functionParameters.map((param) => {
                                                            const { name, type, description } = param;
                                                            return renderParameters(name, type, '', description)
                                                        })
                                                    }
                                                </Grid>
                                            )}
                                        </List.Description>
                                    </List.Content>
                                </List.Item>
                            </List.List>
                        )
                    }) 
                }
            </List.Content>
        </List.Item>
    );
};

export default Documentation;
