package toolbox;

public class Noise {
    private static double seed = 0;

    private static final Grad[] grad3 = {
            new Grad(1, 1, 0), new Grad(-1, 1, 0), new Grad(1, -1, 0), new Grad(-1, -1, 0),
            new Grad(1, 0, 1), new Grad(-1, 0, 1), new Grad(1, 0, -1), new Grad(-1, 0, -1),
            new Grad(0, 1, 1), new Grad(0, -1, 1), new Grad(0, 1, -1), new Grad(0, -1, -1)
    };

    private static final Grad[] grad4 = {
            new Grad(0, 1, 1, 1), new Grad(0, 1, 1, -1), new Grad(0, 1, -1, 1), new Grad(0, 1, -1, -1),
            new Grad(0, -1, 1, 1), new Grad(0, -1, 1, -1), new Grad(0, -1, -1, 1), new Grad(0, -1, -1, -1),
            new Grad(1, 0, 1, 1), new Grad(1, 0, 1, -1), new Grad(1, 0, -1, 1), new Grad(1, 0, -1, -1),
            new Grad(-1, 0, 1, 1), new Grad(-1, 0, 1, -1), new Grad(-1, 0, -1, 1), new Grad(-1, 0, -1, -1),
            new Grad(1, 1, 0, 1), new Grad(1, 1, 0, -1), new Grad(1, -1, 0, 1), new Grad(1, -1, 0, -1),
            new Grad(-1, 1, 0, 1), new Grad(-1, 1, 0, -1), new Grad(-1, -1, 0, 1), new Grad(-1, -1, 0, -1),
            new Grad(1, 1, 1, 0), new Grad(1, 1, -1, 0), new Grad(1, -1, 1, 0), new Grad(1, -1, -1, 0),
            new Grad(-1, 1, 1, 0), new Grad(-1, 1, -1, 0), new Grad(-1, -1, 1, 0), new Grad(-1, -1, -1, 0)
    };

