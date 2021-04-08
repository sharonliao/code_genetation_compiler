package AST;

import Visitors.Visitor;

public class FuncHeadNode extends Node{
    //<funcHead> ::= 'func' 'id' <ClassMethod> '(' <fParams> ')' ':' <funcDeclTail>

    public FuncHeadNode(){
        super("");
    }

    public FuncHeadNode(Node p_parent){
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }

}



