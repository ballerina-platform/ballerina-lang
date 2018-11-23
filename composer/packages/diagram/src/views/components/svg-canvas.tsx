import * as React from "react";
import { DiagramContext } from "../../diagram/index";
import { CompilationUnitViewState } from "../../view-model/index";

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
                    h: bBox.h,
                    w: bBox.w
                };
                const editMode = diagContext.editingEnabled;
                const viewBox = editMode ? "" : `0 0 ${bBox.w} ${bBox.h}`;
                return (
                    <div style={{ width: svgSize.w * zoom, height: svgSize.h * zoom }} >
                        <svg
                            className="diagram-canvas"
                            preserveAspectRatio="xMinYMin"
                            width={svgSize.w * zoom}
                            height={svgSize.h * zoom}
                            viewBox={viewBox}
                        >
                            {children}
                        </svg >
                    </div>
                );
            }}
        </DiagramContext.Consumer>
    );
};
