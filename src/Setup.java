public class Setup {
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("e.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> Zp = pairing.getZr();
        Element g = G0.newRandomElement().getImmutable();
        int U = 100, Deep = 5;
        long kappa = (long) Math.pow(2, 80);
        Element w = G0.newRandomElement().getImmutable();
        Element u1 = G0.newRandomElement().getImmutable();
        Element h1 = G0.newRandomElement().getImmutable();
        Element u2 = G0.newRandomElement().getImmutable();
        Element h2 = G0.newRandomElement().getImmutable();
        Element alpha = Zp.newRandomElement().getImmutable();
        Element beta = Zp.newRandomElement().getImmutable();
        Element l0 = Zp.newRandomElement().getImmutable();
        Random random = new Random();
        String randomInput = generateRandomString(100, random);
        Element H = hashToZp(randomInput, Zp);
        Element H1 = hashToG0(randomInput, G0);
        Element H2 = hashToG0(randomInput, G0);

        for (int d = 10; d <= 100; d += 10) {
            Element[] coefficients = new Element[d + 1];
            coefficients[0] = beta;
            for (int i = 1; i <= d; i++) {
                coefficients[i] = Zp.newRandomElement().getImmutable();
            }

            int N = (int) Math.pow(2, Deep - 1);
            long startTime = System.nanoTime();
            Element[] L = new Element[d];
            for (int i = 1; i <= d; i++) {
                Element q_i = computePolynomial(coefficients, Zp.newElement(i).getImmutable(), Zp);
                L[i - 1] = g.powZn(q_i).getImmutable();
            }

            Element egg_Alpha = pairing.pairing(g, g).powZn(alpha).getImmutable();
            Element g_Beta = g.powZn(beta).getImmutable();

            Element[] RL = new Element[N];
            Element[] BT = new Element[2 * N - 1];
            for (int i = 0; i < N - 1; i++) {
                BT[i] = G0.newRandomElement().getImmutable();
            }

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            System.out.printf("Setup computation time: %.2f ms%n", duration);

            double ppSizeKB = (w.toBytes().length +
                    u1.toBytes().length +
                    h1.toBytes().length +
                    u2.toBytes().length +
                    h2.toBytes().length +
                    g.toBytes().length +
                    egg_Alpha.toBytes().length +
                    g_Beta.toBytes().length +
                    l0.toBytes().length) / 1024.0;

            for (int i = 0; i < d; i++) {
                ppSizeKB += L[i].toBytes().length / 1024.0;
            }
            System.out.printf("Total size of Public parameters (pp): %.2f KB%n", ppSizeKB);

            int mskSize = alpha.toBytes().length +
                    beta.toBytes().length;

            double mskSizeKB = (alpha.toBytes().length +
                    beta.toBytes().length) / 1024.0;

            for (int i = 0; i < N - 1; i++) {
                mskSizeKB += BT[i].toBytes().length / 1024.0;
            }
            System.out.printf("Total size of Master secret key (msk): %.2f KB%n", mskSizeKB);
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

    private static Element hashToG0(String input, Field<Element> G0) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        return G0.newElementFromHash(hash, 0, hash.length).getImmutable();
    }

    private static Element computePolynomial(Element[] coefficients, Element x, Field<Element> Zp) {
        Element result = Zp.newZeroElement();
        Element power = Zp.newOneElement();
        for (Element coefficient : coefficients) {
            result = result.add(coefficient.duplicate().mul(power));
            power = power.mul(x);
        }
        return result.getImmutable();
    }
}
