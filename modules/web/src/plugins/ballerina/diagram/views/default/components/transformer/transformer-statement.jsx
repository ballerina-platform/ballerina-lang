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
import TransformerStatementDecorator from './transform-statement-decorator';
import PropTypes from 'prop-types';

/**
 * Assignment statement decorator.
 * */
class TransformerStatement extends React.Component {

    constructor(props) {
        super(props);
    }

    /**
     * Render Function for the assignment statement.
     * */
    render() {
        const model = this.props.model;
        const expression = this.props.model.getStatementString();
        const xPos = model.viewState.bBox.x;
        const yPos = model.viewState.bBox.y;
        const hPos = model.viewState.bBox.h;
        const wPos = model.viewState.bBox.w;

        return (<TransformerStatementDecorator viewState={model.viewState} expression={expression} model={model} />);
    }
}

TransformerStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    expression: PropTypes.shape({
        expression: PropTypes.string,
    }),
};

export default TransformerStatement;
