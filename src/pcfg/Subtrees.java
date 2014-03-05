package pcfg;

import java.util.ArrayList;

public class Subtrees {
    private boolean onlyCFGRules=true;
    private boolean onlySubtrees=true;
    private ArrayList<Node> subtrees = new ArrayList<Node>();

    public Subtrees(ArrayList<Node> subtrees) {
        this.subtrees = subtrees;
    }

    public ArrayList<Node> getSubtrees() {
        return subtrees;
    }

    public boolean isOnlyCFGRules() {
        return onlyCFGRules;
    }

    public void setOnlyCFGRules(boolean onlyCFGRules) {
        this.onlyCFGRules = onlyCFGRules;
    }

    public boolean isOnlySubtrees() {
        return onlySubtrees;
    }

    public void setOnlySubtrees(boolean onlySubtrees) {
        this.onlySubtrees = onlySubtrees;
    }
    
    
}
