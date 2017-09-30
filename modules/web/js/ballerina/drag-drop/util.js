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
