import React, { StatelessComponent } from "react";
import { Button, Icon } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomOutButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomOut, zoomLevel, hasSyntaxErrors }) => {
                return (
                    <Button
                        icon
                        onClick={zoomOut}
                        disabled={hasSyntaxErrors || (zoomLevel <= 1)}>
                        <Icon className="fw fw-zoom-out" title="Zoom Out" />
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
