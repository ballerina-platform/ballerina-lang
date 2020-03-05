import {
    ASTKindChecker, ASTUtil, BlockFunctionBody,
    Function as FunctionNode, Identifier, Literal, Variable, VariableDef
} from "@ballerina/ast-model";
import { BallerinaEndpoint } from "@ballerina/lang-service";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { DiagramContext } from "../../diagram/index";
import { FunctionViewState } from "../../view-model/index";
import { AddWorkerOrEndpointMenu } from "./add-worker-or-endpoint-menu";
import { Block } from "./block";
import { ClientLine } from "./client-line";
import { LifeLine } from "./life-line";
import { Panel } from "./panel";
import { StartInvocation } from "./start-invocation";
import { Worker } from "./worker";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Function = (props: { model: FunctionNode }) => {
    const { model } = props;
    const viewState: FunctionViewState = model.viewState;
    if (model.lambda || model.body === undefined || !ASTKindChecker.isBlockFunctionBody(model.body)) { return <g />; }
    const blockBody = model.body as BlockFunctionBody;
    return (
        <Panel model={viewState} title={model.name.value}
            icon={viewState.icon} astModel={model}>
            {!model.resource &&
            <ClientLine model={viewState.client.bBox} />}
            <LifeLine title="Default" icon="worker" model={viewState.defaultWorker.lifeline.bBox}
                astModel={model} />
            {blockBody.statements.filter((statement) => ASTUtil.isWorker(statement)).map((worker) => {
                const startY = viewState.defaultWorker.initHeight + viewState.defaultWorker.bBox.y
                    + config.lifeLine.header.height - config.statement.height;
                return <Worker
                    model={worker as VariableDef} startY={startY} client={viewState.defaultWorker.lifeline} />;
            })}
            {model.resource ?
                <StartInvocation
                    client={viewState.client}
                    worker={viewState.defaultWorker.lifeline}
                    y={viewState.defaultWorker.bBox.y + config.lifeLine.header.height}
                    label={
                        model.parameters && model.parameters.map((param: Variable | VariableDef, index) => {
                            if (model.resource && index === 0) {
                                return;
                            }
                            param = param as Variable;
                            return " " + param.name.value;
                        }).toString().replace(",", "")
                    }
                />
            :
                <StartInvocation
                    client={viewState.client}
                    worker={viewState.defaultWorker.lifeline}
                    y={viewState.defaultWorker.bBox.y + config.lifeLine.header.height}
                    label={
                        model.parameters && model.parameters.map((param: Variable , index) => {
                            if (model.resource && index === 0) {
                                return;
                            }
                            const defaultable = param.initialExpression !== undefined;
                            const name: Identifier = param.name;
                            const value: any = defaultable
                                ? (param.initialExpression as Literal).value // TODO: handle other expressions
                                : undefined;
                            return defaultable && value
                                    ? " " + name.value + " = " + value
                                    : " " + name.value;
                        }).toString()
                    }
                />
            }
            {blockBody && <Block model={blockBody} />}
            <DiagramContext.Consumer>
                {({ ast }) => (
                    <AddWorkerOrEndpointMenu
                        triggerPosition={viewState.menuTrigger}
                        onAddEndpoint={(epDef: BallerinaEndpoint) => {
                            if (blockBody && ast) {
                                ASTUtil.addEndpointToBlock(blockBody, ast, epDef, 0);
                            }
                        }}
                        onAddWorker={() => {
                            if (!(blockBody && ast)) {
                                return;
                            }
                            let nextWorkerIndex: number = blockBody.statements.length;
                            blockBody.statements.forEach((statement, i) => {
                                if (ASTUtil.isWorkerFuture(statement)) {
                                    nextWorkerIndex = i + 1;
                                }
                            });
                            ASTUtil.addWorkerToBlock(blockBody, ast, nextWorkerIndex);
                        }}
                    />
                )}
            </DiagramContext.Consumer>
        </Panel>);
};
