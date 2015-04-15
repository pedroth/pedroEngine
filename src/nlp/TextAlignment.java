package nlp;

import algebra.Vec2;
import algebra.Vec3;

/**
 * Implements Needleman–Wunsch algorithm for text alignment
 * 
 * @author pedro
 * 
 */
public class TextAlignment {
	/**
	 * who cares if stores double instead of integers
	 */
	private Vec2[] alignment;

	public TextAlignment() {
		/* empty on purpose */
	}

	public Vec2[] align(String[] t1, String[] t2) {
		/**
		 * dynamic programming matrix, it records the score of the alignment
		 * until position (i,j)
		 */
		int[][] fMat = new int[t2.length + 1][t1.length + 1];
		// % 1 - diagonal
		// % 2 - left
		// % 3 - up
		int[][] auxMat = new int[t2.length + 1][t1.length + 1];
		int d = 1;

		for (int i = 0; i < fMat.length; i++) {
			fMat[i][0] = -(i - 1);
			auxMat[i][0] = 3;
		}

		for (int j = 0; j < fMat[0].length; j++) {
			fMat[0][j] = -(j - 1);
			auxMat[0][j] = 2;
		}

		for (int i = 1; i < auxMat.length; i++) {
			for (int j = 1; j < auxMat[0].length; j++) {
				int score = t2[i - 1].equals(t1[j - 1]) ? 1 : -1;
				Vec2 valInd = TriMax(new Vec3(fMat[i - 1][j - 1] + score, fMat[i][j - 1] - d, fMat[i - 1][j] - d));
				/**
				 * don't worry with the cast since we always work with integers
				 */
				fMat[i][j] = (int) valInd.getX();
				auxMat[i][j] = (int) valInd.getY();
			}
		}

		alignment = new Vec2[Math.max(t1.length, t2.length)];
		int i = fMat.length - 1;
		int j = fMat[0].length - 1;
		int index = alignment.length - 1;
		while (i > 0 || j > 0) {
			int arrow = auxMat[i][j];
			/**
			 * diagonal
			 */
			if (arrow == 1) {
				alignment[index--] = new Vec2(j - 1, i - 1);
				i--;
				j--;
			}
			/**
			 * left
			 */
			else if (arrow == 2) {
				alignment[index--] = new Vec2(j - 1, i);
				j--;
			}
			/**
			 * up
			 */
			else {
				alignment[index--] = new Vec2(j, i - 1);
				i--;
			}
		}
		return alignment;
	}

	/**
	 * 
	 * @param v
	 * @return max of 3 elements, the max value is on the first coordinate of
	 *         the Vec2 and the index on the second coordinate. If some of the
	 *         elements are equal return the index of first maximum. The
	 *         returned index starts at 1.
	 */
	private Vec2 TriMax(Vec3 v) {
		double val = Double.MIN_VALUE;
		int index = -1;
		for (int i = 1; i <= v.getDim(); i++) {
			if (val < v.getX(i)) {
				val = v.getX(i);
				index = i;
			}
		}
		return new Vec2(val, index);
	}
}
