import java.util.*;

public class Comb {

    public static List<Integer> bitprint(int u) {
        List<Integer> result = new ArrayList<>();
        for (int n = 0; u > 0; ++n, u >>= 1)
            if ((u & 1) > 0) result.add(n + 1);
        return result;
    }

    public static int bitcount(int u) {
        int n;
        for (n = 0; u > 0; ++n, u &= (u - 1)) ;
        return n;
    }

    public static List<List<Integer>> comb(int c, int n) {
        List<List<Integer>> s = new ArrayList<>();
        for (int u = 0; u < 1 << n; u++)
            if (bitcount(u) == c) s.add(bitprint(u));
        return s;
    }
}