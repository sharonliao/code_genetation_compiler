package AST;

import Visitors.Visitor;

public class IndiceRepNode  extends Node {

    public IndiceRepNode() {
        super("");
    }

    public IndiceRepNode(Node p_parent) {
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}