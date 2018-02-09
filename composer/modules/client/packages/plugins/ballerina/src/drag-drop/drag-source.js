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
import _ from 'lodash';
import log from 'log';
import { DragSource } from 'react-dnd';
import { ITEM_TYPES } from './constants';

/**
 * Enable drag on given compenent
 *
 * @param {Componet} ToolView React Component for Tool
 *
 */
export function withDragEnabled(ToolView) {
    // drag source spec
    const dragSpec = {
        beginDrag: (props, monitor, component) => {
            const { factoryArgs = {}, nodeFactoryMethod, icon } = props.tool;
            return {
                dragSource: nodeFactoryMethod(factoryArgs),
                icon,
            };
        },
        endDrag: (props, monitor, component) => {
            if (monitor.didDrop()) {
                const { dragSource } = monitor.getItem();
                const { dropTarget, dropBefore } = monitor.getDropResult();
                if (_.isFunction(dropTarget.acceptDrop)) {
                    try {
                        dropTarget.acceptDrop(dragSource, dropBefore);
                    } catch (error) {
                        log.error(`Error while adding dropped node. ${error}`);
                    }
                } else {
                    log.error('Cannot find accept method to add dropped node.');
                }
            }
        },
    };

    // Specifies which props to inject into component
    function collect(connect, monitor) {
        return {
            // Call this function inside render()
            // to let React DnD handle the drag events:
            connectDragSource: connect.dragSource(),
            connectDragPreview: connect.dragPreview(),
            isDragging: monitor.isDragging(),
        };
    }
    return DragSource(ITEM_TYPES.NODE, dragSpec, collect)(ToolView);
}
