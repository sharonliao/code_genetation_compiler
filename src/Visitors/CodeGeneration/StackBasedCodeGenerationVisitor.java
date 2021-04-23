package Visitors.CodeGeneration;

import AST.*;
import SymbolTable.FuncEntry;
import SymbolTable.SymTab;
import SymbolTable.SymTabEntry;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Visitor to generate moon code for simple expressions and assignment and put 
 * statements. Also include code for function calls using a stack-based model.  
 */

public class StackBasedCodeGenerationVisitor extends Visitor {
	
    public Stack<String> m_registerPool   = new Stack<String>();
    public String        m_moonExecCode   = new String();              // moon instructions part
    public String        m_moonDataCode   = new String();              // moon data part
    public String        m_mooncodeindent = new String("          ");
    public String 	     m_outputfilename = new String();
    public HashMap<String,String> relopMap = new HashMap<>(){
    	{
			put("eq","ceq");
			put("neq","cne");
			put("lt","clt");
			put("gt","cgt");
			put("leq","cle");
			put("geq","cge");
		}
	};

    public SymTabEntry currentScopeEntry;
    public SymTab globalTable;

	public int id = 0;
	public int getID(){
		return id++;
	}

    
    public StackBasedCodeGenerationVisitor() {
    	// create a pool of registers as a stack of Strings
    	// assuming only r1, ..., r12 are available
    	for (Integer i = 12; i>=1; i--)
    		m_registerPool.push("r" + i.toString());
    }
    
    public StackBasedCodeGenerationVisitor(String p_filename) {
    	this.m_outputfilename = p_filename; 
       	// create a pool of registers as a stack of Strings
    	// assuming only r1, ..., r12 are available
    	for (Integer i = 12; i>=1; i--)
    		m_registerPool.push("r" + i.toString());
    }


	public void visit(FuncDeclareNode p_node){

	}

	public void visit(InheritNode p_node){

	}

	public void visit(ProgNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		//ClassNode FuncDefNode ProgramBlockNode
        globalTable = p_node.m_symtab;

		for (Node child : p_node.getChildren())
			child.accept(this);	
		// if the Visitor was given a file name, 
		// then write the generated code into this file
		if (!this.m_outputfilename.isEmpty()) {
			File file = new File(this.m_outputfilename);
			try (PrintWriter out = new PrintWriter(file)) {
			    out.println(this.m_moonExecCode);
			    out.println(this.m_moonDataCode);}		
			catch(Exception e){
				e.printStackTrace();}
		}
	};


    public void visit(ProgramBlockNode p_node) {
        // ProgramBlockNode -> VarDeclListNode,StatBlockNode
        // VarDeclListNode -> VarDeclNode
        // StatBlockNode -> AssignStatNode, WhileStatNode,FuncCallNode

        // offset 要和symbol table 搭配
        // global off_set 0是绝对地址
        currentScopeEntry = p_node.m_symtabentry;
        System.out.println("main offset: " + currentScopeEntry.m_offset);



        // generate moon program's entry point
        m_moonExecCode += m_mooncodeindent + "entry\n";
        // make the stack frame pointer (address stored in r14) point
        // to the top address allocated to the moon processor
        m_moonExecCode += m_mooncodeindent + "addi r14,r0,topaddr\n";

        // propagate acceptance of this visitor to all the children
        for (Node child : p_node.getChildren())
            child.accept(this);

		String strSpace = "space db     \" \", 0\n";
		String strInput = "input db \"input:\", 0\n";
		m_moonDataCode += strSpace;
		m_moonDataCode += strInput;


        // generate moon program's end point
        m_moonDataCode += m_mooncodeindent + "% buffer space used for console output\n";
        // buffer used by the lib.m subroutines
        m_moonDataCode += String.format("%-10s" , "buf") + "res 20\n";
        // halting point of the entire program
        m_moonExecCode += m_mooncodeindent + "hlt\n";
    }

