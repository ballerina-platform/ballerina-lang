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
import BlockStatementDecorator from './block-statement-decorator';
import PropTypes from 'prop-types';
import {getComponentForNodeArray} from './utils';

class AbortedStatement extends React.Component {
    constructor(props) {
        super(props);
        this.onAddCommittedClick = this.onAddCommittedClick.bind(this);
    }

    /**
     * Get add committed statement button
     * @return {object} react element or null
     * */
    getAddCommittedStatementButton() {
        let model = this.props.model;
        let parent = model.parent;
        let bBox = model.viewState.bBox;
        if (!parent.getCommittedStatement()) {
            return (<g onClick={this.onAddCommittedClick}>
                <rect x={bBox.x+bBox.w-10} y={bBox.y+bBox.h-25} width={20} height={20} rx={10} ry={10} className='add-else-button'/>
                <text x={bBox.x+bBox.w-4} y={bBox.y+bBox.h-15} width={20} height={20} className='add-else-button-label'>+</text>
            </g>);
        }
        return null;
    }

    /**
     * Event handler for click add committed button.
     * */
    onAddCommittedClick() {
        let parent = this.props.model.parent;
        parent.createCommittedStatement();
    }

    /**
     * Get block statement decorator for aborted statement.
     * @param {object} utilities
     * @return {object}
     * */
    getBlockStatementDecorator(utilities) {
        let model = this.props.model;
        let bBox = model.viewState.bBox;
        let titleWidth = model.viewState.titleWidth;
        let children = getComponentForNodeArray(model.getChildren());
        if (utilities) {
            return (<BlockStatementDecorator dropTarget={model} bBox={bBox} titleWidth={titleWidth}
                                             title={"Aborted"} utilities={utilities}>
                {children}
            </BlockStatementDecorator>);
        } else {
            return (<BlockStatementDecorator dropTarget={model} bBox={bBox} titleWidth={titleWidth}
                                             title={"Aborted"}>
                {children}
            </BlockStatementDecorator>);
        }
    }

    render() {
        return this.getBlockStatementDecorator(this.getAddCommittedStatementButton());
    }
}

AbortedStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired
    })
};

export default AbortedStatement;