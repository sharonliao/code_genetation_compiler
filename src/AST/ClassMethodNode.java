package AST;

import Visitors.Visitor;

public class ClassMethodNode extends Node{

    public ClassMethodNode(String p_data){
        super(p_data);
    }

    public ClassMethodNode(String p_data, Node p_parent){
        super(p_data, p_parent);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}

