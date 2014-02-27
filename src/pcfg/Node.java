package pcfg;

import java.util.ArrayList;
public class Node{
    
    private String value;
    private Node parent;
    private ArrayList<Node> children = new ArrayList<Node>();
    private ArrayList<String> childrenValues = new ArrayList<String>();
    private boolean isOrphan;
    
    public Node(String value)
    {
        this.value = value;
        this.isOrphan = true;
    }

    public void setIsOrphan(boolean isOrphan) {
        this.isOrphan = isOrphan;
    }

    public boolean isIsOrphan() {
        return isOrphan;
    }

    public void addChild(Node child) {
        child.setParent(this);
        child.setIsOrphan(false);
        this.children.add(child);
        this.childrenValues.add(child.getValue());
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public ArrayList<String> getChildrenValues() {
        return childrenValues;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }
}