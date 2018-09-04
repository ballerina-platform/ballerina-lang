import React from 'react';
import PropTypes from 'prop-types';
import { Form, Button } from 'semantic-ui-react';

/**
 * Component for add resource feature
 */
/* eslint-disable */
class OasAddResource extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            resource: ''
        }
    }

    render() {
        const { onAddResource } = this.props;
        return (
            <Form size='mini' className='add-resource'>
                <Form.Field>
                    <label>Resource Name</label>
                    <input placeholder='Example: /users/{userId}' onChange={(e) => this.setState({ 
                        resource: e.target.value
                    })} />
                </Form.Field>
                <Button size='tiny' onClick={()=>{onAddResource(this.state)}}>Save</Button>
            </Form>
        )
    }
}

OasAddResource.propTypes = {
    onAddResource: PropTypes.func,
}

OasAddResource.defaultProps = {
    onAddResource: () => {},
}

export default OasAddResource;
