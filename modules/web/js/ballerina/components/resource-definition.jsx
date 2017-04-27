import React from 'react'

class ResourceDefinition extends React.Component {

    render() {
        return (<g>
                  <g width="1650" height="495" x="50" y="150">
                      <defs>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/down.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/up.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/resource.svg" x="5" y="5" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/delete.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/delete-red.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/annotation.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/annotation-black.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/import.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/import-black.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/export.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                          <pattern width="100%" height="100%">
                              <image xlinkHref="images/export-black.svg" x="0" y="0" width="14" height="14"></image>
                          </pattern>
                      </defs>
                      <g>
                          <rect x="50" y="125" width="1650" height="25" rx="0" ry="0" className="headingRect" data-original-title="" title=""></rect>
                          <g transform="translate(0,0)">
                              <rect x="1675" y="126" width="24" height="24" rx="0" ry="0" className="heading-icon-wrapper hoverable heading-icon-collpase-wrapper" data-original-title="" title=""></rect>
                              <rect x="1680.5" y="130.5" width="14" height="14" rx="0" ry="0" title="" className="headingExpandIcon" style={{opacity: 0.4}} data-original-title="Collapse Pane"></rect>
                              <line x1="1675" y1="130" x2="1675" y2="145" className="operations-separator"></line>
                              <line x1="1650" y1="130" x2="1650" y2="145" className="operations-separator"></line>
                              <line x1="1625" y1="130" x2="1625" y2="145" className="operations-separator"></line>
                              <line x1="1600" y1="130" x2="1600" y2="145" className="operations-separator"></line>
                              <rect x="1650" y="126" width="24" height="23" rx="0" ry="0" className="heading-icon-wrapper heading-icon-delete-wrapper" data-original-title="" title=""></rect>
                              <rect x="1655.5" y="130.5" width="14" height="14" rx="0" ry="0" className="headingDeleteIcon" style={{opacity: 0.4}} data-original-title="" title=""></rect>
                              <rect x="1625" y="126" width="24" height="23" rx="0" ry="0" className="heading-icon-wrapper heading-icon-annotation-wrapper" data-original-title="" title=""></rect>
                              <rect x="1630.5" y="130.5" width="14" height="14" rx="0" ry="0" title="" className="headingAnnotationBlackIcon" style={{opacity: 0.4}} data-original-title="Annotations"></rect>
                              <rect x="1600" y="126" width="24" height="23" rx="0" ry="0" className="heading-icon-wrapper heading-icon-arguments-wrapper" data-original-title="" title=""></rect>
                              <rect x="1605.5" y="130.5" width="14" height="14" rx="0" ry="0" title="" className="headingArgumentsBlackIcon" style={{opacity: 0.4}} data-original-title="Arguments"></rect>
                          </g>
                          <rect x="51" y="126" width="25" height="25" rx="0" ry="0" className="resourceHeadingIconHolder" data-original-title="" title=""></rect>
                          <rect x="50" y="125" width="25" height="25" rx="0" ry="0" className="headingRectIcon" data-original-title="" title=""></rect>
                      </g>
                      <g>
                          <rect x="50" y="150" width="1650" height="470" rx="0" ry="0" className="resource-content" fill="#fff"></rect>
                          <g className="client-life-line">
                              <line x1="150" y1="275" x2="150" y2="565"></line>
                              <polygon points=" 150,307 182,275 150,243 118,275" strokeLinejoin="round"></polygon>
                              <polygon points=" 150,597 182,565 150,533 118,565" strokeLinejoin="round"></polygon><text x="150" y="275" textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">client</text><text x="150" y="565" textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">client</text>
                              <g></g>
                          </g>
                          <g className="default-worker-life-line">
                              <line x1="390.873046875" y1="275" x2="390.873046875" y2="565"></line>
                              <rect x="330.873046875" y="260" width="120" height="30" rx="0" ry="0" className="connector-life-line-top-polygon"></rect>
                              <rect x="330.873046875" y="550" width="120" height="30" rx="0" ry="0" className="connector-life-line-bottom-polygon"></rect><text x="390.873046875" y="275" textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">ResourceWorker</text><text x="390.873046875" y="565" textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">ResourceWorker</text>
                              <g>
                                  <g className="statement-container">
                                      <rect x="290" y="275" width="201.74609375" height="290" rx="0" ry="0" className="main-drop-zone"></rect>
                                      <rect x="300" y="345" width="120" height="30" rx="0" ry="0" className="inner-drop-zone"></rect>
                                      <rect x="330.873046875" y="405" width="120" height="30" rx="0" ry="0" className="inner-drop-zone"></rect>
                                  </g>
                              </g>
                          </g>
                          <g className="start-action" transform="translate(0,0)">
                              <rect x="330.873046875" y="320" width="120" height="30" rx="0" ry="0"></rect><text x="390.873046875" y="335" textAnchor="middle" alignmentBaseline="central" dominantBaseline="central">start</text>
                              <g className="message-container">
                                  <line x1="150" y1="335" x2="330.873046875" y2="335" className="message"></line>
                                  <polyline points="325.873046875,330 330.873046875,335 325.873046875,340" className="action-arrow"></polyline>
                              </g>
                          </g>
                          <g>
                              <rect x="300" y="375" width="181.74609375" height="30" rx="0" ry="0" className="statement-rect"></rect><text x="390.873046875" y="390" className="statement-text">http:convertToResponse(m)</text>
                              <g>
                                  <defs>
                                      <pattern width="100%" height="100%">
                                          <image xlinkHref="images/debug-point.svg" x="4" y="4" width="14" height="14"></image>
                                      </pattern>
                                      <pattern width="100%" height="100%">
                                          <image xlinkHref="images/debug-point-remove.svg" x="4" y="4" width="14" height="14"></image>
                                      </pattern>
                                  </defs>
                                  <rect x="470.74609375" y="364" width="22" height="22" rx="0" ry="0" className="statement-view-breakpoint-indicator" style={{display: "none"}}></rect>
                              </g>
                          </g>
                          <g>
                              <rect x="330.873046875" y="435" width="120" height="30" rx="0" ry="0" className="statement-rect"></rect><text x="390.873046875" y="450" className="statement-text">reply m</text>
                              <g>
                                  <line x1="330.873046875" y1="450" x2="150" y2="450" className="message"></line>
                                  <polyline points="150,450 155,445 155,455" className="action-arrow"></polyline>
                              </g>
                              <g>
                                  <defs>
                                      <pattern width="100%" height="100%">
                                          <image xlinkHref="images/debug-point.svg" x="4" y="4" width="14" height="14"></image>
                                      </pattern>
                                      <pattern width="100%" height="100%">
                                          <image xlinkHref="images/debug-point-remove.svg" x="4" y="4" width="14" height="14"></image>
                                      </pattern>
                                  </defs>
                                  <rect x="439.873046875" y="424" width="22" height="22" rx="0" ry="0" className="statement-view-breakpoint-indicator" style={{display: "none"}}></rect>
                              </g>
                          </g>
                      </g>
                  </g>
              </g>);
    }
}

export default ResourceDefinition;
