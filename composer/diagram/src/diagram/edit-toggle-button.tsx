import React from "react";
import { DiagramContext, IDiagramContext } from "./diagram-context";

export class EditToggleButton extends React.Component {

    public static contextType = DiagramContext;

    public render() {
        const context: IDiagramContext = this.context;
        return <React.Fragment>
            <button onClick={context.toggleEditing} >Toggle Edit Enabled</button>
        </React.Fragment>;
    }
}
