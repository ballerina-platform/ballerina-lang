import * as React from "react";
import { DiagramContext } from "../../diagram/index";

const ZOOM_STEP = 50;

const overlayGroupRef = React.createRef<SVGGElement>();

export const SvgCanvas: React.StatelessComponent<{
        width: number,
        height: number,
        zoom: number,
        fitToWidthOrHeight: boolean,
    }>
    = ({ width, height, children, zoom, fitToWidthOrHeight }) => {

    const canvasWidth = fitToWidthOrHeight ? "100%" : (width + (zoom * ZOOM_STEP));
    const canvasHeight = fitToWidthOrHeight ? "100%" : (height + (zoom * ZOOM_STEP));

    return (
        <DiagramContext.Consumer>
            {(diagContext) => {
                const disabledOpacity = (diagContext.hasSyntaxErrors) ? 0.3 : 1;
                const viewBox =  `0 0 ${width} ${height}`;
                return (
                    <DiagramContext.Provider value={{ ...diagContext, overlayGroupRef }} >
                        <div style={{ opacity: disabledOpacity }}>
                            <svg
                                className="diagram-canvas"
                                width={canvasWidth}
                                height={canvasHeight}
                                viewBox={viewBox}
                                preserveAspectRatio = {"xMinYMin"}
                            >
                                {children}
                                <g ref={overlayGroupRef} className="diagram-overlay" />
                            </svg >
                        </div>
                    </DiagramContext.Provider>
                );
            }}
        </DiagramContext.Consumer>
    );
};
