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
import propTypes from 'prop-types';
import { Form, Button, Select } from 'semantic-ui-react';

/**
 * Component for add operation feature
 */
/* eslint-disable */
class OasAddOperation extends React.Component {

    constructor(props) {
        super(props);

        this.state ={
            operationForm : {
                id: '',
                name: '',
                description: '',
                method: '',
                path: props.path,
            },
            methodOpts: [],
            pathOpts: [],
        }

        this.getMethods();
    }

    getMethods() {
        const { operationForm: { path } } = this.state;
        const { oasJson } = this.props;
        let methodOpts = [];

        let availableMethods = ['GET','POST','PUT','DELETE','PATCH','HEAD','OPTIONS'].filter(method => {
            return !Object.keys(oasJson.paths[path]).includes(method.toLowerCase())
        })

        availableMethods.forEach((method)=>{
            methodOpts.push({
                key: method.toLowerCase(),
                text: method,
                value: method.toLowerCase(),
            })
        });

        this.state.methodOpts = methodOpts;
    }

    render() {
        const { onAddOperation } = this.props;
        return (
            <Form size='mini' className='add-operation'>
                <Form.Field>
                    <label>Name</label>
                    <input placeholder='Short Summery' onChange={(e) => this.setState({ 
                        operationForm: {
                            ...this.state.operationForm,
                            name: e.target.value,
                            id: e.target.value.replace(' ','')
                        }
                    })} />
                </Form.Field>
                <Form.Field>
                    <Form.Field control={Select} label='Method' options={this.state.methodOpts} placeholder='Method' onChange={(e, data) =>{this.setState({
                            operationForm: {
                                ...this.state.operationForm,
                                method: data.value
                            }
                        })
                    }} />
                </Form.Field>
                <Form.Field>
                    <Form.TextArea label='Description' placeholder='Tell us more about...' onChange={(e) => this.setState({ 
                        operationForm: {
                            ...this.state.operationForm,
                            description: e.target.value
                        }
                    })} />
                </Form.Field>
                <Button size='mini' onClick={() => {
                    onAddOperation(this.state.operationForm)
                }}>Save</Button>
            </Form>
        );
    }
}

OasAddOperation.prototypes = {
    onAddOperation: propTypes.func,
    path: propTypes.string,
}

export default OasAddOperation;
