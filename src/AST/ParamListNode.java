package AST;

import Visitors.Visitor;

import java.util.List;

public class ParamListNode extends Node {
	
	public ParamListNode(){
		super("");
	}
	
	public ParamListNode(Node p_parent){
		super("", p_parent);
	}
	
	public ParamListNode(List<Node> p_listOfParamNodes){
		super("");
		for (Node child : p_listOfParamNodes)
			this.addChild(child);
	}
	
	public void accept(Visitor p_visitor) {
		p_visitor.visit(this);
	}
}
