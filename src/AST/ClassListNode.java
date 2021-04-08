package AST;

import Visitors.Visitor;

import java.util.List;

public class ClassListNode extends Node {
	
	public ClassListNode(){
		super("");
	}
	
	public ClassListNode(Node p_parent){
		super("", p_parent);
	}
	
	public ClassListNode(List<Node> p_listOfClassNodes){
		super("");
		for (Node child : p_listOfClassNodes)
			this.addChild(child);
	}

	public void accept(Visitor p_visitor) {
		p_visitor.visit(this);
	}
}