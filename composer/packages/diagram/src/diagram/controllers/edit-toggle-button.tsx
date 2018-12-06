import React, { StatelessComponent } from "react";
import { Button, Icon, Menu } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const EditToggleButton: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                const className = diagContext.editingEnabled
                    ? "secondary"
                    : "primary";
                const icon = diagContext.editingEnabled
                    ? "fw-uneditable"
                    : "fw-edit";
                const text = diagContext.editingEnabled
                    ? "Close Edit"
                    : "Edit";
                return (
                    <Menu.Item onClick={diagContext.toggleEditing}>
                        <Button.Group size="tiny">
                            <Button className={`icon ${className}`}>
                                <Icon className={`fw ${icon}`} /> {text}
                            </Button>
                        </Button.Group>
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
