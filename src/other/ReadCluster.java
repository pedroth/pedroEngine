package other;


import inputOutput.MyText;

public class ReadCluster {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("no arguments given");
        }

        if (args[1] == null || "".equals(args[1])) {
            System.out.println("second argument must be a number between 1 and n");
        }

        MyText text = new MyText(args[0]);
        String[] split = text.getText().split("\n");

        //special vars
        int n0 = 8;
        int step = 2;
        int n = Integer.valueOf(args[1]);
        int alpha = n + step;
        int k0 = (int) Math.ceil((split.length - n0) / alpha) + 1;
        double[][] data = new double[n][k0];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k0; j++) {
                int index = (n0 + i) + alpha * j;
                String[] doubleSplit = split[index].split("\\s");
                data[i][j] = Double.parseDouble(doubleSplit[doubleSplit.length - 1]);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k0; j++) {
                stringBuilder.append(data[i][j] + ((j == k0 - 1) ? "" : ","));
            }
            stringBuilder.append("\n");
        }
        text.write("out.csv", stringBuilder.toString());
    }
}
