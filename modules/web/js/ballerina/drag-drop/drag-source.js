import { DragSource } from 'react-dnd';
import { ITEM_TYPES } from './constants';

/**
 * Enable drag on given compenent
 *
 * @param {Componet} ToolComponent React Component for Tool
 * @param {Object} toolDef Tool Definition
 *
 */
export function withDragEnabled(ToolComponent, toolDef) {
    const { nodeFactoryArgs = {}, nodeFactoryMethod } = toolDef;

    // drag source spec
    const dragSpec = {
        beginDrag: (props, monitor, component) => {
            return {
                node: nodeFactoryMethod(nodeFactoryArgs),
            };
        },
        endDrag: (props, monitor, component) => {

        },
    };

    // Specifies which props to inject into component
    function collect(connect, monitor) {
        return {
            // Call this function inside render()
            // to let React DnD handle the drag events:
            connectDragSource: connect.dragSource(),
            isDragging: monitor.isDragging(),
        };
    }
    return DragSource(ITEM_TYPES.NODE, dragSpec, collect)(ToolComponent);
}
