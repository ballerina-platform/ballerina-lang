import React from 'react';
import PropTypes from 'prop-types';
import cn from 'classnames';
import { withDropEnabled } from './drop-target';

class SVGRectDropZone extends React.Component {
    render() {
        const { x, y, width, height, className, isOver, connectDropTarget } = this.props;
        const rectProps = {
            x, y, width, height,
        };
        return connectDropTarget(
            <rect {...rectProps} className={cn(className, { active: isOver })} />
        );
    }
}

SVGRectDropZone.propTypes = {
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    className: PropTypes.string,
    connectDropTarget: PropTypes.func.isRequired,
    isOver: PropTypes.bool,
    isOverCurrent: PropTypes.bool,
    canDrop: PropTypes.bool,
};

export default withDropEnabled(SVGRectDropZone);
