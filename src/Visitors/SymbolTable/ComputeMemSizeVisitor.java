package Visitors.SymbolTable;

import AST.*;
import SymbolTable.SymTabEntry;
import SymbolTable.VarEntry;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

/** 
 */

public class ComputeMemSizeVisitor extends Visitor {

	public String  m_outputfilename = new String();

	public ComputeMemSizeVisitor() {
	}
	
	public ComputeMemSizeVisitor(String p_filename) {
		this.m_outputfilename = p_filename;
	}

	public int sizeOfEntry(Node p_node) {
		int size = 0;
		if(p_node.m_symtabentry.m_type == "int")
			size = 4;
		else if(p_node.m_symtabentry.m_type == "float")
			size = 8;
		// if it is an array, multiply by all dimension sizes
		VarEntry ve = (VarEntry) p_node.m_symtabentry;
		if(!ve.m_dims.isEmpty())
			for(Integer dim : ve.m_dims)
				size *= dim;	
		return size;
	}
	
	public int sizeOfTypeNode(Node p_node) {
		int size = 0;
		if(p_node.m_type == "int")
			size = 4;
		else if(p_node.m_type == "float")
			size = 8;
		return size;
	}
	
	public void visit(ProgNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		if (!this.m_outputfilename.isEmpty()) {
			File file = new File(this.m_outputfilename);
			try (PrintWriter out = new PrintWriter(file)){ 
			    out.println(p_node.m_symtab);
			}
			catch(Exception e){
				e.printStackTrace();}
		}
	};


	public void visit(FuncDeclareNode p_node){

	}

	public void visit(InheritNode p_node){

	}
	
	public void visit(ProgramBlockNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// compute total size and offsets along the way
		// this should be node on all nodes that represent
		// a scope and contain their own table
		for (SymTabEntry entry : p_node.m_symtab.m_symlist){
			entry.m_offset     = p_node.m_symtab.m_size - entry.m_size;
			p_node.m_symtab.m_size -= entry.m_size;
		}
	};
	
	public void visit(ClassNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// compute total size and offsets along the way		
		// this should be node on all nodes that represent
		// a scope and contain their own table
		for (SymTabEntry entry : p_node.m_symtab.m_symlist){
			entry.m_offset = p_node.m_symtab.m_size - entry.m_size;
			p_node.m_symtab.m_size -= entry.m_size;
		}
	};

	public void visit(FuncDefNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// compute total size and offsets along the way
		// this should be node on all nodes that represent
		// a scope and contain their own table
		// stack frame contains the return value at the bottom of the stack
		p_node.m_symtab.m_size = -(this.sizeOfTypeNode(p_node.getChildren().get(0)));
		//then is the return addess is stored on the stack frame
		p_node.m_symtab.m_size -= 4;
		for (SymTabEntry entry : p_node.m_symtab.m_symlist){
			entry.m_offset = p_node.m_symtab.m_size - entry.m_size; 
			p_node.m_symtab.m_size -= entry.m_size;
		}
	};
	
	public void visit(VarDeclNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// determine the size for basic variables
		p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
	}

	public void visit(MultOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
	};
	
	public void visit(AddOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
	};
	
	// Below are the visit methods for node types for which this visitor does
	// not apply. They still have to propagate acceptance of the visitor to
	// their children.

	public void visit(NumNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
	};
	
	public void visit(AssignStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(ClassListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(DimListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(FuncDefListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(IdNode p_node) {
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
	
	public void visit(PutStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(StatBlockNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(TypeNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
	 };
	
	 public void visit(ParamListNode p_node) {
		 // propagate accepting the same visitor to all the children
		 // this effectively achieves Depth-First AST Traversal
		 for (Node child : p_node.getChildren() )
			 child.accept(this);
	 }

	public void visit(DimNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
	}; 

	public void visit(FuncCallNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
	}; 
	
	public void visit(ReturnStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
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

    };



    public void visit(SignNode p_node) {
        // propagate accepting the same visitor to all the children
        // this effectively achieves Depth-First AST Traversal
        System.out.println("visit SignNode");


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

    }

    public  void visit(AparamList p_node){

    }

    public void visit(IndiceRepNode p_node){

    }
}
