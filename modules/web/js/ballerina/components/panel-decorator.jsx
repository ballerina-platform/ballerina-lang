import React from 'react'

class PanelDecorator extends React.Component {

    render() {
        const bBox = this.props.bBox,
              header_height = 50;

        return <g className="panel">
                  <g className="panel-header">
                      <rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} />
                  </g>;
                  <g className="panel-body">

                  </g>;
               </g>;
    }
}

export default PanelDecorator;