    public void visit(VarDeclNode p_node){

        for (Node child : p_node.getChildren() )
            child.accept(this);
        System.out.println("var decl, name:  "+p_node.m_symtabentry.m_name + ",type: "+ p_node.m_symtabentry.m_type + ",size:"+ p_node.m_symtabentry.m_size);

    }

	
	public void visit(NumNode p_node){
		// First, propagate accepting the same visitor to all the children
		// This effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		// create a local variable and allocate a register to this subcomputation 
		String localregister1 = this.m_registerPool.pop();
		// generate code			
		m_moonExecCode += m_mooncodeindent + "% processing: " + p_node.m_moonVarName  + " := " + p_node.getData() + "\n";
		// create a value corresponding to the literal value
		m_moonExecCode += m_mooncodeindent + "addi " + localregister1 + ",r0," + p_node.getData() + "\n"; 
		// assign this value to a temporary variable (assumed to have been previously created by the symbol table generator)
		m_moonExecCode += m_mooncodeindent + "sw " + p_node.m_symtabentry.m_offset + "(r14)," + localregister1 + "\n";
		// deallocate the register for the current node
		this.m_registerPool.push(localregister1);
	}
	
	public void visit(AddOpNode p_node){
		// First, propagate accepting the same visitor to all the children
		// This effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		// allocate registers to this subcomputation 
		String localregister1 = this.m_registerPool.pop();
		String localregister2 = this.m_registerPool.pop();
		String localregister3 = this.m_registerPool.pop();
		String localregister4 = this.m_registerPool.pop();
		// generate code
		m_moonExecCode += m_mooncodeindent + "% processing: " + p_node.m_moonVarName + " := " + p_node.getSubtreeString()+ "\n";
		// load the values of the operands into registers
        //对varNode单独处理，直接返回offset，因为 addnode， idnode， funccallNode 都可以用moonvarname来search
        int leftOffSet = 0;
        int rightOffSet = 0;
        //System.out.println("p_node.getChildren().get(0).getClass().getSimpleName():"+ p_node.getChildren().get(0).getClass().getSimpleName());
        if(p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
            leftOffSet = p_node.getChildren().get(0).m_symtabentry.m_offset;
            System.out.println("leftOffSet : "+ leftOffSet);
        }else {
            leftOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
        }
        if(p_node.getChildren().get(1).getClass().getSimpleName().equals("VarNode")){
            rightOffSet = p_node.getChildren().get(1).m_symtabentry.m_offset;
            System.out.println("rightOffSet : "+ rightOffSet);
        }else {
            rightOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(1).m_moonVarName).m_offset;
        }

        String operator = "";
        if(p_node.m_data.equals("+")){
			operator = "add ";
		}else {
			operator = "sub ";
		}

		m_moonExecCode += m_mooncodeindent + "lw "   + localregister2 + "," + leftOffSet + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "lw "   + localregister3 + "," + rightOffSet + "(r14)\n";

