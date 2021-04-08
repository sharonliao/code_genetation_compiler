package AST;

import Visitors.Visitor;

import java.util.List;

public class InheritNode extends Node {
    public InheritNode(){
        super("");
    }

    public InheritNode(Node p_parent){
        super("", p_parent);
    }

    public void accept(Visitor p_visitor) {
        System.out.println("visit InheritNode");
        p_visitor.visit(this);
    }
}

