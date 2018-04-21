package algorithms;

import numeric.MyMath;

import java.util.Stack;

public class QuickSortWithPermutation implements Sort {
    public static boolean DECREASING_ORDER = true;
    private int[] permutation;
    private int order;

    public QuickSortWithPermutation() {
        this.order = 1;
    }

    /**
     * Creates instance of quick sort with order defined by isReverseOrder param
     *
     * @param isReverseOrder
     */
    public QuickSortWithPermutation(boolean isReverseOrder) {
        this.order = isReverseOrder ? -1 : 1;
    }

    public static void main(String[] args) {
        Integer[] v = {0, 1, 2, 3, 4, 6, 75, 32, 4, 5};
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation();
        quickSortWithPermutation.sort(v);
        int[] permutation = quickSortWithPermutation.getPermutation();
        for (int i = 0; i < v.length; i++) {
            System.out.println(v[i] + " , " + permutation[i]);
        }
    }

    public int[] getPermutation() {
        return permutation;
    }

    @Override
    public <T extends Comparable> void sort(T[] v) {
        int n = v.length;

        this.permutation = new int[n];
        for (int i = 0; i < n; i++) {
            permutation[i] = i;
        }

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        stack.push(n - 1);
        while (stack.size() > 0) {
            int high = stack.pop();
            int low = stack.pop();
            /*
            * partition
            */
            if (low < high) {
                int pivot = (int) (low + Math.floor((high - low) * Math.random()));
                T pvalue = v[pivot];
                swap(v, pivot, high);
                int j = low;
                for (int i = low; i < high; i++) {
                    if (this.order * v[i].compareTo(pvalue) <= 0) {
                        swap(v, i, j);
                        j++;
                    }
                }
                swap(v, j, high);
                stack.push(low);
                stack.push(j - 1);
                stack.push(j + 1);
                stack.push(high);
            }
        }
    }

    private <T extends Comparable> void swap(T[] v, int i, int j) {
        if (i >= 0 && i < v.length && j >= 0 && j < v.length) {
            T temp = v[i];
            v[i] = v[j];
            v[j] = temp;
            int tempP = this.permutation[i];
            this.permutation[i] = this.permutation[j];
            this.permutation[j] = tempP;
        }
    }

    public <T> T[] permutate(T[] array) {
        assert permutation != null : "You must order something in order to have a permutation of that sorting";
        return MyMath.permutate(array, permutation);
    }
}
