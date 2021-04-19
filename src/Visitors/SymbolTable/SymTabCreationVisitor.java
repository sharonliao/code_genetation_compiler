package Visitors.SymbolTable;

import AST.*;
import SymbolTable.*;
import Visitors.SemanticChecking.CircleDetect;
import Visitors.Visitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.List;

/**
 * Visitor to create symbol tables and their entries.  
 * 
 * This concerns only nodes that either:  
 * 
 * (1) represent identifier declarations/definitions, in which case they need to assemble 
 * a symbol table record to be inserted in a symbol table. These are:  VarDeclNode, ClassNode,  
 * and FuncDefp_node. 
 * 
 * (2) represent a scope, in which case they need to create a new symbol table, and then 
 * insert the symbol table entries they get from their children. These are:  ProgNode, ClassNode, 
 * FuncDefNode, and StatBlockp_node.   
 */

public class SymTabCreationVisitor extends Visitor {
	
    public Integer m_tempVarNum     = 0;
	public String  m_outputfilename = new String();
	public String  m_errorFilename = new String();
	public String  m_errors = "";


	//用于检查是否有环，和 inherit的class是否都存在
	public ArrayList<ClassNode> classList = new ArrayList<>();
	public ArrayList<InheritNode> inheritList = new ArrayList<>();
    public ArrayList<FuncEntry> funcDeclareList = new ArrayList<>();
    public SymTab globalTable;

	public int funcID = 0;
	public int getFuncID(){
		return funcID++;
	}


	public SymTabCreationVisitor() {
	}
	
	public SymTabCreationVisitor(String p_filename) {
		this.m_outputfilename = p_filename; 
	}
    
    public String getNewTempVarName(){
    	m_tempVarNum++;
    	return "t" + m_tempVarNum.toString();  
    }

    public void updateInherit(Node classNode){
		for (InheritNode inheritNode : inheritList) {
			if(inheritNode.m_symtabentry.m_subtable == null){
				if(inheritNode.m_symtabentry.m_name.equals(classNode.m_symtabentry.m_name)){
					inheritNode.m_symtabentry.m_subtable = classNode.m_symtabentry.m_subtable;
//					System.out.println("update inherit class :"+ inheritNode.getParent().m_symtabentry.m_name + " -- " + inheritNode.m_symtabentry.m_name);
				}
			}
		}
	}

	public boolean checkAllInherit(){
		boolean result = true;
		for (InheritNode inheritNode : inheritList) {
			//if sub table is null , mean this inheritance class wasn't linked
			if(inheritNode.m_symtabentry.m_subtable == null){
				System.out.println("couldn't find the inherit class :" + inheritNode.m_symtabentry.m_name);
                this.m_errors += "Inherit Error: couldn't find the inherit class :" + inheritNode.m_symtabentry.m_name +  "\n";
			}
		}
		return result;
	}

	public boolean checkInheritCircle(){
        String result;
        CircleDetect detecter = new CircleDetect(inheritList);
        result = detecter.deepSearch();
        if(result.length()>0){
            this.m_errors += result + "\n";
            return false;
        }
        return true;
    }

    public void checkAllFuncDeclare(){
	    System.out.println("total member function:" + funcDeclareList.size());
        for (FuncEntry entry : funcDeclareList){
            if (entry.definedFlag == false){
                String paramStr = covertParamlsitToStr_varEntry(entry.getParamsEntryList());
                System.out.println("Error: Function ( " + entry.m_name+ " ) hasn't been defined.");
                System.out.println("       func "+ entry.m_name+ " ( "+paramStr + " ) : "+ entry.m_type +"\n");

                this.m_errors += "Error: Function ( " + entry.m_name+ " ) hasn't been defined.\n"
                        +"       func "+ entry.m_name+ " ( "+paramStr + " ) : "+ entry.m_type +"\n";

            }
        }
    }

