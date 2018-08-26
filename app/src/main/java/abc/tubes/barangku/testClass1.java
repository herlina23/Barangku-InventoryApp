package abc.tubes.barangku;

/**
 * Created by User on 7/29/2018.
 */

public class testClass1 {

    static int sumOf(int a, int b) {
        return a+b;
    }

    static int sumOf(int a, int b, int c) {
        return a+b+c;
    }

    static double sumOf(double a, double b) {
        return a+b;
    }

    static double sumOf(double a, double b, double c) {
        return a+b+c;
    }
    public static void main(String[] args) {
        System.out.println(sumOf(1,2));
        System.out.println(sumOf(10d,20d,30d));
    }
}
