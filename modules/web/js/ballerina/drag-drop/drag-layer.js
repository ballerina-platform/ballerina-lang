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
    const transform = `translate(${x}px, ${y}px)`;
    return {
        transform,
        WebkitTransform: transform,
    };
}

class SVGIconDragLayer {
    render() {
        const { item: { icon }, isDragging } = this.props;
        if (!isDragging) {
            return null;
        }
        return (
            <div style={layerStyles}>
                <div style={getItemStyles(this.props)}>
                    <i className={`fw fw-${icon}`} />
                </div>
            </div>
        );
    }
}

SVGIconDragLayer.propTypes = {
    item: PropTypes.objectOf(Object).isRequired,
    currentOffset: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }).isRequired,
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
