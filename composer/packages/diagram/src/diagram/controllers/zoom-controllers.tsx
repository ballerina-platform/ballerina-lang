import React, { StatelessComponent } from "react";
import { Button } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";
import { ZoomFitButton } from "./zoom-fit-button";
import { ZoomInButton } from "./zoom-in-button";
import { ZoomOutButton } from "./zoom-out-button";

export const ZoomControllers: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (!diagContext.editingEnabled &&
                    <Button.Group className="zoom-controllers">
                        <ZoomInButton />
                        <ZoomOutButton />
                        <ZoomFitButton />
                        {children}
                    </Button.Group>
                );
            }}
        </DiagramContext.Consumer>
    );
};
