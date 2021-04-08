import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;



public class Parser_syntactic {
    String filename;
    String outFile;
    String errorFile;
    String dotFile;
    int id = 0;

    public HashMap<String,Production> productionMap = new HashMap<>();
    public ArrayList<String> terminals = new ArrayList<>();
    public ArrayList<String> productions;
    public HashMap<String,String> makeListPrdctMap = new HashMap<>(){{
        put("<inherit>","<nestedid>");
        put("<fparams>","<fparamstail>");
        put("<aparams>","<aparamstail>");
    }};
    public ArrayList<String> makeFamilyProductions = new ArrayList<>(Arrays.asList("<arithexprtail>","<termtail>"));
    public ArrayList<String> terminalCreateNnode =
            new ArrayList<>(Arrays.asList("'integer'","'float'","'string'","'id'","'intlit'","'floatlit'","'stringlit'",
                    "'eq'","'neq'","'lt'","'gt'","'leq'","'geq'","'+'","'-'","'or'","'*'","'/'","'='","'and'","'public'","'private'","'void'"));
    public ArrayList<String> updateRootName = new ArrayList<>(Arrays.asList("'if'","'while'","'read'","'write'","'return'","'break'","'continue'"));
    public ArrayList<String> needValueNode = new ArrayList<>(Arrays.asList("'id'","'intlit'","'floatlit'","'stringlit'"));
    public ArrayList<String> operatorNode =  new ArrayList<>(Arrays.asList("<assignop>","<addop>","<relop>","<multop>"));
    public ArrayList<String> typeNode = new ArrayList<>(Arrays.asList("'integer'","'float'","'string'","'id'"));
    public ArrayList<String> visiableNode = new ArrayList<>(Arrays.asList("'public'","'private'"));
    public ArrayList<String> othersNode = new ArrayList<>(Arrays.asList("'void'"));
//    public ArrayList<String> canRemoveNode = new ArrayList<>(Arrays.asList("<funcorvar>","<factor>","<term>","<arithexpr>"));
    public ArrayList<String> canRemoveNode = new ArrayList<>(Arrays.asList("<memberdecl>","<methodbodyvar>"));

    public ArrayList<String> makeBlankNode = new ArrayList<>(Arrays.asList("<aparams>","<fparam>","<intnum>"));
    public ArrayList<String> flatFuncOrStatmList = new ArrayList<>(Arrays.asList("<funcstattail>","<funcstattailidnest>","<funcorassignstatidnestfunctail>","<funcorassignstatidnestvartail>","<funcorassignstatidnest>","<funcorassignstatidnest>","<funcorassignstat>"));
    public ArrayList<String> flatFuncVarList = new ArrayList<>(Arrays.asList("<funcorvar>","<funcorvaridnest>","<funcorvaridnesttail>","<variable>","<variableidnest>","<variableidnesttail>"));
    public ArrayList<String> signList = new ArrayList<>(Arrays.asList("'-'","'+'"));


    public HashMap<String,String> makeOperatorFamilyPrdctMap = new HashMap<>(){{
        put("<funcorassignstat>","<assignstattail>"); // =
//        put("<arithexpr>","<arithexprtail>"); // <addOp>
//        put("<term>","<termtail>"); //<multOp>
        put("<expr>","<exprtail>"); //<relOp>
    }};
    public int tokenIndex = -1;
    public ArrayList<Token> tokenStream;
    public Token lookahead;

    public int getID(){
        System.out.println("Get ID: "+ id);
        return id ++;
    }
    public boolean isNonterminal(String rhs_name) {
//        System.out.println(rhs_name.substring(0,1));
//        System.out.println(rhs_name.substring(0,1).compareTo("<") == 0);
        return (rhs_name.length() > 0 && rhs_name.substring(0,1).compareTo("<") == 0) ? true : false;
    }


    public ASTNode parse(String srcName) throws IOException {

        ASTNode ASTroot = null;
        initialOutput(srcName);
        boolean result = false;
        // Get token stream from lexer Analyzer
        LexicalAnalyzer lexerAnalyzer = new LexicalAnalyzer();
        tokenStream = lexerAnalyzer.tokenizeFile(srcName);
        tokenIndex = -1;

        if (hasNextToken()){
            lookahead = nextToken();
            // start to parsing
            Production start = productionMap.get("<start>");
            ASTNode startNode = new ASTNode("start",getID());
            // checkValid() is a recursive function
            // startNode is the root of the whole AST
            Pair<Boolean, ASTNode> parseResult = checkValid(start,startNode);
            Boolean isValid = parseResult.isValid;
            ASTroot = parseResult.rootLHS;

            if(isValid && match("\'$\'")) {
                System.out.println("Parsing success");
                result = true;
            }else {
                System.out.println("Parsing error");
            }
        }
        return ASTroot;
    }

