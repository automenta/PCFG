package pcfg;

import java.util.ArrayList;

public class Node {

    private String value;
    private Node parent;
    private Node clonedParent;
    private ArrayList<Node> children = new ArrayList<Node>();
    private ArrayList<String> childrenValues = new ArrayList<String>();
    private boolean isOrphan;

    public Node(String value) {
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
        this.isOrphan = false;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Node)) {
            return false;
        }

        Node rhs = (Node) obj;
        if (this.value.equals(rhs.getValue())) {
            if(this.getChildren().isEmpty()&&rhs.getChildren().isEmpty())
                return true;
            if (this.getChildren().get(0).equals(rhs.getChildren().get(0)) && this.getChildren().get(1).equals(rhs.getChildren().get(1))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public Node copyInto(Node twin)
    {
        twin.value = this.value;
        twin.clonedParent = this.getParent();
        twin.isOrphan = true;
//        if(this.clonedParent!=null){twin.clonedParent=this.clonedParent;}
        for(int i =0;i<this.children.size();i++)
        {
            Node child = new Node(this.children.get(i).getValue());
            twin.addChild(child);
        }
        return twin;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    
    public Node getClonedParent() {
        return clonedParent;
    }
    
}
