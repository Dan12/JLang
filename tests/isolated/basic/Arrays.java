import placeholder.Print;

public class Arrays {
    public static void main(String[] args){
        int[] a = new int[10];

        a[5] = 9999999;

        for (int i=0; i<a.length; i++){
            Print.println(a[i]);
            a[i] = i*2;
        }

        for (int i=0; i<a.length; i++){
            Print.println(a[i]);
        }

        a = new int[]{0, 1, 2, 3, 4};

        for (int i=0; i<a.length; i++){
            Print.println(a[i]);
        }
    }
}