    public boolean hasNextToken(){
        return (tokenIndex < tokenStream.size()-1) ? true : false;
    }
    public Token nextToken(){
        Token token;
        if(hasNextToken()){
            tokenIndex += 1;
            token = tokenStream.get(tokenIndex);
            while (token.tokenType.compareTo("blockcmt") == 0 || token.tokenType.compareTo("inlinecmt")==0){
                if(hasNextToken()){
                    token = nextToken();
                }else {
                    token = new Token();
                    token.tokenType = "$";
                    ++ tokenIndex;
                    break;
                }
            }
        }else {
            token = new Token();
            token.tokenType = "$";
            ++ tokenIndex;
        }

        lookahead = token;
        System.out.println("get next Token : " + token.toString());
        return token;
    }


    public Token getPreToken(int currentIndex){
        Token token;
        if(currentIndex>0 && currentIndex < tokenStream.size()){
            currentIndex -= 1;
            token = tokenStream.get(currentIndex);
            while (token.tokenType.compareTo("blockcmt") == 0 || token.tokenType.compareTo("inlinecmt")==0){
                if(currentIndex>0){
                    token = getPreToken(currentIndex);
                }else {
                    token = new Token();
                    token.tokenType = "start";
                    token.position = 0;
                    break;
                }
            }
        }else if(currentIndex > tokenStream.size()-1){
            token = new Token();
            token.tokenType = "$";
            token.position = tokenStream.size();
        }else {
            token = new Token();
            token.tokenType = "start";
            token.position = 0;
        }
        return token;
    }



    public boolean match(String element) throws IOException {
//        match terminal
        System.out.println("lookahead.tokenType:"+peakToken());
        System.out.println("match element:"+element);

        if (peakToken().compareTo(element) == 0){
            lookahead = nextToken();
            return true;
        } else {
            String errorStr = "matching error around : expect "+ element+ ", not "+ errorTokenInfo(lookahead);
            System.out.println(errorStr);
            writeError(errorStr);
            // advance, missing token
            lookahead = nextToken();
            return false;
        }
    }

    public boolean skipErrors(Production production) throws IOException {
        boolean result = true;
        boolean checkFirstSet = production.mergeFirstSet().contains(peakToken());
        boolean checkFollowSet = checkIfInFLS(production,lookahead);
        if(checkFirstSet || checkFollowSet){
            // no error detected, parse continues in this parsing function
            result =  true;
        }else {
            String errorStr = "syntax error around :"+ errorTokenInfo(getPreToken(tokenIndex)) + ".   " +errorTokenInfo(lookahead);

            writeError(errorStr);

            while (!(checkFirstSet || production.followSet.contains(peakToken())) && lookahead.tokenType.compareTo("$") != 0) {
                lookahead = nextToken();
                if (checkIfInFLS(production,lookahead)){
                    result =  false;
                    break;    // error detected and parsing function should be aborted
                } else
                    continue; // error detected and parse continues in this parsing function
            }
            //return true, will continue parsing the current production
            //return false, will skip the current production
            System.out.println(errorStr);
        }
        return result;
    }

    public String errorTokenInfo(Token token){
        String info = "Line:"+token.position + " ,type:"+ token.tokenType + " ,lexeme:" + token.lexeme;
        return info;
    }

    public boolean checkIfInFLS(Production production,Token token){
        boolean checkFollowSet = production.mergeFirstSet().contains("epsilon") && production.followSet.contains("\'"+token.tokenType+"\'");
        return checkFollowSet;
    }

