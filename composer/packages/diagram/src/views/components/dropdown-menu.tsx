import { getCodePoint } from "@ballerina/font";
import React, { StatelessComponent } from "react";
import { Dropdown } from "semantic-ui-react";
import { SimplePoint } from "../../view-model/simple-point";
import { DropDownMenuTrigger } from "./dropdown-menu-trigger";

export interface DropDownMenuProps {
    triggerPosition: SimplePoint;
    className?: string;
    items: DropDownItem[];
    triggerIcon: string;
}

export interface DropDownItem {
    title: string;
    onSelect?: () => void;
    icon?: string;
}

export const DropDownMenu: StatelessComponent<DropDownMenuProps> =
    ({ triggerPosition, className, items, triggerIcon }) => {
    return <DropDownMenuTrigger position={triggerPosition}>
            <Dropdown
                inline
                icon={<span className="trigger-icon">{getCodePoint(triggerIcon)}</span>}
                className={className} >
                <Dropdown.Menu>
                    {items.map((item) => (
                         <Dropdown.Item
                            text={item.title}
                            onClick={item.onSelect}
                            icon={item.icon ? item.icon : undefined}
                            key={item.title}
                        />
                    ))}
                </Dropdown.Menu>
            </Dropdown>
    </DropDownMenuTrigger>;
};
