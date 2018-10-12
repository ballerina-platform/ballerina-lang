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
import StatementDecorator from '../decorators/statement-decorator';

/**
 * ExpressionStatement Statement Decorator.
 * */
class ExpressionStatementNode extends React.Component {

    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'ExpressionStatement',
            model: this.props.model,
        };
    }

    /**
     * Render Function for ExpressionStatement.
     * */
    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;

        return (
            <StatementDecorator
                model={model}
                viewState={model.viewState}
                expression={expression}
                editorOptions={this.editorOptions}
            />);
    }
}

ExpressionStatementNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

export default ExpressionStatementNode;
