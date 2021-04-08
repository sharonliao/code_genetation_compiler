package AST;

import Visitors.Visitor;

import java.util.List;

public class VarDeclListNode extends Node{
    public VarDeclListNode(){
        super("");
    }

    public VarDeclListNode(Node p_parent){
        super("", p_parent);
    }

    public VarDeclListNode(List<Node> p_listOfVarDeclNodes){
        super("");
        for (Node child : p_listOfVarDeclNodes)
            this.addChild(child);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}

