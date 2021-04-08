package AST;

import Visitors.Visitor;

public class AparamList  extends Node {

    public AparamList() {
        super("");
    }

    public AparamList(Node p_parent) {
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}