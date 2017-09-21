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
import ASTFactory from '../../../../ast/ast-factory';
import './service-definition.css';
import PropTypes from 'prop-types';
/**
 * Add more resources to a service
 *
 * @class ServiceTransportLine
 * @extends {React.Component}
 */
class AddResourceDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.createHttpResource = this.createHttpResource.bind(this);
        this.onAddResourceClick = this.onAddResourceClick.bind(this);
    }

    /**
     * Create http resources for a service
     * @returns {{parameters: [*,*]}}
     */
    createHttpResource() {
        return ({
            parameters: [
                {
                    type: 'http:Request',
                    value: 'req',
                },
                {
                    type: 'http:Response',
                    value: 'resp',
                },
            ],
        });
    }
    /**
     * Handles the click event when adding a resource to a service
     */
    onAddResourceClick() {
        let serviceDef = this.props.model;
        let thisNodeIndex;
        if (ASTFactory.isResourceDefinition(this.props.model)) {
            const resourceDef = this.props.model;
            serviceDef = resourceDef.getParent();
            thisNodeIndex = serviceDef.getIndexOfChild(resourceDef) + 1;
        }
        let args = {};
        switch (serviceDef.getProtocolPkgName()) {
            case 'http':
                args = this.createHttpResource();
                break;
        }
        if (serviceDef.getProtocolPkgName() === 'http') {
            const newResourceDef = ASTFactory.createResourceDefinition(args);
            if (args.parameters.length > 0) {
                // Define the argument param definition holder
                const argumentParameterDefinitionHolder = ASTFactory.createArgumentParameterDefinitionHolder();
                args.parameters.map((parameter) => {
                    const parameterDef = ASTFactory.createParameterDefinition(args);
                    parameterDef.setTypeName(parameter.type);
                    parameterDef.setName(parameter.value);
                    argumentParameterDefinitionHolder.addChild(parameterDef);
                });
                newResourceDef.addChild(argumentParameterDefinitionHolder);
            }
            serviceDef.addChild(newResourceDef, thisNodeIndex, undefined, undefined, true);
        } else if (serviceDef.getProtocolPkgName() === 'ws') {
            // Dropdown is rendered
            this.props.model.getViewState().showWebSocketMethods =
                !this.props.model.getViewState().showWebSocketMethods;
            this.context.editor.update();
        }
    }

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent add resource definition
     * @memberof AddResourceDefinition
     */
    render() {
        let yPosition = this.props.model.viewState.bBox.y + this.props.model.viewState.bBox.h - 40;
        let yPositionOfResourceBtn = 5;
        let yPositionOfResourceBtnLbl = 15;
        let xPositionOfResourceBtn = -12;
        let xPositionOfResourceBtnLbl = -4;
        let xPositionOfAddResourceRect = 5;
        let xPositionOfAddResourceLbl = 55;
        const bBox = this.props.bBox;
        if (ASTFactory.isResourceDefinition(this.props.model)) {
            yPosition = bBox.y + bBox.h;
            yPositionOfResourceBtn = 12;
            yPositionOfResourceBtnLbl = 24;
            xPositionOfResourceBtn = 38;
            xPositionOfResourceBtnLbl = 46;
            xPositionOfAddResourceRect = 55;
            xPositionOfAddResourceLbl = 105;
        }
        return (
            <g onClick={this.onAddResourceClick}>
                <rect
                    x={bBox.x + xPositionOfAddResourceRect}
                    y={yPosition + yPositionOfResourceBtn}
                    width="100"
                    height="25"
                    rx="15"
                    ry="15"
                    fill="#a09c9b"
                />
                <text
                    x={bBox.x + xPositionOfAddResourceLbl}
                    y={yPosition + yPositionOfResourceBtnLbl + 1}
                    alignmentBaseline="middle"
                    textAnchor="middle"
                    className="add-resource-label"
                >Add Resource</text>
                <rect
                    x={bBox.x + xPositionOfResourceBtn}
                    y={yPosition + yPositionOfResourceBtn}
                    width={25}
                    height={25}
                    rx={12.5}
                    ry={12.5}
                    className="add-resource-button"
                    fill={this.props.model.getViewState().showWebSocketMethods ? '#63605f' : '#a09c9b'}
                />
                <text
                    x={bBox.x + xPositionOfResourceBtnLbl}
                    y={yPosition + yPositionOfResourceBtnLbl}
                    width={25}
                    height={25}
                    className="add-resource-button-label"
                >+</text>
            </g>
        );
    }
}

AddResourceDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default AddResourceDefinition;
