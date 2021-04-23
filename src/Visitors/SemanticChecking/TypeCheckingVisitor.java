package Visitors.SemanticChecking;

import AST.*;
import SymbolTable.*;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Visitor to compute the type of subexpressions and assignment statements. 
 * 
 * This applies only to nodes that are part of expressions and assignment statements i.e.
 * AddOpNode, MultOpNode, and AssignStatp_node. 
 * 
 */


//statement 加temp var
//有哪些statement 需要temp var
//AddOpNode MultOpNode NumNode FuncCallNode SignNode RelOpNode 作为tempVar加入table
//SignNode 看做0-node 或者 0+node（+可以忽略）


public class TypeCheckingVisitor extends Visitor {

	public String m_outputfilename = new String();
	public String m_errors         = new String();
	public SymTab currentScope;
	public SymTab globalTable;
    public Integer m_tempVarNum     = 0;

    public String m_symTabfilename = new String();;
    
	public TypeCheckingVisitor() {
	}
	
	public TypeCheckingVisitor(String p_filename) {
		this.m_outputfilename = p_filename; 
	}

    public String getNewTempVarName(){
        m_tempVarNum++;
        return "t" + m_tempVarNum.toString();
    }

	public void visit(ProgNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//        System.out.println("visit ProgNode");
        globalTable = p_node.m_symtab;
        currentScope = p_node.m_symtab;
		for (Node child : p_node.getChildren())
			child.accept(this);

		if (!this.m_outputfilename.isEmpty()) {
			File file = new File(this.m_outputfilename);
			try (PrintWriter out = new PrintWriter(file)){ 
			    out.println(this.m_errors);
			}
			catch(Exception e){
				e.printStackTrace();}
		}
		// output symbolTable with tamp vars
        if (!this.m_symTabfilename.isEmpty()) {
            File file = new File(this.m_symTabfilename);
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

	public void visit(AddOpNode p_node){
        // idNode, NumNode, varNode, funcCall, add, mlt
        // 如果 int b = a[1]+a[2] 那这个node应该是int 不是int[]
		for (Node child : p_node.getChildren() )
			child.accept(this);

		String leftOperandType  = p_node.getChildren().get(0).getType();
		String rightOperandType = p_node.getChildren().get(1).getType();

		if( leftOperandType.equals(rightOperandType) ){
            p_node.setType(leftOperandType);
//            VarEntry entry = new VarEntry("var",leftOperandType,"",new Vector<Integer>());
//            p_node.m_symtabentry = entry;

        } else{
			p_node.setType("typeerror");
			this.m_errors += "AddOpNode type error:  "
                    + p_node.getSubtreeString() + ",  "
					+ p_node.getChildren().get(0).getData()
					+ "(" + p_node.getChildren().get(0).getType() + ")"
					+  " and "
					+ p_node.getChildren().get(1).getData()
					+ "(" + p_node.getChildren().get(1).getType() + ")"
					+ "\n";
		}
        String tempvarname = this.getNewTempVarName();
        p_node.m_moonVarName = tempvarname;
        p_node.m_symtabentry = new VarEntry("tempvar", p_node.getType(), p_node.m_moonVarName, new Vector<Integer>());
        p_node.m_symtab = currentScope;
        p_node.m_symtab.addEntry(p_node.m_symtabentry);
	}
	
	public void visit(MultOpNode p_node){
        System.out.println("visit MultOpNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);

		String leftOperandType  = p_node.getChildren().get(0).getType();
		String rightOperandType = p_node.getChildren().get(1).getType();
		if( leftOperandType.equals(rightOperandType) ){
            p_node.setType(leftOperandType);
        } else{
			p_node.setType("type error");
			this.m_errors += "MultOpNode type error: "
                    + p_node.getSubtreeString() + ",  "
					+ p_node.getChildren().get(0).getData()
					+ "(" + p_node.getChildren().get(0).getType() + ")"
					+  " and "
					+ p_node.getChildren().get(1).getData()
					+ "(" + p_node.getChildren().get(1).getType() + ")"
					+ "\n";
		}
        String tempvarname = this.getNewTempVarName();
        p_node.m_moonVarName = tempvarname;
        p_node.m_symtabentry = new VarEntry("tempvar", p_node.getType(), p_node.m_moonVarName, new Vector<Integer>());
        p_node.m_symtab = currentScope;
        p_node.m_symtab.addEntry(p_node.m_symtabentry);
	}
	
	public void visit(AssignStatNode p_node){
		for (Node child : p_node.getChildren() ){
//            System.out.println("child : "+ child.getClass().getSimpleName());
            child.accept(this);
        }

		String leftOperandType  = p_node.getChildren().get(0).getType();
//		System.out.println("leftOperandType : "+ leftOperandType);

		String rightOperandType = p_node.getChildren().get(1).getType();
		if( leftOperandType.equals(rightOperandType) ){
            p_node.setType(leftOperandType);
            VarEntry entry = new VarEntry("var",leftOperandType,"",new Vector<Integer>());
            p_node.m_symtabentry = entry;
        } else{
			p_node.setType("typeerror");
			this.m_errors += "AssignStatNode type error: "
                    + p_node.getSubtreeString() + ",  "
					+ p_node.getChildren().get(0).getData()
					+ "(" + p_node.getChildren().get(0).getType() + ")"
					+  " and "
					+ p_node.getChildren().get(1).getData()
					+ "(" + p_node.getChildren().get(1).getType() + ")"
					+ "\n\n";
		}
        p_node.m_symtab = currentScope;
	}


    public void setFuncCallType(FuncCallNode funCallNode){
        // varNode 会有3种类型child： IdNode AparamList IdNode IndiceRepNode
        // 用tempVar 来保存每一个操作的结果
        // varNode 可以有一个tempvar list
        // function call 有可能return void

        String tempVarOrFunc = "";
        SymTabEntry entry = null;
        int index = 0;

        Node firstNode = funCallNode.getChildren().get(0);

        if(!(firstNode.getClass().getSimpleName().equals("IdNode") )){
            // toDO
            System.out.println("Var Error");
            funCallNode.m_type = "TypeError";
        } else {
            tempVarOrFunc = firstNode.m_data;
            String entryKind = currentScope.lookupName(tempVarOrFunc).m_kind;

            if (entryKind != null && (entryKind.equals("var") || entryKind.equals("param"))) {
                entry = currentScope.lookupName(tempVarOrFunc);

            } else if (entryKind != null && entryKind.equals("func")){
                //free function
                index ++;
                if(!funCallNode.getChildren().get(index).getClass().getSimpleName().equals("AparamList")){
                    System.out.println(" Error : function call error,  (" + tempVarOrFunc + " ) no params\n");
                    funCallNode.m_type = "TypeError";
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + tempVarOrFunc
                            + " - function call doesn't come with params"
                            + "\n\n";
                    return;
                }
                AparamList aparamList = (AparamList) funCallNode.getChildren().get(index);

                aparamList.accept(this);
                ArrayList<VarEntry> aparams = getParamList(aparamList);

                entry = globalTable.lookUpFunc(tempVarOrFunc,"",aparams);
                if(entry != null){
                    //要把type保存在id node
                    // type 有可能是void
                    funCallNode.getChildren().get(index-1).setType(entry.m_type);

                }else {
                    System.out.println(" Error : function call error,  '  "+ tempVarOrFunc + "( "+ covertParamlsitToStr_node((ArrayList<Node>) aparamList.getChildren()) +" )  ' , can't find this free function \n");
                    funCallNode.m_type = "TypeError";
                    String paramStr = covertParamlsitToStr_node((ArrayList<Node>) aparamList.getChildren());
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "Function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + tempVarOrFunc
                            + "( "+ paramStr.substring(0,paramStr.length()-1) +" )"
                            + ", can't find this free function \n\n";
                    return;
                }

            }else {
                // toDO
                //错误的名字
                System.out.println("Can't find ( " + tempVarOrFunc + " ) \n");
                funCallNode.m_type = "TypeError";
                createFunCallEntry(funCallNode,entry);

                this.m_errors += "function call error:  "
                        + funCallNode.getSubtreeString() + ",  "
                        + "Can't find the  "
                        + tempVarOrFunc + "\n\n";
                return;
            }
        }

        for(int i = index+1; i<funCallNode.getChildren().size(); i++){

            if (funCallNode.getChildren().get(i).getClass().getSimpleName().equals("IdNode") ){
                // check if tempType is a class
                String varType = "";
                if(entry != null && entry.m_kind != null){
                    varType = entry.m_type;
                    funCallNode.getChildren().get(i-1).setType(varType);
                } else {
                    System.out.println("Can't find the var (" + tempVarOrFunc +" )");
                    funCallNode.m_type = "TypeError";
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "Function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + "Can't find the  "
                            + tempVarOrFunc
                            + "\n\n";
                    return;
                }

                SymTabEntry tempClass =  globalTable.lookupName(varType);
                if(tempClass.m_kind == null || !tempClass.m_kind.equals("class")){
                    // toDO
                    System.out.println("Can't find the class (" + varType+" )\n");
                    funCallNode.m_type = "TypeError";
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "Function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + "Can't find the class "
                            + varType
                            + "\n\n";
                    return;
                }else {
                    //是一个class,接着确认id是否合法
                    //1. 是否存在id
                    //2. id 可能是 var 或者 func
                    tempVarOrFunc = funCallNode.getChildren().get(i).m_data;

                    if(tempClass.m_subtable.lookupName(tempVarOrFunc).m_kind == null){
                        System.out.println("Can't find the  : (" + tempVarOrFunc + " )  in the class ( "+ tempClass.m_name+" )\n");
                        funCallNode.m_type = "TypeError";
                        createFunCallEntry(funCallNode,entry);

                        this.m_errors += "Function call error:  "
                                + funCallNode.getSubtreeString() + ",  "
                                + "Can't find  "
                                + tempVarOrFunc
                                + " in the class "+ tempClass.m_name+" \n\n";
                        return;

                    }else if(tempClass.m_subtable.lookupName(tempVarOrFunc).m_kind.equals("var")){
                        //var 和 func 不能重名

                        entry = tempClass.m_subtable.lookupName(tempVarOrFunc);
                        funCallNode.getChildren().get(i).setType(entry.m_type);

                    }else if(tempClass.m_subtable.lookupName(tempVarOrFunc).m_kind.equals("func")){
                        //如果是func 要考虑overloading的问题，匹配参数列表，不过不匹配，找下一个func
                        //先收集参数列表
                        //把参数传给classentry去找相应的function
                        i = i +1;
                        if(!funCallNode.getChildren().get(i).getClass().getSimpleName().equals("AparamList")){
                            System.out.println(" Error : Function call error,  (" + tempVarOrFunc + " ) no params\n");
                            funCallNode.m_type = "TypeError";
                            createFunCallEntry(funCallNode,entry);

                            this.m_errors += "Function call error:  "
                                    + funCallNode.getSubtreeString() + ",  "
                                    + tempVarOrFunc
                                    + " - function call doesn't come with params"
                                    + "\n\n";
                            return;
                        }
                        AparamList aparamList = (AparamList) funCallNode.getChildren().get(i);
                        // apramlist 先deep一遍收集信息
                        aparamList.accept(this);
                        ArrayList<VarEntry> aparams = getParamList(aparamList);

                        entry = ((ClassEntry) tempClass).lookUpFunc(tempVarOrFunc,"",aparams);
                        if(entry != null){
                            //要把type保存在id node
                            // type 有可能是void
                            funCallNode.getChildren().get(i-1).setType(entry.m_type);

                        }else {
                            System.out.println(" Error : Function call error,  (" + tempVarOrFunc + " ) , can't find it in class "+ tempClass.m_name+"\n");
                            funCallNode.m_type = "TypeError";
                            createFunCallEntry(funCallNode,entry);

                            String paramStr = covertParamlsitToStr_node((ArrayList<Node>) aparamList.getChildren());
                            this.m_errors += "Function call error:  "
                                    + funCallNode.getSubtreeString() + ",  "
                                    + tempVarOrFunc
                                    + "( "+ paramStr +" )\n\n";
                            return;
                        }

                    }else {
                        //找到entry，但不是var 也不是 func
                        System.out.println(" Error : the var : (" + tempVarOrFunc + " )  in the class ( "+ tempClass.m_name+" )\n");
                        funCallNode.m_type = "TypeError";
                        createFunCallEntry(funCallNode,entry);

                        this.m_errors += "Function call error:  "
                                + funCallNode.getSubtreeString() + ",  "
                                + tempVarOrFunc
                                + " is a " + tempClass.m_subtable.lookupName(tempVarOrFunc).m_kind + ")\n\n";
                        return;
                    }
                }
            } else if (funCallNode.getChildren().get(i).getClass().getSimpleName().equals("IndiceRepNode") ){

                // ckeck var 的dim

                if(entry == null || !entry.m_kind.equals("var")) {
                    System.out.println("Dimension Error  :" + tempVarOrFunc + " is a function\n");
                    funCallNode.m_type = "TypeError";
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "Function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + tempVarOrFunc
                            + " isn't a variable\n\n";
                    return;
                }

                IndiceRepNode dimNodeList = (IndiceRepNode) funCallNode.getChildren().get(i);
                Vector<Integer> dimList = entry.m_dims;
                if(dimNodeList.getChildren().size() != dimList.size()){
                    // toDO
                    System.out.println("Dimension Error  :" + tempVarOrFunc + " has wrong dimension\n");
                    funCallNode.m_type = "TypeError";
                    createFunCallEntry(funCallNode,entry);

                    this.m_errors += "Function call error:  "
                            + funCallNode.getSubtreeString() + ",  "
                            + tempVarOrFunc
                            + " 's dimension is wrong\n\n";
                    return;
                }else {
                    //进入dimNodeList 中的dimNode，可能有表达式来表示
                    //1. 表达式是否合法
                    //2. 表达式必须是整型
                    // toDO
                    for(Node dimNode : dimNodeList.getChildren()){
                            dimNode.accept(this);
                            //IdNode, NumNode, addOp, FuncCallNode, multOp,
                            if (!dimNode.getType().equals("integer")){
                                System.out.println("Error: dim number must be a integer");
                                funCallNode.m_type = "TypeError";
                                createFunCallEntry(funCallNode,entry);

                                this.m_errors += "Function call error:  "
                                        + funCallNode.getSubtreeString() + ",  "
                                        + tempVarOrFunc
                                        + ", dim number must be a integer\n\n";
                                return;
                            }
                    }
                }

            }
        }

        funCallNode.setType(entry.m_type);
        funCallNode.tag = ((FuncEntry)entry).tag;
        funCallNode.funcCallEntry = entry;
        createFunCallEntry(funCallNode,entry);
    }

    public void createFunCallEntry(FuncCallNode funCallNode, SymTabEntry returnEntry){
	    //怎么同时保存function entry 和 return val
        // 需要entry 传递param
        String tempvarname = this.getNewTempVarName();
        funCallNode.m_moonVarName = tempvarname;

        if(returnEntry != null){
            funCallNode.m_symtabentry = new VarEntry("retval", funCallNode.getType(), funCallNode.m_moonVarName, returnEntry.m_dims);
        }else {
            funCallNode.m_symtabentry = new VarEntry("retval", funCallNode.getType(), funCallNode.m_moonVarName, new Vector<Integer>());
        }
        funCallNode.m_symtab = currentScope;
        funCallNode.m_symtab.addEntry(funCallNode.m_symtabentry);
    }




    public ArrayList<VarEntry> getParamList(Node aparamListNode){
	    //visit完aparamListNode 之后，调用get function

        ArrayList<VarEntry> paramsList = new ArrayList<>();
            for(Node child : aparamListNode.getChildren()){
//                System.out.println("aparamListNode child node:"+ child.getClass().getSimpleName() + ",type:" + child.getType());
                if (child.m_symtabentry != null && child.m_symtabentry.getClass().getSimpleName().equals("VarEntry")){
                    paramsList.add((VarEntry) child.m_symtabentry);
                } else if (child.getClass().getSimpleName().equals("NumNode")){
                    VarEntry varEntry = new VarEntry("var",child.m_type,"",new Vector<Integer>());
                    paramsList.add(varEntry);
                } else if (child.m_type.equals("void")){
                    VarEntry varEntry = new VarEntry("var","void","",new Vector<Integer>());
                    paramsList.add(varEntry);
                } else if (child.getClass().getSimpleName().equals("SignNode")) {
                    VarEntry varEntry = new VarEntry("var", child.m_type, "", new Vector<Integer>());
                    paramsList.add(varEntry);
                } else {
                    System.out.println(" error : aparamListNode child node:"+ child.getClass().getSimpleName() + ",type:" + child.getType());
                }
            }

        return paramsList;
    }


    public String covertParamlsitToStr_varEntry(ArrayList<VarEntry> paramList){
        String str = "";
        for(VarEntry param : paramList){
            str = str + param.m_type + param.getDimsString()+ ",";
        }
        return str;
    }

    public String covertParamlsitToStr_node(ArrayList<Node> paramList){
        String str = "";
        for(Node param : paramList){
            str = str + param.m_type +",";
        }
        return str;
    }





    public void setVarType(VarNode varNode){
        // varNode 会有两种类型child： IdNode IdNode IndiceRepNode
        // 用tempVar 来保存每一个操作的结果
        // varNode 可以有一个tempvar list

        String tempVar = "";
        VarEntry varEntry = null;


        Node firstNode = varNode.getChildren().get(0);

        if(!(firstNode.getClass().getSimpleName().equals("IdNode") )){
            // toDO
            System.out.println("Var Error");
            this.m_errors += "Var error:  use of undeclared local variable"
                    + varNode.getSubtreeString() + ",  "
                    + firstNode.getData()
                    + "\n\n";
            return;

        }else {
            tempVar = firstNode.m_data;
            String entryKind = currentScope.lookupName(tempVar).m_kind;
            if (entryKind != null && (entryKind.equals("var") || entryKind.equals("param"))) {
                varEntry = (VarEntry) currentScope.lookupName(tempVar);
                firstNode.m_symtabentry = varEntry;
                firstNode.m_symtabentry.m_offset = varEntry.m_offset;

            } else {
                // toDO
                System.out.println("Can't find the  ( " + tempVar + " ) \n");
                varNode.m_type = "TypeError";
                this.m_errors += "Var error:  "
                        + varNode.getSubtreeString() + ",  "
                        + "Can't find the ( " + tempVar + " ), use of undeclared variable \n\n";
                return;

            }
        }

        for(int i = 1; i<varNode.getChildren().size(); i++){

            Node child = varNode.getChildren().get(i);

            if (child.getClass().getSimpleName().equals("IdNode") ){
                // check if tempType is a class
                String varType = "";
                if(varEntry != null && varEntry.m_kind != null){
                    varType = varEntry.m_type;
                    varNode.getChildren().get(i-1).setType(varType);

                    varNode.getChildren().get(i-1).m_symtabentry = varEntry;

                } else {
                    System.out.println("Can't find the var (" + tempVar +" )");
                    varNode.m_type = "TypeError";
                    this.m_errors += "Var error:  "
                            + varNode.getSubtreeString() + ",  "
                            + "Can't find the ( " + tempVar + " ), use of undeclared variable \n\n";
                    return;

                }

                SymTabEntry tempClass =  globalTable.lookupName(varType);
                if(tempClass.m_kind == null || !tempClass.m_kind.equals("class")){
                    // toDO
                    System.out.println("Can't find the class (" + varType+" )\n");
                    varNode.m_type = "TypeError";
                    this.m_errors += "Var error:  "
                            + varNode.getSubtreeString() + ",  "
                            + "Can't find the  class ( " + tempVar + " ) , use of undeclared class\n\n";
                    return;

                }else {
                    //是一个class,接着确认变量是否合法
                    //1. 是否存在变量
                    tempVar = varNode.getChildren().get(i).m_data;

                    if(tempClass.m_subtable.lookupName(tempVar).m_kind == null || !tempClass.m_subtable.lookupName(tempVar).m_kind.equals("var")){
                        // toDO
                        System.out.println("Can't find the var : (" + tempVar + " )  in the class ( "+ tempClass.m_name+" )\n");
                        varNode.m_type = "TypeError";
                        this.m_errors += "Var error:  "
                                + varNode.getSubtreeString() + ",  "
                                + "Can't find the var : (" + tempVar + " )  in the class ( "+ tempClass.m_name+" ), use of undeclared variable\n\n";
                        return;

                    }else {
                        varEntry = (VarEntry) tempClass.m_subtable.lookupName(tempVar);
                        varNode.getChildren().get(i).setType(varEntry.m_type);
                        varNode.getChildren().get(i).m_symtabentry = varEntry;
                        varNode.getChildren().get(i).m_symtabentry.m_offset = varEntry.m_offset;

                    }
                }
            }else if (varNode.getChildren().get(i).getClass().getSimpleName().equals("IndiceRepNode") ){
                //说明是varTemp有dim
                // ckeck var 的dim
                if(varEntry != null){
                    IndiceRepNode dimNodeList = (IndiceRepNode) varNode.getChildren().get(i);
                    Vector<Integer> dimList = varEntry.m_dims;
                    if(dimNodeList.getChildren().size() != dimList.size()){
                        // toDO
                        System.out.println("Dimension Error  :" + tempVar + " has a wrong dimension\n");
                        varNode.m_type = "TypeError";
                        this.m_errors += "Dimension Error  :"
                                + varNode.getSubtreeString() + ",  "
                                + tempVar + " has a wrong dimension\n\n";
                        return;

                    }else {
                        //进入dimNodeList 中的dimNode，可能有表达式来表示
                        //1. 表达式是否合法
                        //2. 表达式必须是整型
                        // toDO
                        for(Node dimNode : dimNodeList.getChildren()){
                            dimNode.accept(this);
                            System.out.println("dimNode type:"+ dimNode.getClass().getSimpleName());
//                            //IdNode, NumNode, addOp, FuncCallNode, multOp,
                            if (!dimNode.getType().equals("integer")){
                                System.out.println("Error: dim number must be a integer");
                                varNode.m_type = "TypeError";
                                this.m_errors += "Dimension error: "
                                        + varNode.getSubtreeString() + ",  "
                                        + " dim number must be a integer\n\n";
                                return;
                            }
                        }

                    }
                }
                // 根据IndiceRepNode下标值计算offset，搭配数组维度和大小
                // TODO update pre varNode offset
                varNode.getChildren().get(i-1).m_symtabentry.m_offset = varEntry.m_offset;

            }
        }
        varNode.setType(varEntry.m_type);
        varNode.m_data = varEntry.m_name;
        // 要new一个entry 不能用同一个引用
        varNode.m_symtabentry = new VarEntry(varEntry.m_kind,varEntry.m_type,varEntry.m_name,varEntry.m_dims);;
    }


	public void visit(FuncCallNode p_node) {
        // IdNode AparamList IdNode IndiceRepNode
//        System.out.println("visit FuncCallNode");
        //convert to dot operator
//        for (Node child : p_node.getChildren() ){
////            System.out.println("child : "+ child.getClass().getSimpleName());
//            child.accept(this);
//        }
        setFuncCallType(p_node);
	};

	public void convertDotOperator(FuncCallNode p_node){
	    Node newParentNode = new DotNode("");
	    Node preNode = p_node.getChildren().get(0);
	    for(int i = 1; i<p_node.getChildren().size(); i++){
	        if (p_node.getChildren().get(i).getClass().getSimpleName().equals("IdNode")){
	            DotNode dotNode = new DotNode("");
                dotNode.addChild(preNode);
                dotNode.addChild(p_node.getChildren().get(i));
                preNode = p_node.getChildren().get(i);
            }
        }
    }
	
	// Below are the visit methods for node types for which this visitor does not apply
	// They still have to propagate acceptance of the visitor to their children.
	public void visit(ClassListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(ClassNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        currentScope = p_node.m_symtabentry.m_subtable;
//		for (Node child : p_node.getChildren())
//			child.accept(this);
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

	public void visit(FuncDefNode p_node) {
//	    System.out.println("visit FuncDefNode");
	    if(p_node.m_symtabentry == null){
            System.out.println("FuncDefNode eror, has no m_symtabentry");
        } else {
            currentScope = p_node.m_symtabentry.m_subtable;
            System.out.println(currentScope.toString());
        }

		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()){
		    if (child.getClass().getSimpleName().equals("StatBlockNode")){
                child.accept(this);
            }
        }

//		p_node.setData(p_node.getChildren().get(1).getData());
//		p_node.setType(p_node.getChildren().get(1).getType());
	};

	public void visit(IdNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		if (currentScope.lookupName(p_node.m_data).getClass().getSimpleName().equals("VarEntry")){
            p_node.m_type = currentScope.lookupName(p_node.m_data).m_type;
            p_node.m_symtabentry = currentScope.lookupName(p_node.m_data);
        }else {
		    System.out.println("Error: can't find the var ("+p_node.m_data+") ");
            p_node.m_type = "TypeError";
            this.m_errors += "var error:  "
                    + p_node.getSubtreeString() + ", "
                    +"can't find the ("+p_node.m_data+") \n\n";
        }

        p_node.m_moonVarName = p_node.m_data;
	};

	public void visit(Node p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(NumNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        for (Node child : p_node.getChildren())
            child.accept(this);
        //NumNode  已经设置好type

        String tempvarname = this.getNewTempVarName();
        p_node.m_moonVarName = tempvarname;
        String vartype = p_node.getType(); // int float string
        p_node.m_symtabentry = new VarEntry("litval", vartype, p_node.m_moonVarName, new Vector<Integer>());
        p_node.m_symtab = currentScope;
        p_node.m_symtab.addEntry(p_node.m_symtabentry);
	};

	public void visit(ProgramBlockNode p_node) {
        currentScope = p_node.m_symtab;
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        for (Node child : p_node.getChildren()){
            if (child.getClass().getSimpleName().equals("StatBlockNode")){
                child.accept(this);
            }
        }
	};

	public void visit(PutStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(StatBlockNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
	};

	public void visit(TypeNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren())
			child.accept(this);
		p_node.m_type = p_node.getData();
	};

	public void visit(VarDeclNode p_node) {
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
	}

	public void visit(ReturnStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
        p_node.m_symtab = currentScope;
	};


	public void visit(IfStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit IfStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
        p_node.m_symtab = currentScope;

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
        p_node.m_symtab = currentScope;
	};

	public void visit(RelOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit RelOpNode");
        for (Node child : p_node.getChildren() )
            child.accept(this);

        String leftOperandType  = p_node.getChildren().get(0).getType();
//        System.out.println("leftOperandType : "+ leftOperandType);

        String rightOperandType = p_node.getChildren().get(1).getType();
        if( leftOperandType.equals(rightOperandType) ){
            p_node.setType("integer");
//            VarEntry entry = new VarEntry("var","integer","",new Vector<Integer>());
//            p_node.m_symtabentry = entry;
        } else{
            p_node.setType("typeerror");
            this.m_errors += "RelOpNode type error: "
                    + p_node.getSubtreeString() + ", "
                    + p_node.getChildren().get(0).getData()
                    + "(" + p_node.getChildren().get(0).getType() + ")"
                    +  " and "
                    + p_node.getChildren().get(1).getData()
                    + "(" + p_node.getChildren().get(1).getType() + ")"
                    + "\n\n";
        }
        String tempvarname = this.getNewTempVarName();
        p_node.m_moonVarName = tempvarname;
        p_node.m_symtabentry = new VarEntry("tempvar", p_node.getType(), p_node.m_moonVarName, new Vector<Integer>());
        p_node.m_symtab = currentScope;
        p_node.m_symtab.addEntry(p_node.m_symtabentry);

	};

	public void visit(SignNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
        // 0-node, 0+node
        System.out.println("visit SignNode");
        p_node.setType(p_node.getChildren().get(0).m_type);

        String tempvarname = this.getNewTempVarName();
        p_node.m_moonVarName = tempvarname;
        p_node.m_symtabentry = new VarEntry("tempvar", p_node.getType(), p_node.m_moonVarName, new Vector<Integer>());
        p_node.m_symtab = currentScope;
        p_node.m_symtab.addEntry(p_node.m_symtabentry);
	};


	public void visit(WhileStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit WhileStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
        p_node.m_symtab = currentScope;
	};

	public void visit(WriteStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		System.out.println("visit WriteStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
        p_node.m_symtab = currentScope;
	};

	public void visit(VarNode p_node){
        for(Node child : p_node.getChildren()){
            if(!child.getClass().getSimpleName().equals("IdNode")){
                child.accept(this);
            }
        }
        setVarType(p_node);
    }

    public void visit(AparamList p_node){
	    // NumNode, IdNode, functioncall, VarNode
        //每个param都有type和dim
        //VarNode会有一个entry，从entry访问dim
        //functioncall 如果返回的不是简单类型，int string float，也应该有一个entry
        //IdNode 也会有一儿entry
        for(Node child : p_node.getChildren()){
            child.accept(this);
        }
    }

    public void visit(IndiceRepNode p_node){

    }

    public void visit(DotNode p_node){

    }
}
