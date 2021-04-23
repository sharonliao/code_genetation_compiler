package Visitors.SymbolTable;

import AST.*;
import SymbolTable.SymTab;
import SymbolTable.SymTabEntry;
import SymbolTable.VarEntry;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

/** 
 */

public class ComputeMemSizeVisitor extends Visitor {

	public String  m_outputfilename = new String();

    public SymTab currentScope;
    public SymTab globalTable;


	public ComputeMemSizeVisitor() {
	}
	
	public ComputeMemSizeVisitor(String p_filename) {
		this.m_outputfilename = p_filename;
	}

	public int sizeOfEntry(Node p_node) {

		if(p_node.getType() == null ){
			System.out.println("node has no type:" + p_node.getClass().getSimpleName());
			return 0;
		}else if(p_node.m_symtabentry == null ){
            System.out.println("node has no m_symtabentry:" + p_node.getClass().getSimpleName());
            return 0;
        }

        System.out.println("var declare type:" + p_node.m_symtabentry.m_type);

		int size = 0;
		if(p_node.m_symtabentry.m_type.equals("integer"))
			size = 4;
		else if(p_node.m_symtabentry.m_type.equals("float"))
			size = 8;
		else {
            size = sizeOfTypeNode(p_node.getType());
        }
		// if it is an array, multiply by all dimension sizes
		VarEntry ve = (VarEntry) p_node.m_symtabentry;
		if(!ve.m_dims.isEmpty())
			for(Integer dim : ve.m_dims)
				size *= dim;
		ve.m_size = size;
		return size;
	}

    public int sizeOfEntry(SymTabEntry entry) {

        if(entry.m_type == null ){
            System.out.println("node has no type:" + entry.m_type);
        }

        System.out.println("var declare type:" + entry.m_type);

        int size = 0;
        if(entry.m_type.equals("integer"))
            size = 4;
        else if(entry.m_type.equals("float"))
            size = 8;
        else {
            size = sizeOfTypeNode(entry.m_type);
        }
        // if it is an array, multiply by all dimension sizes
        VarEntry ve = (VarEntry) entry;
        if(!ve.m_dims.isEmpty())
            for(Integer dim : ve.m_dims)
                size *= dim;
        ve.m_size = size;
        return size;
    }
	
	public int sizeOfTypeNode(String type) {
	    System.out.println("get type size:"+type);
		int size = 0;
		if(type.equals("integer"))
			size = 4;
		else if(type.equals("float"))
			size = 8;
		else if(type.equals("void")){
		    size = 0;
        }else if(type.equals("typeerror")){
            size = 0;
        }else {
		    size = globalTable.lookupName(type).m_size;
        }
		return size;
	}
	
	public void visit(ProgNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        // classListNode
        //global offset = 0
        globalTable = p_node.m_symtab;
        currentScope =  p_node.m_symtab;
		for (Node child : p_node.getChildren())
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
        // propagate accepting the same visitor to all the children
        // this effectively achieves Depth-First AST Traversal
	}

	public void visit(InheritNode p_node){

	}
	
	public void visit(ProgramBlockNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		// compute total size and offsets along the way
		// this should be node on all nodes that represent
		// a scope and contain their own table
        currentScope =  p_node.m_symtab;
        int scopeSize = 0; //不包括stack frame
        p_node.m_symtabentry.m_offset = 0;
        for (SymTabEntry entry : p_node.m_symtabentry.m_subtable.m_symlist){
            System.out.println("entry :" + entry.getClass().getSimpleName() +",kind:"+entry.m_kind+ " type:" + entry.m_type);
//            int entrySize = sizeOfTypeNode(entry.m_type);
            int entrySize = sizeOfEntry(entry);
            entry.m_size = entrySize;
            scopeSize += entry.m_size;

            entry.m_offset = p_node.m_symtab.m_size - entry.m_size;
            p_node.m_symtab.m_size -= entry.m_size;
            System.out.println("offset :" + entry.m_offset );

        }
        p_node.m_symtabentry.m_subtable.m_size = scopeSize;

        for (Node child : p_node.getChildren() )
            child.accept(this);
    };
	
