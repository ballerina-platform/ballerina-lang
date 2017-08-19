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
import { util } from './../visitors/sizing-utils';
import ImageUtil from './image-util';
import ExpressionEditor from '../../expression-editor/expression-editor-utils';
import SimpleBBox from "../ast/simple-bounding-box";

class ParameterDefinition extends React.Component {
    /**
     * calculate the component BBox.
     * @param {object} props - props.
     * @return {SimpleBBox} bBox.
     * */
    static calculateComponentBBox(props) {
        const viewState = props.model.viewState;
        const bBox = viewState.bBox;
        return new SimpleBBox(bBox.x + 7,
            bBox.y + 2, viewState.w + 5, viewState.h);
    }

    constructor(props) {
        super(props);
        this.state = {
            inputBox: ParameterDefinition.calculateComponentBBox(props),
        }
    }

    componentWillReceiveProps(props) {
        this.setState({
            inputBox: ParameterDefinition.calculateComponentBBox(props),
        });
    }

    onDelete() {
        this.props.model.remove();
    }

    onEditRequest() {
        this.editorOptions = {
            propertyType: 'text',
            key: 'ParameterDefinition',
            model: this.props.model,
            getterMethod: this.props.model.getParameterDefinitionAsString,
            setterMethod: this.props.model.setParameterFromString,
            fontSize: 12,
            isCustomHeight: true,
        };

        const options = this.editorOptions;
        const packageScope = this.context.environment;
        if (options) {
            new ExpressionEditor(this.state.inputBox,
                text => this.onUpdate(text), options, packageScope).render(this.context.getOverlayContainer());
        }
    }

    onUpdate(text) {
        // TODO: Implement the on update function.
    }

    render() {
        const model = this.props.model;
        const viewState = model.viewState;
        return (<g>
            <g onClick={() => this.onEditRequest()}>
                <rect
                    x={viewState.bBox.x + 7}
                    y={viewState.bBox.y + 5}
                    width={viewState.w + 5}
                    height={viewState.h + 2}
                    rx="0"
                    ry="0"
                    className="parameter-wrapper"
                />
                <text
                    x={viewState.bBox.x + 10}
                    y={viewState.bBox.y + 6}
                    className="parameter-text"
                >
                    {util.getTextWidth(model.getParameterDefinitionAsString()).text}
                </text>
            </g>
            <g onClick={() => this.onDelete()}>
                <rect
                    x={viewState.components.deleteIcon.x}
                    y={viewState.components.deleteIcon.y + 5}
                    width={viewState.components.deleteIcon.w + 2}
                    height={viewState.components.deleteIcon.h + 2}
                    rx="0"
                    ry="0"
                    className="parameter-delete-icon-wrapper"
                />
                <image
                    x={viewState.components.deleteIcon.x + 5}
                    y={viewState.components.deleteIcon.y + 10} width={10}
                    height={10}
                    className="parameter-delete-icon"
                    xlinkHref={ImageUtil.getSVGIconString('cancel')}
                    onClick={() => this.onDelete()}
                >
                    <title>Remove</title>
                </image>
                <rect
                    x={viewState.components.deleteIcon.x + viewState.components.deleteIcon.w + 4}
                    y={viewState.components.deleteIcon.y + 4}
                    width={1}
                    height={viewState.h - 2}
                    rx="0"
                    ry="0"
                    className="parameter-space"
                />
            </g>

        </g>);
    }
}

ParameterDefinition.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default ParameterDefinition;
