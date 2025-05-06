public class Decrypt {

    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Field<Element> G = pairing.getG1();
        Field<Element> GT = pairing.getGT();
        Field<Element> Zp = pairing.getZr();

        Element ck_02_pr = G.newRandomElement().getImmutable();
        Element ck_03_pr = G.newRandomElement().getImmutable();
        Element ck_04_pr = G.newRandomElement().getImmutable();

        Element Q = GT.newRandomElement().getImmutable();
        Element D_1 = GT.newRandomElement().getImmutable();

        Element V = Zp.newRandomElement().getImmutable();

        for (int d = 10; d <= 100; d += 10) {
            Element[] C_5 = new Element[d];
            for (int i = 0; i < d; i++) {
                C_5[i] = G.newRandomElement().getImmutable();
            }

            Element phi_star = Zp.newRandomElement().getImmutable();

            Element[] phi = new Element[d];
            for (int i = 0; i < d; i++) {
                phi[i] = Zp.newRandomElement().getImmutable();
            }

            Element C_1 = G.newRandomElement().getImmutable();
            Element C_2 = G.newRandomElement().getImmutable();
            Element ck_1 = G.newRandomElement().getImmutable();
            Element ck_2 = G.newRandomElement().getImmutable();

            long startTime = System.nanoTime();

            Element X = pairing.pairing(ck_03_pr, C_1).getImmutable();
            Element W = pairing.pairing(ck_04_pr, C_1).powZn(phi_star).getImmutable();

            Element[] Y_5 = new Element[d];
            for (int i = 0; i < d; i++) {
                Y_5[i] = C_5[i].powZn(phi[i]).getImmutable();
            }

            Element product_Y = G.newOneElement().getImmutable();
            for (Element y : Y_5) {
                product_Y = product_Y.mul(y).getImmutable();
            }
            Element Y = pairing.pairing(ck_02_pr, product_Y).getImmutable();

            Element D_2 = X.div(W.mul(Y)).getImmutable();
            Element D_1_V = D_1.powZn(V.invert()).getImmutable();

            Element pair1 = pairing.pairing(C_1, ck_2).getImmutable();
            Element pair2 = pairing.pairing(C_2, ck_1).getImmutable();

            Element M = Q.mul(D_1_V).mul(D_2).mul(pair1.div(pair2)).getImmutable();

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            System.out.printf("Computation time for d = %d: %.2f ms%n", d, duration);

            int storageCost = M.toBytes().length;
            System.out.printf("Storage cost of M for d = %d: %d bytes%n%n", d, storageCost);
        }
    }
}
