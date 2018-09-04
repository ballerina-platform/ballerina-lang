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