	public boolean checkDuplicateName(SymTab subTable, SymTabEntry curEntry ){
//		1. class def 中的 function name，var name是否用重复
//		2. zai subtable中检查，这是同一个scope
//		3. var 和 param 算不算是同类型
//		4. 还要排除overload的情况，检查param list
		String name = curEntry.m_name;
		String type = curEntry.getClass().getSimpleName();

//		System.out.println("checkDuplicateName: "+ type);
//		System.out.println("subTable.m_symlist size: " + subTable.m_symlist.size());


		boolean result = true;
		if(type.compareTo("FuncEntry")==0){
		    int len = subTable.m_symlist.size()-1;
		    for (int i = 0; i<len; i++){
                SymTabEntry entry = subTable.m_symlist.get(i);
                if(entry.m_name.compareTo(name) == 0 && entry.getClass().getSimpleName().compareTo(type) == 0){
//				    System.out.println("checkDuplicateName Function name duplicate");
//				    那么还要考虑overloading的情况, 检查 param list
//				    toDo overloading
                    ArrayList<String> curFuncParams = getParamList(curEntry);
                    ArrayList<String> funcParams = getParamList(entry);
                    System.out.println("\ncurFuncParams: "+covertParamlsitToStr(curFuncParams));
                    System.out.println("funcParams: "+covertParamlsitToStr(funcParams)+"\n");
//				    包括param type的顺序不一样也是overloading
                    if(!curFuncParams.equals(funcParams)){
//					    toDo warning output.
                        this.m_errors += "Warning: "
                                + "Overloading:  function ( "+ curEntry.m_name + " ) overloading function in (  "+ subTable.m_name+" ) class\n";

                    }else {
                        System.out.println("Error: In ( "+ subTable.m_name + " ) class already has ( "+name+" ) function.");
                        System.out.println("       func "+ name + " ( "+ covertParamlsitToStr(curFuncParams) + " ) function.\n");
                        this.m_errors += "Error: "
                                + "In ( "+ subTable.m_name + " ) class already has ( "+name+" ) function.\n"
                                +"       func "+ name + " ( "+ covertParamlsitToStr(curFuncParams) + " ) function.\n";
                        result = false;
//					    toDo error output.
                    }
                }
            }
        } else{
            for (SymTabEntry entry : subTable.m_symlist){
                if( entry.m_name.compareTo(name) == 0 && entry.getClass().getSimpleName().compareTo(type) == 0){
//				in the same scope, var and param can not have the same name.

                    this.m_errors += "Error: In ( "+ subTable.m_name + " ) already has " + type+ " ( "+name+" )\n";

                    result = false;
//				toDo error output.
                }
            }
        }

		return result;
	}



	public ArrayList<String> getParamList(SymTabEntry funcEntry){
		ArrayList<String> paramList = new ArrayList<>();
		if (funcEntry.m_subtable != null){
			for(SymTabEntry entry : funcEntry.m_subtable.m_symlist){
				if (entry.getClass().getSimpleName().compareTo("VarEntry") == 0 && entry.m_kind.compareTo("param")==0){
					paramList.add(entry.m_type);
				}
			}
		}
		return paramList;
	}

    public ArrayList<VarEntry> getParamList(Node ParamListNode){

        SymTab tempDefTable = new SymTab(0,null);
        for (Node child : ParamListNode.getChildren() ) {
            child.m_symtab = tempDefTable;
            child.accept(this);
        }

        ArrayList<VarEntry> paramsList = new ArrayList<>();
        for(SymTabEntry entry : tempDefTable.m_symlist){
            if(entry.m_kind.compareTo("param") == 0){
                paramsList.add((VarEntry) entry);
            }
        }
        return paramsList;
    }


    public String covertParamlsitToStr(ArrayList<String> paramList){
		String str = "";
		for(String param : paramList){
			str = str + param + ",";
		}
		return str;
	}

    public String covertParamlsitToStr_varEntry(ArrayList<VarEntry> paramList){
        String str = "";
        for(VarEntry param : paramList){
            str = str + param.m_type + " " + param.m_name + ",";
        }
        return str;
    }


	public void endingChecking(){
		checkAllInherit();
		checkInheritCircle();
        checkAllFuncDeclare();
	}


	public void visit(ProgNode p_node){
		//ClassListNode FuncDefListNode ProgramBlockNode
		p_node.m_symtab = new SymTab(0,"global", null);
		globalTable = p_node.m_symtab;
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() ) {
			//make all children use this scopes' symbol table
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
        endingChecking();

		if (!this.m_outputfilename.isEmpty()) {
			File file = new File(this.m_outputfilename);
			try (PrintWriter out = new PrintWriter(file)){ 
			    out.println(p_node.m_symtab);
			}
			catch(Exception e){
				e.printStackTrace();}
		}
        if (!this.m_errorFilename.isEmpty()) {
            File file = new File(this.m_errorFilename);
            try (PrintWriter out = new PrintWriter(file)){
                out.println(this.m_errors);
            }
            catch(Exception e){
                e.printStackTrace();}
        }


	};

