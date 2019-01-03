import * as React from "react";
import { DiagramContext } from "../../diagram/index";
import { CompilationUnitViewState } from "../../view-model/index";

const overlayGroupRef = React.createRef<SVGGElement>();

export const SvgCanvas: React.StatelessComponent<{
        model: CompilationUnitViewState,
        zoom: number
    }>
    = ({ model, children, zoom }) => {
    const bBox = model.bBox;

    return (
        <DiagramContext.Consumer>
            {(diagContext) => {
                const svgSize = {
                    h: diagContext.diagramHeight,
                    w: diagContext.diagramWidth
                };
                const disabledOpacity = (diagContext.hasSyntaxErrors) ? 0.3 : 1;
                const viewBox =  `0 0 ${bBox.w} ${bBox.h}`;
                return (
                    <DiagramContext.Provider value={{ ...diagContext, overlayGroupRef }} >
                        <div style={{ width: svgSize.w * zoom, height: svgSize.h * zoom, opacity: disabledOpacity }}>
                            <svg
                                className="diagram-canvas"
                                preserveAspectRatio="xMinYMin"
                                width={svgSize.w * zoom}
                                height={svgSize.h * zoom}
                                viewBox={viewBox}
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
