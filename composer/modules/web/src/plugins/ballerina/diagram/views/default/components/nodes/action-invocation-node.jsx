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
import ActionInvocationDecorator from '../decorators/action-invocation-decorator';
import ActiveArbiter from '../decorators/active-arbiter';
import TreeUtil from '../../../../../model/tree-util';

/**
 * Action Invocation Node component. This is a custom component for rendering all statements
 * with an action invocation expression.
 * Includes : assignment node, var def node and invocation statements with action invocations.
 * */
class InvocationNode extends React.Component {


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
        let expression = model.viewState.expression;

        if (model.viewState.isActionInvocation) {
            expression = TreeUtil.getInvocationSignature(this.props.model);
        }

        return (
            <ActionInvocationDecorator
                model={model}
                viewState={model.viewState}
                expression={expression}
                editorOptions={this.editorOptions}
            />);
    }
}

InvocationNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

InvocationNode.contextTypes = {
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
    designer: PropTypes.instanceOf(Object).isRequired,
};

export default InvocationNode;
