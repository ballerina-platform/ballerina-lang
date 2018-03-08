/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the 'License"); you may not use this file except
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
 */

import React from 'react';
import { Button, Input, Icon, Form } from 'semantic-ui-react';
import PropTypes from 'prop-types';

class RequestHeaderItem extends React.Component {
    constructor() {
        super();
        this.onSubmit = this.onSubmit.bind(this);
        this.valueChange = this.valueChange.bind(this);
        this.keyChange = this.keyChange.bind(this);
        this.delete = this.delete.bind(this);
    }
    onSubmit(_, event) {
        // add new item
        this.props.onAddNew();
    }
    keyChange(event, data) {
        this.props.onChange({
            ...this.props.header,
            key: data.value,
        });
    }
    valueChange(event, data) {
        this.props.onChange({
            ...this.props.header,
            value: data.value,
        });
    }
    delete(event, data) {
        console.log(event, data);
        this.props.onDelete();
    }
    render() {
        const { header } = this.props;
        return (
            <Form
                inverted
                onSubmit={this.onSubmit}
            >
                <Form.Group inline>
                    <Form.Field width={7}>
                        <Input
                            placeholder='Key'
                            type='text'
                            className='header-input form-control'
                            value={header.key}
                            onChange={this.keyChange}
                        />
                    </Form.Field>

                    <Form.Field width={7}>
                        <Input
                            placeholder='Value'
                            type='text'
                            className='header-input form-control'
                            value={header.value}
                            onChange={this.valueChange}
                        />
                    </Form.Field>
                    <Form.Field width={2}>
                        <Button
                            type='submit'
                            style={{ display: 'none' }}
                            compact
                        />
                        <Form.Button
                            onClick={this.delete}
                        >
                            <Icon name='trash' style={{ margin: 0 }} />
                        </Form.Button>
                    </Form.Field>
                </Form.Group>
            </Form >);
    }

}

RequestHeaderItem.propTypes = {
    header: PropTypes.shape({
        key: PropTypes.string,
        id: PropTypes.string,
        value: PropTypes.string,
    }).isRequired,
    onChange: PropTypes.func.isRequired,
    onDelete: PropTypes.func.isRequired,
    onAddNew: PropTypes.func.isRequired,
};

export default RequestHeaderItem;
