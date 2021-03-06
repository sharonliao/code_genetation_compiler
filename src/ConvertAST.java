import AST.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConvertAST {

    String dotFile;
    String filename;
    int id = 0;
    ArrayList<String> literal = new ArrayList<>(Arrays.asList("'intlit'","'floatlit'","'stringlit'"));


    public Node generateNewAST(ASTNode astRoot) throws IOException {

        while (astRoot.id == 0 || astRoot.type.equals("<start>")){
            astRoot = astRoot.childrenList.get(0);
        }

        System.out.println("AST root: "+ astRoot.type);

        Node progNode = generateSubTree(astRoot);

        return progNode;
    }

    public void writeDotFile(Node astTree) {

        this.dotFile = this.filename + ".outast";
        try {
            File output = new File("src/result/" + dotFile);
            if (output.createNewFile()) {
                System.out.println(dotFile + " created. ");
            } else {
                System.out.println("File already exists.");
                output.delete();
                output.createNewFile();
                System.out.println(dotFile + " deleted and created a new noe. ");
            }

            FileWriter dotWrite = new FileWriter("src/result/"+this.dotFile,true);
            dotWrite.write("digraph name {" + "\n");
            dotWrite.close();

            dotWrite = new FileWriter("src/result/"+this.dotFile,true);

            if (astTree != null){
                System.out.println("new AST root:"+astTree.getClass().getSimpleName());
                String nodeInfo = astTree.m_nodeId+"[label=\""+astTree.getClass().getSimpleName() + ","+ astTree.getData()+"\"]";
                dotWrite.write(nodeInfo + "\n");

                depthTraverse(astTree);
            }else {
                System.out.println("Error : AST is null ");
            }

            dotWrite.write("}" + "\n");
            dotWrite.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeDot(Node astNode, int parentID) throws IOException {
        String nodeInfo = astNode.m_nodeId+"[label=\""+astNode.getClass().getSimpleName() + ","+ astNode.getData()+"\"]";
        String relation = parentID + "->" + astNode.m_nodeId;
        FileWriter dotWrite = new FileWriter("src/result/"+this.dotFile,true);
        dotWrite.write(nodeInfo + "\n");
        dotWrite.write(relation + "\n");
        dotWrite.close();
    }

    void depthTraverse(Node root) throws IOException {
        if (root.getChildren().size() == 0)
            return ;
        for(Node child : root.getChildren()){
            writeDot(child,root.m_nodeId);
            depthTraverse(child);
        }
    }


    public int getID(){
//        System.out.println("Get ID: "+ id);
        return id ++;
    }


    Node generateSubTree(ASTNode root) throws IOException {


        // class list
        /**
         * class class1{
         *   float float1;
         *   int int1;
         *   }
         */


// variable declaration subtree for a float float1
// root <vardecl>
//        Node type4       = new TypeNode("float");
//        Node id4         = new IdNode("float1");
//        Node dimlist4    = new DimListNode();
//        Node vd4         = new VarDeclNode(type4, id4, dimlist4);

//        Node class1name     = new IdNode("class1");
//        Node class1         = new ClassNode(class1name, Arrays.asList(vd4,vd5));


        // var and fparam declare
//        System.out.println("root type: " + root.type);
        if(root.type.equals("'id'")){
            Node id = new IdNode(root.name);
            id.m_nodeId = getID();
            return id;

        } else if (root.type.equals("<vardecl>")|| root.type.equals("<fparam>")) {
            //<varDecl> ::= <type> 'id' <ArraySizeRept> ';'
            //<fParam> ::= <type> 'id' <ArraySizeRept>
            Node varDel = new VarDeclNode();
            varDel.m_nodeId = getID();
            for (ASTNode node : root.childrenList) {
                if (node.type.compareTo("<type>") == 0) {
                    Node type = new TypeNode(node.name);
                    type.m_nodeId = getID();
                    varDel.addChild(type);
                } else if (node.type.compareTo("'id'") == 0) {
                    Node id = new IdNode(node.name);
                    id.m_nodeId = getID();
                    varDel.addChild(id);
                }
            }
            int childrenNum = root.childrenList.size();
            if (childrenNum > 0 && root.childrenList.get(childrenNum - 1).type.equals("<arraysizerept>")) {
                ASTNode arraysize = root.childrenList.get(childrenNum - 1);
                Node dimlist = new DimListNode();
                dimlist.m_nodeId = getID();
                for (ASTNode dim : arraysize.childrenList) {
                    //<ArraySizeRept> ::= '[' <IntNum> ']' <ArraySizeRept>
                    //<IntNum> ::= 'intLit'
                    //<IntNum> ::= EPSILON
                    if (dim.childrenList.size()>0){
                        Node dimNode = new DimNode(dim.childrenList.get(0).name);
                        dimNode.m_nodeId = getID();
                        dimlist.addChild(dimNode);
                    }else {
                        Node dimNode = new DimNode("");
                        dimNode.m_nodeId = getID();
                        dimlist.addChild(dimNode);
                    }
                }
                varDel.addChild(dimlist);
            }
            return varDel;

        } else if (root.type.equals("<fparams>")){
            //<fParams> ::= <fParam> <FParamsTail>
            Node paramList = new ParamListNode();
            paramList.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node newChiild = generateSubTree(child);
                paramList.addChild(newChiild);
            }
            return paramList;

        }else if (root.type.equals("<funcdecl>") ){
            // <funcDecl> ::= 'func' 'id' '(' <fParams> ')' ':' <funcDeclTail> ';'
            //<funcDeclTail> ::= <type>
            //<funcDeclTail> ::= 'void'
            Node funcdecl = new FuncDeclareNode();
            funcdecl.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node body = generateSubTree(child);
                funcdecl.addChild(body);

            }
            return funcdecl;

        }else if(root.type.equals("<funcdecltail>")){
            Node type = null;
            if(root.childrenList.size()>0){
                type = new TypeNode(root.childrenList.get(0).name);
                type.m_nodeId = getID();
            } else {
                type = new TypeNode("void");
                type.m_nodeId = getID();
            }
            return type;

        }else if(root.type.compareTo("<classdeclbody>") == 0){
            //<classdeclbody>
            //<ClassDeclBody> ::= <visibility> <memberDecl>
            //<memberDecl> ::= <funcDecl>
            //<memberDecl> ::= <varDecl>
            Node classdeclbody = null;
            if (root.childrenList.size()>1){
                classdeclbody = generateSubTree(root.childrenList.get(root.childrenList.size()-1));
                ASTNode visitNode = root.childrenList.get(0);
                Node visit = new VisibilityNode(visitNode.name);
                visit.m_nodeId = getID();
                classdeclbody.addChild(visit);
                return classdeclbody;
            } else {
                classdeclbody = generateSubTree(root.childrenList.get(0));
                return classdeclbody;
            }

        } else if(root.type.equals("<classdeclbodylist>")){
            //<ClassDeclBodyList> ::= <ClassDeclBody> <ClassDeclBodyList>
            Node classdeclbodylist = new ClassDeclBodyList();
            classdeclbodylist.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node body = generateSubTree(child);
                classdeclbodylist.addChild(body);
            }
            return classdeclbodylist;

        }else if(root.type.equals("<inherit>")){
            // <Inherit>
            Node inheritList = new InheritListNode();
            inheritList.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node inheritNode = new InheritNode();
                inheritNode.m_nodeId = getID();
                inheritNode.setData(child.name);
                inheritList.addChild(inheritNode);
            }
            return inheritList;

        } else if(root.type.equals("<classdecl>")){
            //<classDecl> ::= 'class' 'id' <Inherit> '{' <ClassDeclBodyList> '}' ';'
            Node classdecl = new ClassNode();
            classdecl.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                if(child.type.equals("'id'")){
                    Node id = new IdNode(child.name);
                    id.m_nodeId = getID();
                    classdecl.addChild(id);
                } else if(child.type.equals("<inherit>")){
                    Node inheritList = generateSubTree(child);
                    for(Node inheritNode : inheritList.getChildren()){
                        classdecl.addChild(inheritNode);
                    }

                }else if(child.type.equals("<classdeclbodylist>")){
                    Node classdeclbodylist = generateSubTree(child);
                    for(Node classdeclbody : classdeclbodylist.getChildren()){
                        classdecl.addChild(classdeclbody);
                    }
                }
            }
            return classdecl;

        }else if(root.type.equals("<classdecllist>")){
            //<prog> ::= <classDeclList> <FuncDef> 'main' <funcBody>
            //<classDeclList> ::= <classDecl> <classDeclList>
            Node classdecllist = new ClassListNode();
            classdecllist.m_nodeId = getID();
            for(ASTNode child : root.childrenList ){
                Node classdecl = generateSubTree(child);
                classdecllist.addChild(classdecl);
            }
            return classdecllist;

        }else if(root.type.equals("<funchead>")){
            // function define
            //<Function> ::= <funcHead> <funcBody>
            //<funcHead> ::= 'func' 'id' <ClassMethod> '(' <fParams> ')' ':' <funcDeclTail>
            //<ClassMethod> ::= 'sr' 'id'
            //<ClassMethod> ::= EPSILON
            Node funchead = new FuncHeadNode();
            funchead.m_nodeId = getID();
            if(root.childrenList.size()>1 && root.childrenList.get(1).type.equals("<classmethod>")){
                for(ASTNode child : root.childrenList){
                    if(child.type.equals("'id'")){
                        Node classmethod = new ClassMethodNode(child.name);
                        classmethod.m_nodeId = getID();
                        funchead.addChild(classmethod);
                    }else if(child.type.equals("<classmethod>")){
                        Node id = new IdNode(child.childrenList.get(0).name);
                        id.m_nodeId = getID();
                        funchead.addChild(id);
                    }else if(child.type.equals("<fparams>")){
                        Node fparams = generateSubTree(child);
                        funchead.addChild(fparams);
                    }else if(child.type.equals("<funcdecltail>")){
                        Node type = generateSubTree(child);
                        funchead.addChild(type);
                    }
                }
            }else {
                for(ASTNode child : root.childrenList){
                    if(child.type.equals("'id'")){
                        Node id = new IdNode(child.name);
                        id.m_nodeId = getID();
                        funchead.addChild(id);
                    }else if(child.type.equals("<fparams>")){
                        Node fparams = generateSubTree(child);
                        funchead.addChild(fparams);
                    }else if(child.type.equals("<funcdecltail>")){
                        Node type = generateSubTree(child);
                        funchead.addChild(type);
                    }
                }
            }
            return funchead;

        }else if(root.type.equals("<vardeclrep>")){
            //<VarDeclRep> ::= <varDecl> <VarDeclRep>
            Node vardeclList = new VarDeclListNode();
            vardeclList.m_nodeId = getID();
            for (ASTNode child : root.childrenList){
                Node vardecl = generateSubTree(child);
                vardeclList.addChild(vardecl);
            }
            return vardeclList;

        }else if(root.type.equals("<statement>")){
            //<StatementList> ::= <statement> <StatementList>
            Node statement = generateStatSubtree(root);
            statement.m_nodeId = getID();
            return statement;

        }else if(root.type.equals("<statementlist>")){
            //<StatementList> ::= <statement> <StatementList>
            Node stateList = new StatBlockNode();
            stateList.m_nodeId = getID();
            for (ASTNode child : root.childrenList){
                Node statement = generateSubTree(child);
                stateList.addChild(statement);
            }
            return stateList;

        }else if(root.type.equals("<funcbody>")){
            //<funcBody> ::= '{' <vardeclrep> <statementlist> '}'
//            Node funcbody = new FuncBodyNode();
            Node programBlockNode = new ProgramBlockNode();
            programBlockNode.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node node = generateSubTree(child);
                programBlockNode.addChild(node);
            }
            return programBlockNode;

        } else if(root.type.equals("<function>")){
            //<Function> ::= <funcHead> <funcBody>
            Node function = new FuncDefNode();
            function.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node node = generateSubTree(child);
                //??????funcHead ??? funcBody
                for(Node newchild : node.getChildren()){
                    function.addChild(newchild);
                }
            }
            return function;

        }else if(root.type.equals("<funcdef>")){
            //<prog> ::= <classDeclList> <FuncDef> 'main' <funcBody>
            //<FuncDef> ::= <Function> <FuncDef>
            Node funcdefList = new FuncDefListNode();
            funcdefList.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node node = generateSubTree(child);
                funcdefList.addChild(node);
            }
            return funcdefList;
        }else if(root.type.equals("<prog>")){
            //<prog> ::= <classDeclList> <FuncDef> 'main' <funcBody>
            Node prog = new ProgNode();
            prog.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node node = generateSubTree(child);
                prog.addChild(node);
            }
            return prog;
        }else {
            System.out.println(" error branch : "+ root.type);
            Node rootNode = new ProgNode();
            return rootNode;
        }
    }


    public Node generateStatSubtree(ASTNode root) throws IOException {
        //<statement> ::= <FuncOrAssignStat> ';'
        //<statement> ::= 'if' '(' <expr> ')' 'then' <statBlock> 'else' <statBlock> ';'
        //<statement> ::= 'while' '(' <expr> ')' <statBlock> ';'
        //<statement> ::= 'read' '(' <variable> ')' ';'
        //<statement> ::= 'write' '(' <expr> ')' ';'
        //<statement> ::= 'return' '(' <expr> ')' ';'
        //<statement> ::= 'break' ';'
        //<statement> ::= 'continue' ';'
        Node newRoot = null;

        if(root.name.equals("'if'")){
            newRoot = new IfStatNode();
            newRoot.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node newNode = getBottomNode(child);
                newRoot.addChild(newNode);
            }

        }else if(root.name.equals("'while'")){
            newRoot = new WhileStatNode();
            newRoot.m_nodeId = getID();
            for(ASTNode child : root.childrenList){
                Node newNode = getBottomNode(child);
                newRoot.addChild(newNode);
            }

        }else if(root.name.equals("'read'")){
            newRoot = new ReadStatNode();
            newRoot.m_nodeId = getID();
            Node child = getBottomNode(root.childrenList.get(0));
            newRoot.addChild(child);

        }else if(root.name.equals("'write'")){
            newRoot = new WriteStatNode();
            newRoot.m_nodeId = getID();
            Node child = getBottomNode(root.childrenList.get(0));
            newRoot.addChild(child);

        }else if(root.name.equals("'return'")){
            newRoot = new ReturnStatNode();
            newRoot.m_nodeId = getID();
            Node child = getBottomNode(root.childrenList.get(0));
            newRoot.addChild(child);

        }else if(root.name.equals("'break'")){
            newRoot = new BreakStatNode();
            newRoot.m_nodeId = getID();

        }else if(root.name.equals("'continue'")){
            newRoot = new ContinueStatNode();
            newRoot.m_nodeId = getID();

        }else if(root.childrenList.size()==1 && root.childrenList.get(0).type.equals("<assignop>")){
            newRoot = new AssignStatNode();
            newRoot.m_nodeId = getID();
            root = root.childrenList.get(0);
            for(ASTNode child : root.childrenList){
                Node newNode = getBottomNode(child);
                newRoot.addChild(newNode);
            }

        }else if(root.childrenList.size()==1 && root.childrenList.get(0).type.equals("<funcorassignstat>")){
            newRoot = new FuncCallNode();
            newRoot.m_nodeId = getID();
            root = root.childrenList.get(0);
            for(ASTNode child : root.childrenList){
                Node newNode = getBottomNode(child);
                newRoot.addChild(newNode);
            }

        }else {
            newRoot = new AssignStatNode();
            System.out.println("error statement:"+root.type);
        }
        return newRoot;
    }

    public Node getBottomNode(ASTNode root) throws IOException {
        Node bottomNode = null;
        if(root.childrenList.size()==0) {
            if (root.type.compareTo("'id'") == 0) {
                bottomNode = generateSubTree(root);
            }else if(root.type.compareTo("<aparams>") == 0){
                bottomNode = new AparamList();
                bottomNode.m_nodeId = getID();
            }else if(literal.contains(root.type)){
                //"'intlit'","'floatlit'","'stringlit'"
                // ???type????????????????????????????????????
                bottomNode = new NumNode(root.name);
                bottomNode.m_nodeId = getID();
                String type = "";
                if(root.type.equals("'intlit'")){
                    type = "integer";
                }else if (root.type.equals("'floatlit'")){
                    type = "float";
                }else {
                    type = "string";
                }
                bottomNode.setType(type);

            }
        }else if(root.childrenList.size() == 1){
            //??????????????????child??????????????????root
            if(root.type.equals("<indicerep>")){
                bottomNode = new IndiceRepNode();
                bottomNode.m_nodeId = getID();
                Node indice = getBottomNode(root.childrenList.get(0));
                bottomNode.addChild(indice);

            }else if(root.type.equals("<aparams>")){
                bottomNode = new AparamList();
                bottomNode.m_nodeId = getID();
                Node aparam = getBottomNode(root.childrenList.get(0));
                bottomNode.addChild(aparam);

            }else if(root.type.equals("<sign>")){
                bottomNode = new SignNode(root.name.substring(1,root.name.length()-1));
                bottomNode.m_nodeId = getID();
                Node signData = getBottomNode(root.childrenList.get(0));
                bottomNode.addChild(signData);

            }else if(root.type.equals("<not>")){
                bottomNode = new NotNode();
                bottomNode.m_nodeId = getID();
                Node notData = getBottomNode(root.childrenList.get(0));
                bottomNode.addChild(notData);

            }else if(root.type.equals("<statblock>") && root.childrenList.get(0).type.equals("<statement>") ){
                bottomNode = new StatBlockNode();
                bottomNode.m_nodeId = getID();
                Node statblock = generateSubTree(root.childrenList.get(0));
                bottomNode.addChild(statblock);

            }else if(root.type.equals("<statblock>") && root.childrenList.get(0).type.equals("<statementlist>")){
                bottomNode = new StatBlockNode();
                bottomNode.m_nodeId = getID();
                bottomNode = generateSubTree(root.childrenList.get(0));
            }else {
                bottomNode = getBottomNode(root.childrenList.get(0));
            }
        }else if(root.childrenList.size() > 1){
            if(root.type.equals("<funcorvar>") && isFunc(root)){
                //function
                bottomNode = new FuncCallNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else if(root.type.equals("<funcorvar>") && !isFunc(root)){
                //var
                bottomNode = new VarNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }

            }else if(root.type.equals("<aparams>")){
                bottomNode = new AparamList();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else if(root.type.equals("<addop>")){
                //???m_data???????????????
                bottomNode = new AddOpNode(root.name);
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else if(root.type.equals("<multop>")){
                //expr
                bottomNode = new MultOpNode(root.name);
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else if(root.type.equals("<relop>")){
                //expr
                bottomNode = new RelOpNode(root.name);
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }

            }else if(root.type.equals("<qm>")){
                //expr
                bottomNode = new QmNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }

            }else if(root.type.equals("<indicerep>")){
                bottomNode = new IndiceRepNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }

            }else if(root.type.equals("<var>") ){
                //??????var????????????function call
                // ?????????assign???????????????function call
                // ???op??????????????????function call, ?????????function call??????????????????callNode?????????????????????
                bottomNode = new VarNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else if(root.type.equals("<variable>") ){
                //variable ?????????function call
                //<statement> ::= 'read' '(' <variable> ')' ';'
                bottomNode = new VarNode();
                bottomNode.m_nodeId = getID();
                for(ASTNode child : root.childrenList){
                    Node newNode = getBottomNode(child);
                    bottomNode.addChild(newNode);
                }
            }else {
                System.out.println("bottom Node error:"+root.type);
                bottomNode = new MultOpNode("*");
            }
        }
        return bottomNode;
    }

    public Boolean isFunc(ASTNode root){
        Boolean result = false;
        for(ASTNode child : root.childrenList){
            if(child.type.equals("<aparams>")){
                result = true;
                break;
            }
        }
        return result;
    }



}
