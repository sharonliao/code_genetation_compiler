package Visitors.AST;

import AST.*;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Visitor to construct a string that represents the subexpression
 * of the subtree for which the current node is the head. 
 * 
 * This applies only to nodes that are part of expressions, i.e.
 * IdNode, AddOpNode, MultOpNode, and AssignStatp_node. 
 * 
 * Note that this is just as an example. Such functionality is not
 * required in the project. 
 * 
 * However, note that this is essentially how the code generation phase
 * will eventually proceed. 
 * 
 */

public class ReconstructSourceProgramVisitor  extends Visitor {
	
	public String m_outputfilename = new String();
	public HashMap<String,String> opMap = new HashMap<>(){
        {
            put("eq"," == ");
            put("neq"," != ");
            put("lt"," < ");
            put("gt"," > ");
            put("leq"," <= ");
            put("geq"," >= ");
            put("mq"," ? ");
            put("sr"," :: ");
        }
    };


	public ReconstructSourceProgramVisitor() {
	}
	
	public ReconstructSourceProgramVisitor(String p_filename) {
		this.m_outputfilename = p_filename; 
	}

	public void visit(FuncDeclareNode p_node){

	}

	public void visit(InheritNode p_node){

	}
	
	public void visit(ProgNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		for (Node child : p_node.getChildren()) {
			p_node.m_subtreeString += child.m_subtreeString; 
		}
		if (!this.m_outputfilename.isEmpty()) {
			File file = new File(this.m_outputfilename);
			try (PrintWriter out = new PrintWriter(file)){ 
			    out.println(p_node.m_subtreeString);
			}
			catch(Exception e){
				e.printStackTrace();}
		}
	};
	
	public void visit(ProgramBlockNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_subtreeString += "program{\n";
		for (Node child : p_node.getChildren()) {
			p_node.m_subtreeString += "  " + child.m_subtreeString + "\n"; 
		}
		p_node.m_subtreeString += "}\n";
	};

	public void visit(StatBlockNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_subtreeString += "{\n";
		for (Node child : p_node.getChildren()) {
			p_node.m_subtreeString += "  " + child.m_subtreeString + "\n"; 
		}
		p_node.m_subtreeString += "}\n";

	};
	
	public void visit(ClassNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_subtreeString += "class ";
		p_node.m_subtreeString += p_node.getChildren().get(0).getData() + "{\n";		
		for (int childindex = 1; childindex < p_node.getChildren().size(); childindex++ ) {
			Node child = p_node.getChildren().get(childindex);
			p_node.m_subtreeString += "  " + child.m_subtreeString + "\n"; 
		}
		p_node.m_subtreeString += "}";		
		
	};
	

	public void visit(FuncCallNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        // varNode 会有3种类型child： IdNode AparamList IdNode IndiceRepNode
		for (Node child : p_node.getChildren() )
			child.accept(this);

        String str = p_node.getChildren().get(0).m_subtreeString;
        Node preNode;
        Node curNode;
        for(int i = 1; i<p_node.getChildren().size(); i++ ) {
            preNode = p_node.getChildren().get(i - 1);
            curNode = p_node.getChildren().get(i);
            if (curNode.getClass().getSimpleName().equals("IdNode")) {
                str += "." + curNode.getSubtreeString();
            } else if (curNode.getClass().getSimpleName().equals("IndiceRepNode")) {
                str += curNode.getSubtreeString();
            } else if (curNode.getClass().getSimpleName().equals("AparamList")) {
                str += curNode.getSubtreeString();
            }
        }
        p_node.setSubtreeString(str);
	}; 
	
	public void visit(PutStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_subtreeString += "put(";
		for (Node child : p_node.getChildren() )
			p_node.m_subtreeString += child.m_subtreeString; 
		p_node.m_subtreeString += ");";		
	};

	public void visit(ReturnStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		p_node.m_subtreeString += "return(";
		for (Node child : p_node.getChildren() )
			p_node.m_subtreeString += child.m_subtreeString; 
		p_node.m_subtreeString += ");";		
	}; 
	
	public void visit(FuncDefNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		for (Node child : p_node.getChildren() )
			p_node.m_subtreeString += child.m_subtreeString; 
	};

