class Node {
    scope: string;
    color: String;
    parent: any;
    children: Node[];

    constructor(data: string, scopeColor: String) {
        this.scope = data;
        this.color = scopeColor;
        this.parent = null;
        this.children = [];
    }

    setChild(child: Node) {
        this.children.push(child);
    }

    setParent(parent: Node) {
        this.parent = parent;
    }
}

class Queue {
    data: Node[];

    constructor() {
        this.data = [];
    }

    enqueue(node: Node) {
        this.data.push(node);
    }

    dequeue() {
        return this.data.shift();
    }

}

class ScopeTree {
    root: Node;

    constructor(node: Node) {
        this.root = node;
    }

    traverse(scope: string) {
        const queue = new Queue();

        queue.enqueue(this.root);
        let cur = queue.dequeue();

        while (cur) {
            if (cur.scope === scope) {
                return cur.color;
            }
            else {
                for (var i = 0; i < cur.children.length; i++) {
                    queue.enqueue(cur.children[i]);
                }
                cur = queue.dequeue();
            }
        }
    }

    link(parent: Node, child: Node) {
        parent.setChild(child);
        child.setParent(parent);
    }
}

export function getScopeColor(scope: string) {

    const node1 = new Node("endpoint.ballerina", "#F2A618");
    const tree = new ScopeTree(node1);

    const node2 = new Node("unused.ballerina", "#FF751A");
    tree.link(node1, node2);

    const node5 = new Node("keyword.ballerina", "#FFFFFF");
    tree.link(node1, node5);

    const node3 = new Node("string.ballerina", "#000000");
    tree.link(node2, node3);

    return tree.traverse(scope);
}

export function getScopeName(scopeId: number) : string {
    let scopeArray: [string][] = [["endpoint.ballerina"],["unused.ballerina"]];
    let selectedScope = scopeArray[scopeId];
    let element;
    let scope = "";
    for(element of selectedScope){
        if(element !== "") {
            scope = element;
            break;
        }
    }
    return scope;
}
