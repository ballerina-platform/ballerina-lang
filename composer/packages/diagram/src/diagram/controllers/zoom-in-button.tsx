import React, { StatelessComponent } from "react";
import { Button } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomInButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (
                    <Button icon onClick={diagContext.zoomIn}>
                        <i className="fw fw-zoom-in" title="Zoom In" />
                        {children}
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
