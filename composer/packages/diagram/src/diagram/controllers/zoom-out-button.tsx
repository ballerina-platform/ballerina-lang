import React, { StatelessComponent } from "react";
import { Button } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomOutButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (
                    <Button icon onClick={diagContext.zoomOut}>
                        <i className="fw fw-zoom-out" title="Zoom Out" />
                        {children}
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
