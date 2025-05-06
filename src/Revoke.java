public class Revoke {
    public static void main(String[] args) {
        Pairing pairing = PairingFactory.getPairing("e.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> G1 = pairing.getG2();
        Field<Element> Zp = pairing.getZr();

        Element g1 = G0.newRandomElement().getImmutable();
        Element g2 = G1.newRandomElement().getImmutable();

        for (int Deep = 2; Deep <= 11;  Deep += 1) {
            System.out.println("Running for Deep = " + Deep);
            int N = (int) Math.pow(2, Deep - 1);
            List<String> RL = new ArrayList<>(N * Deep);

            Element[] gid = new Element[N];
            Element[][] g_delta = new Element[N][Deep];

            for (int i = 0; i < N; i++) {
                gid[i] = Zp.newRandomElement().getImmutable();
                for (int delta = 0; delta < Deep; delta++) {
                    g_delta[i][delta] = G0.newRandomElement().getImmutable();
                }
            }

            Element t = Zp.newRandomElement().getImmutable();
            long startTime = System.nanoTime();

            for (int i = 0; i < N; i++) {
                for (int delta = 0; delta < Deep; delta++) {
                    String tuple = String.format("[gid: %s, t: %s, g_delta: %s]",
                            gid[i].toString(), t.toString(), g_delta[i][delta].toString());
                    RL.add(tuple);
                }
            }

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            System.out.printf("Computation time: %.2f ms%n", duration);

            System.out.println("RL contents:");
            double totalSizeKB = 0.0;
            for (String record : RL) {
                totalSizeKB += record.getBytes().length / 1024.0;
            }

            System.out.printf("Total storage overhead: %.2f KB%n", totalSizeKB);
        }
    }
}
