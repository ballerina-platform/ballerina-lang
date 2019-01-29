
import { ASTKindChecker, ASTNode, ASTUtil, Block as BlockNode,
        Function as FunctionNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { BlockViewState } from "../../view-model/block";
import { FunctionViewState, ViewState } from "../../view-model/index";
import { ReturnViewState } from "../../view-model/return";
import { BlockDropdown } from "./block-dropdown";
import { Return } from "./return";

export class Block extends React.Component<{ model: BlockNode }, { isHovered: boolean }> {

    public state = {
        isHovered: false
    };

    public render() {
        const { model }  = this.props;
        const { isHovered } = this.state;
        const statements: React.ReactNode[] = [];
        const viewState: BlockViewState = model.viewState;
        const triggerPosition = viewState.menuTrigger;
        const { x, y, w: width, h: height } = viewState.hoverRect;
        const hoverRectProps = {
            height,
            width,
            x,
            y
        };
        statements.push(DiagramUtils.getComponents(model.statements));
        const additionalComponents: React.ReactNode[] = [];
        if (ASTKindChecker.isFunction(model.parent as ASTNode)) {
            const implicitReturn = ASTUtil.createReturnNode();
            const returnViewState: ViewState = ((model.parent as FunctionNode)
                    .viewState as FunctionViewState)
                    .implicitReturn;
            returnViewState.bBox.x = triggerPosition.x;
            returnViewState.bBox.y = triggerPosition.y;
            implicitReturn.viewState = returnViewState;
            if (!(implicitReturn.viewState as ReturnViewState).hidden) {
                additionalComponents.push(<Return model={implicitReturn} />);
            }
        }
        const dropDownProps = { model, isHovered, triggerPosition };
        return (
            <g className="worker-block-container">
                <rect
                    className="hover-rect"
                    {...hoverRectProps}
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
                {...additionalComponents}
                {<BlockDropdown {...dropDownProps} />}
            </g>);
    }
}
