import java.util.ArrayList;

public class ASTNode {
    public ArrayList<ASTNode> childrenList = new ArrayList<>();
    public ASTNode parent = null;
    public String name = "";
    public String value = "";
    public int level = 0;
    public int id ;

    public String type = "";

    public ASTNode(String type, int id){
        this.type = type;
        this.id = id;

    }

    public void addChild(ASTNode child) {
        child.parent = this;
        this.childrenList.add(child);
    }

    public boolean isLeaf() {
        return  this.childrenList.size() == 0 ? true : false;
    }

    public void removeParent() {
        this.parent = null;
    }
}

