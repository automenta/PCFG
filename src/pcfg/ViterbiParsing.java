package pcfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ViterbiParsing {

    private static final Map<String, Map<ArrayList<String>, Float>> rules_probs_map = new HashMap<String, Map<ArrayList<String>, Float>>();
    private static final Map<String, float[][]> piMap = new HashMap<String, float[][]>(); // for CKY algorithm
    private static final Map<String, boolean[][]> piBooleanMap = new HashMap<String, boolean[][]>(); // for CKY algorithm - To cache the already calculated values
    private static final Map<String, String[][]> bpMap = new HashMap<String, String[][]>(); // for CKY algorithm - back pointer maps
    private static final Map<String, Node> sentence_tree_map = new HashMap<String, Node>(); // for storing each sentences parse tree

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        
        initGrammar();

        
        //printRulesProbs("S");
        
        //calls CKY to find the most probable parse tree starting at "S"
        CKY("maximum velocity", "NP-BAR", false); 
        CKY("maximum *", "NP-BAR", false); 
        //CKY("maximum velocity", "S", false); 
        
        //CKY("The minimum unit is $ 100,000 .", "S", false);
        CKY("The minimum unit * $ 100,000 .", "S", false);
        CKY("* minimum unit * $ 100,000 .", "S", false);
        //CKY("The minimum unit was $ 10,000 .", "S", false);
        
        CKY("3f83h9f", "S", false);
        
        //CKY("The minimum unit is $ 100,000.", "S", false);
//        CKY("And expect slower UNK-LC-s.", "S"); 
//        CKY("Mr. Tomash will remain as a director emeritus.", "S");
    }

    public static void initGrammar() throws FileNotFoundException, URISyntaxException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
        Scanner sc = new Scanner(new File(file.getParentFile().getParent() + "/resource/cnf.txt"));
        
        while (sc.hasNextLine()) {
            String sentence = "";
            Node root = new Node("S");
            String line = sc.nextLine();
//            System.out.println(line);
            ArrayList<Integer> index_openBrackets = new ArrayList<Integer>();
            ArrayList<Node> nodes_list = new ArrayList<Node>();
            String splt[] = line.split(" ");
            for (int i = 0; i < splt.length; i++) {
                if (splt[i].contains("(")) {
                    index_openBrackets.add(i);
                    Node node = new Node(splt[i].replace("(", ""));
                    if (i == 0) {
                        root = node;
                    }//stores the root of the tree
                    nodes_list.add(node);
                }
                if (splt[i].contains(")")) {
                    Node node = new Node(splt[i].replace(")", ""));
                    sentence += splt[i].replace(")", "") + " ";
                    nodes_list.add(node);

                    int numberOfRequiredCollapses = 0;
                    for (int j = 0; j < splt[i].length(); j++) {
                        if (splt[i].charAt(j) == ')') {
                            numberOfRequiredCollapses++;
                        }
                    }
                    //it adds children to each node - a.k.a creates rules
                    while (numberOfRequiredCollapses != 0) {

                        Node tempParent = nodes_list.get(index_openBrackets.get(index_openBrackets.size() - 1));
                        int parentIndex = index_openBrackets.get(index_openBrackets.size() - 1);
                        index_openBrackets.remove(index_openBrackets.size() - 1);
                        numberOfRequiredCollapses--;

                        int k = parentIndex + 1;
                        while (i - k >= 0) {
                            if (nodes_list.get(k).isIsOrphan()) {
                                tempParent.addChild(nodes_list.get(k));
                            }
                            k++;
                        }

                        Map<ArrayList<String>, Float> rightHandSides = new HashMap<ArrayList<String>, Float>();
                        if (!rules_probs_map.containsKey(tempParent.getValue())) {
                            rightHandSides.put(tempParent.getChildrenValues(), (float) 1);
                            rules_probs_map.put(tempParent.getValue(), rightHandSides);
                        } else {
                            rightHandSides = rules_probs_map.get(tempParent.getValue());
                            if (!rightHandSides.containsKey(tempParent.getChildrenValues())) {
                                rightHandSides.put(tempParent.getChildrenValues(), (float) 1);
                            } else {
                                float count = rightHandSides.get(tempParent.getChildrenValues());
                                rightHandSides.put(tempParent.getChildrenValues(), count + 1);
                            }
                            rules_probs_map.put(tempParent.getValue(), rightHandSides);
                        }
                    }
                }
            }
            sentence_tree_map.put(sentence, root);//stores the root node of the tree
//            printTreeAlpha(nodes_list.get(0));
//            printTreeBeta(nodes_list.get(0));
        }
        convertCountsToProbs(); // if we don't run this, the map is "rule,count" map
        
    }
    /*
     * Takes rules and their counts Map and converts it to rules and probs Map
     * a.k.a : generates PCFG
     */
    public static void convertCountsToProbs() {
        for (String left : rules_probs_map.keySet()) {
            float overallCount = 0;
            for (float count : rules_probs_map.get(left).values()) {
                overallCount += count;
            }
            for (ArrayList<String> right : rules_probs_map.get(left).keySet()) {
                rules_probs_map.get(left).put(right, rules_probs_map.get(left).get(right) / overallCount);
            }
        }
    }

    public static String[] tokenize(String s) {
        return s.split(" ");
    }
    /*
     * Assuming the CGF is in CNF form
     * CKY finds the most probable parse tree
     * for the given sentence
     */
    public static void CKY(String sentence, String rootNode, boolean subtrees) {
        String sentenceTokens[] = tokenize(sentence);
        
        
        //initialize the piTable for each potenial root
        for (String root : rules_probs_map.keySet()) {
            
            float piTable[][] = new float[sentenceTokens.length][sentenceTokens.length];
            String bpTable[][] = new String[sentenceTokens.length][sentenceTokens.length];
            boolean piBooleanTable[][] = new boolean[sentenceTokens.length][sentenceTokens.length];
            
            for (int i = 0; i < sentenceTokens.length; i++) {
                boolean wildcard = sentenceTokens[i].equals("*");
                if (wildcard) {
                    piTable[i][i] = 1.0f;
                    piBooleanTable[i][i] = true;
                }
                else {
                    ArrayList<String> child = new ArrayList<String>();
                    child.add(sentenceTokens[i]);
                    if (rules_probs_map.get(root).containsKey(child)) {
                        piTable[i][i] = rules_probs_map.get(root).get(child); // returns the probability of this rule
                        piBooleanTable[i][i] = true;
                    }
                }
            }
            piMap.put(root, piTable);
            piBooleanMap.put(root, piBooleanTable);
            bpMap.put(root, bpTable);
        }
        for (int l = 1; l < sentenceTokens.length; l++) {
            for (int i = 0; i < sentenceTokens.length - l; i++) {
                int j = i + l;
                for (String treeHead : piMap.keySet()) {
                    PI(i, j, treeHead);
                }
            }
        }

        
        Node root = new Node(rootNode);
        ViterbiParse(rootNode, 0, sentenceTokens.length - 1, root);
        System.out.println("/////////Parse Tree for: \"" + sentence + "\" ////////////////");
        PrintParseTree(root, "");
        System.out.println("/////////////////////////////////////////////////////////////////////////");
        System.out.println();

        if (subtrees) {
            System.out.println("/////////Subtrees of the Parse Tree of: \"" + sentence + "\" ////////////////");
    //        //***********Categorizes Subtrees of Parse Tree to CFGOnly and SubtreeOnly******//
            ArrayList<Subtrees> subtrees_list = extractSubsetOfSubtrees(root);
            for (Subtrees subtreesSubSet : subtrees_list) {
                for (Node node : subtreesSubSet.getSubtrees()) {
                    if (!node.getChildren().get(0).getChildren().isEmpty() || !node.getChildren().get(1).getChildren().isEmpty()) {
                        subtreesSubSet.setOnlyCFGRules(false);
                    }
                    if (node.getChildren().get(0).getChildren().isEmpty() && node.getChildren().get(1).getChildren().isEmpty()) {
                        subtreesSubSet.setOnlySubtrees(false);
                    }
                    PrintParseTree(node, "");
                }
                System.out.println("----");
            }
            //*************************************************//
            System.out.println("/////////////////////////////////////////////////////////////////////////");
        }
        
    }

    public static float PI(int i, int j, String treeHead) {
        if (piBooleanMap.get(treeHead)[i][j]) {
            return piMap.get(treeHead)[i][j];
        }
        float MAXprobab = 0;
        String MAXrule = "";
        Map<ArrayList<String>, Float> rulesMap = rules_probs_map.get(treeHead); // map of rules with treeHead on their left
        for (ArrayList<String> pulledRight : rulesMap.keySet()) {
            if (pulledRight.size() == 2) {
                float pOfrule = rulesMap.get(pulledRight);
                String nonterminal1 = pulledRight.get(0);
                String nonterminal2 = pulledRight.get(1);
                for (int s = i; s < j; s++) {
                    float tp = pOfrule * PI(i, s, nonterminal1) * PI(s + 1, j, nonterminal2);
                    if (tp > MAXprobab) {
                        MAXprobab = tp;
                        MAXrule = nonterminal1 + " " + nonterminal2 + " " + String.valueOf(s);
                    }
                }
            }
        }
        String bpTable_treeHead[][] = bpMap.get(treeHead);
        bpTable_treeHead[i][j] = MAXrule;
        bpMap.put(treeHead, bpTable_treeHead);

        float piTable_treeHead[][] = piMap.get(treeHead);
        boolean piBooleanTable_treeHead[][] = piBooleanMap.get(treeHead);
        piTable_treeHead[i][j] = MAXprobab;

        piBooleanTable_treeHead[i][j] = true;
        piMap.put(treeHead, piTable_treeHead);
        piBooleanMap.put(treeHead, piBooleanTable_treeHead);
        return MAXprobab;
    }

    public static void ViterbiParse(String root, int i, int j, Node parent) {
        if (i == j) {
            return;
        }
        String bpTable[][] = bpMap.get(root);

        
        String[] split = null;
        if (bpTable!=null) {
            String b = bpTable[i][j];           
            if (b!=null) split = b.split(" ");        
        }
        
        if (split == null || split.length!=3) {
            
            /*System.err.println(" " + root + " " + i + " " + j + " " + parent + " -> " + Arrays.toString(split));
            split = new String[] { "N","",Integer.toString(i) };*/
            return;
                    
        }
        Node left_child = new Node(split[0]);
        left_child.setParent(parent);
        parent.addChild(left_child);
        Node right_child = new Node(split[1]);
        parent.addChild(right_child);
        right_child.setParent(parent);

//        System.out.println(root + "->" + bpTable[i][j]);
        ViterbiParse(split[0], i, Integer.parseInt(split[2]), left_child);
        ViterbiParse(split[1], Integer.parseInt(split[2]) + 1, j, right_child);
    }

    public static void PrintParseTree(Node root, String space) {
        if (root == null) {
            return;
        }

        System.out.println(space + root.getValue() + " -> " + root.getChildrenValues());
        space += "\t";
        final ArrayList<Node> ch = root.getChildren();
        if (ch.size() >= 1) {
            if (!ch.get(0).getChildren().isEmpty()) {
                PrintParseTree(ch.get(0), space);
            }
            if (ch.get(0).getChildren().isEmpty()) {
                System.out.println(space + ch.get(0).getValue() + " -> Terminal");
            }
        }
        if (ch.size() == 2) {
            if (!ch.get(1).getChildren().isEmpty()) {
                PrintParseTree(ch.get(1), space);
            }
            if (ch.get(1).getChildren().isEmpty()) {
                System.out.println(space + ch.get(1).getValue() + " -> Terminal");
            }
        }
    }

    public static ArrayList<Subtrees> extractSubsetOfSubtrees(Node root) {

        if (root == null) {
            return null;
        }
        ArrayList<Subtrees> left = new ArrayList<Subtrees>();
        ArrayList<Subtrees> right = new ArrayList<Subtrees>();
        if (!root.getChildren().get(0).getChildren().isEmpty()) {
            left = extractSubsetOfSubtrees(root.getChildren().get(0));
        }
        if (!root.getChildren().get(1).getChildren().isEmpty()) {
            right = extractSubsetOfSubtrees(root.getChildren().get(1));
        }
        return combinateSubtrees(left, right, root);
    }

    public static ArrayList<Subtrees> combinateSubtrees(ArrayList<Subtrees> left, ArrayList<Subtrees> right, Node root) {
        if (left.isEmpty() && right.isEmpty()) {
            ArrayList<Subtrees> merged_subtrees_set = new ArrayList<Subtrees>();
            ArrayList<Node> separateList = new ArrayList<Node>();
            separateList.add(root.copyInto(new Node(null)));
            Subtrees separate = new Subtrees(separateList);
            merged_subtrees_set.add(separate);
            return merged_subtrees_set;
        } else {
            ArrayList<Subtrees> left_subtrees = new ArrayList<Subtrees>();
            ArrayList<Subtrees> right_subtrees = new ArrayList<Subtrees>();
            for (Subtrees sub : left) {
                ArrayList<Node> leftNodeMix = new ArrayList<Node>();
                ArrayList<Node> separateList = new ArrayList<Node>();
                separateList.add(root.copyInto(new Node(null)));
                separateList.addAll(sub.getSubtrees());
                Subtrees separate = new Subtrees(separateList);
                left_subtrees.add(separate);
                for (Node node : sub.getSubtrees()) {
                    Node clonedRoot = new Node(null);
                    root.copyInto(clonedRoot);
                    if (node.getClonedParent().equals(root)) {
                        clonedRoot.getChildren().remove(0);
                        clonedRoot.getChildren().add(0, node);
                        node.setParent(clonedRoot);
                        leftNodeMix.add(clonedRoot);
                    } else {
                        leftNodeMix.add(node);
                    }
                }
                if (!leftNodeMix.isEmpty()) {
                    Subtrees combinedLeft = new Subtrees(leftNodeMix);
                    left_subtrees.add(combinedLeft);
                }
            }
            for (Subtrees sub : right) {
                ArrayList<Node> rightNodeMix = new ArrayList<Node>();
                ArrayList<Node> separateList = new ArrayList<Node>();
                separateList.add(root.copyInto(new Node(null)));
                separateList.addAll(sub.getSubtrees());
                Subtrees separate = new Subtrees(separateList);
                right_subtrees.add(separate);
                for (Node node : sub.getSubtrees()) {
                    Node clonedRoot = new Node(null);
                    root.copyInto(clonedRoot);
                    if (node.getClonedParent().equals(root)) {
                        clonedRoot.getChildren().remove(1);
                        clonedRoot.getChildren().add(1, node);
                        node.setParent(clonedRoot);
                        rightNodeMix.add(clonedRoot);
                    } else {
                        rightNodeMix.add(node);
                    }
                }
                if (!rightNodeMix.isEmpty()) {
                    Subtrees combinedRight = new Subtrees(rightNodeMix);
                    right_subtrees.add(combinedRight);
                }
            }
            return mergeLeftRight(left_subtrees, right_subtrees, root);
        }
    }

    public static ArrayList<Subtrees> mergeLeftRight(ArrayList<Subtrees> left_subtrees, ArrayList<Subtrees> right_subtrees, Node root) {
        ArrayList<Subtrees> merged_subtrees_set = new ArrayList<Subtrees>();

        if (left_subtrees.isEmpty()) {
            merged_subtrees_set.addAll(right_subtrees);
            return merged_subtrees_set;
        }
        if (right_subtrees.isEmpty()) {
            merged_subtrees_set.addAll(left_subtrees);
            return merged_subtrees_set;
        } else {
            for (Subtrees left_sub : left_subtrees) {
                for (Subtrees right_sub : right_subtrees) {
                    ArrayList<Node> nodeList = new ArrayList<Node>();
                    Node left_bridge = new Node(null);
                    for (Node node : left_sub.getSubtrees()) {
                        if (!node.getOriginal().equals(root)) {
                            nodeList.add(node);
                        } else {
                            left_bridge.copy(node);
                        }
                    }
                    Node right_bridge = new Node(null);
                    for (Node node : right_sub.getSubtrees()) {
                        if (!node.getOriginal().equals(root)) {
                            nodeList.add(node);
                        } else {
                            right_bridge.copy(node);
                        }
                    }
                    Node left = new Node("LEFT");
                    left.copy(left_bridge.getChildren().get(0));
                    right_bridge.getChildren().remove(0); //XXXXXXXXXXX
                    right_bridge.getChildren().add(0, left); //XXXXXXXXXXXXX
                    nodeList.add(right_bridge);
                    Subtrees combined = new Subtrees(nodeList);
                    merged_subtrees_set.add(combined);
                }
            }
            return merged_subtrees_set;
        }
    }

    public static void printRulesProbs(String nonterminal) {
        if (!rules_probs_map.containsKey(nonterminal)) {
            System.out.println(nonterminal + " is a terminal!");
            return;
        }
        Map<ArrayList<String>, Float> retr = rules_probs_map.get(nonterminal);

        System.out.println(nonterminal + " goes to :");
        for (ArrayList<String> nodes : retr.keySet()) {
            for (String node : nodes) {
                System.out.print("\t" + node + " ");
            }
            System.out.println("  :   " + retr.get(nodes));
        }
    }

    public static void printTreeBeta(Node node) {
        ArrayList<Node> lst = node.getChildren();
        System.out.print(node.getValue() + " : ");
        for (int i = 0; i < lst.size(); i++) {
            if (lst.size() == 1 && lst.get(0).getChildren().size() != 0) {
                System.out.print(lst.get(i).getValue() + "\t");
            }
        }
        System.out.println();
        for (int j = 0; j < lst.size(); j++) {
            if (!lst.get(j).getChildren().isEmpty()) {
                printTreeBeta(lst.get(j));
            }
        }
    }

    public static void printTreeAlpha(Node node) {
        ArrayList<Node> lst = node.getChildren();
        if (!lst.isEmpty()) {
            for (int j = 0; j < lst.size(); j++) {
                printTreeAlpha(lst.get(j));
            }
            System.out.println(node.getValue());
            return;
        } else {
            System.out.println(node.getValue());
            return;
        }
    }
}
