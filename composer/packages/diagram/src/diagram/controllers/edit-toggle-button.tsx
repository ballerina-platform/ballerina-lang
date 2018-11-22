import React, { StatelessComponent } from "react";
import { IDiagramContext } from "../diagram-context";

export const EditToggleButton: StatelessComponent<{}> = (
            { children },
            diagContext: IDiagramContext
        ) => {
    return  (
        <button onClick={diagContext.toggleEditing}>{children}</button>
    );
};
