import React, { StatelessComponent } from "react";
import ReactDOM from "react-dom";
import { DiagramContext } from "../../diagram/diagram-context";

export const SVGOverlayComponent: StatelessComponent<{}> =
    ({ children }) => {
    return <DiagramContext.Consumer>
        {({ overlayGroupRef }) => {
            return overlayGroupRef && overlayGroupRef.current
                && ReactDOM.createPortal(
                    children,
                    overlayGroupRef.current
                );
        }}
    </DiagramContext.Consumer>;
};
