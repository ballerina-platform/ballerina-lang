import React from 'react';
import Description from './Description';
import { Table } from 'semantic-ui-react';
import './Documentation.css';

const Documentation = ({ docDetails }) => {
    const { title, description, parameters, returnParameter } = docDetails;
    return (
        <div className='documentation'>
            <div className='title'><i className='fw fw-function fw-fw icon'></i>{title}()</div>
            <Description source={description} className='description' />
            {returnParameter && returnParameter.type !== 'nil' && (
                <div className='return-details'>
                    <div><strong>return:</strong> <span className='type'>{returnParameter.type}</span></div> 
                    <div className='return-description'>{<Description source={returnParameter.description || ''} />}</div>
                </div>
            )}
            {parameters.length > 0 && (
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
                            parameters.map((param) => {
                                const { name, type, defaultValue, description } = param;
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