package AST;

import Visitors.Visitor;

public class QmNode extends Node{
    //node.data = sign
    public QmNode(){
        super("");
    }


    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}
