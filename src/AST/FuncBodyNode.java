package AST;

import Visitors.Visitor;

public class FuncBodyNode extends Node{
    //<funcBody> ::= '{' <MethodBodyVar> <StatementList> '}'

    public FuncBodyNode(){
        super("");
    }

    public FuncBodyNode(Node p_parent){
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}


