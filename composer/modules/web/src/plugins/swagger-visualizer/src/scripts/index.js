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
import SwaggerValidator from 'swagger-parser';
import { Message } from 'semantic-ui-react';

import OasResources from './resources/resources';
import SwaggerAppContext from '../context/app-context';

import 'semantic-ui-css/semantic.min.css';

const EVENTS = {
    "ADD_RESOURCE": "add-resource",
    "ADD_OPERATION": "add-operation",
}


/**
 * A Component to visualize a swagger json
 */
/* eslint-disable */
class SwaggerVisualizer extends React.Component {
    /**
     * Constructor
     * @param {Object} props props object
     */
    constructor(props) {
        super(props);

        this.state = {
            oasJson: {},
            isError: {
                state: false,
                message: '',
            },
            actionState: {
                state: '',
                message: ''
            }
        };

        this.onAddOperation = this.onAddOperation.bind(this);
        this.onAddParameter = this.onAddParameter.bind(this);
        this.onAddResource = this.onAddResource.bind(this);

        this.onDeleteOperation = this.onDeleteOperation.bind(this);
    }

    /**
     * Component did mount life cycle
     * @returns {Object} Error object if any
     */
    componentDidMount() {
        const { oasJson } = this.props;

        if (!oasJson) {
            this.setState({
                isError: {
                    state: true,
                    message: 'Open API Specification compliant JSON obejct is required.',
                },
            });
            return false;
        }

        SwaggerValidator.validate(oasJson)
            .then((json) => {
                this.setState({
                    oasJson: json,
                });
            })
            .catch((error) => {
                this.setState({
                    isError: {
                        state: true,
                        message: error.message,
                    },
                });
            });

        return null;
    }

    onAddParameter(){

    }

    onAddResource(resourceObj){
        const { onAddResource, onDidChange } = this.props;
        const resource = resourceObj.resource.replace(' ','');

        this.setState(prevState => ({
            ...prevState,
            oasJson: {
                ...prevState.oasJson,
                paths: {
                    ...prevState.oasJson.paths,
                    ['/' + resource]: {}
                }
            }
        }),()=>{
            if(this.state.oasJson.paths['/' + resource]){
                
                //Exposed event for post add resource event
                onAddResource(resource, this.state.oasJson);

                onDidChange(EVENTS.ADD_RESOURCE, resource, this.state.oasJson);

                this.setState({
                    actionState: {
                        state: 'success',
                        message: 'Added resource to the swagger.'
                    }
                })

            }
        })
    }

    onAddOperation(operationsObj) {
        const { onAddOperation } = this.props;
        const path = operationsObj.path;

        this.setState(prevState => ({
            ...prevState,
            oasJson: {
                ...prevState.oasJson,
                paths: {
                    ...prevState.oasJson.paths,
                    [path] : {
                        ...prevState.oasJson.paths[path],
                        [operationsObj.method] : {
                            consumes : [],
                            description: operationsObj.description,
                            operationId: operationsObj.id,
                            parameters: [],
                            produces : ["application/xml", "application/json"],
                            responses :{},
                            security: [],
                            summary: operationsObj.name,
                            tags:[]
                        }
                    }
                }
            }
        }),()=>{
            
            //Exposed event for post add resource event
            onAddOperation(operationsObj, this.state.oasJson);

            this.setState({
                actionState: {
                    state: 'success',
                    message: 'Added operation to ' + path
                }
            })

        });
    }

    onDeleteOperation(deletedOp) {
        const path = deletedOp.path;
        const operation = deletedOp.operation;
        const { onDeleteOperation } = this.props;

        delete this.state.oasJson.paths[path][operation];
        this.setState(this.state, ()=>{
            onDeleteOperation(path, operation, deletedOp.operationObj, this.state);
        });
    }

    /**
     * Render Method
     * @returns {React.ReactElement} DOM element
     */
    render() {
        const { isError, oasJson: { paths, info }, actionState, oasJson } = this.state;
        if (isError.state) {
            return (
                <Message error content={isError.message} />
            );
        }

        return (
            <SwaggerAppContext.Provider
                value={{
                    oasJson: oasJson,
                    onAddResource: this.onAddResource,
                    onAddOperation: this.onAddOperation,
                    onAddParameter: this.onAddParameter,
                    onDeleteOperation: this.onDeleteOperation,
                }}
            >
                {info && 
                    <div className='api-details'>
                        <h1>
                            {info.title}
                            <sup>{info.version}</sup>
                        </h1>
                        <pre>
                            <code>[ Base URL : {oasJson.host}{oasJson.basePath} ]</code>
                        </pre>
                        <p>{info.description}</p>
                    </div>
                }
                {(() => {
                    if(actionState.state === 'error') {
                        return <Message error content={actionState.message} />
                    } else if (actionState.state === 'success'){
                        return <Message success content={actionState.message} />
                    } else if(actionState.state === 'warning') {
                        return <Message warning content={actionState.message} />
                    }
                })()}
                <OasResources
                    resObj={paths}
                >
                </OasResources>
            </SwaggerAppContext.Provider>
        );
    }
}

SwaggerVisualizer.propTypes = {
    oasJson: propTypes.object.isRequired,
    onAddResource: propTypes.func,
    onAddParameter: propTypes.func,
    onAddOperation: propTypes.func,
    onDeleteOperation: propTypes.func,
    onDidChange: propTypes.func
};

SwaggerVisualizer.defaultProps = {
    onAddResource: () => {},
    onAddParameter: () => {},
    onAddOperation: () => {},
    onDeleteOperation: () => {},
    onDidChange: () => {}
};

export default SwaggerVisualizer;