        // add operands
		m_moonExecCode += m_mooncodeindent + operator + " "  + localregister4 + "," + localregister2 + "," + localregister3 + "\n";
		// assign the result into a temporary variable (assumed to have been previously created by the symbol table generator)
		m_moonExecCode += m_mooncodeindent + "sw "   + p_node.m_symtab.lookupName(p_node.m_moonVarName).m_offset + "(r14)," + localregister4 + "\n";		
		// deallocate the registers 
		this.m_registerPool.push(localregister1);
		this.m_registerPool.push(localregister2);		
		this.m_registerPool.push(localregister3);
		this.m_registerPool.push(localregister4);
		}

	public void visit(MultOpNode p_node){
		// First, propagate accepting the same visitor to all the children
		// This effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		// create a local variable and allocate a register to this subcomputation 
		String localregister1 = this.m_registerPool.pop();
		String localregister2 = this.m_registerPool.pop();
		String localregister3 = this.m_registerPool.pop();
		String localregister4 = this.m_registerPool.pop();

        int leftOffSet = 0;
        int rightOffSet = 0;
        System.out.println("p_node.getChildren().get(0).getClass().getSimpleName():"+ p_node.getChildren().get(0).getClass().getSimpleName());
        if(p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
            leftOffSet = p_node.getChildren().get(0).m_symtabentry.m_offset;
            System.out.println("leftOffSet : "+ leftOffSet);
        }else {
            leftOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
        }
        if(p_node.getChildren().get(1).getClass().getSimpleName().equals("VarNode")){
            rightOffSet = p_node.getChildren().get(1).m_symtabentry.m_offset;
            System.out.println("rightOffSet : "+ rightOffSet);
        }else {
            rightOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(1).m_moonVarName).m_offset;
        }

		String operator = "";
		if(p_node.m_data.equals("*")){
			operator = "mul ";
		}else {
			operator = "div ";
		}


		// generate code
		m_moonExecCode += m_mooncodeindent + "% processing: " + p_node.m_moonVarName + " := " +  p_node.getSubtreeString() + "\n";
		// load the values of the operands into registers
		m_moonExecCode += m_mooncodeindent + "lw "   + localregister2 + "," + leftOffSet + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "lw "   + localregister3 + "," + rightOffSet+ "(r14)\n";
		// multiply operands
		m_moonExecCode += m_mooncodeindent + operator + " "  + localregister4 + "," + localregister2 + "," + localregister3 + "\n";
		// assign the result into a temporary variable (assumed to have been previously created by the symbol table generator)
		m_moonExecCode += m_mooncodeindent + "sw "   + p_node.m_symtab.lookupName(p_node.m_moonVarName).m_offset + "(r14)," + localregister4 + "\n";
		// deallocate the registers for the two children, and the current node
		this.m_registerPool.push(localregister1);
		this.m_registerPool.push(localregister2);		
		this.m_registerPool.push(localregister3);
		this.m_registerPool.push(localregister4);
	}
	
	public void visit(AssignStatNode p_node){
        // left hand side can be IdNode or VarNode
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// allocate local registers
		String localregister1 = this.m_registerPool.pop();
		String localregister2 = this.m_registerPool.pop();
		String localregister3 = this.m_registerPool.pop();	
		//generate code
        int leftOffSet = 0;
        int rightOffSet = 0;
        System.out.println("p_node.getChildren().get(0).getClass().getSimpleName():"+ p_node.getChildren().get(0).getClass().getSimpleName());
        if(p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
            leftOffSet = p_node.getChildren().get(0).m_symtabentry.m_offset;
            System.out.println("leftOffSet : "+ leftOffSet);
        }else {
            leftOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
        }

        if(p_node.getChildren().get(1).getClass().getSimpleName().equals("VarNode")){
            rightOffSet = p_node.getChildren().get(1).m_symtabentry.m_offset;
            System.out.println("rightOffSet : "+ rightOffSet);
        }else {
            rightOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(1).m_moonVarName).m_offset;
        }

		m_moonExecCode += m_mooncodeindent + "% processing: AssignStatNode("  + p_node.m_subtreeString + ")\n";
		m_moonExecCode += m_mooncodeindent + "% processing: "  + p_node.getChildren().get(0).m_moonVarName + " := " + p_node.getChildren().get(1).m_moonVarName + "\n";
		// load the assigned value into a register
		m_moonExecCode += m_mooncodeindent + "lw "   + localregister2 + "," + rightOffSet + "(r14)\n";
		// assign the value to the assigned variable
		m_moonExecCode += m_mooncodeindent + "sw "   + leftOffSet + "(r14)," + localregister2 + "\n";
		// deallocate local registers
		this.m_registerPool.push(localregister1);		
		this.m_registerPool.push(localregister2);		
		this.m_registerPool.push(localregister3);		
	}

	
	public void visit(PutStatNode p_node) {
        //TODO write statement
//          % processing: put(t29)
//          lw r3,-156(r14)
//          % put value on stack
//          addi r14,r14,-184
//          sw -8(r14),r3
//          % link buffer to stack
//          addi r3,r0, buf
//          sw -12(r14),r3
//          % convert int to string for output
//          jl r15, intstr
//          sw -8(r14),r13
//          % output to console
//          jl r15, putstr
//          subi r14,r14,-184

		// First, propagate accepting the same visitor to all the children
		// This effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		// Then, do the processing of this nodes' visitor		
		// create a local variable and allocate a register to this subcomputation 
		String localregister1 = this.m_registerPool.pop();
		String localregister2 = this.m_registerPool.pop();
		//generate code
		//m_moonExecCode += m_mooncodeindent + "% processing: put("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
		m_moonExecCode += m_mooncodeindent + "% processing: put("  + p_node.m_subtreeString + ")\n";
		// put the value to be printed into a register
		m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "% put value on stack\n";
		// make the stack frame pointer point to the called function's stack frame
		m_moonExecCode += m_mooncodeindent + "addi r14,r14," + p_node.m_symtab.m_size + "\n";
		// copy the value to be printed in the called function's stack frame
		m_moonExecCode += m_mooncodeindent + "sw -8(r14)," + localregister1 + "\n";
		m_moonExecCode += m_mooncodeindent + "% link buffer to stack\n";	
		m_moonExecCode += m_mooncodeindent + "addi " + localregister1 + ",r0, buf\n";
		m_moonExecCode += m_mooncodeindent + "sw -12(r14)," + localregister1 + "\n";
		m_moonExecCode += m_mooncodeindent + "% convert int to string for output\n";	
		m_moonExecCode += m_mooncodeindent + "jl r15, intstr\n";
		// receive the return value in r13 and right away put it in the next called function's stack frame
		m_moonExecCode += m_mooncodeindent + "sw -8(r14),r13\n";
		m_moonExecCode += m_mooncodeindent + "% output to console\n";	
		m_moonExecCode += m_mooncodeindent + "jl r15, putstr\n";
		// make the stack frame pointer point back to the current function's stack frame
		m_moonExecCode += m_mooncodeindent + "subi r14,r14," + p_node.m_symtab.m_size + "\n";
		//deallocate local register
		this.m_registerPool.push(localregister1);
		this.m_registerPool.push(localregister2);
	};

	public void visit(FuncDefNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		m_moonExecCode += m_mooncodeindent + "% processing function definition: "  + p_node.m_moonVarName + "\n";
		//create the tag to jump onto
		// each function has a unique tag which generated in type checking stage
		// and copy the jumping-back address value in the called function's stack frame
		m_moonExecCode += String.format("%-10s", ((FuncEntry)p_node.m_symtabentry).tag)  + "sw -4(r14),r15\n" ;
		//generate the code for the function body
		for (Node child : p_node.getChildren())
			child.accept(this);
		// copy back the jumping-back address into r15
		m_moonExecCode += m_mooncodeindent + "lw r15,-4(r14)\n";
		// jump back to the calling function
		m_moonExecCode += m_mooncodeindent + "jr r15\n";	
	};
	
	public void visit(ReturnStatNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		String localregister1 = this.m_registerPool.pop();
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// copy the result of the return value into the first memory cell in the current stack frame
		// this way, the return value is conveniently at the top of the calling function's stack frame
		int offset = getOffset(p_node);
		//m_moonExecCode += m_mooncodeindent + "% processing: return("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
		m_moonExecCode += m_mooncodeindent + "% processing: return("  + p_node.m_subtreeString + ")\n";
		m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "sw "   + "0(r14)," + localregister1 + "\n";
		this.m_registerPool.push(localregister1);	
	}

	public void visit(FuncCallNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() ){
			child.accept(this);
		}

		String localregister1 = this.m_registerPool.pop();
		// pass parameters
        System.out.println("func call tag:"+ p_node.tag);

		ArrayList<SymTabEntry> paramInFunc = getParamLIstFromTable(p_node.funcCallEntry);

		//m_moonExecCode += m_mooncodeindent + "% processing: function call to "  + p_node.getChildren().get(0).m_moonVarName + " \n";
		m_moonExecCode += m_mooncodeindent + "% processing: function call to "  + p_node.m_subtreeString + " \n";

        //假设最后一个node是
        AparamList aparamList = (AparamList)p_node.getChildren().get(p_node.getChildren().size()-1);

        // match var -> param
        for(int i = 0; i<paramInFunc.size(); i++){
            Node varNode = aparamList.getChildren().get(i);
            SymTabEntry param = paramInFunc.get(i);
            //only support integer param now
            if(param.m_type.equals("integer") &&  param.m_dims.size()==0){
				int offset = 0;
				if (varNode.getClass().getSimpleName().equals("VarNode")){
					offset = varNode.m_symtabentry.m_offset;
				}else {
					offset = p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset;
				}

                m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
                //p_node.m_symtab.m_size, size of the current scope
				// minus p_node.m_symtab.m_size, to create a new start addr
                int offsetofparam = -p_node.m_symtab.m_size + param.m_offset;

				System.out.println("p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset:"+p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset);
				System.out.println("param offset:"+param.m_offset);
				System.out.println("offsetofparam:"+offsetofparam);
				System.out.println("p_node.m_symtab.m_size:"+p_node.m_symtab.m_size);
				System.out.println("p_node.m_symtabentry.m_size:"+p_node.m_symtabentry.m_size);
				System.out.println("p_node.funcCallEntry.m_size:"+p_node.funcCallEntry.m_size);

                m_moonExecCode += m_mooncodeindent + "sw " + offsetofparam + "(r14)," + localregister1 + "\n";
            }

        }

		// make the stack frame pointer point to the called function's stack frame
		m_moonExecCode += m_mooncodeindent + "addi r14,r14," + -p_node.m_symtab.m_size + "\n";
		// jump to the called function's code
		// here the function's name is the unique tag created in the type checking stage
		m_moonExecCode += m_mooncodeindent + "jl r15," + p_node.tag + "\n";
		// upon jumping back, set the stack frame pointer back to the current function's stack frame  
		m_moonExecCode += m_mooncodeindent + "subi r14,r14," + -p_node.m_symtab.m_size + "\n";

		// copy the return value in memory space to store it on the current stack frame
		// to evaluate the expression in which it is 
		m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + -p_node.m_symtab.m_size + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "sw " + p_node.m_symtab.lookupName(p_node.m_moonVarName).m_offset + "(r14)," + localregister1 + "\n";
		this.m_registerPool.push(localregister1);
	};

	public void passParams(Node p_node){
		ArrayList<SymTabEntry> paramInFunc = getParamLIstFromTable(p_node.funcCallEntry);
		AparamList aparamList = (AparamList)p_node.getChildren().get(p_node.getChildren().size()-1);

		for(int i = 0; i<paramInFunc.size(); i++){
			Node varNode = aparamList.getChildren().get(i);
			SymTabEntry param = paramInFunc.get(i);
			//only support integer param now
			if(param.m_type.equals("integer") &&  param.m_dims.size()==0){
				String localregister1 = this.m_registerPool.pop();
				int offset = 0;
				if (varNode.getClass().getSimpleName().equals("VarNode")){
					offset = varNode.m_symtabentry.m_offset;
				}else {
					offset = p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset;
				}

				m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
				//p_node.m_symtab.m_size, size of the current scope
				// minus p_node.m_symtab.m_size, to create a new start addr
				int offsetofparam = -p_node.m_symtab.m_size + param.m_offset;

				System.out.println("p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset:"+p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset);
				System.out.println("param offset:"+param.m_offset);
				System.out.println("offsetofparam:"+offsetofparam);
				System.out.println("p_node.m_symtab.m_size:"+p_node.m_symtab.m_size);
				System.out.println("p_node.m_symtabentry.m_size:"+p_node.m_symtabentry.m_size);
				System.out.println("p_node.funcCallEntry.m_size:"+p_node.funcCallEntry.m_size);

				m_moonExecCode += m_mooncodeindent + "sw " + offsetofparam + "(r14)," + localregister1 + "\n";
				this.m_registerPool.push(localregister1);
			}else if(param.m_type.equals("integer") &&  param.m_dims.size()>0){
				//array
				int totalSize = 1;
				for (int dim : param.m_dims){
					totalSize *= dim;
				}

				int offset = 0;
				if (varNode.getClass().getSimpleName().equals("VarNode")){
					offset = varNode.m_symtabentry.m_offset;
				}else {
					offset = p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset;
				}

				for(int j =0; j <totalSize; j ++){
					int subOffset = offset + 4*j;
					int offsetofparam = -p_node.m_symtab.m_size + param.m_offset + 4*j;
					String localregister1 = this.m_registerPool.pop();
					m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + subOffset + "(r14)\n";
					m_moonExecCode += m_mooncodeindent + "sw " + offsetofparam + "(r14)," + localregister1 + "\n";
					this.m_registerPool.push(localregister1);
				}
			}else if(!param.m_type.equals("integer")  &&  param.m_dims.size() == 0){
				//object
				//但是object里还有object就不可以了
				SymTabEntry classEntry = globalTable.lookupName(param.m_type);

				int offset = 0;
				if (varNode.getClass().getSimpleName().equals("VarNode")){
					offset = varNode.m_symtabentry.m_offset;
				}else {
					offset = p_node.m_symtab.lookupName(varNode.m_moonVarName).m_offset;
				}

				for(SymTabEntry member: classEntry.m_subtable.m_symlist){
					if(member.getClass().getSimpleName().equals("VarEntry")){
						int subOffset = offset + classEntry.m_subtable.m_size + member.m_offset;
						int offsetofparam = -p_node.m_symtab.m_size + param.m_offset + param.m_size + member.m_offset;
						String localregister1 = this.m_registerPool.pop();
						m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + subOffset + "(r14)\n";
						m_moonExecCode += m_mooncodeindent + "sw " + offsetofparam + "(r14)," + localregister1 + "\n";
						this.m_registerPool.push(localregister1);
					}
				}
			}
		}
	}

	public ArrayList<SymTabEntry> getParamLIstFromTable(SymTabEntry funcEntry){
	    System.out.println("func name:"+ funcEntry.m_name);
        ArrayList<SymTabEntry> paramList = new ArrayList<>();
        for(SymTabEntry entry : funcEntry.m_subtable.m_symlist){
            if (entry.m_kind.equals("param")){
                paramList.add(entry);
            }
        }
        return paramList;
    }

    public int getOffset(Node p_node){
		int offset = 0;
		if (p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
			offset = p_node.getChildren().get(0).m_symtabentry.m_offset;
		}else {
			offset = p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
		}
		return offset;
	}

	// Below are the visit methods for node types for which this visitor does
	// not apply. They still have to propagate acceptance of the visitor to
	// their children.
	public void visit(ClassListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(ClassNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        //VarDeclNode  FuncDeclareNode
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


	public void visit(IdNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
	}	
	
	public void visit(Node p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(StatBlockNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visiting StatBlockNode");
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(TypeNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
    };


	public void visit(IfStatNode p_node) {
		System.out.println("visit IfStatNode");
        if(p_node.m_symtab!=null)
        {
            String localregister1 = this.m_registerPool.pop();
            p_node.getChildren().get(0).accept(this);
			int id = getID();
            int offset = getOffset(p_node);

            m_moonExecCode += m_mooncodeindent + "% processing: if("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
            m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
            m_moonExecCode += m_mooncodeindent + "bz " + localregister1 + ",else"+id+"\n";

            p_node.getChildren().get(1).accept(this);
            m_moonExecCode += m_mooncodeindent + "j endif"+id + "\n";

            m_moonExecCode += "else"+id+ "\n";
            if(p_node.getChildren().size()>2){
				p_node.getChildren().get(2).accept(this);
			}
            m_moonExecCode += "endif"+id + "\n";
            this.m_registerPool.push(localregister1);
        }
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
		//deep traversal, get leaf info

//		addi   r1,r0,buf     % Get X
//      sw     -8(r14),r1
//      jl     r15,getstr
//      jl     r15,strint    % Convert to integer
//      sw     x(r0),r13     % Store X
		writeSpace("input",p_node);
		String localregister1 = this.m_registerPool.pop();
		int offset = getOffset(p_node);
		m_moonExecCode += m_mooncodeindent + "% processing: read("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
		m_moonExecCode += m_mooncodeindent + "addi " + localregister1 + ", r0, buf\n";
		m_moonExecCode += m_mooncodeindent + "sw -8(r14)," + localregister1 + "\n";
		m_moonExecCode += m_mooncodeindent + "jl r15,getstr\n";
		m_moonExecCode += m_mooncodeindent + "jl r15,strint\n";
		m_moonExecCode += m_mooncodeindent + "sw " + offset +"(r14), r13\n";
		this.m_registerPool.push(localregister1);
	};

	public void visit(RelOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit RelOpNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
		// Then, do the processing of this nodes' visitor
		// allocate registers to this subcomputation
		String localregister1 = this.m_registerPool.pop();
		String localregister2 = this.m_registerPool.pop();
		String localregister3 = this.m_registerPool.pop();
		String localregister4 = this.m_registerPool.pop();
		// generate code
		m_moonExecCode += m_mooncodeindent + "% processing RelOp: " + p_node.m_moonVarName + " := " + p_node.getSubtreeString()+ "\n";
		// load the values of the operands into registers

		int leftOffSet = 0;
		int rightOffSet = 0;
		//System.out.println("p_node.getChildren().get(0).getClass().getSimpleName():"+ p_node.getChildren().get(0).getClass().getSimpleName());
		if(p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
			leftOffSet = p_node.getChildren().get(0).m_symtabentry.m_offset;
			System.out.println("leftOffSet : "+ leftOffSet);
		}else {
			leftOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
		}
		if(p_node.getChildren().get(1).getClass().getSimpleName().equals("VarNode")){
			rightOffSet = p_node.getChildren().get(1).m_symtabentry.m_offset;
			System.out.println("rightOffSet : "+ rightOffSet);
		}else {
			rightOffSet =  p_node.m_symtab.lookupName(p_node.getChildren().get(1).m_moonVarName).m_offset;
		}

		String operator = relopMap.get(p_node.m_data);

		m_moonExecCode += m_mooncodeindent + "lw "   + localregister2 + "," + leftOffSet + "(r14)\n";
		m_moonExecCode += m_mooncodeindent + "lw "   + localregister3 + "," + rightOffSet + "(r14)\n";

		// add operands
		m_moonExecCode += m_mooncodeindent + operator + " " + localregister4 + "," + localregister2 + "," + localregister3 + "\n";
		// assign the result into a temporary variable (assumed to have been previously created by the symbol table generator)
		m_moonExecCode += m_mooncodeindent + "sw "   + p_node.m_symtab.lookupName(p_node.m_moonVarName).m_offset + "(r14)," + localregister4 + "\n";
		// deallocate the registers
		this.m_registerPool.push(localregister1);
		this.m_registerPool.push(localregister2);
		this.m_registerPool.push(localregister3);
		this.m_registerPool.push(localregister4);
	};



	public void visit(SignNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit SignNode");


	};

	public void visit(WhileStatNode p_node) {

		System.out.println("visit WhileStatNode");
		if(p_node.m_symtab!=null)
		{
			String localregister1 = this.m_registerPool.pop();
			int id = getID();
			int offset = getOffset(p_node);
			m_moonExecCode += m_mooncodeindent + "% processing: while("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
			m_moonExecCode += m_mooncodeindent + "j gowhile"+id + "\n";

			m_moonExecCode += "gowhile"+id+ "\n";

			p_node.getChildren().get(0).accept(this);

			m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
			m_moonExecCode += m_mooncodeindent + "bz " + localregister1 + ",endwhile"+id+"\n";

			p_node.getChildren().get(1).accept(this);
			m_moonExecCode += m_mooncodeindent + "j gowhile"+id + "\n";

			m_moonExecCode += "endwhile"+id + "\n";
			this.m_registerPool.push(localregister1);
		}
	};

	public void visit(WriteStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit WriteStatNode");
        // First, propagate accepting the same visitor to all the children
        // This effectively achieves Depth-First AST Traversal
        for (Node child : p_node.getChildren())
            child.accept(this);
        // Then, do the processing of this nodes' visitor
        // create a local variable and allocate a register to this subcomputation
        String localregister1 = this.m_registerPool.pop();
        String localregister2 = this.m_registerPool.pop();

        int offset = 0;
        if (p_node.getChildren().get(0).getClass().getSimpleName().equals("VarNode")){
			offset = p_node.getChildren().get(0).m_symtabentry.m_offset;
		}else {
			offset = p_node.m_symtab.lookupName(p_node.getChildren().get(0).m_moonVarName).m_offset;
		}
        //generate code
        m_moonExecCode += m_mooncodeindent + "% processing: put("  + p_node.getChildren().get(0).m_moonVarName + ")\n";
        // put the value to be printed into a register
        m_moonExecCode += m_mooncodeindent + "lw " + localregister1 + "," + offset + "(r14)\n";
        m_moonExecCode += m_mooncodeindent + "% put value on stack\n";
        // make the stack frame pointer point to the called function's stack frame
        m_moonExecCode += m_mooncodeindent + "addi r14,r14," + (-p_node.m_symtab.m_size) + "\n";
        // copy the value to be printed in the called function's stack frame
        m_moonExecCode += m_mooncodeindent + "sw -8(r14)," + localregister1 + "\n";
        m_moonExecCode += m_mooncodeindent + "% link buffer to stack\n";
        m_moonExecCode += m_mooncodeindent + "addi " + localregister1 + ",r0, buf\n";
        m_moonExecCode += m_mooncodeindent + "sw -12(r14)," + localregister1 + "\n";
        m_moonExecCode += m_mooncodeindent + "% convert int to string for output\n";
        m_moonExecCode += m_mooncodeindent + "jl r15, intstr\n";
        // receive the return value in r13 and right away put it in the next called function's stack frame
        m_moonExecCode += m_mooncodeindent + "sw -8(r14),r13\n";
        m_moonExecCode += m_mooncodeindent + "% output to console\n";
        m_moonExecCode += m_mooncodeindent + "jl r15, putstr\n";
        // make the stack frame pointer point back to the current function's stack frame
        m_moonExecCode += m_mooncodeindent + "subi r14,r14," + (-p_node.m_symtab.m_size) + "\n";
        //deallocate local register
        this.m_registerPool.push(localregister1);
        this.m_registerPool.push(localregister2);
        writeSpace("space",p_node);
	};


	public void visit(VarNode p_node){
        System.out.println("visit VarNode");
        int offSet = p_node.m_symtabentry.m_offset;
        System.out.println("var offset:"+ p_node.m_symtabentry.m_name + ","+offSet);
	}

	public void visit(AparamList p_node){
	    // TODO 先执行param
        for (Node child : p_node.getChildren() )
            child.accept(this);
	}

	public void visit(IndiceRepNode p_node){

	}

    public void visit(DotNode p_node){

    }

    public void writeSpace(String tag, Node p_node){

//		addi   r1,r0,entx    % Ask for X
//		sw     -8(r14),r1
//		jl     r15,putstr
		String localregister1 = this.m_registerPool.pop();
		m_moonExecCode += m_mooncodeindent + "% put value on stack\n";
		// make the stack frame pointer point to the called function's stack frame
		m_moonExecCode += m_mooncodeindent + "addi "+localregister1+",r0,"+tag+"\n";
		m_moonExecCode += m_mooncodeindent + "addi r14,r14," + (-p_node.m_symtab.m_size) + "\n";
		// copy the value to be printed in the called function's stack frame
		m_moonExecCode += m_mooncodeindent + "sw -8(r14)," + localregister1 + "\n";
		m_moonExecCode += m_mooncodeindent + "jl r15, putstr\n";
		// make the stack frame pointer point back to the current function's stack frame
		m_moonExecCode += m_mooncodeindent + "subi r14,r14," + (-p_node.m_symtab.m_size) + "\n";
		this.m_registerPool.push(localregister1);
	}
}
