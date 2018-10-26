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
                            <Table.HeaderCell singleLine>Parameter Name</Table.HeaderCell>
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
                                        <Table.Cell>{name}</Table.Cell>
                                        <Table.Cell className='type'>{type}</Table.Cell>
                                        <Table.Cell>{defaultValue}</Table.Cell>
                                        <Table.Cell>{<Description source={description} />}</Table.Cell>
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