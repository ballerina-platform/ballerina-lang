import * as React from "react";
import { CompilationUnitViewState } from "../../view-model/index";

export const SvgCanvas: React.StatelessComponent<{ model: CompilationUnitViewState }>
    = ({ model, children }) => {

    return <svg
        className="ballerina-diagram"
        preserveAspectRatio="xMinYMin"
        width={model.bBox.w}
        height={model.bBox.h}
    >
        {children}
    </svg >;
};
