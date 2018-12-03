import React, { StatelessComponent } from "react";
import { DiagramContext } from "../../diagram/diagram-context";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGCircleButton } from "./svg-circle-button";
import { SVGOverlayComponent } from "./svg-overlay-component";

export interface SVGDropDownMenuTriggerProps {
    position: SimplePoint;
    className?: string;
    onClick?: () => void;
    radius?: number;
    icon?: string;
}

export const SVGDropDownMenuTrigger: StatelessComponent<SVGDropDownMenuTriggerProps> =
    ({ position, onClick, children, radius = 10, icon = "add" }) => {
    const btnProps = {
        icon, onClick, position, radius
    };
    return <DiagramContext.Consumer>
        {({ editingEnabled }) => {
            return editingEnabled &&
                <SVGOverlayComponent>
                   <SVGCircleButton {...btnProps} />
                   {children}
                </SVGOverlayComponent>;
        }}
    </DiagramContext.Consumer>;
};
