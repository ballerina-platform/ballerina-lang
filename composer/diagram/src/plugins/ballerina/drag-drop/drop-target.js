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
import { DropTarget } from 'react-dnd';
import { ITEM_TYPES } from './constants';

/**
 * Enable drop on given compenent
 *
 * @param {Componet} DropArea React Component for droppable area
 *
 */
export function withDropEnabled(DropArea) {
    // drop target spec
    const dropSpec = {
        drop: (props, monitor, component) => {
            return {
                dropTarget: props.dropTarget,
                dropBefore: props.dropBefore,
            };
        },
        hover: (props, monitor, component) => {

        },
        canDrop: (props, monitor, component) => {
            const { dropTarget, canDrop, dropBefore } = props;
            const { dragSource } = monitor.getItem();
            let validDrop = false;
            // Try to validate drop through node methods
            if (!_.isNil(dropTarget) && _.isFunction(dropTarget.canAcceptDrop)) {
                validDrop = dropTarget.canAcceptDrop(dragSource, dropBefore) && (_.isFunction(dragSource.canBeDropped)
                    ? dragSource.canBeDropped(dropTarget, dropBefore) : true);
            }
            // give priority to validate callback
            // directly given to DropZone
            if (_.isFunction(canDrop)) {
                validDrop = canDrop(dragSource);
            }
            return validDrop;
        },
    };

    // Specifies which props to inject into component
    function collect(connect, monitor) {
        return {
            connectDropTarget: connect.dropTarget(),
            isOver: monitor.isOver(),
            isOverCurrent: monitor.isOver({ shallow: true }),
            canDrop: monitor.canDrop(),
            isDragging: monitor.getItem() !== null,
        };
    }
    return DropTarget(ITEM_TYPES.NODE, dropSpec, collect)(DropArea);
}
