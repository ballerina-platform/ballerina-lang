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
import _ from 'lodash';
import './receiver-node.css';
import SizingUtils from '../../sizing-util';
import TreeUtils from './../../../../../model/tree-util';
import ImageUtil from '../../../../image-util';
import OverlayComponentsRenderingUtil from './../utils/overlay-component-rendering-util';

class ReceiverNode extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            editingTitle: this.props.model.getName().getValue(),
            titleEditing: false,
        };
        this.checkForStructBindedFunction = this.checkForStructBindedFunction.bind(this);
        this.showStructsInPackage = this.showStructsInPackage.bind(this);
        this.getStructs = this.getStructs.bind(this);
    }

    getStructs() {
        let structNodeArray = [];
        const model = this.props.model;
        if (!TreeUtils.isMainFunction(model)) {
            const currentPackage = this.context.environment.getCurrentPackage();
            if (currentPackage.getStructDefinitions()) {
                currentPackage.getStructDefinitions().forEach((struct) => {
                    const structType = struct.getName();
                    structNodeArray.push(structType + ' ' + structType.charAt(0).toLowerCase());
                });
            }
            if (model.getReceiver()) {
                const receiverType = model.getReceiver().getTypeNode().getTypeName().value;
                structNodeArray = _.without(structNodeArray, receiverType + ' ' + receiverType.charAt(0).toLowerCase());
            }
        }
        return structNodeArray;
    }

    checkForStructBindedFunction(model) {
        let structBindedFunction = false;
        if (!TreeUtils.isMainFunction(model)) {
            if (model.getReceiver()) {
                structBindedFunction = true;
            }
        }
        return structBindedFunction;
    }

    showStructsInPackage(x, y) {
        const node = this.props.model;
        node.viewState.showOverlayContainer = true;
        OverlayComponentsRenderingUtil.showStructsInPackageForBinding(node, x, y, this.getStructs());
        this.context.editor.update();
    }
    render() {  
        const { x, y, model, showStructBinding } = this.props;
        const typeTextX = x + 25;
        const iconSize = 14;
        const typeTextY = y + 15;
        // check if the function is struct binded
        const structBindedFunction = this.checkForStructBindedFunction(model);
        let receiverType;
        if (structBindedFunction) {
            receiverType = model.getReceiver().getTypeNode().getTypeName().value + ' ' +
                model.getReceiver().getName().value;
        }
        const receiverTypeWidth = new SizingUtils().getTextWidth(receiverType, 0).w || 0;
        // Calculate the x,y positions of the image
        const addBindingStructX = x - 5;
        const addBindingStructY = y + 8;
        return (
            <g className='statement-body'>
                { (showStructBinding && this.getStructs().length > 0 && !structBindedFunction) &&
                    <g>
                        <text
                            x={addBindingStructX}
                            y={addBindingStructY}
                            width={iconSize}
                            height={iconSize}
                            fontFamily='font-ballerina'
                            fontSize={iconSize}
                            xlinkHref={ImageUtil.getCodePoint('bind')}
                            onClick={e => this.showStructsInPackage(addBindingStructX, addBindingStructY)}
                        > <title> Bind functions with structs </title> </text> </g>}
                {structBindedFunction &&
                    <g>
                        <text
                            x={typeTextX + 50}
                            y={typeTextY + 5}
                            cursor='pointer'
                            onClick={e => this.showStructsInPackage(addBindingStructX, addBindingStructY)}
                            fill='white'
                        >{receiverType}</text>
                    </g>
                }
            </g>
        );
    }
}

ReceiverNode.propTypes = {
    showStructBinding: PropTypes.bool.isRequired,
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    model: PropTypes.instanceOf(Object).isRequired,
};

ReceiverNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default ReceiverNode;
