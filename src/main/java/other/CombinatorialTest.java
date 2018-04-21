package other;

import utils.StopWatch;

public class CombinatorialTest {

    public static void main(String[] args) {
        int n = 100;
        int nn = n * n;
        int nnn = n * n * n;
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    System.out.println(i + " : " + j + " : " + k);
                }
            }
        }
        double t1 = stopWatch.getEleapsedTime();
        System.out.println("=========================");
        stopWatch.resetTime();
        for (int index = 0; index < nnn; index++) {
            int i = index % n;
            int j = index % nn / n;
            int k = index % nnn / nn;
            System.out.println(i + " : " + j + " : " + k);
        }
        double t2 = stopWatch.getEleapsedTime();
        System.out.println(t1 / t2);
//
//        final List<String> bmp = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Thesis/figs/results/", "bmp");
//        for (String image : bmp) {
//            System.out.println("magick " + image.replace("\\", "/") + " " + image.replace("\\", "/").replace(".bmp", ".png"));
//        }
    }
}
