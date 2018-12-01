import React, { StatelessComponent } from "react";
import { Button, Icon } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomInButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (
                    <Button icon onClick={diagContext.zoomIn}>
                        <Icon className="fw fw-zoom-in" title="Zoom In" />
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
