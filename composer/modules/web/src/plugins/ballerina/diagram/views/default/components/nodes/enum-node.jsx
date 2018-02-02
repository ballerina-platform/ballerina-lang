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
import PanelDecorator from './../decorators/panel-decorator';
import * as DesignerDefaults from './../../designer-defaults';
import ImageUtil from '../../../../image-util';
import EditableText from './editable-text';
import TreeUtils from './../../../../../model/tree-util';
import EnumeratorNode from './enumerator-node';
import DefaultNodeFactory from './../../../../../model/default-node-factory';


/**
 * Class for Enum Node view.
 * @class EnumNode
 * @extends React.Component
 * */
class EnumNode extends React.Component {
    /**
     * constructor for Enum node class.
     * @param {object} props - properties for enum node.
     * */
    constructor(props) {
        super(props);

        this.state = {
            newIdentifier: '',
            newIdentifierEditing: false,
        };
        this.handleAddIdentifierClick = this.handleAddIdentifierClick.bind(this);
        this.addIdentifier = this.addIdentifier.bind(this);
    }

    /**
     * Handle clicking on add identifier placeholder.
     * */
    handleAddIdentifierClick() {
        this.setState({ newIdentifierEditing: true });
    }

    /**
     * Add new Identifier to the enum.
     * */
    addIdentifier() {
        if (this.state.newIdentifier && !this.isAlreadyExist(this.state.newIdentifier)) {
            const enumNode = DefaultNodeFactory.createEnumerator(this.state.newIdentifier);
            if (!enumNode.error) {
                enumNode.getEnumerators()[0].clearWS();
                this.props.model.addEnumerators(enumNode.getEnumerators()[0]);
            } else {
                this.context.alert.showError('Invalid Identifier Provided !');
            }
        }
        this.setState({
            newIdentifier: '',
        });
    }

    isAlreadyExist(name) {
        const model = this.props.model;
        const isExist = model.getEnumerators().find((enumerator) => {
            return enumerator.getName().getSource() === name;
        });
        return !!isExist;
    }

    /**
     * get render content for input.
     *
     * */
    renderContentOperations({ x, y, w, h }, columnSize) {
        const typeCellbox = {
            x: x + DesignerDefaults.structDefinition.panelPadding,
            y: y + DesignerDefaults.structDefinition.panelPadding,
            width: columnSize - DesignerDefaults.structDefinition.panelPadding
            - (DesignerDefaults.structDefinition.columnPadding / 2),
            height: h - (DesignerDefaults.structDefinition.panelPadding * 2),
        };
        return (
            <g>
                <rect
                    x={x}
                    y={y}
                    width={w}
                    height={h}
                    className='struct-content-operations-wrapper'
                    fill='#3d3d3d'
                />
                <g onClick={this.handleAddIdentifierClick}>
                    <rect {...typeCellbox} className='struct-input-value-wrapper' />
                    <text
                        x={typeCellbox.x + 10}
                        y={y + (DesignerDefaults.contentOperations.height / 2) + 2}
                    >
                        Add Identifier
                    </text>
                </g>
                <EditableText
                    {...typeCellbox}
                    y={y + (DesignerDefaults.contentOperations.height / 2)}
                    placeholder='i.e. IDENTIFIER'
                    onBlur={() => {
                        this.setState({
                            newIdentifierEditing: false,
                        });
                    }}
                    editing={this.state.newIdentifierEditing}
                    onChange={(e) => {
                        if (e.target.value.length) {
                            this.setState({
                                newIdentifier: e.target.value,
                            });
                        }
                    }}
                >
                    {this.state.newIdentifierEditing ? this.state.newIdentifier : ''}
                </EditableText>
                <rect
                    x={x + w - 30}
                    y={y + 10}
                    width={25}
                    height={25}
                    onClick={() => this.addIdentifier()}
                    className='struct-added-value-wrapper-light'
                />
                <image
                    x={x + w - 29}
                    style={{ cursor: 'pointer' }}
                    y={y + 10}
                    width={20}
                    height={20}
                    onClick={() => this.addIdentifier()}
                    className='struct-add-icon-wrapper'
                    xlinkHref={ImageUtil.getSVGIconString('check')}
                />
            </g>
        );
    }

    /**
     * Render the view for enum node.
     * @return {XML} react component.
     * */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const title = model.getName().getSource();
        const enumerators = model.getEnumerators();
        const coDimensions = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left,
            y: bBox.y + DesignerDefaults.panel.body.padding.top + model.viewState.components.annotation.h,
            w: DesignerDefaults.enumPanel.contentOperations.w,
            h: DesignerDefaults.contentOperations.height,
        };
        const columnSize = (coDimensions.w - DesignerDefaults.structDefinition.submitButtonWidth);
        return (
            <PanelDecorator icon='tool-icons/enum' title={title} bBox={bBox} model={model}>
                {this.renderContentOperations(coDimensions, columnSize)}
                <g>
                    {
                        enumerators.map((child, i) => {
                            return TreeUtils.isEnumerator(child) ? (
                                <EnumeratorNode
                                    x={coDimensions.x}
                                    y={coDimensions.y + DesignerDefaults.contentOperations.height + 10}
                                    w={coDimensions.w}
                                    h={DesignerDefaults.structDefinitionStatement.height}
                                    model={child}
                                />
                            ) : '';
                        })
                    }
                </g>
            </PanelDecorator>
        );
    }
}

EnumNode.propTypes = {
    model: PropTypes.shape({
        addEnumerators: PropTypes.func.isRequired,
    }).isRequired,
};

EnumNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    alert: PropTypes.instanceOf(Object).isRequired,
};


export default EnumNode;
