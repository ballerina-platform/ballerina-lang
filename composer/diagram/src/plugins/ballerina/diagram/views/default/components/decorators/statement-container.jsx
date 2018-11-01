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
import DropZone from './../../../../../drag-drop/DropZone';
import Node from './../../../../../model/tree/node';
import './statement-container.css';

class StatementContainer extends React.Component {

    render() {
        const bBox = this.props.bBox;
        return (
            <g className='statement-container'>
                <DropZone
                    x={bBox.x}
                    y={bBox.y}
                    width={bBox.w}
                    height={bBox.h}
                    baseComponent='rect'
                    dropTarget={this.props.dropTarget}
                    enableDragBg
                />
                {this.props.children}
            </g>
        );
    }
}

StatementContainer.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }).isRequired,
    dropTarget: PropTypes.instanceOf(Node).isRequired,
    children: PropTypes.arrayOf(PropTypes.element),
    draggable: PropTypes.func,
};

StatementContainer.defaultProps = {
    draggable: undefined,
    children: [],
};

export default StatementContainer;