	public void visit(StatBlockNode p_node){
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		//怎么做type checking
        //1. assignment 两边的type
        //2. addOp 两边的type
        //3. multOP 两边的type
        //4. RelOP 两边的type
        //5. var 是否是用正确的dim 调用可用的var和funciton，是否是合法class

//        symbol table 目前不需要 statement block

//		for (Node child : p_node.getChildren() ) {
//			child.m_symtab = p_node.m_symtab;
//			child.accept(this);
//		}
	};

	
	public void visit(ProgramBlockNode p_node){
		int p_level = p_node.m_symtab.getLevel()+1;
        SymTab localtable = new SymTab(p_level,"program", p_node.m_symtab);
        p_node.m_symtabentry = new FuncEntry("void","program","", localtable);
		//检查是否有重复def
		checkDuplicateName(p_node.m_symtab,p_node.m_symtabentry);

		//String p_type, String p_name, String p_classMethod, SymTab p_table
		p_node.m_symtab.addEntry(p_node.m_symtabentry);
		p_node.m_symtab = localtable;
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() ) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};


	public void visit(ClassNode p_node){
		//IdNode(classname),InheritNode,VarDeclNode（可能有多个),FuncDeclareNode(可能有多个)
		//InheritListNode 要怎么处理 也应该create一个record，link到相应的class
		//<classDecl> ::= 'class' 'id' <Inherit> '{' <ClassDeclBodyList> '}' ';'
		//ClassDeclBodyList : FuncDeclareNode,VarDeclNode
		//每个class都有一个local table

		//p_node 拿到 prog的m_symtab
		//所以这个p_node的m_symtabentry 是 add 到 p_node.m_symtab
		classList.add(p_node);
		String classname = p_node.getChildren().get(0).getData();
		int p_level = p_node.m_symtab.getLevel()+1;
		SymTab localtable = new SymTab(p_level,classname, p_node.m_symtab);
		//class需要create一个entry
		p_node.m_symtabentry = new ClassEntry(classname, localtable);
		//检查是否有重复def
		checkDuplicateName(p_node.m_symtab,p_node.m_symtabentry);


		//加入prog的m_symtab中
		p_node.m_symtab.addEntry(p_node.m_symtabentry);
		// 这一步？ 太奇怪了吧
		// 切换table，class entry加入pro symtable  之后，可以吧current m_symtab 切换成当前node的table
		p_node.m_symtab = localtable;

		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		// check if there are classes inherit this class, if yes, update inherit node localtable like
		updateInherit(p_node);


		for (Node child : p_node.getChildren() ) {
			//应该要skip IdNode？
//			System.out.println(child.getClass().getSimpleName());
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(InheritNode p_node){
//		System.out.println("enter InheritNode");

		//IdNode(classname),InheritNode,VarDeclNode（可能有多个),FuncDeclareNode(可能有多个)
		//InheritListNode 要怎么处理 也应该create一个record，link到相应的class
		//<classDecl> ::= 'class' 'id' <Inherit> '{' <ClassDeclBodyList> '}' ';'
		//ClassDeclBodyList : FuncDeclareNode,VarDeclNode
		//每个class都有一个local table

		String classname = p_node.getData();
		//可能会报错找不到 找不到相应的class
		SymTab localtable = null;
		if(p_node.m_symtab.lookupName(classname) != null){
			localtable = p_node.m_symtab.lookupName(classname).m_subtable;

		}else {
			//可以加上报错信息
			// two pass 解决顺序问题
			//还需要检测inherit chain是否有环（toDo）
		}

		p_node.m_symtabentry = new InheritEntry(classname, localtable);
		//检查是否有重复def
		checkDuplicateName(p_node.m_symtab,p_node.m_symtabentry);

		p_node.m_symtab.addEntry(p_node.m_symtabentry);


		//如果localtable == null 需要更新class table
		inheritList.add(p_node);
	};


	//VarDeclNode
	//可能是vardecl 也可能是 fparam

	public void visit(VarDeclNode p_node){
		//TypeNode IdNode DimListNode(可能没有) VisibilityNode(可能没有)

		String vartype = "";
		String varid = "";
		String visibility = "";

		Vector<Integer> dimlist = new Vector<Integer>();
		for (Node child : p_node.getChildren()){
			if(child.getClass().getSimpleName().compareTo("TypeNode") == 0){
//				vartype = child.getData().substring(1,child.getData().length()-1);
                vartype = child.getData();

			}else if(child.getClass().getSimpleName().compareTo("IdNode") == 0){
				varid = child.getData();

			}else if(child.getClass().getSimpleName().compareTo("DimListNode") == 0){
				for (Node dim : p_node.getChildren().get(2).getChildren()){
					// parameter dimension
					Integer dimval;
					if (dim.getData().compareTo("")==0){
						dimval = -1;// 表示为没有数字 a[] 在判断维度是否一致是 是否会忽略掉数字？ toDo
					}else {
						dimval = Integer.parseInt(dim.getData());
					}
					dimlist.add(dimval);
				}
			}else if(child.getClass().getSimpleName().compareTo("VisibilityNode") == 0){
				visibility = child.getData();
			}

		}

		String kind = "var";
		if(p_node.getParent().getClass().getSimpleName().compareTo("ParamListNode") == 0){
			kind = "param";
		}

		p_node.m_symtabentry = new VarEntry(kind, vartype, varid, dimlist, visibility);
		p_node.setType(vartype);

		checkDuplicateName(p_node.m_symtab,p_node.m_symtabentry);

		p_node.m_symtab.addEntry(p_node.m_symtabentry);

		for (Node child : p_node.getChildren() ) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}

	}

	public void visit(FuncDeclareNode p_node){
		//ClassMethodNode IdNode ParamListNode TypeNode VisibilityNode

		//Function overloading (i.e. two functions with the same name but with different parameter lists) toDo
		//detect Multiply declared identifiers toDo

		String classMethod = "";
		String ftype = "";
		String fname = "";
		String Visibility = "";
		SymTab localtable;

		for (int i=0; i<p_node.getChildren().size();i++){
			Node child = p_node.getChildren().get(i);
			if(child.getClass().getSimpleName().compareTo("ClassMethodNode") == 0){
				classMethod = child.getData();

			}else if(child.getClass().getSimpleName().compareTo("IdNode") == 0){
				fname = child.getData();

			}else if(child.getClass().getSimpleName().compareTo("ParamListNode") == 0){


			}else if(child.getClass().getSimpleName().compareTo("TypeNode") == 0){
				ftype = child.getData();

			}else if(child.getClass().getSimpleName().compareTo("VisibilityNode") == 0){
				Visibility = child.getData();
			}
		}

		int p_level = p_node.m_symtab.getLevel()+1;
		localtable = new SymTab(p_level,fname, p_node.m_symtab);
		p_node.m_symtabentry = new FuncEntry(ftype, fname, classMethod, Visibility, localtable);
        funcDeclareList.add((FuncEntry) p_node.m_symtabentry);

		//String p_type, String p_name, String p_classMethod, String p_visibility, SymTab p_table
		p_node.m_symtab.addEntry(p_node.m_symtabentry);
		p_node.m_symtab = localtable;

		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() ) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}

		//检查是否有重复def, 因为function 要检查param，所以要等整个function遍历完在来检查是否重复
		checkDuplicateName(p_node.m_symtab.m_uppertable,p_node.m_symtabentry);
	}


	public void visit(FuncDefNode p_node){
		//如何找到之前的 class method
		//ClassMethodNode IdNode ParamListNode TypeNode VarDeclListNode StatBlockNode
		String classMethod = "";
		String ftype = "";
		String fname = "";
		ParamListNode paranlistNode ;
		SymTab localtable;

		// “no definition for declared member function” semantic error toDo
		// “definition provided for undeclared member function” ” semantic error toDo
		//Function overloading (i.e. two functions with the same name but with different parameter lists) toDo
		//detect Multiply declared identifiers,怎么判断已经define过了 加flag toDo

		if(p_node.getChildren().get(0).getClass().getSimpleName().compareTo("ClassMethodNode") == 0){
			classMethod = p_node.getChildren().get(0).getData();
			fname = p_node.getChildren().get(1).getData();
            ftype = p_node.getChildren().get(3).getData();
            paranlistNode = (ParamListNode) p_node.getChildren().get(2);
            ArrayList<VarEntry> paramList = getParamList(paranlistNode);


			//找到class the table
			//向上找
			SymTabEntry class_entry = p_node.m_symtab.lookupName(classMethod);
			//根据name和param和return找到相应的function
            FuncEntry funcEntry;
            if (class_entry.m_name != null){
                //lookUpFunc 可以根据参数列表确定 function
                funcEntry = ((ClassEntry) class_entry).lookUpFunc(fname,ftype,paramList);
                if(funcEntry != null){
                    // 检查是否已经define过了
                    if (funcEntry.definedFlag == false){
                        funcEntry.definedFlag = true;
                        localtable = funcEntry.m_subtable;
                        if (localtable != null){
                            if(p_node.getChildren().size()>4){
                                List<Node> subChildlist =  p_node.getChildren().subList(4,p_node.getChildren().size()-1);
                                for (Node child : subChildlist ) {
                                    child.m_symtab = localtable;
                                    child.accept(this);
                                }
                            }
                        }else {
                            System.out.println("Error: In ( "+fname+" ) function define, there is no localtable.\n" );
                        }
                        p_node.m_symtabentry = funcEntry;
                    }else {
                        //   toDo error output, 重复define
                        System.out.println("Error: ( "+fname+" ) function has been defined." );
                        System.out.println("       "+fname + "( "+covertParamlsitToStr_varEntry(paramList)+" ) ,  return :" + ftype+"\n");

                        this.m_errors += "Error: "
                                + p_node.getSubtreeString()
                                +", function has been defined \n";
                    }
                } else {
                    //    toDo error output 在class里没找到function
                    System.out.println("Error: In ( "+fname+" ) function define, can't find ( "+ fname+" ) function in  ( "+ classMethod+" ) class.");
                    System.out.println("       "+fname + "( "+covertParamlsitToStr_varEntry(paramList)+" ) ,  return :" + ftype+"\n");

                    this.m_errors += "Error: "
                            + p_node.getSubtreeString()
                            +", In ( "+fname+" ) function define, can't find ( "+ fname+" ) function in  ( "+ classMethod+" ) class. \n";
                }
            } else {
//                toDo error output  没找到class
                System.out.println("Error: In ( "+fname+" ) function define, can't find class ( "+ classMethod+" )");
                System.out.println("       "+fname + "( "+covertParamlsitToStr_varEntry(paramList)+" ) ,  return :" + ftype+"\n");

                this.m_errors += "Error: "
                        + p_node.getSubtreeString()
                        +", can't find class ( "+ classMethod+" )\n";
            }

		}else {
			// free function
			fname = p_node.getChildren().get(0).getData();
			ftype = p_node.getChildren().get(2).getData();
			paranlistNode = (ParamListNode) p_node.getChildren().get(1);
			ArrayList<VarEntry> paramList = getParamList(paranlistNode);

			SymTabEntry funcEntry = globalTable.lookUpFunc(fname,"",paramList);
			if (funcEntry != null){
				System.out.println("Error: ( "+fname+" ) function has been defined." );
				System.out.println("       "+fname + "( "+covertParamlsitToStr_varEntry(paramList)+" ) ,  return :" + ftype+"\n");

				this.m_errors += "Error: free function ("
						+ fname
						+" ) has been defined \n";
			}else {

				SymTabEntry findName1 = globalTable.lookupName(fname);
				if (findName1.getClass().getSimpleName().equals("FuncEntry")){
					System.out.println("Warning: overloading function." );
					System.out.println("       "+fname + "( "+covertParamlsitToStr_varEntry(paramList)+" ) ,  return :" + ftype+"\n");

					this.m_errors += "Warning: overloading free function ("
							+ fname
							+" ) \n";
				}
				int p_level = p_node.m_symtab.getLevel()+1;
				localtable = new SymTab(p_level,fname, p_node.m_symtab);
				p_node.m_symtabentry = new FuncEntry(ftype, fname, classMethod, localtable);


				//String p_type, String p_name, String p_classMethod, String p_visibility, SymTab p_table
				p_node.m_symtab.addEntry(p_node.m_symtabentry);
				p_node.m_symtab = localtable;

				// propagate accepting the same visitor to all the children
				// this effectively achieves Depth-First AST Traversal
				for (Node child : p_node.getChildren() ) {
					child.m_symtab = p_node.m_symtab;
					child.accept(this);
				}
			}
		}
		p_node.setType(p_node.m_symtabentry.m_type);
		p_node.m_data = p_node.m_symtabentry.m_name;
		((FuncEntry)p_node.m_symtabentry).tag = p_node.m_symtabentry.m_name + getFuncID();
	}



	public void visit(AddOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
		String tempvarname = this.getNewTempVarName();
		p_node.m_moonVarName = tempvarname;
//		add可以直接默认为 float吗 toDo
//		在做type check的时候再根据左右child来update类型， 两个int 就是int 有一个是float 那就是float
//		String vartype = p_node.getType();

		String vartype = "float";
		Vector<Integer> dimlist = new Vector<Integer>();
		p_node.m_symtabentry = new VarEntry("tempvar", vartype, p_node.m_moonVarName, dimlist);
		p_node.m_symtab.addEntry(p_node.m_symtabentry);

	};

	public void visit(MultOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}		
		String tempvarname = this.getNewTempVarName();
		p_node.m_moonVarName = tempvarname;
