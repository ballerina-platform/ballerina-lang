import * as _ from 'lodash';

export class ASTUtil {
    /**
     * Will convert json model in to source
     *
     * @static
     * @param {Object} json AST
     * @returns {String} source
     */
    public static getSource(ast: any): string {
        let source = '';
        let ws = ASTUtil.extractWS(ast);
        ws = _.sortBy(ws, ['i']);
        ws.forEach(element => {
            source += element.ws + element.text;
        });
        return source;
    }

    public static extractWS(json: any): any[] {
        let childName;
        let pot: any[] = [];

        if (json.ws !== undefined) {
            pot = pot.concat(json.ws);
        }
        // with the following loop we will recursivle dive in to the child nodes and extract ws.
        for (childName in json) {
            // if child name is position || whitespace skip convection.
            if (childName !== 'position' && childName !== 'ws') {
                const child = json[childName];
                if (_.isPlainObject(child)) {
                    pot = pot.concat(ASTUtil.extractWS(child));
                } else if (child instanceof Array) {
                    for (const item of child) {
                        pot = pot.concat(ASTUtil.extractWS(item));
                    }
                }
            }
        }

        return pot;
    }
}
