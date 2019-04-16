import classNames from "classnames";
import React from "react";
import { getCodePoint } from "../../utils";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGDropDownMenuTrigger } from "./svg-dropdown-menu-trigger";

export interface SVGDropDownMenuProps {
    triggerPosition: SimplePoint;
    menuWidth?: number;
    className?: string;
    items: SVGDropDownItem[];
    triggerIcon: string;
    onMouseOut?: () => void;
    onMouseOver?: () => void;
}

export interface SVGDropDownMenuState {
    active: boolean;
}

export interface SVGDropDownItem {
    name: string;
    onClick?: () => void;
    icon?: string;
}

export class SVGDropDownMenu extends React.Component<SVGDropDownMenuProps, SVGDropDownMenuState> {

    public state = {
        active: false
    };

    private wrapperRef = React.createRef<SVGGElement>();

    constructor(props: any, context: any) {
        super(props, context);
        this.handleClickOutside = this.handleClickOutside.bind(this);
    }

    public componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
    }

    public componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

    public render() {
        const { triggerIcon, triggerPosition, triggerPosition: { x, y },
                items, className, onMouseOut, onMouseOver } = this.props;
        const { active } = this.state;
        const btnRadius = 8;
        const itemHeight = 25;
        const itemWidth = 150;
        const iconOffsetLeft = 5;
        const textOffsetLeft = 25;

        const menuItemPosition = {
            x: x + btnRadius / 2,
            y: y + btnRadius / 2
        };
        const menuItems = items.map(({ name, icon, onClick }, index) => {
                const itemPosition = {
                    x: menuItemPosition.x,
                    y: menuItemPosition.y + index * itemHeight
                };
                const itemSize = {
                    height: itemHeight,
                    width: itemWidth
                };
                const iconPosition = {
                    x: itemPosition.x + iconOffsetLeft,
                    y: itemPosition.y + itemSize.height / 2
                };
                const textPosition = {
                    x: itemPosition.x + textOffsetLeft,
                    y: itemPosition.y + itemSize.height / 2
                };

                return <g className="item" onClick={() => {
                    if (onClick) {
                        onClick();
                    }
                    this.setState({
                        active: false
                    });
                }}>
                    <rect
                        className="item-body"
                        {...itemPosition}
                        {...itemSize}
                    />
                    {icon && <text {...iconPosition} className="item-icon" >
                        {getCodePoint(icon)}</text>}
                    <text {...textPosition} className="item-title">{name}</text>
                </g>;
        });
        return <SVGDropDownMenuTrigger
                icon={triggerIcon}
                position={triggerPosition}
                radius={btnRadius}
                onClick={() => {
                    this.setState({
                        active: !active
                    });
                }}
                className={classNames("svg-dropdown-menu", className, { active })}
                onMouseOut={onMouseOut}
                onMouseOver={onMouseOver}
            >
                {active &&
                    <g className="content" ref={this.wrapperRef}>
                        {menuItems}
                    </g>
                }
        </SVGDropDownMenuTrigger>;
    }

    private handleClickOutside(event: MouseEvent) {
        if (this.wrapperRef && this.wrapperRef.current && !this.wrapperRef.current.contains(event.target as Node)) {
            this.setState({
                active: false
            });
        }
    }
}
