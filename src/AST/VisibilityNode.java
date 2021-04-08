package AST;

import Visitors.Visitor;

public class VisibilityNode extends Node {
    public VisibilityNode(String p_data){
        super(p_data);
    }

    public VisibilityNode(String p_data, Node p_parent){
        super(p_data, p_parent);
    }

    public VisibilityNode(String p_data, String p_type){
        super(p_data, p_type);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }
}

