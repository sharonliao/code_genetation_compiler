package AST;

import Visitors.Visitor;

public class VarNode extends Node {

    public VarNode() {
        super("");
    }

    public VarNode(Node p_parent) {
        super("", p_parent);
    }


    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }

}