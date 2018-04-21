package graphicEngine;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BVManager<B extends Identifier & VolumeBounded> {
    private Map<Integer, Leaf> elements;
    private Node tree;

    public BVManager() {
        tree = new Node();
        elements = new HashMap<>();
    }

    public void addElement(B element) {
        Leaf leaf;
        int id = element.getId();
        if (elements.containsKey(id)) {
            leaf = elements.get(id);
            leaf.remove();
            tree.insert(leaf);
        } else {
            leaf = new Leaf(element);
            elements.put(id, leaf);
            tree.insert(leaf);
        }
    }

    public B getElement(int id) {
        return elements.get(id).getElement();
    }

    public List<B> getElementsInVolume(BoundingVolume boundingVolume) {
        List<B> list = new ArrayList<>();
        tree.retrieve(boundingVolume, list);
        return list;
    }

    private class Node {
        private Node root;
        private Node left;
        private Node right;
        private BoundingVolume volume;
        private int leftInserts, rightInserts;

        public Node() {
        }

        public Node getRoot() {
            return root;
        }

        public void setRoot(Node root) {
            this.root = root;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public BoundingVolume getVolume() {
            return volume;
        }

        public void setVolume(BoundingVolume volume) {
            this.volume = volume;
        }

        public void insert(Leaf node) {
            this.volume = this.volume.union(node.getVolume());
            if (this.left == null) {
                setLeft(node);
                node.setRoot(this);
                leftInserts++;
            } else if (this.right == null) {
                setRight(node);
                node.setRoot(this);
                rightInserts++;
            } else {
                Node chosen;
                BoundingVolume rightCenter = this.getRight().getVolume();
                BoundingVolume leftCenter = this.getLeft().getVolume();
                BoundingVolume center = node.getVolume();
                double leftDist = leftCenter.dist(center);
                double rightDist = rightCenter.dist(center);
                if (leftDist < rightDist) {
                    chosen = left;
                    leftInserts++;
                } else if (leftDist > rightDist) {
                    chosen = right;
                    rightInserts++;
                } else {
                    if (leftInserts < rightInserts) {
                        chosen = left;
                        leftInserts++;
                        left = null;
                    } else {
                        chosen = right;
                        rightInserts++;
                        right = null;
                    }
                }
                chosen.insert(node);
            }
        }

        /**
         * debug only
         */
        public void draw() {

        }

        public void retrieve(BoundingVolume boundingVolume, List<B> list) {
            if (!this.getVolume().intersection(boundingVolume).isEmpty()) {
                if (this.left != null) {
                    this.left.retrieve(boundingVolume, list);
                }
                if (this.right != null) {
                    this.right.retrieve(boundingVolume, list);
                }
            }
        }

    }

    private class Leaf extends Node {
        private B element;

        public Leaf(B element) {
            this.element = element;
            this.setVolume(element.getBoundingVolume());
        }

        public B getElement() {
            return element;
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
        public void retrieve(BoundingVolume boundingVolume, List<B> list) {
            if (!this.getVolume().intersection(boundingVolume).isEmpty()) {
                list.add(element);
            }
        }

        public void remove() {
            Node root = this.getRoot();
            if (root.getLeft() == this) {
                root.setLeft(null);
            } else {
                //it can be shown that if root left leaf isn't this then this is the right root leaf
                root.setRight(null);
            }
        }
    }


}
