package AST;

import Visitors.Visitor;

public class FuncDeclareNode extends Node{
    public FuncDeclareNode(){
        super("");
    }

    public FuncDeclareNode(Node p_parent){
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}
