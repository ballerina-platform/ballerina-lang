import { Color } from 'vscode';

class Node {
    scope: string;
    color: Color;
    parent: any;
    children: Node[];

    constructor(data: string, scopeColor: Color) {
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

    const node1 = new Node("source.ballerina", new Color(0, 0, 255, 0));
    const tree = new ScopeTree(node1);

    const node2 = new Node("string.ballerina", new Color(255, 0, 0, 0));
    tree.link(node1, node2);

    const node5 = new Node("keyword.ballerina", new Color(100, 100, 0, 0));
    tree.link(node1, node5);

    const node3 = new Node("string.begin.ballerina", new Color(0, 255, 0, 0));
    tree.link(node2, node3);

    const node4 = new Node("string.end.ballerina", new Color(0, 100, 0, 0));
    tree.link(node2, node4);

    return tree.traverse(scope);

}