	public void visit(ClassNode p_node){

		System.out.println("computer ClassNode");
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// compute total size and offsets along the way		
		// this should be node on all nodes that represent
		// a scope and contain their own table

        int scopeSize = 0;
		for (SymTabEntry entry : p_node.m_symtabentry.m_subtable.m_symlist){
		    if(entry.getClass().getSimpleName().equals("FuncEntry") || entry.getClass().getSimpleName().equals("InheritEntry")){
		        continue;
            }
			System.out.println("child entry"+ entry.m_size);
			scopeSize += entry.m_size;

			// m_symtab.m_size default 0，
			entry.m_offset = p_node.m_symtab.m_size - entry.m_size;
			p_node.m_symtab.m_size -= entry.m_size;
		}
        p_node.m_symtabentry.m_subtable.m_size = scopeSize;
        p_node.m_symtabentry.m_size = scopeSize;
	};

	public void visit(FuncDefNode p_node){
	    //FuncDeclare 不统计 size， 在define中统计
        //ClassMethodNode IdNode ParamListNode TypeNode VarDeclListNode StatBlockNode
        //有entry的node ParamListNode VarDeclListNode StatBlockNode
        //var 和 param 已经确定了type 可以直接确定m_size
        //StatBlockNode中temp var 也可以直接确认size
        currentScope =  p_node.m_symtab;

        for (Node child : p_node.getChildren() )
            child.accept(this);
        // compute total size and offsets along the way
        // this should be node on all nodes that represent
        // a scope and contain their own table
        // stack frame contains the return value at the bottom of the stack

        // 首先确定return type
        int returnTypeSize = this.sizeOfTypeNode(p_node.getType());
        p_node.m_symtab.m_size = -(returnTypeSize);
        // 用来计算stack frame的offset
        //then is the return addess is stored on the stack frame
        p_node.m_symtab.m_size -= 4;

        int scopeSize = returnTypeSize+4;
        for (SymTabEntry entry : p_node.m_symtabentry.m_subtable.m_symlist){
            System.out.println("entry :" + entry.getClass().getSimpleName() +",kind:"+entry.m_kind+ " type:" + entry.m_type);
            int entrySize = sizeOfTypeNode(entry.m_type);
            entry.m_size = entrySize;
            scopeSize += entry.m_size;

            entry.m_offset = p_node.m_symtab.m_size - entry.m_size;
            p_node.m_symtab.m_size -= entry.m_size;
        }
        p_node.m_symtabentry.m_subtable.m_size = scopeSize;
        p_node.m_symtabentry.m_size = scopeSize;

	};
	
	public void visit(VarDeclNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// determine the size for basic variables
        if(p_node.m_symtabentry == null){
            p_node.m_symtabentry = new SymTabEntry();
            p_node.m_symtabentry.m_type = "typeerror";
            p_node.m_symtabentry.m_size = 0;
        }else {
            p_node.m_symtabentry.m_size = this.sizeOfEntry(p_node);
        }

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
//		 for (Node child : p_node.getChildren() )
//			 child.accept(this);
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
        System.out.println("visit RelOpNode");
        for (Node child : p_node.getChildren() )
            child.accept(this);
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
        System.out.println("visit VarNode");
        setVarOffset(p_node);
        //polynomial1.b
        //获取polynomial1的offset
//
//        SymTabEntry entry = currentScope.lookupName(p_node.getChildren().get(0).m_data);
//        System.out.println("class offset: "+ p_node.getChildren().get(0).m_data + ":"+ entry.m_offset);
//        p_node.m_symtabentry.m_offset = entry.m_offset - p_node.m_symtabentry.m_offset;
        System.out.println("final VarNode offset: "+ p_node.getChildren().get(0).m_data + ":"+ p_node.m_symtabentry.m_offset);



    }

