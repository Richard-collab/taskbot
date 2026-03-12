package org.example.ruledetect.slotrule;

import com.google.common.collect.Lists;

import java.util.*;

@Deprecated
public class AcAutomaton2 {

    private Node root;                                  //根结点

    public AcAutomaton2(Collection<String> keywords) {
        this.root = new Node(true);
        for (String keyword: keywords) {
            addKeyword(keyword);
        }
        constructFailureStates();
    }

    private static class Node {
        private Map<Character, Node> map;   //用于放这个Node的所有子节点，储存形式是：Map(char, Node)
        private List<String> keywords; //该节点处包含的所有keywords
        private Node failure;               //fail指针指向的node
        private Boolean isRoot = false;     //是否为根结点

        public Node(){
            map = new HashMap<>();
            keywords = new ArrayList<>();
        }

        public Node(Boolean isRoot) {
            this();
            this.isRoot = isRoot;
        }

        //用于build trie，如果一个字符character存在于子节点中，不做任何操作，返回这个节点的node
        //否则，建一个node，并将map(char,node)添加到当前节点的子节点里，并返回这个node
        public Node insert(Character character) {
            Node node = this.map.get(character);
            if (node == null) {
                node = new Node();
                map.put(character, node);
            }
            return node;
        }

        public void addKeyword(String keyword) {
            keywords.add(keyword);
        }

        public void addKeywords(Collection<String> keywords) {
            this.keywords.addAll(keywords);
        }

        public Node find(Character character) {
            return map.get(character);
        }


        /**
         * 利用父节点fail node来寻找子节点的fail node
         * or
         * parseText时找下一个匹配的node
         */
        private Node nextState(Character transition) {
            //用于构建fail node时，这里的this是父节点的fail node
            //首先从父节点的fail node的子节点里找有没有值和当前失败节点的char值相同的
            Node state = this.find(transition);

            //如果找到了这样的节点，那么该节点就是当前失败位置节点的fail node
            if (state != null) {
                return state;
            }

            //如果没有找到这样的节点，而父节点的fail node又是root，那么返回root作为当前失败位置节点的fail node
            if (this.isRoot) {
                return this;
            }

            //如果上述两种情况都不满足，那么就对父节点的fail node的fail node再重复上述过程，直到找到为止
            //这个地方借鉴了KMP算法里面求解next列表的思想
            return this.failure.nextState(transition);
        }


        public Collection<Node> children() {
            return this.map.values();
        }


        public void setFailure(Node node) {
            failure = node;
        }


        public Node getFailure() {
            return failure;
        }


        //返回一个Node的所有子节点的键值，也就是这个子节点上储存的char
        public Set<Character> getTransitions() {
            return map.keySet();
        }


        public Collection<String> getKeywords() {
            return this.keywords == null ? Collections.emptyList() : this.keywords;
        }
    }


    private static class Slot {
        private final String value;   //匹配到的模式串
        private final int start;        //起点
        private final int end;          //终点

        public Slot(final int start, final int end, final String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return super.toString() + "=" + this.value;
        }
    }



    /**
     * 添加一个模式串(内部使用字典树构建)
     */
    public void addKeyword(String keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        Node currentState = this.root;
        for (Character character : keyword.toCharArray()) {
            //如果char已经在子节点里，返回这个节点的node；否则建一个node，并将map(char,node)加到子节点里去
            currentState = currentState.insert(character);
        }
        //在每一个尾节点处，将从root到尾节点的整个string添加到这个叶节点的PattenString里
        currentState.addKeyword(keyword);
    }



    /**
     * 用ac自动机做匹配，返回text里包含的keywords及其在text里的起始位置
     */
    public Collection<Slot> parseText(String text) {
        Node currentState = this.root;
        List<Slot> collectedSlots = new ArrayList<>();
        for (int position = 0; position < text.length(); position++) {
            Character character = text.charAt(position);
            //依次从子节点里找char，如果子节点没找到，就到子节点的fail node找，并返回最后找到的node；如果找不到就会返回root
            //这一步同时也在更新currentState，如果找到了就更新currentState为找到的node，没找到currentState就更新为root，相当于又从头开始找
            currentState = currentState.nextState(character);
            Collection<String> keywords = currentState.getKeywords();
            //如果找到的node的keywords非空，说明有keyword被匹配到了
            for (String keyword : keywords) {
                collectedSlots.add(new Slot(position - keyword.length() + 1, position, keyword));
            }
        }
        return collectedSlots;//返回匹配到的所有slots
    }



    /**
     * 建立Fail表(核心,BFS遍历)
     */
    private void constructFailureStates() {
        Queue<Node> queue = new LinkedList<>();

        //首先从把root的子节点的fail node全设为root
        //然后将root的所有子节点加到queue里面
        for (Node depthOneState : this.root.children()) {
            depthOneState.setFailure(this.root);
            queue.add(depthOneState);
        }

        while (!queue.isEmpty()) {
            Node parentNode = queue.poll();
            //下面给parentNode的所有子节点找fail node
            for (Character transition : parentNode.getTransitions()) {           //transition是父节点的子节点的char
                Node childNode = parentNode.find(transition);                    //childNode是子节点中对应上面char值的节点的Node值
                queue.add(childNode);                                            //将这个parentNode的所有子节点加入queue，在parentNode的所有兄弟节点都过了一遍之后，就会过这些再下一层的节点
                Node failNode = parentNode.getFailure().nextState(transition);   //利用父节点的fail node来构建子节点的fail node
                childNode.setFailure(failNode);

                //每个节点处的keyword要加上它的fail node处的keyword
                //因为能匹配到这个位置的话，那么fail node处的keyword一定是匹配的keyword
                childNode.addKeywords(failNode.getKeywords());
            }
        }
    }


    public static void main(String[] args) {
        List<String> keywords = Lists.newArrayList("he", "she", "his", "hers");
        AcAutomaton2 acAutomaton = new AcAutomaton2(keywords);

        //匹配text，并返回匹配到的slots
        Collection<Slot> slots = acAutomaton.parseText("ushers");
        for (Slot Slot : slots) {
            System.out.println(Slot.start + " " + Slot.end + "\t" + Slot.getValue());
        }
    }
}
