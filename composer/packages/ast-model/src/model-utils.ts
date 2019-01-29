import {
    ASTNode, Invocation, ObjectType, SimpleVariableRef,
    TypeDefinition, Variable, VariableDef, WorkerReceive
} from "./ast-interfaces";
import { Visitor } from "./base-visitor";
import { ASTKindChecker } from "./check-kind-util";

const metaNodes = ["viewState", "ws", "position", "parent"];

export function traversNode(node: ASTNode, visitor: Visitor, parent?: ASTNode) {
    let beginVisitFn: any = (visitor as any)[`beginVisit${node.kind}`];
    if (!beginVisitFn) {
        beginVisitFn = visitor.beginVisitASTNode && visitor.beginVisitASTNode;
    }

    if (beginVisitFn) {
        beginVisitFn.bind(visitor)(node, parent);
    }

    const keys = Object.keys(node);

    keys.forEach((key) => {
        if (metaNodes.includes(key)) {
            return;
        }

        const childNode = (node as any)[key] as any;
        if (Array.isArray(childNode)) {
            childNode.forEach((elementNode) => {
                if (!elementNode.kind) {
                    return;
                }

                traversNode(elementNode, visitor, node);
            });
            return;
        }

        if (!childNode.kind) {
            return;
        }

        traversNode(childNode, visitor, node);
    });

    let endVisitFn: any = (visitor as any)[`endVisit${node.kind}`];
    if (!endVisitFn) {
        endVisitFn = visitor.endVisitASTNode && visitor.endVisitASTNode;
    }
    if (endVisitFn) {
        endVisitFn.bind(visitor)(node, parent);
    }
}

export function isActionInvocation(node: ASTNode): Invocation | boolean {
    let invocation;
    traversNode(node, {
        beginVisitInvocation(element: Invocation) {
            if (element.actionInvocation) {
                invocation = element;
            }
        }
    });
    if (invocation) {
        // Return identifire of the endpoint.
        return invocation;
    }
    return false;
}

export function getEndpointName(node: Invocation): string | undefined {
    if (node.expression && ASTKindChecker.isSimpleVariableRef(node.expression)) {
        const simpleVariableRef = node.expression as SimpleVariableRef;
        return simpleVariableRef.variableName.value;
    }
    return;
}

export function isWorker(node: ASTNode) {
    if (ASTKindChecker.isVariableDef(node)) {
        if (ASTKindChecker.isVariable((node as VariableDef).variable)) {
            const name: string = ((node as VariableDef).variable as Variable).name.value;
            if (/^0.*/.test(name)) {
                return true;
            }
        }
    }
    return false;
}

export function isWorkerFuture(node: ASTNode) {
    if (ASTKindChecker.isVariableDef(node)) {
        if (ASTKindChecker.isVariable((node as VariableDef).variable)) {
            const initialExp = ((node as VariableDef).variable as Variable).initialExpression;

            if (initialExp === undefined) {
                return false;
            }

            if (ASTKindChecker.isInvocation(initialExp)) {
                const exp = (initialExp as Invocation).expression;
                if (exp === undefined) {
                    return false;
                }
                if (ASTKindChecker.isSimpleVariableRef(exp)) {
                    const simpleVarName: string = (exp as SimpleVariableRef).variableName.value;
                    return /^0.*/.test(simpleVarName);
                }
            }
        }
    }
    return false;
}

export function isValidObjectType(node: ASTNode): boolean {
    if (ASTKindChecker.isTypeDefinition(node)) {
        const typeDefinition = node as TypeDefinition;
        if (ASTKindChecker.isObjectType(typeDefinition.typeNode)) {
            const objectType = typeDefinition.typeNode as ObjectType;
            // Check if it has nun interface functions.
            const functions = objectType.functions.filter((element) => {
                return !element.interface;
            });
            if (functions.length > 0) {
                return true;
            }
        }
    }
    return false;
}

export function isWorkerReceive(node: ASTNode): boolean | string {
    let found: boolean | string = false;
    traversNode(node, {
        beginVisitASTNode(element: ASTNode) {
            if (ASTKindChecker.isWorkerReceive(element)) {
                found = (element as WorkerReceive).workerName.value;
            }
        }
    });
    return found;
}
