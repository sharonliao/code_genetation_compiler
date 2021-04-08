package AST;

import Visitors.Visitor;

public class BreakStatNode extends Node{

    public BreakStatNode(){
        super("");
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}
