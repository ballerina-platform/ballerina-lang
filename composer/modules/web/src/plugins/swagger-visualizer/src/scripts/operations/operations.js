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
import proptypes from 'prop-types';
import { Accordion, Button, Icon } from 'semantic-ui-react';

import OasOperation from './operation';
import OsaAddOperation from './add-operation';
import SwaggerAppContext from '../../context/app-context';

/* eslint-disable */
class OasOperations extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            activeIndex: -1,
            showAddOperation: false,
        };

        this.handleOperationExpand = this.handleOperationExpand.bind(this);
        this.handleShowAddOperation = this.handleShowAddOperation.bind(this);
    }

    /**
     * Click handler for accordion expand.
     * @param {Object} e event Object
     * @param {Object} titleProps accordion title props
     */
    handleOperationExpand(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;

        this.setState({ activeIndex: newIndex });
    }

    /**
     * Event handler for show add resource form
     */
    handleShowAddOperation() {
        const { showAddOperation } = this.state;
        this.setState({
            showAddOperation: !showAddOperation,
        });
    }

    render() {
        const { oasOperations, path } = this.props;
        const { activeIndex, showAddOperation } = this.state;
        return (
            <React.Fragment>
                <Button size='mini' icon labelPosition='left' onClick={this.handleShowAddOperation}>
                    <Icon name='plus' />
                    Add Operation
                </Button>
                {showAddOperation && 
                    <SwaggerAppContext.Consumer>
                        {(appContext) => {
                            return (
                                <OsaAddOperation
                                    onAddOperation={appContext.onAddOperation}
                                    oasJson={appContext.oasJson}
                                    path={path}
                                />
                            );
                        }}
                    </SwaggerAppContext.Consumer>
                }
                {Object.keys(oasOperations).length > 0 && 
                    <Accordion fluid>
                        {oasOperations && Object.keys(oasOperations).map((operation, index) => {
                            return (
                                <SwaggerAppContext.Consumer>
                                    {(appContext) => {
                                        return (
                                            <OasOperation
                                                path={path}
                                                opType={operation}
                                                oasOp={oasOperations[operation]}
                                                activeIndex={activeIndex}
                                                currIndex={index}
                                                handleExpand={this.handleOperationExpand}
                                                onDeleteOperation={appContext.onDeleteOperation}
                                            />
                                        );
                                    }}
                                </SwaggerAppContext.Consumer>
                            );
                        })}
                    </Accordion>
                }
            </React.Fragment>
        );
    }
}

OasOperations.prototypes = {
    oasOperations: proptypes.object.isRequired,
    path: proptypes.string.isRequired,
};

export default OasOperations;
