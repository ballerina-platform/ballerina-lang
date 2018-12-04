
import { Block as BlockNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { BlockViewState } from "../../view-model/block";
import { BlockDropdown } from "./block-dropdown";

export class Block extends React.Component<{ model: BlockNode }, { isHovered: boolean }> {

    public state = {
        isHovered: false
    };

    public render() {
        const { model }  = this.props;
        const { isHovered } = this.state;
        const statements: React.ReactNode[] = [];
        const viewState: BlockViewState = model.viewState;
        const { x, y, w, h, leftMargin } = viewState.bBox;
        const triggerPosition = viewState.menuTrigger;
        const hoverRectBBox = {
            height: h, width : w + leftMargin, x: x - leftMargin,  y
        };
        statements.push(DiagramUtils.getComponents(model.statements));

        return (
            <g className="worker-block-container">
                <rect
                    className="hover-rect"
                    {...hoverRectBBox}
                    onMouseOver={() => {
                        this.setState({
                            isHovered: true
                        });
                    }}
                    onMouseOut={() => {
                        this.setState({
                            isHovered: false
                        });
                    }}
                    visibility="hidden"
                />
                {statements}
                {<BlockDropdown active={isHovered} triggerPosition={triggerPosition} model={model} />}
            </g>);
    }
}
