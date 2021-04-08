package AST;

import Visitors.Visitor;

public class ContinueStatNode extends Node{

    public ContinueStatNode(){
        super("");
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}
