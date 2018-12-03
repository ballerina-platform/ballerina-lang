import React from "react";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGDropDownMenuTrigger } from "./svg-dropdown-menu-trigger";

export interface SVGDropDownMenuProps {
    triggerPosition: SimplePoint;
    menuWidth?: number;
    className?: string;
    items: SVGDropDownItem[];
    triggerIcon: string;
}

export interface SVGDropDownMenuState {
    active: boolean;
}

export interface SVGDropDownItem {
    title: string;
    onSelect?: () => void;
    icon?: string;
}

export class SVGDropDownMenu extends React.Component<SVGDropDownMenuProps, SVGDropDownMenuState> {

    public state = {
        active: false
    };

    public render() {
        const { triggerPosition, triggerPosition: { x, y }, menuWidth = 100 } = this.props;
        const btnRadius = 10;
        const menuPosition = {
            x: x + btnRadius / 2,
            y: y + btnRadius / 2
        };
        return <SVGDropDownMenuTrigger
                position={triggerPosition}
                radius={btnRadius}
                onClick={() => {
                    this.setState({
                        active: !this.state.active
                    });
                }}
            >
                {this.state.active &&
                    <g className="">

                    </g>
                }
        </SVGDropDownMenuTrigger>;
    }
}
