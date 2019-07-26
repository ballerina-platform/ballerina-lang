import * as React from "react";
import { DiagramContext } from "../../diagram/index";

const overlayGroupRef = React.createRef<SVGGElement>();

export const SvgCanvas: React.StatelessComponent<{
        panZoomRootRef: React.Ref<SVGAElement>,
    }>
    = ({ children, panZoomRootRef }) => {

    return (
        <DiagramContext.Consumer>
            {(diagContext) => {
                const viewBox =  `-20 -20 1000 1000`;
                return (
                    <DiagramContext.Provider value={{ ...diagContext, overlayGroupRef }} >
                        <svg
                            className="diagram-canvas"
                            viewBox={viewBox}
                            preserveAspectRatio = {"xMinYMin"}
                        >
                            <g ref={panZoomRootRef}>
                                {children}
                            </g>
                            <g ref={overlayGroupRef} className="diagram-overlay" />
                        </svg >
                    </DiagramContext.Provider>
                );
            }}
        </DiagramContext.Consumer>
    );
};
