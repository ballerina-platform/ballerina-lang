import { DropTarget } from 'react-dnd';
import { ITEM_TYPES } from './constants';

/**
 * Enable drop on given compenent
 *
 * @param {Componet} DropArea React Component for droppable area
 * @param {Node} targetNode Target Node
 *
 */
export function withDropEnabled(DropArea, targetNode) {
    // drop target spec
    const dropSpec = {
        drop: (props, monitor, component) => {

        },
        hover: (props, monitor, component) => {

        },
        canDrop: (props, monitor, component) => {

        },
    };

    // Specifies which props to inject into component
    function collect(connect, monitor) {
        return {
            connectDropTarget: connect.dropTarget(),
            isOver: monitor.isOver(),
            isOverCurrent: monitor.isOver({ shallow: true }),
            canDrop: monitor.canDrop(),
        };
    }
    return DropTarget(ITEM_TYPES.NODE, dropSpec, collect)(DropArea);
}
