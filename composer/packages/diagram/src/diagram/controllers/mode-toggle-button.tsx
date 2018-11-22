import React, { StatelessComponent } from "react";
import { DiagramMode, IDiagramContext } from "../diagram-context";

export const ModeToggleButton: StatelessComponent<{}> = (
        { children },
        diagContext: IDiagramContext
    ) => {
    return  (
        <button onClick={() => {
                    const currentMode = diagContext.mode;
                    if (currentMode === DiagramMode.ACTION) {
                        diagContext.changeMode(DiagramMode.DEFAULT);
                    } else {
                        diagContext.changeMode(DiagramMode.ACTION);
                    }
                }}
        >
            {children}
        </button>
    );
};
