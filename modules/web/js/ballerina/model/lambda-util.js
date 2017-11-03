import getSourceOf from './source-gen';

export default function splitVariableDefByLambda(node) {
    const sourceFragments = getSourceOf(node, false, 0, true).split('$ function LAMBDA $');
    const lambdas = [];
    let i = 0;
    node.accept({
        beginVisit(n) {
            if (n.kind === 'Lambda' && i === 0) {
                lambdas.push(n.functionNode);
                i++;
            }
        },
        endVisit(n) {
            if (n.kind === 'Lambda') {
                i--;
            }
        },

    });
    return { sourceFragments, lambdas };
}
