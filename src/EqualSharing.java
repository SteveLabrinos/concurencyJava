import java.util.stream.IntStream;


public class EqualSharing {
    public static final int TOTAL_SHARE = 7;
    public static final int NUM_OF_OBJ = 3;

    private int num;

    public EqualSharing (int share) {
        this.num = share;
    }

    public int getNum () { return num; }

    public static void main (String[] args) {
        EqualSharing[] objs = new EqualSharing[NUM_OF_OBJ];
        IntStream.range(0, objs.length).forEach(i -> {
            int size = (i + 1) * TOTAL_SHARE / NUM_OF_OBJ - (i * TOTAL_SHARE) / NUM_OF_OBJ;
            objs[i] = new EqualSharing(size);
        });

        int sum = 0;
        for (EqualSharing o : objs) {
            sum += o.getNum();
            System.out.println("Obj num = " + o.getNum());
        }

        System.out.println("Total sum = " + sum);

    }
}
