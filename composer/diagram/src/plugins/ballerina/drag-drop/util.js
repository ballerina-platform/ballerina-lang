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
 *
 */

import { DropTarget } from 'react-dnd';
import { ITEM_TYPES } from './constants';

/**
 * Injects dragState prop to wrapping component.
 * dragState object will look like below
 * {
 *    isDragging: {Boolean} indicates whether a drag is in progress,
 *    connectIsOverTarget: {function} Inside wrapping component's render method,
 *                          wrap the component which you need to be able to check
 *                          isOverCurrent (see below), with this function.
 *    isOverCurrent: {Boolean} If a component is connected for isOver check (see above prop),
 *                             indicates whether drag is over component
 * }
 *
 * @param {React.Component} Component wrapping component
 */
export function withDragStateKnowledge(Component) {
    return DropTarget(ITEM_TYPES.NODE, {}, (connect, monitor) => {
        return {
            dragState: {
                isDragging: monitor.getItem() !== null,
                connectIsOverTarget: connect.dropTarget(),
                isOverCurrent: monitor.isOver({ shallow: true }),
            },
        };
    })(Component);
}
