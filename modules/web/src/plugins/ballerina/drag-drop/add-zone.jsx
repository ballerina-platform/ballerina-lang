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
        this.onAddActionClick = this.onAddActionClick.bind(this);
    }

    onMouseOverArea() {
        this.setState({ overArea: true });
    }
    onMouseOutArea() {
        this.setState({ overArea: false });
    }

    onAddButtonClick() {
        const blocksToBeAdded = [];
        blocksToBeAdded.push({
            name: 'endpoint',
            addBlock: this.onAddEndpointClick,
        });
        blocksToBeAdded.push({
            name: 'if',
            addBlock: this.onAddIfClick,
        });
        blocksToBeAdded.push({
            name: 'while',
            addBlock: this.onAddWhileClick,
        });
        blocksToBeAdded.push({
            name: 'action',
            addBlock: this.onAddActionClick,
        });

        const overlayComponents = {
            kind: 'MultiBlockSelect',
            props: {
                key: 'test',
                model: this.props.model,
                blocksToBeAdded,
            },
        };
        this.props.model.viewState.showOverlayContainer = true;
        this.props.model.viewState.overlayContainer = overlayComponents;
        this.context.editor.update();
    }

    onAddEndpointClick() {

    }

    onAddIfClick() {
        this.props.model.parent.addStatements(DefaultNodeFactory.createIf(),
              this.props.model.parent.getIndexOfStatements(this.props.model));
    }

    onAddWhileClick() {
        this.props.model.parent.addStatements(DefaultNodeFactory.createWhile(),
              this.props.model.parent.getIndexOfStatements(this.props.model));
    }

    onAddActionClick() {

    }

    drawAddButton(x, y, height, width) {
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
                y={y + 2.5}
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
                y={y + 12.5}
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
        // render nothing when drag-drop isn't happening
        if (!isDragging && renderUponDragStart) {
            return this.drawAddButton(restProps.x, restProps.y, restProps.height, restProps.width);
        }

        const result = (
            <g>
                {this.drawAddButton(restProps.x, restProps.y, restProps.height, restProps.width)}
            </g>
        );
        return connectDropTarget(result);
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
