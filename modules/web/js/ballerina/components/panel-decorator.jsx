import React from 'react';
import ImageUtils from './image-util';

class PanelDecorator extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const titleHeight = 25;
        const iconSize = 14;
        const collapsed = this.props.collapsed || false;
        return ( <g className="panel">
                     <g className="panel-header">
                         <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleHeight } rx="0" ry="0" className="headingRect" data-original-title="" title=""></rect>
                         <text x={ bBox.x + titleHeight } y={ bBox.y + titleHeight / 2 + 5 }>{this.props.title}</text>
                         <g className="panel-header-controls">
                             <rect x={ bBox.x + bBox.w - 19.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } rx="0" ry="0" title="" className="headingExpandIcon" style={{opacity: 0.4}} data-original-title="Collapse Pane"></rect>
                             <line x1={ bBox.x + bBox.w - 25} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 25} y2={ bBox.y + 20} className="operations-separator"></line>
                         </g>
                     </g>
                     <g className="panel-body">
                        { !collapsed &&
                             <rect x={ bBox.x } y={ bBox.y + titleHeight} width={ bBox.w } height={ bBox.h - titleHeight } rx="0" ry="0" className="resource-content" fill="#fff"></rect>
                        }
                        { !collapsed && this.props.children}
                     </g>
                 </g>);
    }
}

export default PanelDecorator;
