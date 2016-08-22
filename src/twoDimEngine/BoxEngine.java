package twoDimEngine;

import algebra.src.Vec2;
import graph.Graph;
import twoDimEngine.elements.Quad2D;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoxEngine extends AbstractEngine2D {
    private static int nextID = 0;
    private List<AbstractDrawAble2D> things;
    private Map<Integer, Leaf> leafByIdMap = new HashMap<>();
    private Node tree;

    public BoxEngine(int width, int height) {
        super(width, height);
        things = new ArrayList<>();
        tree = new Node();
    }

    private void draw(AbstractDrawAble2D e) {
        PaintMethod2D painter = e.getMyPainter();
        if (e.getMyPainter() != null) {
            e.draw(painter);
        } else {
            e.draw(defaultPainter);
        }
    }

    public void buildBoundigBoxTree() {
        tree = new Node();
        for (AbstractDrawAble2D th : things) {
            if (th.isDestroyed()) {
                things.remove(th);
            } else {
                Leaf node = new Leaf(th);
                leafByIdMap.put(node.getId(), node);
                tree.insert(node);
            }
        }
    }

    public void addtoList(AbstractDrawAble2D e) {
        things.add(e);
    }

    public void addtoList(AbstractDrawAble2D e, PaintMethod2D painter) {
        e.setMyPainter(painter);
        things.add(e);
    }

    public List<AbstractDrawAble2D> getThingsOnRectangle(BoundingBox rect) {
        List<AbstractDrawAble2D> list = new ArrayList<AbstractDrawAble2D>();
        tree.retrieve(rect, list);
        return list;
    }

    @Override
    public void drawElements() {
        BoundingBox view = new BoundingBox(xmin, ymin, xmax, ymax);
        List<AbstractDrawAble2D> visibleThings = getThingsOnRectangle(view);
        for (AbstractDrawAble2D thing : visibleThings) {
            if (thing.isVisible()) {
                draw(thing);
            }
        }
    }


    public Graph getKnnGraph(int k) {
        if (k <= 0) {
            return new Graph();
        }
        Graph graph = new Graph();

        for (Integer id : leafByIdMap.keySet()) {
            unvisitNode(tree);
            visitLeaf(id, k, graph);
        }
        return graph;
    }

    private void unvisitNode(Node node) {
        node.setVisited(false);
        for (Node child : node.children) {
            if (child != null) {
                unvisitNode(child);
            }
        }
    }

    private void visitLeaf(Integer id, int k, Graph graph) {
        int kcount = k;
        kcount -= graph.getDegree(id);

        Leaf leaf = leafByIdMap.get(id);
        leaf.setVisited(true);
        graph.putVertexProperty(leaf.getId(), "pos", leaf.getRectangle().getCenter());


        Stack<Node> stack = new Stack<>();
        Stack<Node> stackDepth = new Stack<>();
        stack.push(leaf.getRoot());

        while (!stack.isEmpty() && kcount > 0) {
            Node pop = stack.pop();

            // depth search
            stackDepth.push(pop);
            while (!stackDepth.isEmpty() && kcount > 0) {
                Node node = stackDepth.pop();
                node.setVisited(true);
                if (node.isLeaf() && kcount > 0) {
                    kcount--;
                    Leaf leafNode = (Leaf) node;
                    graph.addEdge(id, leafNode.getId());
                    graph.addEdge(leafNode.getId(), id);
                } else {
                    for (Node child : node.children) {
                        if (!child.isVisited()) {
                            stackDepth.push(child);
                        }
                    }
                }
            }
            if (pop.getRoot() != null) {
                stack.push(pop.getRoot());
            }
        }
    }

    public void drawTree() {
        tree.draw();
    }

    public void removeAllElements() {
        things.removeAll(things);
    }


    public List<AbstractDrawAble2D> getThings() {
        return things;
    }

    public void setThings(List<AbstractDrawAble2D> things) {
        this.things = things;
    }


    private class Node {
        private Node root;
        private Node[] children;
        private BoundingBox rectangle;
        private int leftInserts, rightInserts;
        private double leftRightDist = Double.MAX_VALUE;
        private boolean isVisited = false;
        private int id;

        public Node() {
            rectangle = new BoundingBox();
            children = new Node[2];
            this.id = nextID++;
        }

        public boolean isVisited() {
            return isVisited;
        }

        public void setVisited(boolean visited) {
            isVisited = visited;
        }

        public Node getRoot() {
            return root;
        }

        public void setRoot(Node root) {
            this.root = root;
        }

        public Node getLeft() {
            return children[0];
        }

        public void setLeft(Node left) {
            this.children[0] = left;
        }

        public Node getRight() {
            return this.children[1];
        }

        public void setRight(Node right) {
            this.children[1] = right;
        }

        public BoundingBox getRectangle() {
            return rectangle;
        }

        public void setRectangle(BoundingBox rectangle) {
            this.rectangle = rectangle;
        }

        public void insert(Leaf node) {
            this.rectangle = BoundingBox.union(this.rectangle,
                    node.getRectangle());

            if (getLeft() == null) {
                setLeft(node);
                node.setRoot(this);
                leftInserts++;
            } else if (getRight() == null) {
                setRight(node);
                node.setRoot(this);
                leftRightDist = Vec2.diff(getLeft().getRectangle().getCenter(), getRight().getRectangle().getCenter()).norm();
                rightInserts++;
            } else {
                int chosenChildIndex;
                double chosenDist;

                Vec2 rightCenter = this.getRight().getRectangle().getCenter();
                Vec2 leftCenter = this.getLeft().getRectangle().getCenter();
                Vec2 center = node.getRectangle().getCenter();

                double leftDist = Vec2.diff(leftCenter, center).norm();
                double rightDist = Vec2.diff(rightCenter, center).norm();

                if (leftDist <= rightDist) {
                    chosenChildIndex = 0;
                    chosenDist = leftDist;
                } else {
                    chosenChildIndex = 1;
                    chosenDist = rightDist;
                }

                if (children[chosenChildIndex] instanceof Leaf && children[(chosenChildIndex + 1) % 2] instanceof Leaf && chosenDist > leftRightDist) {
                    if (chosenChildIndex == 0) {
                        Leaf aux = (Leaf) getLeft();
                        setLeft(node);
                        node.setRoot(this);
                        this.insert(aux);
                    } else {
                        Leaf aux = (Leaf) getRight();
                        setRight(node);
                        node.setRoot(this);
                        this.insert(aux);
                    }
                } else {
                    children[chosenChildIndex].insert(node);
                    if (chosenChildIndex == 0) {
                        leftInserts++;
                    } else {
                        rightInserts++;
                    }
                }
            }
        }

        public boolean isLeaf() {
            return false;
        }

        /**
         * debug only
         */
        public void draw() {
            float red = 0.0f;
            float blue = 240.0f / 360.0f;
            if (!rectangle.isEmpty()) {
                Quad2D rect = new Quad2D(new Vec2(rectangle.getXmin(),
                        rectangle.getYmin()), new Vec2(rectangle.getXmax(),
                        rectangle.getYmin()), new Vec2(rectangle.getXmax(),
                        rectangle.getYmax()), new Vec2(rectangle.getXmin(),
                        rectangle.getYmax()));
                float hue = (leftInserts + rightInserts)
                        / (1.0f * (tree.leftInserts + tree.rightInserts));
                hue = blue + (red - blue) * hue;
                rect.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
                rect.draw(defaultPainter);
//                if (this.isLeaf()) {
//                    String2D string2D = new String2D(this.getRectangle().getCenter(), String.valueOf(((Leaf) this).getId()));
//                    string2D.setColor(Color.black);
//                    string2D.draw(defaultPainter);
//                }
            }

            Node left = getLeft();
            if (left != null) {
                left.draw();
            }

            Node right = getRight();
            if (right != null) {
                right.draw();
            }
        }

        public void retrieve(BoundingBox rect, List<AbstractDrawAble2D> list) {
            if (!BoundingBox.intersection(this.getRectangle(), rect).isEmpty()) {
                Node left = this.getLeft();
                if (left != null) {
                    left.retrieve(rect, list);
                }
                Node right = this.getRight();
                if (right != null) {
                    right.retrieve(rect, list);
                }
            }
        }

        public int getId() {
            return id;
        }
    }

    private class Leaf extends Node {
        private AbstractDrawAble2D thing;

        public Leaf(AbstractDrawAble2D thing) {
            this.thing = thing;
            this.setRectangle(thing.getBoundingBox());
        }

        @Override
        public void insert(Leaf node) {
            Node oldRoot = this.getRoot();
            Node newNode = new Node();
            newNode.setRoot(oldRoot);
            newNode.insert(this);
            newNode.insert(node);
            /**
             * it can be shown that 'this' is always in the oldRoot
             */
            if (oldRoot.getLeft() == this) {
                oldRoot.setLeft(newNode);
            } else {
                oldRoot.setRight(newNode);
            }
        }

        @Override
        public void retrieve(BoundingBox rect, List<AbstractDrawAble2D> list) {
            if (!BoundingBox.intersection(this.getRectangle(), rect).isEmpty()) {
                list.add(thing);
            }
        }

        @Override
        public boolean isLeaf() {
            return true;
        }
    }
}
