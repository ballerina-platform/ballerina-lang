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
            const { meta = {}, nodeFactoryMethod, icon } = props.tool.attributes;
            return {
                node: nodeFactoryMethod(meta),
                icon,
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
            connectDragPreview: connect.dragPreview(),
            isDragging: monitor.isDragging(),
        };
    }
    return DragSource(ITEM_TYPES.NODE, dragSpec, collect)(ToolView);
}
