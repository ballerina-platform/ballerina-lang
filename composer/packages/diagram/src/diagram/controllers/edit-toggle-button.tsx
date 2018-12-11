import React, { StatelessComponent } from "react";
import { Button, Icon, Menu } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const EditToggleButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ editingEnabled, hasSyntaxErrors, toggleEditing }) => {
                const className = editingEnabled
                    ? "secondary"
                    : "primary";
                const icon = editingEnabled
                    ? "fw-uneditable"
                    : "fw-edit";
                const text = editingEnabled
                    ? "Close Edit"
                    : "Edit";
                return (
                    <Menu.Item>
                        <Button.Group size="tiny">
                            <Button
                                disabled={hasSyntaxErrors}
                                className={`icon ${className}`}
                                onClick={toggleEditing}
                            >
                                <Icon className={`fw ${icon}`} /> {text}
                            </Button>
                        </Button.Group>
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
