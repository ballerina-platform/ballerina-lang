/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
import React from 'react';
import PropTypes from 'prop-types';
import ClientResponderDecorator from '../decorators/client-responder-decorator';

/**
 * Action Invocation Node component. This is a custom component for rendering all statements
 * with an action invocation expression.
 * Includes : assignment node, var def node and invocation statements with action invocations.
 * */
class ClientResponderNode extends React.Component {


    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'Assignment',
            model: this.props.model,
        };
    }

    /**
     * ToDo Update the edited expression
     */
    updateExpression(value) {
    }

    /**
     * Render Function for the assignment statement.
     * */
    render() {
        const model = this.props.model;

        return (
            <ClientResponderDecorator
                model={model}
                viewState={model.viewState}
                editorOptions={this.editorOptions}
            />);
    }
}

ClientResponderNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

ClientResponderNode.contextTypes = {
    designer: PropTypes.instanceOf(Object).isRequired,
};

export default ClientResponderNode;
