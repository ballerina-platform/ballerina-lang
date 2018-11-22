import React, { RefObject, StatelessComponent } from "react";
import { Sticky } from "semantic-ui-react";
import { EditToggleButton } from "./edit-toggle-button";
import { ModeToggleButton } from "./mode-toggle-button";

export const ControllerPanel: StatelessComponent<{
            stickTo: RefObject<HTMLDivElement>
        }> = (
            { children, stickTo }
        ) => {
    return <Sticky className="diagram-controllers" context={stickTo.current || undefined}>
        <div className="panel">
            <EditToggleButton>{"Toggle Editing"}</EditToggleButton>
            <ModeToggleButton>{"Toggle Mode"}</ModeToggleButton>
            {children}
        </div>
    </Sticky>;
};
