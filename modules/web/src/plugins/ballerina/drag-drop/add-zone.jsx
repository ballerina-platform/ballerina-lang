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
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import { withDropEnabled } from './drop-target';
import TreeUtil from '../model/tree-util';
import './drop-zone.scss';


class AddZone extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            mouseHovering: false,
            overButton: false,
        };
        this.onMouseOverArea = this.onMouseOverArea.bind(this);
        this.onMouseOutArea = this.onMouseOutArea.bind(this);
        this.onAddButtonClick = this.onAddButtonClick.bind(this);
        this.onAddEndpointClick = this.onAddEndpointClick.bind(this);
        this.onAddIfClick = this.onAddIfClick.bind(this);
        this.onAddWhileClick = this.onAddWhileClick.bind(this);
        this.onAddWorkerClick = this.onAddWorkerClick.bind(this);
    }

    onMouseOverArea() {
        this.setState({ overArea: true });
    }
    onMouseOutArea() {
        this.setState({ overArea: false });
    }

    onAddButtonClick() {
        const blocksToBeAdded = [];
        let yPosition;

        blocksToBeAdded.push({
            name: 'if',
            addBlock: this.onAddIfClick,
        });
        blocksToBeAdded.push({
            name: 'while',
            addBlock: this.onAddWhileClick,
        });
        // blocksToBeAdded.push({
        //     name: 'endpoint',
        //     addBlock: this.onAddEndpointClick,
        // });
        // blocksToBeAdded.push({
        //     name: 'action',
        //     addBlock: this.onAddWorkerClick,
        // });

        let component;
        if (TreeUtil.isFunction(this.props.model) || TreeUtil.isResource(this.props.model)) {
            component = this.props.model.viewState.components.defaultWorkerLine;
            yPosition = component.y + component.h - 30;
        } else {
            component = this.props.model.viewState.components['statement-box'];
            yPosition = component.y - 10;
        }
        const xPosition = component.w + this.props.model.viewState.bBox.expansionW + (component.x / 2) + 10;

        const overlayComponents = {
            kind: 'AddBlockSelect',
            props: {
                key: 'test',
                model: this.props.model,
                blocksToBeAdded,
                x: xPosition,
                y: yPosition,
            },
        };
        this.props.model.viewState.showOverlayContainer = true;
        this.props.model.viewState.overlayContainer = overlayComponents;
        this.context.editor.update();
    }

    onAddEndpointClick() {
        let block;
        if (TreeUtil.isFunction(this.props.model) || TreeUtil.isResource(this.props.model)) {
            block = this.props.model.getBody();
        } else {
            block = this.props.model.parent;
        }
        block.addStatements(DefaultNodeFactory.createEndpoint(),
        block.getIndexOfStatements(this.props.model));
    }

    onAddIfClick() {
        let block;
        if (TreeUtil.isFunction(this.props.model) || TreeUtil.isResource(this.props.model)) {
            block = this.props.model.getBody();
        } else {
            block = this.props.model.parent;
        }
        block.addStatements(DefaultNodeFactory.createIf(),
              block.getIndexOfStatements(this.props.model));
    }

    onAddWhileClick() {
        let block;
        if (TreeUtil.isFunction(this.props.model) || TreeUtil.isResource(this.props.model)) {
            block = this.props.model.getBody();
        } else {
            block = this.props.model.parent;
        }
        block.addStatements(DefaultNodeFactory.createWhile(),
              block.getIndexOfStatements(this.props.model));
    }

    onAddWorkerClick() {
        if (TreeUtil.isFunction(this.props.model) || TreeUtil.isResource(this.props.model)) {
            this.props.model.addWorkers(DefaultNodeFactory.createWorker());
        }
    }

    drawAddButton(x, y, height, width) {
        const buttonY = y + height - 25;

        return (<g>
            <title>Add Block</title>
            <rect
                x={x}
                y={y}
                className='add-statement-area'
                onMouseOver={this.onMouseOverArea}
                onMouseOut={this.onMouseOutArea}
                width={width}
                height={height}
            />
            <rect
                x={x + (width / 2) - 10}
                y={buttonY + 2.5}
                width='20'
                height='20'
                onClick={this.onClick}
                onMouseOver={this.onMouseOverArea}
                onMouseOut={this.onMouseOutArea}
                rx='10'
                ry='10'
                className={this.state.overArea ? 'add-statement-button'
                  : 'add-statement-button-hidden'}
            />
            <text
                x={x - 4 + (width / 2)}
                y={buttonY + 12.5}
                width='20'
                height='20'
                onClick={this.onAddButtonClick}
                onMouseOver={this.onMouseOverArea}
                onMouseOut={this.onMouseOutArea}
                className={this.state.overArea ? 'add-statement-button-label'
                  : 'add-statement-button-label-hidden'}
            >+</text>
        </g>);
    }

    render() {
        const { connectDropTarget, isDragging, renderUponDragStart,
            ...restProps } = this.props;
        if (!isDragging) {
            return this.drawAddButton(restProps.x, restProps.y, restProps.height, restProps.width);
        } else {
            return connectDropTarget(<g />);
        }
    }
}

AddZone.propTypes = {
    connectDropTarget: PropTypes.func.isRequired,
    renderUponDragStart: PropTypes.bool,
};

AddZone.defaultProps = {
    enableDragBg: false,
    renderUponDragStart: false,
};

AddZone.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default withDropEnabled(AddZone);
