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
import log from 'log';
import _ from 'lodash';
import './global-item.css';
import { variablesPane as variablesPaneDefaults } from '../../designer-defaults';
import ExpressionEditor from '../../../../../../expression-editor/expression-editor-utils';
import { parseContent } from './../../../../../../api-client/api-client';
import TreeBuilder from './../../../../../model/tree-builder';
import TreeUtils from './../../../../../model/tree-util';

/**
 * React component for an entry representing a variable in the expanded variable pane.
 *
 * @class GlobalDefinitionItem
 * @extends {React.Component}
 */
export default class GlobalDefinitionItem extends React.Component {

    /**
     * Creates an instance of Annotation.
     * @param {Object} props - props of the React component
     * @param {Object} props.bBox - The bounding box for rendering the react component.
     * @param {function} props.onDeleteClick - Function invoked when delete button is clicked.
     * @param {*} props.globalDec - Object representing the viewed variable.
     * @param {function} props.getValue - The function called passing the variable object to obtain the viewed text.
     * @memberof GlobalDefinitionItem
     */
    constructor(props) {
        super(props);
        this.state = {
            highlighted: false,
        };
        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
        this.handleDeleteClick = this.handleDeleteClick.bind(this);
    }

    /**
     * Delete button click event handler.
     * */
    handleDeleteClick() {
        this.props.onDeleteClick(this.props.globalDec);
    }

    /**
     * Called when the mouse pointer enters the component
     * */
    handleMouseEnter() {
        this.setState({ highlighted: true });
    }

    /**
     * Called when the mouse pointer leaves the component
     * */
    handleMouseLeave() {
        this.setState({ highlighted: false });
    }

    setEditedSource(value) {
        const oldNode = this;
        // Remove the semicolon if its already there
        if (value.endsWith(';')) {
            value = value.replace(';', '');
        }
        value += ';\n';
        parseContent(value)
            .then((jsonTree) => {
                if (jsonTree.model.topLevelNodes[0]) {
                    this.parent.replaceTopLevelNodes(oldNode,
                        TreeBuilder.build(jsonTree.model.topLevelNodes[0], this.parent, this.parent.kind));
                }
            })
            .catch(log.error);
    }
    /**
     * renders an ExpressionEditor in the add new variable area.
     * @param {Object} bBox - bounding box ExpressionEditor should be rendered.
     */
    openEditor(bBox) {
        let setterFunc;
        const editorOuterPadding = 10;
        let parentNode = this.props.globalDec.parent;
        if (TreeUtils.isService(parentNode) || TreeUtils.isConnector(parentNode)) {
            parentNode = parentNode.parent;
        }
        if (_.includes(parentNode.filterTopLevelNodes({ kind: 'Variable' })
                .concat(parentNode.filterTopLevelNodes({ kind: 'Xmlns' })), this.props.globalDec)) {
            setterFunc = this.setEditedSource;
        }
        const options = {
            propertyType: 'text',
            key: 'Global Variable',
            model: this.props.globalDec,
            setterMethod: setterFunc,
        };

        const editorBBox = {
            x: (bBox.x + (editorOuterPadding / 2)),
            y: bBox.y,
            h: bBox.h,
            w: (bBox.w - editorOuterPadding),
        };

        const packageScope = this.context.enviornment;

        new ExpressionEditor(editorBBox, (s) => {}, options, packageScope)
            .render(this.context.getOverlayContainer());
    }

    /**
     * Renders the view for a GlobalDefinitionItem component.
     * @returns {ReactElement} The react element for GlobalDefinitionItem.
     * @memberof GlobalDefinitionItem
     * @override
     */
    render() {
        const { x, y, w, h } = this.props.bBox;
        const leftPadding = variablesPaneDefaults.globalItemLeftPadding;

        const deleteStyle = {};

        if (!this.state.highlighted) {
            deleteStyle.display = 'none';
        }

        let className = 'global-definition-item';

        if (this.state.highlighted) {
            className = 'global-definition-item-hightlighted';
        }
        this.props.getValue(this.props.globalDec);
        return (

            <g className={className} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>

                <title> {_.trimEnd(this.props.getValue(this.props.globalDec), ';')} </title>
                <g onClick={(e) => { this.openEditor(this.props.bBox); }}>
                    <rect
                        x={x}
                        y={y}
                        height={h}
                        width={w}
                        className="background"
                    />
                    <text
                        x={x + leftPadding}
                        y={y + (h / 2)}
                        rx="0"
                        ry="0"
                        className="global-definition-text"
                    >
                        {this.props.globalDec.viewState.globalText }
                    </text>
                </g>
                <rect
                    x={x + w - 30}
                    y={y}
                    height={h}
                    width={30}
                    className="delete-background"
                    onClick={this.handleDeleteClick}
                />
                <text
                    x={x + w - 18}
                    y={y + (h / 2)}
                    style={deleteStyle}
                    className="delete-x"
                    onClick={this.handleDeleteClick}
                >
                    x
                </text>
            </g>
        );
    }
}

GlobalDefinitionItem.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }).isRequired,
    onDeleteClick: PropTypes.func.isRequired,
    // globalDec object could take any shape as long as it could be passed to the getValue function to get a string
    // eslint-disable-next-line react/forbid-prop-types
    globalDec: PropTypes.object.isRequired,
    getValue: PropTypes.func.isRequired,
};

GlobalDefinitionItem.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};
