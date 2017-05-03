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
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './canvas-decorator.css';

class CanvasDecorator extends React.Component {
    constructor(props) {
      super(props);
      this.state = {dropZoneActivated: false};
    }

    render() {
        const { bBox = {} } = this.props;
        const dropZoneActivated = this.state.dropZoneActivated;
        return (<svg className="svg-container" width={ this.props.bBox.w } height={ this.props.bBox.h }>
                    <rect x="0" y="0"width="100%" height="100%"
                        className={(!dropZoneActivated) ? "drop-zone" : "drop-zone active"}
                        onMouseOver={(e) => this.onDropZoneActivate(e)}
                        onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
                  {this.props.children}
              </svg>);
    }

    onDropZoneActivate (e) {
        const dragDropManager = this.context.dragDropManager,
              dropTarget = this.props.dropTarget;
        if(dragDropManager.isOnDrag()) {
            if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget);
            this.setState({dropZoneActivated: true});
            dragDropManager.once('drop-target-changed', () => {
                this.setState({dropZoneActivated: false});
            });
        }
        e.stopPropagation();
    }

    onDropZoneDeactivate (e) {
        const dragDropManager = this.context.dragDropManager,
              dropTarget = this.props.dropTarget;
        if(dragDropManager.isOnDrag()){
            if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                dragDropManager.clearActivatedDropTarget();
                this.setState({dropZoneActivated: false});
            }
        }
        e.stopPropagation();
    }
}

CanvasDecorator.propTypes = {
    bBox: PropTypes.shape({
        h: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
    }).isRequired,
    children: PropTypes.node.isRequired,
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired
}

CanvasDecorator.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default CanvasDecorator;
