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
    render() {
        const { baseComponent, className, connectDropTarget, isOver, isOverCurrent, enableCenterOverlayLine,
            isDragging, dropTarget, dropBefore, canDrop, enableDragBg, renderUponDragStart,
            ...restProps } = this.props;
        // render nothing when drag-drop isn't happening
        if (!isDragging && renderUponDragStart) {
            return (<g />);
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
            );
        }
        return connectDropTarget(result);
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