//		multiple可以直接默认为 float吗 toDo
//		在做type check的时候再根据左右child来update类型
//		String vartype = p_node.getType();

		String vartype = "float";
		Vector<Integer> dimlist = new Vector<Integer>();
//		for (Node dim : p_node.getChildren().get(1).getChildren()){
//			// parameter dimension
//			Integer dimval = Integer.parseInt(dim.getData());
//			dimlist.add(dimval);
//		}
		p_node.m_symtabentry = new VarEntry("tempvar", vartype, p_node.m_moonVarName, dimlist);
		p_node.m_symtab.addEntry(p_node.m_symtabentry);
	};

	public void visit(IdNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
		p_node.m_moonVarName = p_node.m_data;
	};

	public void visit(NumNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal

		// root.type "'intlit'","'floatlit'","'stringlit'"
		// 用type判断是那种数据类型的字段
//		bottomNode = new NumNode(root.name);
//		bottomNode.m_nodeId = getID();
//		String type = root.type.substring(1,root.type.length()-1);
//		bottomNode.setType(type);

		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
		String tempvarname = this.getNewTempVarName();
		p_node.m_moonVarName = tempvarname;
		String vartype = p_node.getType(); // int float string
		p_node.m_symtabentry = new VarEntry("litval", vartype, p_node.m_moonVarName, new Vector<Integer>());
		p_node.m_symtab.addEntry(p_node.m_symtabentry);
	};
	
	// Below are the visit methods for node types for which this visitor does
	// not apply. They still have to propagate acceptance of the visitor to
	// their children.

	public void visit(AssignStatNode p_node) {
		// type check 的时候可以直接左右孩子，是否type一致
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(ClassListNode p_node) {
		// ClassNode list
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		//没有create ClassListNode 的table,直接接上global
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(DimListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(FuncDefListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(VarDeclListNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(Node p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(PutStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	};

	public void visit(TypeNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
	 };

	 public void visit(ParamListNode p_node) {
			// propagate accepting the same visitor to all the children
			// this effectively achieves Depth-First AST Traversal
		 for (Node child : p_node.getChildren()) {
			 child.m_symtab = p_node.m_symtab;
			 child.accept(this);
		 }
	 }

	public void visit(DimNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		 for (Node child : p_node.getChildren()) {
			 child.m_symtab = p_node.m_symtab;
			 child.accept(this);
		 }
	}; 
	
	public void visit(FuncCallNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
		for (Node child : p_node.getChildren() )
			child.accept(this);
	}; 
	
	public void visit(ReturnStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit ReturnStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(IfStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit IfStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(QmNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit QmNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(BreakStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit BreakStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(ContinueStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit ContinueStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(ReadStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit ReadStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(RelOpNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit RelOpNode");

		for (Node child : p_node.getChildren()) {
			child.accept(this);
		}
	};



	public void visit(SignNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit SignNode");
		for (Node child : p_node.getChildren()) {
			child.m_symtab = p_node.m_symtab;
			child.accept(this);
		}
//		String tempvarname = this.getNewTempVarName();
//		p_node.m_moonVarName = tempvarname;
//		String vartype = "float";
//		//		在做type check的时候再根据左右child来update类型
//		Vector<Integer> dimlist = new Vector<Integer>();
//		p_node.m_symtabentry = new VarEntry("tempvar", vartype, p_node.m_moonVarName, dimlist);
//		p_node.m_symtab.addEntry(p_node.m_symtabentry);

	};

	public void visit(WhileStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit WhileStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

	public void visit(WriteStatNode p_node) {
		// propagate accepting the same visitor to all the children
		// this effectively achieves Depth-First AST Traversal
//		System.out.println("visit WriteStatNode");
		for (Node child : p_node.getChildren() )
			child.accept(this);
	};

    public void visit(VarNode p_node){

    }

    public  void visit(AparamList p_node){

    }

    public void visit(IndiceRepNode p_node){

    }

	public void visit(DotNode p_node){

	}

}

