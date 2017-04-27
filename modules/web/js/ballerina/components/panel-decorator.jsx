import React from 'react'

class PanelDecorator extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const titleHeight = 35;
        const iconSize = 14;
        return ( <g>
                     <g>
                         <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleHeight } rx="0" ry="0" className="headingRect" data-original-title="" title=""></rect>
                         <g>
                             <rect x={ bBox.x + bBox.w - 25} y={ bBox.y + 1} width={ titleHeight - 1 } height={ titleHeight - 1 } rx="0" ry="0" className="heading-icon-wrapper hoverable heading-icon-collpase-wrapper" data-original-title="" title=""></rect>
                             <rect x={ bBox.x + bBox.w - 19.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } rx="0" ry="0" title="" className="headingExpandIcon" style={{opacity: 0.4}} data-original-title="Collapse Pane"></rect>
                             <line x1={ bBox.x + bBox.w - 25} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 25} y2={ bBox.y + 20} className="operations-separator"></line>
                             <line x1={ bBox.w } y1={ bBox.y + 5} x2={ bBox.w } y2={ bBox.y + 20} className="operations-separator"></line>
                             <line x1={ bBox.x + bBox.w - 75} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 75} y2={ bBox.y + 20} className="operations-separator"></line>
                             <line x1={ bBox.x + bBox.w - 100} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 100} y2={ bBox.y + 20} className="operations-separator"></line>
                             <rect x={ bBox.w } y={ bBox.y + 1} width={ titleHeight - 1 } height={ titleHeight - 2 } rx="0" ry="0" className="heading-icon-wrapper heading-icon-delete-wrapper" data-original-title="" title=""></rect>
                             <rect x={ bBox.x + bBox.w - 44.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } rx="0" ry="0" className="headingDeleteIcon" style={{opacity: 0.4}} data-original-title="" title=""></rect>
                             <rect x={ bBox.x + bBox.w - 75} y={ bBox.y + 1} width={ titleHeight - 1 } height={ titleHeight - 2 } rx="0" ry="0" className="heading-icon-wrapper heading-icon-annotation-wrapper" data-original-title="" title=""></rect>
                             <rect x={ bBox.x + bBox.w - 59.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } rx="0" ry="0" title="" className="headingAnnotationBlackIcon" style={{opacity: 0.4}} data-original-title="Annotations"></rect>
                             <rect x={ bBox.x + bBox.w - 100} y={ bBox.y + 1} width={ titleHeight - 1 } height={ titleHeight - 2 } rx="0" ry="0" className="heading-icon-wrapper heading-icon-arguments-wrapper" data-original-title="" title=""></rect>
                             <rect x={ bBox.x + bBox.w - 94.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } rx="0" ry="0" title="" className="headingArgumentsBlackIcon" style={{opacity: 0.4}} data-original-title="Arguments"></rect>
                         </g>
                         <rect x={ bBox.x + 1 } y={ bBox.y + 1} width={ titleHeight } height={ titleHeight } rx="0" ry="0" className="resourceHeadingIconHolder" data-original-title="" title=""></rect>
                         <rect x={ bBox.x } y={ bBox.y } width={ titleHeight } height={ titleHeight } rx="0" ry="0" className="headingRectIcon" data-original-title="" title=""></rect>
                     </g>
                        {this.props.children}
                     <g>
                         <rect x={ bBox.x } y={ bBox.y + titleHeight} width={ bBox.w } height={ bBox.h - titleHeight } rx="0" ry="0" className="resource-content" fill="#fff"></rect>
                     </g>
                 </g>);
    }
}

export default PanelDecorator;
