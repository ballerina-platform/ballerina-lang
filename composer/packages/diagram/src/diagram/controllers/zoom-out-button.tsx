import React, { StatelessComponent } from "react";
import { Button, Icon } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomOutButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (
                    <Button icon onClick={diagContext.zoomOut} disabled={(diagContext.zoomLevel <= 1)}>
                        <Icon className="fw fw-zoom-out" title="Zoom Out" />
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
