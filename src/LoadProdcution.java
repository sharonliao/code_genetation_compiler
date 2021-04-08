import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LoadProdcution {

    String filename;
    String outFile;
    String errorFile;
    String graphFile;

    public ArrayList<ArrayList<String>> TT = new ArrayList<ArrayList<String>>();
    public HashMap<String,Production> productionMap = new HashMap<>();
    public ArrayList<String> terminals = new ArrayList<>();
    public ArrayList<String> productions;

    public int tokenIndex = -1;
    public ArrayList<Token> tokenStream;
    public Token lookahead;
    public ArrayList<String> LHFamily = new ArrayList<>(Arrays.asList("prog","classList","funcDefList","funcBody"));
    public ArrayList<String> makeNodeList = new ArrayList<>(Arrays.asList("intLit","floatLit","stringLit"));
    HashMap<String,String> terminalReplaceMap = new HashMap<>();

    public void initialReplMap(){
        terminalReplaceMap.put("plus","+");
        terminalReplaceMap.put("minus","-");
        terminalReplaceMap.put("comma",",");
        terminalReplaceMap.put("lsqbr","[");
        terminalReplaceMap.put("rsqbr","]");
        terminalReplaceMap.put("equal","=");
        terminalReplaceMap.put("lcurbr","{");
        terminalReplaceMap.put("rcurbr","}");
        terminalReplaceMap.put("semi",";");
        terminalReplaceMap.put("lpar","(");
        terminalReplaceMap.put("rpar",")");
        terminalReplaceMap.put("colon",":");
        terminalReplaceMap.put("dot",".");
        terminalReplaceMap.put("mult","*");
        terminalReplaceMap.put("div","/");
    }

    public void biuldGrm() throws FileNotFoundException {
        String productionPath = "asgn2.grm";
        String followPath = "asgn2.grm.followSet";
//        String productionPath = "grammar_output_completed_0.grm";
//        String fisrtPath = "grammar_output_completed_0.grm.first";
//        String followPath = "grammar_output_completed_0.grm.follow";
        readProduction(productionPath);
        buildFirstSet();
        readFollowsetUcal(followPath);
    }

    public Scanner initialScanner(String filename) throws FileNotFoundException {

        File file = new File("src/grammar/" + filename);
        Scanner reader = new Scanner(file);
        return reader;
    }


    public void readProduction(String path) throws FileNotFoundException {
        // read grammar from the grammar file line by line
        //<prog> ::= <classDeclList> <FuncDef> 'main' <funcBody>
        //LHS :  <prog>
        //RHS :  <classDeclList> <FuncDef> 'main' <funcBody>
        //Each LHS has a production object,
        //All of those projects will be stored in a hashmap productionMap <String,Production>
        Scanner productionReader = initialScanner(path);
        while (productionReader.hasNextLine()) {
            String line = productionReader.nextLine();
            System.out.println(line);
            if (line.length()>0){
                String[] p = line.split("::=");
                String lhs = p[0].trim().toLowerCase();
                if (p.length >= 2){
                    // check if exist this LHS
                    if(productionMap.containsKey(lhs)){
                        //exist, add RHS
                        String[] rhs = p[1].trim().split(" ");
                        //lowcase
                        for(int i=0 ; i < rhs.length; i++){
                            rhs[i] = rhs[i].toLowerCase();
                            if (!isNonterminal(rhs[i].trim()) && !terminals.contains(rhs[i].trim())){
                                terminals.add(rhs[i].trim());
                            }
                        }
                        // append this rhs string list to the LHS object
                        productionMap.get(lhs).RHS.add(rhs);
                    } else {
                        //this LHS doesn't exist in the productionMap
                        // new a production
                        Production production = new Production();
                        production.LHS = p[0].trim().toLowerCase();
                        String[] rhs = p[1].trim().split(" ");
                        for(int i=0 ; i < rhs.length; i++){
                            rhs[i] = rhs[i].toLowerCase();
                            if (!isNonterminal(rhs[i].trim()) && !terminals.contains(rhs[i].trim())){
                                terminals.add(rhs[i].trim());
                            }
                        }
                        production.RHS.add(rhs);
                        productionMap.put(production.LHS,production);
                    }

                }else {
                    System.out.println("=== error ===");
                    System.out.println(line);
                }
            }
        }
        productions = new ArrayList<String>(productionMap.keySet());
        System.out.println(productionMap.get("<factor>").printProduction());
        System.out.println(productionMap.size());
        System.out.println(terminals.size());
    }


    public void buildFirstSet(){
        for(Production production : productionMap.values()) {
            System.out.println(production.printProduction());
            //check if first set of this element have bee generated,
            // if yes, just skip this element
            if(production.firstSetReady == true) {
                continue;
            }
            for(int i = 0; i < production.RHS.size(); i++){

                String firstElement = production.RHS.get(i)[0];
//                System.out.println("findFirst: "+firstElement);
                if (!isNonterminal(firstElement)){
                    // the first element is terminal
                    // add terminal to first set
                    // finish this RHS branch's first set search
                    Set<String> firstset = new HashSet<>();
                    firstset.add(firstElement);
                    production.firstSet.add(firstset);
                } else {
                    //the first element is nonterminal
                    //calling findFirst() function to get this nonterminal's first set
                    Set<String> firstSet =  findFirst(firstElement);
                    production.firstSet.add(firstSet);
                    String str = "";
                    for (String substr : firstSet) {
                        str +=  ","+substr;
                    }
                    System.out.println("firstElement: "+ firstElement + " " + str);
                    //if the first set of this nonterminal contains epsilon,
                    // then continue to get next element's first set.
                    if (firstSet.contains("epsilon")){
                        int rhsIndex = 1;
                        while (rhsIndex < production.RHS.get(i).length){
                            String nextElement = production.RHS.get(i)[rhsIndex];
                            System.out.println("firstSet has epsilon, get next element: "+nextElement);
                            firstSet =  findFirst(nextElement);
                            production.firstSet.get(i).addAll(firstSet);
                            if (!firstSet.contains("epsilon")) break;
                            rhsIndex += 1;
                        }
                        // check if the first set of last element contains epsilon
                        // if not, remove epsilon from the first set
                        if (rhsIndex != production.RHS.get(i).length){
                            // means this production can not be "epsilon"
                            production.firstSet.get(i).remove("epsilon");
                        }
                    }
                }
            }
            production.firstSetReady = true;
//            System.out.println("==buildFirstSet==");
            System.out.println(production.printFirstSet());
        }
    }


    public void readFollowset(String path) throws FileNotFoundException {
        Scanner followSetReader = initialScanner(path);
        while (followSetReader.hasNextLine()) {
            String line = followSetReader.nextLine();
            if(line.length()> "FOLLOW(".length()) {
                String productionName = line.split("= ")[0].trim().substring(7,line.split("= ")[0].trim().length()-1);
                String teminalStr = line.split("= ")[1].trim();
                String[] teminals = teminalStr.substring(1,teminalStr.length()-1).split(", ");

                Production production = productionMap.get(productionName);
                if (production != null){
                    Set<String> followSet = new HashSet<>();
                    for (String f : teminals){
                        followSet.add(f.trim());
                    }
                    production.followSet.addAll(followSet);
                    System.out.println(production.printFollowSet());
                } else {
                    System.out.println("No production: "+ productionName);
                }

            }

        }
    }

    public void readFollowsetUcal(String path) throws FileNotFoundException {
        initialReplMap();
        Scanner followSetReader = initialScanner(path);
        while (followSetReader.hasNextLine()) {
            String line = followSetReader.nextLine();
            if(line.length()> 3) {
                String productionName = line.split("###")[0].trim().toLowerCase();
                productionName = "<"+productionName+">";
                String teminalStr = line.split("###")[1].trim();
                String[] teminals = teminalStr.split(" ");
                //replace some terminal
                for(int i=0; i<teminals.length; i++){
                    if (terminalReplaceMap.containsKey(teminals[i])){
                        teminals[i] = "'"+terminalReplaceMap.get(teminals[i])+"'";
                    } else {
                        teminals[i] = "'"+teminals[i]+"'";
                    }
                }

                Production production = productionMap.get(productionName);
                if (production != null){
                    Set<String> followSet = new HashSet<>();
                    for (String f : teminals){
                        followSet.add(f.trim());
                    }
                    production.followSet.addAll(followSet);
                    System.out.println(production.printFollowSet());
                } else {
                    System.out.println("No production: "+ productionName);
                }
            }

        }
    }



    public boolean isNonterminal(String rhs_name) {
//        System.out.println(rhs_name.substring(0,1));
//        System.out.println(rhs_name.substring(0,1).compareTo("<") == 0);
        return (rhs_name.length() > 0 && rhs_name.substring(0,1).compareTo("<") == 0) ? true : false;
    }


    public Set<String> findFirst(String element) {
        System.out.println("findFirst-element: " + element);
        if (!isNonterminal(element)){
            Set<String> firstSet =  new HashSet<>();
            firstSet.add(element);
            return firstSet;
        }
        Production production = productionMap.get(element);
//        System.out.println("findFirst:" + production.printProduction());
        if (production.firstSetReady == true) {
            return production.mergeFirstSet();
        }
        for(int i = 0; i < production.RHS.size(); i++){
            String firstElement = production.RHS.get(i)[0];
            System.out.println("firstElement: " + firstElement);
            if (firstElement == "epsilon") {
//                System.out.println("epsilon: " + firstElement);
                Set<String> firstset = new HashSet<>();
                firstset.add(firstElement);
                production.firstSet.add(firstset);
            } else if (!isNonterminal(firstElement)){
//                System.out.println("terminal: " + firstElement);
                // the first element is terminal
                Set<String> firstset = new HashSet<>();
                firstset.add(firstElement);
                production.firstSet.add(firstset);
            } else {
                //the first element is nonterminal
                Set<String> firstSet =  findFirst(firstElement);
                production.firstSet.add(firstSet);
                String str = "";
                for (String substr : firstSet) {
                    str +=  ","+substr;
                }
                System.out.println("firstElement: "+ firstElement + " " + str);

                if (firstSet.contains("epsilon")){
                    int rhsIndex = 1;
                    while (rhsIndex < production.RHS.get(i).length){
                        String nextElement = production.RHS.get(i)[rhsIndex];
                        System.out.println("firstSet has epsilon, get next element: "+nextElement);
                        firstSet =  findFirst(nextElement);
                        production.firstSet.get(i).addAll(firstSet);
                        if (!firstSet.contains("epsilon")) break;
                        rhsIndex += 1;
                    }
                    if (rhsIndex != production.RHS.get(i).length){
                        // means this production can not be "epsilon"
                        production.firstSet.get(i).remove("epsilon");
                    }
                }
            }
        }
        production.firstSetReady = true;
//        System.out.println("==findFirst==");
        System.out.println(production.printFirstSet());
        return production.mergeFirstSet();
    }



    public static void main(String[] args) throws FileNotFoundException {
        LoadProdcution loadProdcution = new LoadProdcution();
        loadProdcution.biuldGrm();
    }
}


