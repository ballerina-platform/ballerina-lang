import React from 'react';
import PropTypes from 'prop-types';

class CanvasDecorator extends React.Component {

    render() {
        return <svg className="svg-container" width="100%" height={this.props.height}>
                  {this.props.children}
              </svg>;
    }
}

CanvasDecorator.propTypes = {
	height: PropTypes.number,
    children: PropTypes.node.isRequired
}


export default CanvasDecorator;
