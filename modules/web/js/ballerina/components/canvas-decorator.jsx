import React from 'react';
import PropTypes from 'prop-types';

class CanvasDecorator extends React.Component {

    render() {
        return <svg className="svg-container" width={ this.props.bBox.w } height={ this.props.bBox.h }>
                  {this.props.children}
              </svg>;
    }
}

CanvasDecorator.propTypes = {
    height: PropTypes.number,
    children: PropTypes.node.isRequired
}


export default CanvasDecorator;
