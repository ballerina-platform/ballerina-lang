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
import EditableText from '../nodes/editable-text';

class TransformerNodeDetails extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            editingTitle: this.props.model.getName().getValue(),
            titleEditing: false,
        };
        this.onNameClick = this.onNameClick.bind(this);
        this.onNameInputBlur = this.onNameInputBlur.bind(this);
        this.onNameInputChange = this.onNameInputChange.bind(this);
    }

    onNameClick() {
        this.setState({ titleEditing: true });
    }

    onNameInputBlur() {
        this.props.model.getName().setValue(this.state.editingTitle);

        this.setState({
            titleEditing: false,
        });
    }

    onNameInputChange(e) {
        this.setState({ editingTitle: e.target.value });
    }

    render() {
        const { x, y, model } = this.props;
        const { viewState } = model;
        const { bBox } = viewState;
        const typeTextX = x;
        const typeTextY = y + (bBox.h / 2);
        const nameTextX = typeTextX + viewState.typeTextWidth;
        const nameTextY = y + (bBox.h / 2);
        const paramTextX = nameTextX + viewState.nameTextWidth;
        const paramTextY = y + (bBox.h / 2);

        return (
            <g className='statement-body'>
                <text
                    x={typeTextX}
                    y={typeTextY}
                    dominantBaseline='central'
                    cursor='pointer'
                    onClick={this.props.onExpand}
                >{viewState.typeText}</text>
                <EditableText
                    x={nameTextX}
                    y={nameTextY}
                    width={viewState.nameTextWidth}
                    onBlur={this.onNameInputBlur}
                    onClick={this.onNameClick}
                    editing={this.state.titleEditing}
                    onChange={this.onNameInputChange}
                    displayText={viewState.nameText || ' '}
                >
                    {this.state.editingTitle}
                </EditableText>
                {
                    !this.state.editingTitle && <text
                        x={nameTextX}
                        y={nameTextY}
                        onClick={this.onNameClick}
                        dominantBaseline='central'
                        fill='#999'
                    >
                        {viewState.defaultNameText}
                    </text>
                }
                <text
                    x={paramTextX}
                    y={paramTextY}
                    dominantBaseline='central'
                    cursor='pointer'
                    onClick={this.props.onExpand}
                >{viewState.paramText}</text>
            </g>
        );
    }
}

export default TransformerNodeDetails;
