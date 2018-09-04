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
