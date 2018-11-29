import React, { StatelessComponent } from "react";
import { Button } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomFitButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (
                    <Button icon onClick={diagContext.zoomFit}>
                        <i className="fw fw-fit" title="Zoom Fit" />
                        {children}
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