class Production {

    public String LHS;
    public ArrayList<String[]> RHS = new ArrayList<>(); //can have multiple rhs
    public ArrayList<Set<String>> firstSet = new ArrayList<>();
    public Set<String> followSet = new HashSet<>();
    public boolean firstSetReady = false;

    public Set<String> mergeFirstSet(){
        Set<String> fullset = new HashSet<>();
        for (Set<String> fSet : firstSet) {
            fullset.addAll(fSet);
        }
        return fullset;
    }


    public String printProduction(){
        String str = LHS + " ::= ";
        for (String[] rhs: RHS) {
            String rhs_str = "";
            for (String substr : rhs)
                rhs_str +=  substr + " ";
            str += rhs_str + " | ";
        }
        return str.substring(0,str.length()-1);
    }

    public String printFirstSet(){
        String str = "";
        for (Set<String> fSet : firstSet) {
            for (String subStr : fSet) {
                str += subStr + ",";
            }
            str += "\n";
        }
        return LHS +" FirstSet:"+str.substring(0,str.length()-1);
    }

    public String printFollowSet(){
        String str = "";
        for (String follow : followSet) {
            str += follow + ",";
        }
        return LHS +" FollowSet:"+str.substring(0,str.length()-1);
    }

    public boolean checkgrm(){
        Set<String> firstSet = mergeFirstSet();
        if (firstSet.contains("epsilon")){
            firstSet.retainAll(followSet);
            if (firstSet.size()>0) {
                System.out.println(printProduction());
                return false;
            }else
                return true;
        } else
            return true;
    }

    public boolean checkAmbious(){
        int count = 0;
        for (Set<String> firsts : firstSet){
            count += firsts.size();
        }
        if (count != mergeFirstSet().size()){
            System.out.println(printProduction());
            return false;
        }
        return true;
    }

    public boolean isNonterminal(String rhs_name) {
//        System.out.println(rhs_name.substring(0,1));
//        System.out.println(rhs_name.substring(0,1).compareTo("<") == 0);
        return (rhs_name.length() > 0 && rhs_name.substring(0,1).compareTo("<") == 0) ? true : false;
    }

}
