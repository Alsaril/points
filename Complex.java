public class Complex {

    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public Complex(double re) {
        this(re, 0);
    }

    public Complex() {
        this(0, 0);
    }

    public double re() {
        return re;
    }

    public double im() {
        return im;
    }

    public double qabs() {
        return re * re + im * im;
    }

    public double abs() {
        return Math.sqrt(qabs());
    }

    public double arg() {
        return Math.atan2(im, re);
    }

    @Override
    public String toString() {
        return String.format("%f+%fi", re, im);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Complex)) return false;
        Complex c = (Complex) obj;
        return Complex.sub(this, c).abs() < 0.1;
    }

    public Complex conj() {
        return new Complex(re, -im);
    }

    public Complex pow(int n) {
        final double abs = abs();
        final double arg = arg();
        return Complex.mul(new Complex(Math.cos(n * arg), Math.sin(n * arg)), Math.pow(abs, n));
    }

    public static Complex add(Complex c1, Complex c2) {
        return new Complex(c1.re + c2.re, c1.im + c2.im);
    }

    public static Complex sub(Complex c1, Complex c2) {
        return new Complex(c1.re - c2.re, c1.im - c2.im);
    }

    public static Complex mul(Complex c1, Complex c2) {
        return new Complex(c1.re * c2.re - c1.im * c2.im,
                c1.re * c2.im + c1.im * c2.re);
    }

    public static Complex mul(Complex c, double k) {
        return new Complex(k * c.re, k * c.im);
    }

    public static Complex div(Complex c1, Complex c2) {
        return mul(mul(c1, c2.conj()), 1 / c2.qabs());
    }
}
