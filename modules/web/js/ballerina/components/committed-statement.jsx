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
import {getComponentForNodeArray} from './utils';
import BlockStatementDecorator from './block-statement-decorator';
import CommitedStatementAST from './../ast/statements/committed-statement';

/**
 * React component for committed statement.
 *
 * @class CommittedStatement
 * @extends {React.PureComponent}
 */
class CommittedStatement extends React.PureComponent {

    constructor(props) {
        super(props);
    }

    /**
     * Renders view for a commit statement.
     *
     * @returns {object} The view.
     * @memberof CommittedStatement
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const titleWidth = model.viewState.titleWidth;
        const children = getComponentForNodeArray(model.getChildren());
        return (<BlockStatementDecorator
            model={model}
            dropTarget={model}
            bBox={bBox}
            titleWidth={titleWidth}
            title={'Committed'}
        >
            {children}
        </BlockStatementDecorator>);
    }
}

CommittedStatement.propTypes = {
    model: PropTypes.instanceOf(CommitedStatementAST).isRequired,
};

export default CommittedStatement;
