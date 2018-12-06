import React, { RefObject, StatelessComponent } from "react";
import { Menu, Sticky } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";
import { AddDefinitionsMenu } from "./add-definitions-menu";
import { EditToggleButton } from "./edit-toggle-button";
import { ModeToggleButton } from "./mode-toggle-button";
import { ZoomControllers } from "./zoom-controllers";

export const RESPOSIVE_MENU_TRIGGER = {
    HIDDEN_MODE: 460,
    ICON_MODE: 650
};

export const ControllerPanel: StatelessComponent<{
            stickTo: RefObject<HTMLDivElement>
        }> = (
            { children, stickTo }
        ) => {
    return <DiagramContext.Consumer>
        {({ diagramWidth: width }) => (
            <Sticky
                className="diagram-controllers"
                context={stickTo.current || undefined}
            >
                <Menu
                    className={
                        "top-menu "
                        + (width > RESPOSIVE_MENU_TRIGGER.HIDDEN_MODE ? "" : "hidden")
                        + (width > RESPOSIVE_MENU_TRIGGER.ICON_MODE ? "" : " mobile-top-bar")
                    }
                >
                    <Menu.Menu position="left">
                        <EditToggleButton />
                        <ModeToggleButton />
                        <ZoomControllers />
                        <AddDefinitionsMenu />
                    </Menu.Menu>
                </Menu>
            </Sticky>
        )}
    </DiagramContext.Consumer>;
};
