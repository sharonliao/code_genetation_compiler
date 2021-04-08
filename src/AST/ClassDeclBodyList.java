package AST;

import Visitors.Visitor;

import java.util.List;

public class ClassDeclBodyList extends Node {

    public ClassDeclBodyList(){
        super("");
    }

    public ClassDeclBodyList(Node p_parent){
        super("", p_parent);
    }

    public ClassDeclBodyList(List<Node> p_listOfClassDeclBody){
        super("");
        for (Node child : p_listOfClassDeclBody)
            this.addChild(child);
    }

    public void accept(Visitor p_visitor) {
        p_visitor.visit(this);
    }

}
