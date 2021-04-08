package AST;

import Visitors.Visitor;

public class NotNode extends Node{
    //node.data = sign
    public NotNode(){
        super("");
    }


    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}

