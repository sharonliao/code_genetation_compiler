package AST;

import Visitors.Visitor;

import java.util.List;

public class InheritListNode extends Node {
    public InheritListNode() {
        super("");
    }

    public InheritListNode(Node p_parent) {
        super("", p_parent);
    }

    public InheritListNode(List<Node> p_listOfInheritNodes) {
        super("");
        for (Node child : p_listOfInheritNodes)
            this.addChild(child);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);

    }
}

