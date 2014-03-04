package pcfg;

import java.util.ArrayList;

public class Node {

    private String value;
    private Node parent;
    private Node clonedParent;
    private Node original;

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
        if (this == null && obj == null) {
            return true;
        } else if (obj == null && this != null) {
            return false;
        } else if (this == null && obj != null) {
            return false;
        } else if (!(obj instanceof Node)) {
            return false;
        } else {
            Node rhs = (Node) obj;
            if (this.value.equals(rhs.getValue())) {
                if (this.getChildren().isEmpty() && rhs.getChildren().isEmpty()) {
                    return true;
                }
                if (!this.getChildren().isEmpty() && rhs.getChildren().isEmpty()) {
                    return false;
                }
                if (this.getChildren().isEmpty() && !rhs.getChildren().isEmpty()) {
                    return false;
                }
                if (this.getChildren().get(0).equals(rhs.getChildren().get(0)) && this.getChildren().get(1).equals(rhs.getChildren().get(1))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public void copy(Node src) {
//        this.children = src.children;
        this.children = new ArrayList<Node>();
        for (Node src_node : src.getChildren()) {
            Node child = new Node(null);
            child.copy(src_node);
            this.addChild(child);
        }
        this.value = src.value;
        this.isOrphan = src.isOrphan;
        this.parent = src.parent;
        this.clonedParent = src.clonedParent;
        
        this.original = src.original;
    }

    public Node copyInto(Node twin) {
        twin.original = this;
        twin.value = this.value;
        twin.clonedParent = this.getParent();
        twin.isOrphan = true;
//        if(this.clonedParent!=null){twin.clonedParent=this.clonedParent;}
        for (int i = 0; i < this.children.size(); i++) {
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

    public Node getOriginal() {
        return original;
    }

}