    public void setVarOffset(VarNode varNode){

        String tempVar = "";
        VarEntry varEntry = null;
        int offset_var = 0;


        Node firstNode = varNode.getChildren().get(0);

        if(!(firstNode.getClass().getSimpleName().equals("IdNode") )){
            // toDO
            System.out.println("Var Error");
            return;

        }else {
            tempVar = firstNode.m_data;
            String entryKind = currentScope.lookupName(tempVar).m_kind;
            if (entryKind != null && (entryKind.equals("var") || entryKind.equals("param"))) {
                varEntry = (VarEntry) currentScope.lookupName(tempVar);
                firstNode.m_symtabentry = varEntry;
                firstNode.m_symtabentry.m_offset = varEntry.m_offset;
                //computer the class's offset, start point add class's size
                offset_var = varEntry.m_offset+ varEntry.m_size;
                System.out.println("class offset_var,"+ varEntry.m_name+" ( " + offset_var + " ) \n");

            } else {
                // toDO
                System.out.println("Can't find the  ( " + tempVar + " ) \n");
                varNode.m_type = "TypeError";
                return;

            }
        }

        for(int i = 1; i<varNode.getChildren().size(); i++){
            Node child = varNode.getChildren().get(i);
            if (child.getClass().getSimpleName().equals("IdNode") ){
                //current varEntry is a class
                tempVar = varNode.getChildren().get(i).m_data;
                String varType = varEntry.m_type;
                SymTabEntry tempClass =  globalTable.lookupName(varType);
                // current id node
                varEntry = (VarEntry) tempClass.m_subtable.lookupName(tempVar);
                if(!(varEntry.m_type.equals("integer")||(varEntry.m_type.equals("float")))){
                    offset_var += varEntry.m_offset + varEntry.m_size;
                    System.out.println("class offset_var,"+ varEntry.m_name+" ( " + offset_var + " ) \n");
                }else {
                    offset_var += varEntry.m_offset;
                    System.out.println("var offset_var,"+ varEntry.m_name+" ( " + offset_var + " ) \n");
                }

            }else if (varNode.getChildren().get(i).getClass().getSimpleName().equals("IndiceRepNode") ){
                if(varEntry != null){
                    IndiceRepNode dimNodeList = (IndiceRepNode) varNode.getChildren().get(i);
                    Vector<Integer> dimList = varEntry.m_dims;
                    if(dimNodeList.getChildren().size() != dimList.size()){
                        // toDO
                        System.out.println("Dimension Error  :" + tempVar + " has a wrong dimension\n");
                        varNode.m_type = "TypeError";
                        return;

                    }else {
                        //dimNodeList
                        int arrayOffset = 0;
                        ArrayList<Integer> arrayDim = new ArrayList<>();

                        for(Node dimNode : dimNodeList.getChildren()){
                            //IdNode, NumNode, addOp, FuncCallNode, multOp,
                            System.out.println("dimNode type:"+ dimNode.getClass().getSimpleName());
                            //just accept constant num now
                            arrayDim.add(Integer.parseInt(dimNode.m_data));
                        }

                        for(int j=0; j<arrayDim.size(); j++){
                            int dimSize = 1;
                            int num = arrayDim.get(j);
                            for(int k = j+1; k<arrayDim.size(); k++){
                                dimSize *= dimList.get(k);
                            }
                            arrayOffset += num*dimSize;
                        }
                        arrayOffset = arrayOffset*varEntry.m_size;
                        offset_var += -arrayOffset;
                    }
                }
            }
        }
        varNode.setType(varEntry.m_type);
        varNode.m_data = varEntry.m_name;

        varNode.m_symtabentry = new VarEntry(varEntry.m_kind,varEntry.m_type,varEntry.m_name,varEntry.m_dims);;
        varNode.m_symtabentry.m_offset = offset_var;

        System.out.println("var offset: "+ varNode.m_subtreeString + ":"+ offset_var);

    }

    public  void visit(AparamList p_node){

    }

    public void visit(IndiceRepNode p_node){

    }

    public void visit(DotNode p_node){

    }
}
