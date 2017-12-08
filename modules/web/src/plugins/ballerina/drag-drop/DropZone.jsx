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
import cn from 'classnames';
import { withDropEnabled } from './drop-target';
import Node from './../model/tree/node';
import './drop-zone.scss';

const BASE_TYPES = {
    DIV: 'div',
    RECT: 'rect',
};

class DropZone extends React.Component {
  constructor(props) {
      super(props);
      this.state = {
          mouseHovering:false,
          overButton: false,
      };
      this.onMouseOverArea = this.onMouseOverArea.bind(this);
      this.onMouseOutArea = this.onMouseOutArea.bind(this);
  }

    render() {
        const { baseComponent, className, connectDropTarget, isOver, isOverCurrent, enableCenterOverlayLine,
            isDragging, dropTarget, dropBefore, canDrop, enableDragBg, renderUponDragStart,
            ...restProps } = this.props;
        // render nothing when drag-drop isn't happening
        if (!isDragging && renderUponDragStart) {
            return this.drawAddButton(restProps.x, restProps.y, restProps.height, restProps.width);
}
        const Component = baseComponent;
        let result;
        if (isDragging && enableDragBg && BASE_TYPES.RECT === baseComponent) {
            // only get position info for background rect while dragging
            const positionProps = (({ x, y, width, height }) => ({ x, y, width, height }))(restProps);
            const { x, y, width, height } = positionProps;
            result = (
                <g>
                    <Component
                        {...positionProps}
                        className='drop-zone-background'
                    />
                    <Component
                        {...restProps}
                        className={
                            cn(baseComponent, className, 'drop-zone',
                                { active: isOverCurrent },
                                { blocked: !canDrop },
                                { possible: isDragging && !isOverCurrent && canDrop },
                            )
                        }
                    />
                    {enableCenterOverlayLine &&
                        <line
                            x1={x + (width / 2)}
                            y1={y}
                            x2={x + (width / 2)}
                            y2={y + height}
                            className='drop-zone-center-line unhoverable'
                        />
                    }
                </g>
            );
        } else {
            result = (
              <g>
                {this.drawAddButton(restProps.x, restProps.y + (restProps.height - 25), restProps.height, restProps.width)}
                                <Component
                                    {...restProps}
                                    className={
                                        cn(baseComponent, className, 'drop-zone',
                                            { active: isOverCurrent },
                                            { blocked: !canDrop },
                                            { possible: isDragging && !isOverCurrent && canDrop },
                                        )
                                    }
                                />
              </g>

            );
        }
        return connectDropTarget(result);
    }

    drawAddButton(x, y, height, width) {
      return (<g>
                  <title>Add Block</title>
                  <rect x={x}
                    y={y}
                    className='add-statement-area'
                    onMouseOver={this.onMouseOverArea}
                    onMouseOut={this.onMouseOutArea}
                    width={120}
                    height={50}/>
                  <rect
                      x={x + (width / 2) - 10}
                      y={y + 2.5}
                      width='20'
                      height='20'
                      onMouseOver={this.onMouseOverArea}
                      onMouseOut={this.onMouseOutArea}
                      rx='10'
                      ry='10'
                      className= {this.state.overArea ? 'add-statement-button'
                        : 'add-statement-button-hidden'}
                  />
                  <text
                      x={x - 4 + (width / 2)}
                      y={y + 12.5}
                      width='20'
                      height='20'
                      onMouseOver={this.onMouseOverArea}
                      onMouseOut={this.onMouseOutArea}
                      className={this.state.overArea ? 'add-statement-button-label'
                        : 'add-statement-button-label-hidden'}
                  >+</text>
              </g>);
    }

    onMouseOverArea() {
        this.setState({overArea:true});
    }
    onMouseOutArea() {
        this.setState({ overArea:false });
    }
}

DropZone.propTypes = {
    baseComponent: PropTypes.string.isRequired,
    dropTarget: PropTypes.instanceOf(Node).isRequired,
    dropBefore: PropTypes.instanceOf(Node),
    className: PropTypes.string,
    connectDropTarget: PropTypes.func.isRequired,
    enableDragBg: PropTypes.bool,
    enableCenterOverlayLine: PropTypes.bool,
    renderUponDragStart: PropTypes.bool,
    isOver: PropTypes.bool,
    isOverCurrent: PropTypes.bool,
    canDrop: PropTypes.bool,
    isDragging: PropTypes.bool,
};

DropZone.defaultProps = {
    dropBefore: undefined,
    enableDragBg: false,
    enableCenterOverlayLine: false,
    renderUponDragStart: false,
    className: '',
};

export default withDropEnabled(DropZone);
