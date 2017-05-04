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
import './annotation-attribute-decorator.css';

import ImageUtil from './image-util';
/**
 * Annotation Attribute Decorator
 * */
class AnnotationAttributeDecorator extends React.Component {
    constructor(props) {
        super(props);
        this.state = {isEdit: false};
    }

    /**
     * if isEdit is false: Change the state to show the edit text box.
     * if isEdit is true: Change the state to hide the edit text box.
     * */
    editState() {
        if (this.state.isEdit) {
            this.setState({
                isEdit: false
            });
        } else {
            this.setState({
                isEdit: true
            });
        }
    }

    /**
     * Add Annotation Attribute to annotation definition.
     * */
    addAnnotationAttribute() {
        let model = this.props.model;
        model.addAnnotationAttributeDefinition("int", "a", "0");
    }

    render() {
        let bBox = this.props.bBox;
        if (this.state.isEdit) {
            return (
                <g className="attribute-content-operations-wrapper">
                    <rect x={bBox.x + 50} y={bBox.y + 50} width={300} height={30}
                          className="attribute-content-operations-wrapper"/>
                    <rect x={bBox.x + 350 + 10} y={bBox.y + 50} width={30} height={30} className=""
                          onClick={() => this.addAnnotationAttribute()}/>

                    <image x={bBox.x + 350 + 15} y={bBox.y + 55} width={20} height={20}
                           xlinkHref={ImageUtil.getSVGIconString('add')}/>
                </g>
            );
        } else {
            return (
                <g className="attribute-content-operations-wrapper">
                    <rect x={bBox.x + 50} y={bBox.y + 50} width={300} height={30}
                          className="attribute-content-operations-wrapper"/>
                    <rect x={bBox.x + 350 + 10} y={bBox.y + 50} width={30} height={30} className=""
                          onClick={() => this.addAnnotationAttribute()}/>

                    <text x={bBox.x + 60} y={bBox.y + 70} width={300} height={30} onClick={() => this.editState()}>
                        Enter
                        Variable
                    </text>
                    <image x={bBox.x + 350 + 15} y={bBox.y + 55} width={20} height={20}
                           xlinkHref={ImageUtil.getSVGIconString('add')}/>
                </g>
            );
        }
    }
}

export default AnnotationAttributeDecorator;