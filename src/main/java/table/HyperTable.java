package table;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Pedroth on 4/25/2016.
 *
 * @param <I> the type parameter of the Indexing variable
 * @param <E> the type parameter of the stored object
 */
public class HyperTable<I, E> implements Table<I, E>, Iterable<HyperTable<I, E>.Leaf> {
    private int dimension;
    private Node table;
    private int numOfElements;

    /**
     * Instantiates a new Hyper table.
     *
     * @param dimension the dimension
     */
    public HyperTable(int dimension) {
        this.dimension = dimension;
        this.numOfElements = 0;
        this.table = new Node();
    }

    /**
     * Check valid index.
     *
     * @param x the index variable
     */
    public void checkValidIndex(I[] x) {
        if (x.length > dimension) {
            throw new RuntimeException("number of coordinates is more than the number of dimensions");
        }
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    /**
     * Gets num of elements.
     *
     * @return the num of elements
     */
    public int getNumOfElements() {
        return numOfElements;
    }

    @Override
    public E get(I[] x) {
        checkValidIndex(x);
        Stack<Node> stack = new Stack<>();
        stack.push(table);
        int index = 0;
        E ans = null;
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (!node.containsKey(x[index])) {
                return null;
            }
            Node nextNode = node.getNode(x[index++]);
            if (nextNode instanceof HyperTable.Leaf) {
                ans = ((Leaf) nextNode).getElement();
                break;
            }
            stack.push(nextNode);
        }
        return ans;
    }

    @Override
    public void set(I[] x, E element) {
        checkValidIndex(x);
        Stack<Node> stack = new Stack<>();
        stack.push(table);
        int index = 0;
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (!node.containsKey(x[index])) {
                if (index == this.getDimension() - 1) {
                    Leaf leaf = new Leaf(element, x);
                    node.putNode(x[index], leaf);
                    numOfElements++;
                    break;
                }
                node.putNode(x[index], new Node());
            }
            Node nextNode = node.getNode(x[index++]);
            if (nextNode instanceof HyperTable.Leaf) {
                ((Leaf) nextNode).setElement(element);
                break;
            }
            stack.push(nextNode);
        }
    }

    @Override
    public Iterator<Leaf> iterator() {
        return new HyperTableIterator();
    }


    //Inner auxiliar classes

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<Leaf> iterator = new HyperTableLeafIterator();
        while (iterator.hasNext()) {
            Leaf next = iterator.next();
            stringBuilder.append(next.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String toString(Function<HyperTable, String> printer) {
        return printer.apply(this);
    }

    /**
     * The type Hyper table iterator.
     */
    class HyperTableLeafIterator implements Iterator<Leaf> {
        private List<Node> stack;

        /**
         * Instantiates a new Hyper table iterator.
         */
        public HyperTableLeafIterator() {
            this.stack = new LinkedList<>();
            this.stack.add(table);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Leaf next() {
            Leaf ans = null;
            while (hasNext()) {
                Node pop = stack.get(0);
                stack.remove(0);
                /*
                    Temporary list to put elements in order
                 */
                List<Node> tempList = new ArrayList<>(pop.getNodeMap().size());
                for (Map.Entry<I, Node> iNodeEntry : pop.getNodeMap().entrySet()) {
                    tempList.add(iNodeEntry.getValue());
                }
                for (int i = tempList.size() - 1; i >= 0; i--) {
                    stack.add(0, tempList.get(i));
                }
                if (pop instanceof HyperTable.Leaf) {
                    ans = (Leaf) pop;
                    break;
                }
            }
            return ans;
        }
    }


    class HyperTableIterator implements Iterator<Leaf> {
        private HyperTableLeafIterator hyperTableLeafIterator;

        public HyperTableIterator() {
            this.hyperTableLeafIterator = new HyperTableLeafIterator();
        }

        @Override
        public boolean hasNext() {
            return hyperTableLeafIterator.hasNext();
        }

        @Override
        public Leaf next() {
            return hyperTableLeafIterator.next();
        }
    }

    private class Node {
        private Map<I, Node> nodeMap;

        /**
         * Instantiates a new Node.
         */
        public Node() {
            this.nodeMap = new HashMap<>();
        }

        /**
         * Gets node.
         *
         * @param x the x
         * @return the node
         */
        public Node getNode(I x) {
            return nodeMap.get(x);
        }

        /**
         * Put node.
         *
         * @param x    the x
         * @param node the node
         */
        public void putNode(I x, Node node) {
            nodeMap.put(x, node);
        }

        /**
         * Gets node map.
         *
         * @return the node map
         */
        public Map<I, Node> getNodeMap() {
            return nodeMap;
        }

        /**
         * Contains key.
         *
         * @param x the x
         * @return the boolean
         */
        public boolean containsKey(I x) {
            return nodeMap.containsKey(x);
        }

        @Override
        public String toString() {
            return new StringBuilder().append("Node{").append("nodeMap=").append(nodeMap.toString()).append('}').toString();
        }
    }

    public class Leaf extends Node {
        private E element;
        private I[] index;

        /**
         * Instantiates a new Leaf.
         *
         * @param element the element
         * @param index   the index
         */
        public Leaf(E element, I[] index) {
            this.element = element;
            this.index = index;
        }

        @Override
        public Node getNode(I x) {
            return null;
        }

        @Override
        public void putNode(I x, Node node) {
            //blank on purpose
        }

        /**
         * Gets element.
         *
         * @return the element
         */
        public E getElement() {
            return element;
        }

        /**
         * Sets element.
         *
         * @param element the element
         */
        public void setElement(E element) {
            this.element = element;
        }


        /**
         * Get index.
         *
         * @return the i [ ]
         */
        public I[] getIndex() {
            return index;
        }

        /**
         * Sets index.
         *
         * @param index the index
         */
        public void setIndex(I[] index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return "Leaf{" +
                    "element=" + element +
                    ", index=" + Arrays.toString(index) +
                    '}';
        }
    }


}
