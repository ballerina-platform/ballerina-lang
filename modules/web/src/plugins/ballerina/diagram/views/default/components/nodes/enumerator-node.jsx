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
import ImageUtil from './../../../../image-util';
import * as DesignerDefault from './../../designer-defaults';

/**
 * Class for Enumerator Node view.
 * @class EnumeratorNode
 * @extends React.Component
 * */
class EnumeratorNode extends React.Component {

    constructor(props) {
        super(props);
        this.onDelete = this.onDelete.bind(this);
    }

    /**
     * Handle enumerator on delete functionality.
     * */
    onDelete() {
        const model = this.props.model;
        if (model.parent.getEnumerators().length > 1) {
            model.parent.removeEnumerators(model);
        }
    }

    /**
     * Render the view.
     * @return {XML} react component.
     * */
    render() {
        const model = this.props.model;
        const viewState = model.viewState;
        const isEnabled = model.parent.getEnumerators().length > 1;
        return (
            <g>
                <g>
                    <rect
                        x={viewState.bBox.x}
                        y={viewState.bBox.y}
                        width={viewState.w}
                        height={viewState.h}
                        rx='0'
                        ry='0'
                        className='struct-added-value-wrapper-light'
                    />
                    <text
                        x={viewState.bBox.x}
                        y={viewState.bBox.y + DesignerDefault.enumIdentifierStatement.textPadding.top}
                        className='parameter-text'
                    >
                        {model.viewState.components.expression.text}
                    </text>
                </g>
                <g onClick={this.onDelete}>
                    <rect
                        x={viewState.components.deleteIcon.x}
                        y={viewState.components.deleteIcon.y
                        + DesignerDefault.enumIdentifierStatement.textPadding.top + 2}
                        width={viewState.components.deleteIcon.w}
                        height={viewState.components.deleteIcon.h}
                        rx='0'
                        ry='0'
                        className='struct-delete-icon-wrapper-light'
                    />
                    <image
                        x={viewState.components.deleteIcon.x}
                        y={viewState.components.deleteIcon.y
                        + DesignerDefault.enumIdentifierStatement.textPadding.top + 2}
                        width={10}
                        height={10}
                        className={isEnabled ? 'parameter-delete-icon' : 'parameter-delete-icon-disabled'}
                        xlinkHref={ImageUtil.getSVGIconString('cancel')}
                    >
                        <title>Remove</title>
                    </image>
                </g>

            </g>
        );
    }
}

EnumeratorNode.propTypes = {
    model: PropTypes.shape({
        addEnumerators: PropTypes.func.isRequired,
        getEnumerators: PropTypes.func.isRequired,
    }).isRequired,
};
export default EnumeratorNode;