    private void initialOutput(String filename) {
        this.filename = filename;
        this.outFile = this.filename + ".outderivation";
        this.errorFile = this.filename + ".outsyntaxerrors";
        try {
            File output = new File("src/result/"+this.outFile);
            if (output.createNewFile()) {
                System.out.println(outFile+" created. ");
            } else {
                System.out.println("File already exists.");
                output.delete();
                output.createNewFile();
                System.out.println(outFile+" deleted and created a new noe. ");
            }

            File error = new File("src/result/"+this.errorFile);
            if (error.createNewFile()) {
                System.out.println(errorFile+" created. ");
            } else {
                System.out.println("File already exists.");
                error.delete();
                error.createNewFile();
                System.out.println(errorFile+" deleted and created a new noe. ");
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


    }

    public void writeResult(String derivation) throws IOException {
        FileWriter outputWrite = new FileWriter("src/result/"+this.outFile,true);
        outputWrite.write(derivation);
        outputWrite.close();

    }

    public void writeError(String error) throws IOException {
        FileWriter errorWrite = new FileWriter("src/result/"+this.errorFile,true);
        errorWrite.write(error + "\n");
        errorWrite.close();
    }

    public String peakToken(){
        if (tokenIndex < tokenStream.size()){
            Token curToken = tokenStream.get(tokenIndex);
            return "\'" + curToken.tokenType + "\'";
        } else {
            String tokenType = "$";
            return "\'" + tokenType + "\'";
        }
    }

    public boolean isRecursiveProduction(Production production, int rhsRow){
        System.out.println("isRecursiveProduction:" + production.LHS + "- rhs: "+ rhsRow);
        boolean result = false;
        String lhs = production.LHS;
        String rhs[] = production.RHS.get(rhsRow);
        if(makeFamilyProductions.contains(production.LHS)){
            result = false;
        } else if (lhs.compareTo(rhs[rhs.length-1]) == 0)
            result = true;
        System.out.println("result:" + result);
        return result;
    }



    public boolean ifMakeList(Production production, int rhsRow, ASTNode inheritsNode, ASTNode curNode){

        String inherType = inheritsNode.type;
        String curType = curNode.type;

        if (isRecursiveProduction(production, rhsRow) && inheritsNode.type.compareTo(curNode.type) == 0) {
//            System.out.println("111111111");
            return true;
        }else if(
                makeListPrdctMap.get(inherType) != null &&
                makeListPrdctMap.get(inherType).compareTo(curType) == 0){
//            System.out.println("22222222");
            return true;
        } else {
//            System.out.println("33333333");
            return false;
        }
    }

    public boolean ifRecursiveOperator(Production production, int rhsRow, ASTNode inheritsNode, ASTNode curNode){
        String inherType = inheritsNode.type;
        String curType = curNode.type;

//        if(makeFamilyProductions.contains(inherType) && inherType.equals(curType)){
        if(makeFamilyProductions.contains(curType)){
            //"<arithexprtail>","<termtail>"
            return true;
        }else {
            return false;
        }
    }


    public ASTNode mergeNode(Production production, int rhsRow, ASTNode inheritsNode, ASTNode curNode){
        ASTNode root ;
        if(curNode.name.compareTo("null") == 0){
            // current rhs is epsilon, return inheritsNode
            root = inheritsNode;
        }else if(ifMakeList(production,rhsRow,inheritsNode, curNode)){
            //MakeList，recursive rhs
            inheritsNode.childrenList.addAll(curNode.childrenList);
            root = inheritsNode;

        } else if (ifRecursiveOperator(production,rhsRow,inheritsNode, curNode)){
            //"<arithexprtail>","<termtail>"

            //MakeList, recursive operator addOp, mulOp
            //<expr> ::= <arithExpr> <exprTail>
            //<exprTail> ::= <relOp> <arithExpr>
            //<expr> ::= <arithExpr> <exprTail>
            //<exprTail> ::= <relOp> <arithExpr>
            //<exprTail> ::= EPSILON
            //<arithExpr> ::= <term> <arithExprTail>
            //<arithExprTail>::= <addOp> <term> <arithExprTail>
            //<arithExprTail>::= EPSILON

            //<term> ::= <factor> <termTail>
            //<termTail> ::= EPSILON
            //<termTail> ::= <multOp> <factor> <termTail>


//            ASTNode var1 =  inheritsNode.childrenList.get(inheritsNode.childrenList.size()-1);
//            inheritsNode.childrenList.remove(inheritsNode.childrenList.size()-1);
//            ASTNode var2 =  curNode.childrenList.get(1);
//
//            ASTNode opNode = curNode.childrenList.get(0);
//            opNode.childrenList.add(var1);
//            opNode.childrenList.add(var2);
//
//            inheritsNode.childrenList.add(opNode);
//            root = inheritsNode;

            //找到cur node 的最右边孩子的最左边的叶子节点
            //和inherit 最右child make famliy
            // root = curent node
            // 初始情况是 <arithExprTail>，最右边孩子就是叶子节点
            //return addOp,

            if(curNode.type.equals("<arithexprtail>") ){
                if(!curNode.childrenList.get(1).type.equals("<addop>")){
                    ASTNode var1 =  inheritsNode.childrenList.get(inheritsNode.childrenList.size()-1);
                    inheritsNode.childrenList.remove(inheritsNode.childrenList.size()-1);
                    ASTNode var2 =  curNode.childrenList.get(1);

                    ASTNode opNode = curNode.childrenList.get(0);
                    opNode.childrenList.add(var1);
                    opNode.childrenList.add(var2);

                    inheritsNode.childrenList.add(opNode);
                }else {
                    //找到current的最右child
                    ASTNode child = curNode.childrenList.get(1);
                    ASTNode parent = null;
                    while (true){
                        if(child.type.equals("<addop>")){
                            parent = child;
                            child = child.childrenList.get(0);
                        }else {
                            break;
                        }
                    }
                    ASTNode var1 =  inheritsNode.childrenList.get(inheritsNode.childrenList.size()-1);
                    inheritsNode.childrenList.remove(inheritsNode.childrenList.size()-1);
                    ASTNode var2 =  child;
                    ASTNode opNode = curNode.childrenList.get(0);
                    opNode.childrenList.add(var1);
                    opNode.childrenList.add(var2);
                    parent.childrenList.set(0,opNode);
                    inheritsNode.childrenList.add(curNode.childrenList.get(1));
                }
            }else {
                if(!curNode.childrenList.get(1).type.equals("<multop>")){

                    ASTNode var1 =  inheritsNode.childrenList.get(inheritsNode.childrenList.size()-1);
                    inheritsNode.childrenList.remove(inheritsNode.childrenList.size()-1);
                    ASTNode var2 =  curNode.childrenList.get(1);

                    ASTNode opNode = curNode.childrenList.get(0);
                    opNode.childrenList.add(var1);
                    opNode.childrenList.add(var2);

                    inheritsNode.childrenList.add(opNode);
                }else {
                    //找到current的最右child
                    ASTNode child = curNode.childrenList.get(1);
                    ASTNode parent = null;
                    while (true){
                        if(child.type.equals("<multop>")){
                            parent = child;
                            child = child.childrenList.get(0);
                        }else {
                            break;
                        }
                    }
                    ASTNode var1 =  inheritsNode.childrenList.get(inheritsNode.childrenList.size()-1);
                    inheritsNode.childrenList.remove(inheritsNode.childrenList.size()-1);
                    ASTNode var2 =  child;
                    ASTNode opNode = curNode.childrenList.get(0);
                    opNode.childrenList.add(var1);
                    opNode.childrenList.add(var2);
                    parent.childrenList.set(0,opNode);
                    inheritsNode.childrenList.add(curNode.childrenList.get(1));
                }
            }
            root = inheritsNode;

        } else if(flatFuncOrStatmList.contains(inheritsNode.type) && flatFuncOrStatmList.contains(curNode.type)){
            // flat function statement or assign statement element
            inheritsNode.childrenList.addAll(curNode.childrenList);
            root = inheritsNode;

        }else if(flatFuncVarList.contains(inheritsNode.type) && flatFuncVarList.contains(curNode.type)){
            // flat function or var element
            inheritsNode.childrenList.addAll(curNode.childrenList);
            root = inheritsNode;

        } else if(curNode.type.equals("<sign>") && curNode.childrenList.size() ==0){
            inheritsNode.type = "<sign>";
            inheritsNode.name = curNode.name;
            root = inheritsNode;
        }
        else {
            //merge
            inheritsNode.addChild(curNode);
            root = inheritsNode;
        }


        //Operator make Family
        if(makeOperatorFamilyPrdctMap.get(root.type) != null &&
                makeOperatorFamilyPrdctMap.get(root.type).equals(root.childrenList.get(root.childrenList.size()-1).type)){
            // make an assign statement family
            ASTNode newParent = root.childrenList.get(root.childrenList.size()-1).childrenList.get(0);
            root.childrenList.get(root.childrenList.size()-1).childrenList.remove(0);
            ArrayList<ASTNode> newChildren = new ArrayList<>();
            ArrayList<ASTNode> var2 = root.childrenList.get(root.childrenList.size()-1).childrenList;
            root.childrenList.remove(root.childrenList.size()-1);
            root.type = "<var>";
            newChildren.add(root);
            newChildren.addAll(var2);
            newParent.childrenList = newChildren;
            root = newParent;
        }

        //shorter path
        if(root.childrenList.size()==1){
            ASTNode child = root.childrenList.get(0);
            if(isNonterminal(child.type) && canRemoveNode.contains(root.type)){
//                child.type = root.type;
                root = child;
            }
        }
        return root;
    }




    public boolean ifCreatNode(String terminal){
        return terminalCreateNnode.contains(terminal);
    }

    public ASTNode creatNode(String terminal){
        Token token = tokenStream.get(tokenIndex-1);
        ASTNode newNode = new ASTNode(terminal,getID());
        if (needValueNode.contains(terminal))
            newNode.value = token.lexeme;
        return newNode;
    }




    public Pair<Boolean, ASTNode> checkValid(Production production, ASTNode inheritsNode) throws IOException {

        System.out.println("\n"+production.printProduction());
        System.out.println("lookahead: "+lookahead.tokenType);
        System.out.println("firstset: "+production.printFirstSet());
        System.out.println("followset: "+production.printFollowSet());

        Boolean isValid = true;
        ASTNode rootLHS = new ASTNode(production.LHS,getID());
        Pair<Boolean, ASTNode> result = new Pair<>(isValid, rootLHS);


        String lh = "\'" + lookahead.tokenType + "\'";

        System.out.println("1.lh: "+lh);
        // error detecting and error recovery
        if (!skipErrors(production)) {
            result.isValid = true;
            return result;
        }

        if(production.mergeFirstSet().contains(lh)){
            //  check one rhs
            System.out.println("1.lookahead in mergeFirstSet");
            int rhsRow = 0;
            for(int i = 0; i < production.RHS.size(); i++){
                rhsRow = i;
                result.isValid = true;
                System.out.println("1.1 check "+i+" RHS: "+convertStringLits(production.RHS.get(i)));
                if(production.firstSet.get(i).contains(lh)){

                    String[] rhs = production.RHS.get(i);
                    // get correct rhs, generate a empty ast list
                    // match terminal
                    for(int j = 0; j < rhs.length; j++){
                        System.out.println("1.1.1 match element: "+ rhs[j]);
                        System.out.println("1.1.2 is Nonterminal: "+ isNonterminal(rhs[j]));
                        System.out.println("1.1.3 match curtoken: "+ peakToken());

                        if (rhs[j] == "epsilon" && production.followSet.contains(peakToken())) {
                            result.rootLHS.name = "null";
                            System.out.println("**** 1.1.4 derived production: "+production.LHS + " -> " + convertStringLits(rhs));
                        } else if(!isNonterminal(rhs[j]) && !match(rhs[j])){
                            result.isValid = false;
                        }else if (isNonterminal(rhs[j])) {
                            // recursively checking nonterminal item
                            Production subProduction = productionMap.get(rhs[j]);
                            Pair<Boolean, ASTNode> subResult = checkValid(subProduction,rootLHS);
                            if (!subResult.isValid){
                                result.isValid = false;
                            }else {
                                // update LHS root, because the checking result of sub production have integrate the pre root.
                                // result, newRoot = checkValid(subProduction,rootLHS)
                                rootLHS = subResult.rootLHS;
                            }
                        }else {
                            //terminal and matching
                            System.out.println("1.1.4 match terminal: "+ rhs[j]);
                            //Create leaf node id，connect to the rootLHS
                            //Because terminal node will be the only child of the rootLHS, so just update rootLHS info
                            //Set this lhs root name, if terminal is if, while, +,-...
                            if(ifCreatNode(rhs[j])){
                                if(operatorNode.contains(production.LHS) || visiableNode.contains(rhs[j]) || othersNode.contains(rhs[j])){
                                    //operator, visiable
                                    result.rootLHS.name = rhs[j].substring(1,rhs[j].length()-1);
                                }else if (typeNode.contains(rhs[j]) && production.LHS.compareTo("<type>") == 0){
                                    //"'integer'","'float'","'string'","'id'"
                                    if (rhs[j].compareTo("'id'")==0){
                                        result.rootLHS.name = getPreToken(tokenIndex).lexeme;
                                    } else
                                        result.rootLHS.name = rhs[j].substring(1,rhs[j].length()-1);
                                } else if (needValueNode.contains(rhs[j])){
                                    // intlit, floatlit, stringlit ,not,sight
                                    ASTNode node= creatNode(rhs[j]);
                                    node.name = getPreToken(tokenIndex).lexeme;
                                    result.rootLHS.addChild(node);
                                } else if(production.LHS.compareTo("<sign>") == 0 && signList.contains(rhs[j])){
                                    result.rootLHS.name = rhs[j];
                                }
                            }else if(updateRootName.contains(rhs[j])){
                                //"'if'","'while'","'read'","'write'","'return'","'break'","'continue'"
                                result.rootLHS.name = rhs[j];
                            }else if(rhs[j].compareTo("'qm'")==0){
                                result.rootLHS.type = "<qm>";
                                //result.rootLHS.name = rhs[j];
                            }else if(rhs[j].compareTo("'not'")==0){
                                result.rootLHS.type = "<not>";
                                //result.rootLHS.name = rhs[j];
                            }
                        }
                    }
                    if (result.isValid == true){

                        result.rootLHS = mergeNode(production,rhsRow,inheritsNode,rootLHS);

                        writeResult(production.LHS + " -> " + convertStringLits(rhs) + "\n");
                        System.out.println("**** 1.2 derived production: "+production.LHS + " -> " + convertStringLits(rhs));
                        break;
                    }
                }
            }
        }else if(production.followSet.contains(lh)){
            System.out.println("2.lookahead in followSet");
            result.isValid = true;
            //param,dim
            if(makeBlankNode.contains(production.LHS)){
                result.rootLHS.name = "None";
                inheritsNode.childrenList.add(result.rootLHS);
            }
            result.rootLHS = inheritsNode;
            writeResult(production.LHS+ " -> epsilon\n");
            System.out.println("**** derived production: "+ production.LHS+ " -> epsilon");
        } else {
            result.isValid = false;
        }

//        if(isValid == false){
////            skip error token
//            writeError(production.printProduction(),lookahead);
//            System.out.println("**** error in production: "+ production.printProduction());
//        }

        return result;
    }


    public String convertStringLits(String[] strs){
        String str = "";
        for (String substr : strs){
            str += substr+" ";
        }
        return str;
    }

    public String convertRHS(String[] strs){
        String str = "";
        for (String substr : strs){
            if (!isNonterminal(substr)){
                substr = substr.substring(1,substr.length()-1);
                substr = "<" + substr +">";
            }
            str += substr+" ";
        }
        return str;
    }

    public void checkgrmValid(){
        System.out.println("\ncheck if firstSet and followSet have common elements ");

        for (Production production : productionMap.values()){
            production.checkgrm();
        }

        System.out.println("\ncheck Ambious");

        for (Production production : productionMap.values()){
            production.checkAmbious();
        }

    }


    public void loadgrammar() throws FileNotFoundException {
        LoadProdcution loadProdcution = new LoadProdcution();
        loadProdcution.biuldGrm();
        this.productionMap = loadProdcution.productionMap;
        this.terminals = loadProdcution.terminals;
        this.productions = loadProdcution.productions;
    }


    public void writeDotFile(ASTNode astTree) {

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

    public void writeDot(ASTNode astNode, int parentID) throws IOException {
        String nodeInfo = astNode.id+"[label=\""+astNode.type + ","+ astNode.name+"\"]";
        String relation = parentID + "->" + astNode.id;
        FileWriter dotWrite = new FileWriter("src/result/"+this.dotFile,true);
        dotWrite.write(nodeInfo + "\n");
        dotWrite.write(relation + "\n");
        dotWrite.close();
    }

    void depthTraverse(ASTNode root) throws IOException {
        if (root.childrenList.size() == 0)
            return ;
        for(ASTNode child : root.childrenList){
            writeDot(child,root.id);
            depthTraverse(child);
        }
    }





    public static void main(String[] args) throws IOException {
        Parser_syntactic parsersyntactic = new Parser_syntactic();
        parsersyntactic.loadgrammar();
        parsersyntactic.checkgrmValid();
//        ASTNode astTree =  parsersyntactic.parse("bubblesort");
//        ASTNode astTree =  parsersyntactic.parse("polynomial");
        ASTNode astTree =  parsersyntactic.parse("myTest");
        parsersyntactic.writeDotFile(astTree);

    }
}

class Pair<T, U> {
    public  T isValid;
    public  U rootLHS;

    public Pair(T isValid, U rootLHS) {
        this.isValid= isValid;
        this.rootLHS= rootLHS;
    }
}






