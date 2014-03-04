package pcfg;

import java.util.ArrayList;

public class Subtrees {

    private ArrayList<Node> subtrees = new ArrayList<Node>();

    public Subtrees(ArrayList<Node> subtrees) {
        this.subtrees = subtrees;
    }

    public ArrayList<Node> getSubtrees() {
        return subtrees;
    }
}
