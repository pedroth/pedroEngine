package other;


import inputOutput.MyText;

public class ReadCluster {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("no arguments given");
        }

        MyText text = new MyText(args[0]);
        String[] split = text.getText().split("\n");
        int n = 8;
        int k0 = (int) Math.ceil((split.length - 16.0) / 10.0) + 1;
        double[][] data = new double[n][k0];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k0; j++) {
                int index = (8 + i) + 10 * j;
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