    private static final short[] p = {
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
            8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117,
            35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71,
            134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41,
            55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89,
            18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226,
            250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
            189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43,
            172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97,
            228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239,
            107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150,
            254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    private static final short[] perm = new short[512];
    private static final short[] permMod12 = new short[512];

    public static void init(final double seed) {
        Noise.seed = seed;

        for (int i = 0; i < 512; i++) {
            Noise.perm[i] = Noise.p[i & 255];
            Noise.permMod12[i] = (short) (Noise.perm[i] % 12);
        }
    }

    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
    private static final double F3 = 1.0 / 3.0;
    private static final double G3 = 1.0 / 6.0;
    private static final double F4 = (Math.sqrt(5.0) - 1.0) / 4.0;
    private static final double G4 = (5.0 - Math.sqrt(5.0)) / 20.0;

    private static int fastFloor(final double x) {
        final int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double dot(final Grad g, final double x, final double y) {
        return g.x * x + g.y * y;
    }

    private static double dot(final Grad g, final double x, final double y, final double z) {
        return g.x * x + g.y * y + g.z * z;
    }

    private static double dot(final Grad g, final double x, final double y, final double z, final double w) {
        return g.x * x + g.y * y + g.z * z + g.w * w;
    }

    public static double noise(double xin, double yin) {
        final double n0, n1, n2;

        xin += Noise.seed;
        yin += Noise.seed;

        final double s = (xin + yin) * Noise.F2;
        final int i = Noise.fastFloor(xin + s);
        final int j = Noise.fastFloor(yin + s);
        final double t = (i + j) * Noise.G2;
        final double X0 = i - t;
        final double Y0 = j - t;
        final double x0 = xin - X0;
        final double y0 = yin - Y0;

        final int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        final double x1 = x0 - i1 + Noise.G2;
        final double y1 = y0 - j1 + Noise.G2;
        final double x2 = x0 - 1.0 + 2.0 * Noise.G2;
        final double y2 = y0 - 1.0 + 2.0 * Noise.G2;

        final int ii = i & 255;
        final int jj = j & 255;
        final int gi0 = Noise.permMod12[ii + Noise.perm[jj]];
        final int gi1 = Noise.permMod12[ii + i1 + Noise.perm[jj + j1]];
        final int gi2 = Noise.permMod12[ii + 1 + Noise.perm[jj + 1]];

        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * Noise.dot(Noise.grad3[gi0], x0, y0);
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * Noise.dot(Noise.grad3[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * Noise.dot(Noise.grad3[gi2], x2, y2);
        }

        return 70.0 * (n0 + n1 + n2);
    }


    // 3D simplex noise
    public static double noise(double xin, double yin, double zin) {
        final double n0, n1, n2, n3;

        xin += Noise.seed;
        yin += Noise.seed;
        zin += Noise.seed;

        final double s = (xin + yin + zin) * Noise.F3;
        final int i = Noise.fastFloor(xin + s);
        final int j = Noise.fastFloor(yin + s);
        final int k = Noise.fastFloor(zin + s);
        final double t = (i + j + k) * Noise.G3;
        final double X0 = i - t;
        final double Y0 = j - t;
        final double Z0 = k - t;
        final double x0 = xin - X0;
        final double y0 = yin - Y0;
        final double z0 = zin - Z0;

        final int i1, j1, k1, i2, j2, k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        final double x1 = x0 - i1 + Noise.G3;
        final double y1 = y0 - j1 + Noise.G3;
        final double z1 = z0 - k1 + Noise.G3;
        final double x2 = x0 - i2 + 2.0 * Noise.G3;
        final double y2 = y0 - j2 + 2.0 * Noise.G3;
        final double z2 = z0 - k2 + 2.0 * Noise.G3;
        final double x3 = x0 - 1.0 + 3.0 * Noise.G3;
        final double y3 = y0 - 1.0 + 3.0 * Noise.G3;
        final double z3 = z0 - 1.0 + 3.0 * Noise.G3;

        final int ii = i & 255;
        final int jj = j & 255;
        final int kk = k & 255;
        final int gi0 = Noise.permMod12[ii + Noise.perm[jj + Noise.perm[kk]]];
        final int gi1 = Noise.permMod12[ii + i1 + Noise.perm[jj + j1 + Noise.perm[kk + k1]]];
        final int gi2 = Noise.permMod12[ii + i2 + Noise.perm[jj + j2 + Noise.perm[kk + k2]]];
        final int gi3 = Noise.permMod12[ii + 1 + Noise.perm[jj + 1 + Noise.perm[kk + 1]]];

        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * Noise.dot(Noise.grad3[gi0], x0, y0, z0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * Noise.dot(Noise.grad3[gi1], x1, y1, z1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * Noise.dot(Noise.grad3[gi2], x2, y2, z2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * Noise.dot(Noise.grad3[gi3], x3, y3, z3);
        }

        return 32.0 * (n0 + n1 + n2 + n3);
    }


    // 4D simplex noise, better simplex rank ordering method 2012-03-09
    public static double noise(double x, double y, double z, double w) {
        final double n0;  // Noise contributions from the five corners
        final double n1;
        final double n2;
        final double n3;
        final double n4;

        x += Noise.seed;
        y += Noise.seed;
        z += Noise.seed;
        w += Noise.seed;

        // Skew the (x,y,z,w) space to determine which cell of 24 simplices we're in
        final double s = (x + y + z + w) * Noise.F4; // Factor for 4D skewing
        final int i = Noise.fastFloor(x + s);
        final int j = Noise.fastFloor(y + s);
        final int k = Noise.fastFloor(z + s);
        final int l = Noise.fastFloor(w + s);
        final double t = (i + j + k + l) * Noise.G4; // Factor for 4D unskewing
        final double X0 = i - t; // Unskew the cell origin back to (x,y,z,w) space
        final double Y0 = j - t;
        final double Z0 = k - t;
        final double W0 = l - t;
        final double x0 = x - X0;  // The x,y,z,w distances from the cell origin
        final double y0 = y - Y0;
        final double z0 = z - Z0;
        final double w0 = w - W0;
        // For the 4D case, the simplex is a 4D shape I won't even try to describe.
        // To find out which of the 24 possible simplices we're in, we need to
        // determine the magnitude ordering of x0, y0, z0 and w0.
        // Six pair-wise comparisons are performed between each possible pair
        // of the four coordinates, and the results are used to rank the numbers.
        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        if (x0 > y0) {
            rankx++;
        } else {
            ranky++;
        }
        if (x0 > z0) {
            rankx++;
        } else {
            rankz++;
        }
        if (x0 > w0) {
            rankx++;
        } else {
            rankw++;
        }
        if (y0 > z0) {
            ranky++;
        } else {
            rankz++;
        }
        if (y0 > w0) {
            ranky++;
        } else {
            rankw++;
        }
        if (z0 > w0) {
            rankz++;
        } else {
            rankw++;
        }
        final int i1;  // The integer offsets for the second simplex corner
        final int j1;
        final int k1;
        final int l1;
        final int i2;  // The integer offsets for the third simplex corner
        final int j2;
        final int k2;
        final int l2;
        final int i3;  // The integer offsets for the fourth simplex corner
        final int j3;
        final int k3;
        final int l3;
        // simplex[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some order.
        // Many values of c will never occur, since e.g. x>y>z>w makes x<z, y<w and x<w
        // impossible. Only the 24 indices which have non-zero entries make any sense.
        // We use a thresholding to set the coordinates in turn from the largest magnitude.
        // Rank 3 denotes the largest coordinate.
        i1 = rankx >= 3 ? 1 : 0;
        j1 = ranky >= 3 ? 1 : 0;
        k1 = rankz >= 3 ? 1 : 0;
        l1 = rankw >= 3 ? 1 : 0;
        // Rank 2 denotes the second largest coordinate.
        i2 = rankx >= 2 ? 1 : 0;
        j2 = ranky >= 2 ? 1 : 0;
        k2 = rankz >= 2 ? 1 : 0;
        l2 = rankw >= 2 ? 1 : 0;
        // Rank 1 denotes the second smallest coordinate.
        i3 = rankx >= 1 ? 1 : 0;
        j3 = ranky >= 1 ? 1 : 0;
        k3 = rankz >= 1 ? 1 : 0;
        l3 = rankw >= 1 ? 1 : 0;
        // The fifth corner has all coordinate offsets = 1, so no need to compute that.
        final double x1 = x0 - i1 + Noise.G4; // Offsets for second corner in (x,y,z,w) coords
        final double y1 = y0 - j1 + Noise.G4;
        final double z1 = z0 - k1 + Noise.G4;
        final double w1 = w0 - l1 + Noise.G4;
        final double x2 = x0 - i2 + 2.0 * Noise.G4; // Offsets for third corner in (x,y,z,w) coords
        final double y2 = y0 - j2 + 2.0 * Noise.G4;
        final double z2 = z0 - k2 + 2.0 * Noise.G4;
        final double w2 = w0 - l2 + 2.0 * Noise.G4;
        final double x3 = x0 - i3 + 3.0 * Noise.G4; // Offsets for fourth corner in (x,y,z,w) coords
        final double y3 = y0 - j3 + 3.0 * Noise.G4;
        final double z3 = z0 - k3 + 3.0 * Noise.G4;
        final double w3 = w0 - l3 + 3.0 * Noise.G4;
        final double x4 = x0 - 1.0 + 4.0 * Noise.G4; // Offsets for last corner in (x,y,z,w) coords
        final double y4 = y0 - 1.0 + 4.0 * Noise.G4;
        final double z4 = z0 - 1.0 + 4.0 * Noise.G4;
        final double w4 = w0 - 1.0 + 4.0 * Noise.G4;
        // Work out the hashed gradient indices of the five simplex corners
        final int ii = i & 255;
        final int jj = j & 255;
        final int kk = k & 255;
        final int ll = l & 255;
        final int gi0 = Noise.perm[ii + Noise.perm[jj + Noise.perm[kk + Noise.perm[ll]]]] % 32;
        final int gi1 = Noise.perm[ii + i1 + Noise.perm[jj + j1 + Noise.perm[kk + k1 + Noise.perm[ll + l1]]]] % 32;
        final int gi2 = Noise.perm[ii + i2 + Noise.perm[jj + j2 + Noise.perm[kk + k2 + Noise.perm[ll + l2]]]] % 32;
        final int gi3 = Noise.perm[ii + i3 + Noise.perm[jj + j3 + Noise.perm[kk + k3 + Noise.perm[ll + l3]]]] % 32;
        final int gi4 = Noise.perm[ii + 1 + Noise.perm[jj + 1 + Noise.perm[kk + 1 + Noise.perm[ll + 1]]]] % 32;
        // Calculate the contribution from the five corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * Noise.dot(Noise.grad4[gi0], x0, y0, z0, w0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * Noise.dot(Noise.grad4[gi1], x1, y1, z1, w1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * Noise.dot(Noise.grad4[gi2], x2, y2, z2, w2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * Noise.dot(Noise.grad4[gi3], x3, y3, z3, w3);
        }
        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 < 0) {
            n4 = 0.0;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * Noise.dot(Noise.grad4[gi4], x4, y4, z4, w4);
        }
        // Sum up and scale the result to cover the range [-1,1]
        return 27.0 * (n0 + n1 + n2 + n3 + n4);
    }

    private static class Grad {
        double x, y, z, w;

        Grad(final double x, final double y, final double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Grad(final double x, final double y, final double z, final double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }
}
