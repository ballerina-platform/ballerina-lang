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