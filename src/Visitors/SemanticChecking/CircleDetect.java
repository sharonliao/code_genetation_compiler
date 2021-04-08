package Visitors.SemanticChecking;

import AST.InheritNode;

import java.util.ArrayList;

public class CircleDetect {

//    1.根据inherit list来确定有有多少个vector
//    2.初始化一个二维数组
//    3.第二次遍历inherit list，来确定边
//    4.用matrix来表示图
//    5.深度遍历检查是否有环


    ArrayList<InheritNode> inheritList;
    ArrayList<String> classNodeList = new ArrayList<>();
    ArrayList<String> visitOrder = new ArrayList<>();
    int[][] graph;
    int[] visitFlags;
    boolean isDAG = true;
    String errorStr = "";


    public CircleDetect(ArrayList<InheritNode> inheritList){
        this.inheritList = inheritList;
    }


    public void generateGraph(){

        for (InheritNode inheritNode : inheritList){
            String className = inheritNode.getParent().m_symtabentry.m_name;
            String inheritClassName = inheritNode.m_symtabentry.m_name;
            if (!classNodeList.contains(className)){
                classNodeList.add(className);
            }
            if (!classNodeList.contains(inheritClassName)){
                classNodeList.add(inheritClassName);
            }
        }

        int classCount = classNodeList.size();

        // initial a matrix int[][]
        graph = new int[classCount][classCount];
        visitFlags = new int[classCount];

        for(int i=0;i<classCount;i++){
            visitFlags[i] = 0;
            for(int j=0;j<classCount;j++){
                graph[i][j] = 0;
            }
        }

        //update edge
        for (InheritNode inheritNode : inheritList){
            String className = inheritNode.getParent().m_symtabentry.m_name;
            String inheritClassName = inheritNode.m_symtabentry.m_name;
            int indexOfclas = classNodeList.indexOf(className);
            int indexOfinheritClass = classNodeList.indexOf(inheritClassName);
            graph[indexOfclas][indexOfinheritClass] = 1;
        }

    }

    public void DFS(int i){
        visitOrder.add(classNodeList.get(i));
        System.out.println("visiting : "+ classNodeList.get(i));

        visitFlags[i] = 1;
        int vNum = classNodeList.size();
        for(int j=0;j<vNum;j++){

            if(graph[i][j] != 0){

                if(visitFlags[j] == 1){
                    isDAG = false;
                    System.out.println("Error: Inherit chain has the loop at :"+ classNodeList.get(j));
                    System.out.println("Error : loop path:"+loopPath(classNodeList.get(j)));
                    errorStr += "Error: Inherit chain has the loop at :"+ classNodeList.get(j)
                            + "      loop path: "+loopPath(classNodeList.get(j))+"\n";
                    break;
                }else if(visitFlags[j] == -1){

                    continue;
                }else{
                    DFS(j);
                }
            }
        }

        visitFlags[i] = -1;
    }

    String loopPath(String loopNode){
        String result = loopNode;
        for(int i = visitOrder.size()-1; i >= 0; i--){
            String className = visitOrder.get(i);
            if (className.compareTo(loopNode) != 0){
                result = className + " -> " + result;
            }else {
                result = className + " -> " + result;
                break;
            }
        }
        return result;
    }


    public String deepSearch(){
        generateGraph();
        int vNum = classNodeList.size();
        for(int i=0;i<vNum;i++){
            //该结点后边的结点都被访问过了，跳过它
            if(visitFlags[i] == -1){
                continue;
            }
            DFS(i);
            if(!isDAG){
                isDAG = false;
                 break;
            }
        }
        if(isDAG){
//            System.out.println("no circle");
        }

        return errorStr;
    }





}
