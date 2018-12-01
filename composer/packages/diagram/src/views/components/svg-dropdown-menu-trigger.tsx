import React, { StatelessComponent } from "react";
import { DiagramContext } from "../../diagram/diagram-context";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGCircleButton } from "./svg-circle-button";
import { SVGOverlayComponent } from "./svg-overlay-component";

export interface SVGDropDownMenuTriggerProps {
    position: SimplePoint;
    className?: string;
    onClick?: () => void;
}

export const SVGDropDownMenuTrigger: StatelessComponent<SVGDropDownMenuTriggerProps> =
    ({ position, onClick, children }) => {
    return <DiagramContext.Consumer>
        {({ editingEnabled }) => {
            return editingEnabled &&
                <SVGOverlayComponent>
                   <SVGCircleButton position={position} radius={10} icon="add" onClick={onClick} />
                   {children}
                </SVGOverlayComponent>;
        }}
    </DiagramContext.Consumer>;
};
