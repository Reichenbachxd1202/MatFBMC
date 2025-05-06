public class DKeyGen {
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> Zp = pairing.getZr();
        Element g = G0.newRandomElement().getImmutable();
        Element w = G0.newRandomElement().getImmutable();
        Element u1 = G0.newRandomElement().getImmutable();
        Element h1 = G0.newRandomElement().getImmutable();
        Element alpha = Zp.newRandomElement().getImmutable();
        Element beta = Zp.newRandomElement().getImmutable();
        Element l0 = Zp.newRandomElement().getImmutable();
        Element t = Zp.newRandomElement().getImmutable();
        Random random = new Random();
        String randomInput = generateRandomString(100, random);
        Element H = hashToZp(randomInput, Zp);
        Element A = g.powZn(alpha).getImmutable();
        Element B = g.powZn(beta).getImmutable();
        Element mu = Zp.newRandomElement().getImmutable();
        Element omega = Zp.newRandomElement().getImmutable();
        int n = 100, l = 100;

        for (int Deep = 2; Deep <= 11; Deep++) {
            System.out.println("Running for l = " + l);
            Element[][] M = new Element[l][n];
            for (int i = 0; i < l; i++) {
                for (int j = 0; j < n; j++) {
                    M[i][j] = Zp.newRandomElement().getImmutable();
                }
            }
            Element[] vec_v = new Element[n];
            vec_v[0] = mu;
            for (int i = 1; i < n; i++) {
                vec_v[i] = Zp.newRandomElement().getImmutable();
            }
            Element[] vec_lam = new Element[l];
            for (int i = 0; i < l; i++) {
                vec_lam[i] = Zp.newZeroElement();
                for (int j = 0; j < n; j++) {
                    vec_lam[i] = vec_lam[i].add(M[i][j].mul(vec_v[j]));
                }
            }
            Element[] rho = new Element[l];
            for (int i = 0; i < l; i++) {
                rho[i] = G0.newRandomElement().getImmutable();
            }
            int N = (int) Math.pow(2, Deep - 1);
            Element[] BT = new Element[N];
            for (int i = 0; i < N; i++) {
                BT[i] = G0.newRandomElement().getImmutable();
            }
            Element[] g_delta = new Element[Deep];
            for (int i = 0; i < Deep; i++) {
                g_delta[i] = G0.newRandomElement().getImmutable();
            }
            Element[][] r = new Element[Deep][l];
            for (int delta = 0; delta < Deep; delta++) {
                for (int i = 0; i < l; i++) {
                    r[delta][i] = Zp.newRandomElement().getImmutable();
                }
            }
            Element[] AK_1 = new Element[Deep];
            Element[][] AK_1_2 = new Element[Deep][l];
            Element[][] AK_1_3 = new Element[Deep][l];
            Element[][] AK_1_4 = new Element[Deep][l];
            long startTime = System.nanoTime();
            Element ck_01 = l0;
            Element ck_02 = g.powZn(mu).getImmutable();
            Element ck_03 = B.powZn(mu.add(omega)).getImmutable();
            Element ck_04 = g.powZn(l0.mul(mu)).getImmutable();
            Element ck_1 = g.powZn(omega).getImmutable();
            Element ck_2 = ck_02.powZn(t).getImmutable();
            for (int i = 0; i < Deep; i++) {
                AK_1[i] = B.powZn(omega).mul(A).div(g_delta[i]).getImmutable();
            }
            for (int delta = 0; delta < Deep; delta++) {
                for (int i = 0; i < l; i++) {
                    AK_1_2[delta][i] = g.powZn(vec_lam[i]).mul(w.powZn(r[delta][i])).getImmutable();
                }
            }
            for (int delta = 0; delta < Deep; delta++) {
                for (int i = 0; i < l; i++) {
                    AK_1_3[delta][i] = g.powZn(r[delta][i]).getImmutable();
                }
            }
            for (int delta = 0; delta < Deep; delta++) {
                for (int i = 0; i < l; i++) {
                    AK_1_4[delta][i] = rho[i].powZn(r[delta][i]).getImmutable();
                }
            }
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            System.out.printf("Computation time for l = %d: %.2f ms%n", l, duration);
            double ckSizeKB = (ck_01.toBytes().length + ck_02.toBytes().length +
                    ck_03.toBytes().length + ck_04.toBytes().length +
                    ck_1.toBytes().length + ck_2.toBytes().length) / 1024.0;
            System.out.printf("Total size of CK for l = %d: %.2f KB%n", l, ckSizeKB);
            double ak1SizeKB = 0;
            for (int i = 0; i < Deep; i++) {
                ak1SizeKB += AK_1[i].toBytes().length / 1024.0;
            }
            double ak2SizeKB = 0;
            double ak3SizeKB = 0;
            double ak4SizeKB = 0;
            double akSizeKB = 0;
            for (int delta = 0; delta < Deep; delta++) {
                for (int i = 0; i < l; i++) {
                    ak2SizeKB += AK_1_2[delta][i].toBytes().length / 1024.0;
                    ak3SizeKB += AK_1_3[delta][i].toBytes().length / 1024.0;
                    ak4SizeKB += AK_1_4[delta][i].toBytes().length / 1024.0;
                    akSizeKB = ak1SizeKB + ak2SizeKB + ak3SizeKB + ak4SizeKB;
                }
            }
            System.out.printf("Total size of AK for l = %d: %.2f KB%n", l, akSizeKB);
        }
    }

    private static String generateRandomString(int length, Random random) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static Element hashToZp(String input, Field<Element> Zp) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        return Zp.newElementFromHash(hash, 0, hash.length).getImmutable();
    }
}
