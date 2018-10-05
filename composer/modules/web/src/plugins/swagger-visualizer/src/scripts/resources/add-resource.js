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
