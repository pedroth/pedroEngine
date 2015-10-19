package twoDimEngine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import twoDimEngine.elements.Quad2D;
import twoDimEngine.shaders.PaintMethod2D;
import algebra.Vec2;

public class BoxEngine extends AbstractEngine2D {
	private List<AbstractDrawAble2D> things;
	private Node tree;

	private class Node {
		private Node root;
		private Node left;
		private Node right;
		private BoundingBox rectangle;
		private int leftInserts, rightInserts;

		public Node() {
			rectangle = new BoundingBox();
		}

		public Node getRoot() {
			return root;
		}

		public Node getLeft() {
			return left;
		}

		public Node getRight() {
			return right;
		}

		public BoundingBox getRectangle() {
			return rectangle;
		}

		public void setRoot(Node root) {
			this.root = root;
		}

		public void setLeft(Node left) {
			this.left = left;
		}

		public void setRight(Node right) {
			this.right = right;
		}

		public void setRectangle(BoundingBox rectangle) {
			this.rectangle = rectangle;
		}

		public void insert(Leaf node) {
			this.rectangle = BoundingBox.union(this.rectangle,
					node.getRectangle());
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
				Vec2 rightCenter = this.getRight().getRectangle().getCenter();
				Vec2 leftCenter = this.getLeft().getRectangle().getCenter();
				Vec2 center = node.getRectangle().getCenter();
				double leftDist = Vec2.diff(leftCenter, center).norm();
				double rightDist = Vec2.diff(rightCenter, center).norm();
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
			}
			if (left != null) {
				left.draw();
			}
			if (right != null) {
				right.draw();
			}
		}

		public void retrieve(BoundingBox rect, List<AbstractDrawAble2D> list) {
			if (!BoundingBox.intersection(this.getRectangle(), rect).isEmpty()) {
				if (this.left != null) {
					this.left.retrieve(rect, list);
				}
				if (this.right != null) {
					this.right.retrieve(rect, list);
				}
			}
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
	}

	public BoxEngine(int width, int height) {
		super(width, height);
		things = new ArrayList<AbstractDrawAble2D>();
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
			if(th.isDestroyed()) {
				things.remove(th);
			}else {
				tree.insert(new Leaf(th));
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

	public void drawTree() {
		tree.draw();
	}

	public void removeAllElements() {
		things.removeAll(things);
	}
}
