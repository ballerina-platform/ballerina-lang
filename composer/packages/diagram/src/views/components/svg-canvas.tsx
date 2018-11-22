import * as React from "react";
import { CompilationUnitViewState } from "../../view-model/index";

export const SvgCanvas: React.StatelessComponent<{ model: CompilationUnitViewState }>
    = ({ model, children }) => {

    return <svg
        className="diagram-canvas"
        preserveAspectRatio="xMinYMin"
        width={model.bBox.w}
        height={model.bBox.h}
    >
        {children}
    </svg >;
};
