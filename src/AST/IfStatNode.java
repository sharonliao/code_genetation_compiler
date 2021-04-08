package AST;

import Visitors.Visitor;

public class IfStatNode extends Node{

    public IfStatNode(){
        super("");
    }


    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }

}

