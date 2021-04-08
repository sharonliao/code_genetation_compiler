package AST;

import Visitors.Visitor;

public class WhileStatNode extends Node{

    public WhileStatNode(){
        super("");
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}
