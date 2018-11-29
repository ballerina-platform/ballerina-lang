import React, { StatelessComponent } from "react";
import { Menu } from "semantic-ui-react";
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
                    <Menu.Item onClick={diagContext.toggleEditing}
                        className={`menu-button ui button ${className}`}
                    >
                        <i className={`fw ${icon} menu-icon-right`} />
                        <span className="text">{text}</span>
                        {children}
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
