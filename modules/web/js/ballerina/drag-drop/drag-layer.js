import React from 'react';
import PropTypes from 'prop-types';
import { DragLayer } from 'react-dnd';

const layerStyles = {
    position: 'fixed',
    pointerEvents: 'none',
    zIndex: 100,
    left: 0,
    top: 0,
    width: '100%',
    height: '100%',
};

function getItemStyles(props) {
    const { currentOffset } = props;
    if (!currentOffset) {
        return {
            display: 'none',
        };
    }

    const { x, y } = currentOffset;
    const transform = `translate(${x}px, ${y - 10}px)`;
    return {
        transform,
        WebkitTransform: transform,
    };
}

class SVGIconDragLayer extends React.Component {
    render() {
        if (!this.props.isDragging) {
            return null;
        }
        const { item: { icon } } = this.props;
        return (
            <div style={layerStyles}>
                <div style={getItemStyles(this.props)}>
                    <i style={{ color: 'black' }} className={`fw fw-2x fw-${icon}`} />
                </div>
            </div>
        );
    }
}

SVGIconDragLayer.propTypes = {
    item: PropTypes.objectOf(Object),
    currentOffset: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    isDragging: PropTypes.bool.isRequired,
};

function collect(monitor) {
    return {
        item: monitor.getItem(),
        currentOffset: monitor.getSourceClientOffset(),
        isDragging: monitor.isDragging(),
    };
}

export default DragLayer(collect)(SVGIconDragLayer);