	public void visit(ParamListNode p_node) {
			// propagate accepting the same visitor to all the children
			// this effectively achieves Depth-First AST Traversal
			for (Node child : p_node.getChildren() )
				child.accept(this);
			p_node.m_subtreeString += "(";
			boolean first = true;
			for (Node child : p_node.getChildren() ) {
				if (first)
					p_node.m_subtreeString += child.m_subtreeString;
				else
					p_node.m_subtreeString += " " + child.m_subtreeString;
				first = false;
			}
			p_node.m_subtreeString += ")";			
	 };

	public void visit(TypeNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.accept(this);
		}
			p_node.setSubtreeString(p_node.getData() + " ");
	};
	
	public void visit(VarDeclNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		for (Node child : p_node.getChildren() )
			p_node.m_subtreeString += child.m_subtreeString; 
		p_node.m_subtreeString += ";"; 
		
	 }; 

	public void visit(DimListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		for (Node child : p_node.getChildren()) {
			p_node.m_subtreeString += "[" + child.m_subtreeString + "]";
		}

	};
	
	public void visit(IdNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		p_node.setSubtreeString(p_node.getData());
	}
	
	public void visit(NumNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		p_node.setSubtreeString(p_node.getData());
	}

	public void visit(AddOpNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		p_node.setSubtreeString(	p_node.getChildren().get(0).getSubtreeString() + 
								p_node.getData() +
								p_node.getChildren().get(1).getSubtreeString() );
	}

	public void visit(MultOpNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		p_node.setSubtreeString(	p_node.getChildren().get(0).getSubtreeString() + 
								p_node.getData() +
								p_node.getChildren().get(1).getSubtreeString() );
	}
	
	public void visit(AssignStatNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		p_node.setSubtreeString(	p_node.getChildren().get(0).getSubtreeString() + 
								p_node.getData() +
								p_node.getChildren().get(1).getSubtreeString() + ";");
	}

	// Below are the visit methods for node types for which this visitor does
	// not apply. They still have to propagate acceptance of the visitor to
	// their children.
	
	public void visit(FuncDefListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		for (Node child : p_node.getChildren())
			p_node.m_subtreeString += child.m_subtreeString;		
	};
	
	public void visit(ClassListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};
	
	public void visit(Node p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(DimNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		for (Node child : p_node.getChildren() )
			child.accept(this);
		p_node.m_subtreeString = p_node.getData();
	};


	public void visit(IfStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit IfStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(QmNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit QmNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(BreakStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit BreakStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(ContinueStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit ContinueStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(ReadStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit ReadStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(RelOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        for (Node child : p_node.getChildren() )
            child.accept(this);
        String leftOpStr = p_node.getChildren().get(0).getSubtreeString();
        String rightOPStr = p_node.getChildren().get(1).getSubtreeString();
        String op = opMap.get(p_node.m_data) == null ? p_node.m_data : opMap.get(p_node.m_data);
        String str = leftOpStr + op + rightOPStr;
        p_node.setSubtreeString(str);
	};



	public void visit(SignNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit SignNode");
		String str = p_node.m_data + p_node.getChildren().get(0).getData();
		p_node.setSubtreeString(str);
	};

	public void visit(WhileStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit WhileStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);

	};

	public void visit(WriteStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit WriteStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(VarNode p_node){
		// varNode 会有两种类型child： IdNode IdNode IndiceRepNode
        for(Node child : p_node.getChildren()){
            child.accept(this);
        }

        String str = p_node.getChildren().get(0).m_subtreeString;
        Node preNode;
        Node curNode;
        for(int i = 1; i<p_node.getChildren().size(); i++ ){
            preNode = p_node.getChildren().get(i-1);
            curNode = p_node.getChildren().get(i);
            if(preNode.getClass().getSimpleName().equals(curNode.getClass().getSimpleName()) && preNode.getClass().getSimpleName().equals("IdNode")){
                str += "."+ curNode.getSubtreeString();
            }else if(curNode.getClass().getSimpleName().equals("IndiceRepNode")){
                str += curNode.getSubtreeString();
            }
        }
        p_node.setSubtreeString(str);
	}

	public void visit(AparamList p_node){
        String str = "(";
        for(Node child : p_node.getChildren()){
            child.accept(this);
            str += child.m_subtreeString + ",";
        }
        str = str.substring(0,str.length()-1) + ")";
        p_node.setSubtreeString(str);

	}


    public void visit(IndiceRepNode p_node){
	    String str = "";
	    for(Node child : p_node.getChildren()){
	        child.accept(this);
            str += "["+child.getSubtreeString()+"]";
        }
        p_node.setSubtreeString(str);
    }
}
