public class EKeyGen {
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("d224.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> G1 = pairing.getG2();
        Field<Element> Zp = pairing.getZr();

        Element g = G0.newRandomElement().getImmutable();
        Element w = G0.newRandomElement().getImmutable();
        Element u2 = G0.newRandomElement().getImmutable();
        Element h2 = G0.newRandomElement().getImmutable();

        Element alpha = Zp.newRandomElement().getImmutable();
        Element T = g.powZn(alpha).getImmutable();

        Random random = new Random();

        for (int k = 10; k <= 100; k += 10) {
            for (int Deep = 1; Deep <= 10; Deep++) {
                String randomInput = generateRandomString(100, random);
                Element H2 = hashToZp(randomInput, G0);

                Element[] g_i = new Element[Deep];
                Element[] gpr_i = new Element[Deep];
                for (int delta = 0; delta < Deep; delta++) {
                    g_i[delta] = G0.newRandomElement().getImmutable();
                    gpr_i[delta] = T.div(g_i[delta]).getImmutable();
                }

                Element[] S = new Element[k];
                for (int i = 0; i < k; i++) {
                    S[i] = Zp.newRandomElement().getImmutable();
                }

                Element tau = Zp.newRandomElement().getImmutable();
                Element[] tau_i = new Element[k];
                for (int i = 0; i < k; i++) {
                    tau_i[i] = Zp.newRandomElement().getImmutable();
                }

                long startTime = System.nanoTime();

                Element T1 = w.powZn(tau).getImmutable();
                Element[] ek1 = new Element[Deep];
                for (int delta = 0; delta < Deep; delta++) {
                    ek1[delta] = T1.mul(gpr_i[delta]).getImmutable();
                }

                Element ek2 = g.powZn(tau).getImmutable();

                Element[] ek3 = new Element[k];
                for (int i = 0; i < k; i++) {
                    ek3[i] = g.powZn(tau_i[i]).getImmutable();
                }

                Element g_neg_tau = g.powZn(tau.negate()).getImmutable();
                Element[] ek4 = new Element[k];
                for (int i = 0; i < k; i++) {
                    Element temp = u2.powZn(S[i]).mul(h2).powZn(tau_i[i]);
                    ek4[i] = temp.mul(g_neg_tau).getImmutable();
                }

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / 1_000_000.0;

                double ek1Size = 0;
                for (Element e : ek1) ek1Size += e.toBytes().length / 1024.0;
                double ek2Size = ek2.toBytes().length / 1024.0;
                double ek3Size = 0;
                for (Element e : ek3) ek3Size += e.toBytes().length / 1024.0;
                double ek4Size = 0;
                for (Element e : ek4) ek4Size += e.toBytes().length / 1024.0;
                double totalSize = ek1Size + ek2Size + ek3Size + ek4Size;

                System.out.printf("=== [k = %d, Deep = %d] ===%n", k, Deep);
                System.out.printf("EKeyGen computation time: %.2f ms%n", duration);
                System.out.printf("Total storage: %.2f KB%n", totalSize);
                System.out.println();
            }
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
