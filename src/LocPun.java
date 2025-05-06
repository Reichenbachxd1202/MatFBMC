public class LocPun {
    public static void main(String[] args) {
        Pairing pairing = PairingFactory.getPairing("e.properties");
        Field G0 = pairing.getG1();
        Field Zp = pairing.getZr();
        Element g = G0.newRandomElement().getImmutable();

        Element beta = Zp.newRandomElement().getImmutable();
        Element l_0 = Zp.newRandomElement().getImmutable();
        Element g_beta = g.powZn(beta).getImmutable();
        int d = 100;

        Element[] q = new Element[d + 1];
        q[0] = beta;
        for (int i = 1; i <= d; i++) {
            q[i] = Zp.newRandomElement().getImmutable();
        }

        Element omega = Zp.newRandomElement().getImmutable();
        Element t = Zp.newRandomElement().getImmutable();
        Element ck_01 = g.powZn(omega).getImmutable();
        Element ck_02 = l_0.getImmutable();
        Element ck_03 = g_beta.powZn(omega.add(t)).getImmutable();

        Element q_l_0 = Zp.newZeroElement().getImmutable();
        Element l_0_pow = Zp.newOneElement().getImmutable();
        for (int i = 0; i <= d; i++) {
            q_l_0 = q_l_0.add(q[i].mul(l_0_pow)).getImmutable();
            l_0_pow = l_0_pow.mul(l_0).getImmutable();
        }
        Element ck_04 = g.powZn(omega.mul(q_l_0)).getImmutable();
        Element delta = Zp.newRandomElement().getImmutable();
        Element y_0 = Zp.newRandomElement().getImmutable();

        for (int x = 0; x <= 100; x += 10) {
            long startTime = System.nanoTime();

            Element ck_01_pr = g.powZn(y_0).mul(ck_01).getImmutable();
            Element ck_02_pr = ck_02.getImmutable();
            Element ck_03_pr = g_beta.powZn(y_0.sub(delta)).mul(ck_03).getImmutable();
            Element ck_04_pr = g.powZn(q_l_0).powZn(y_0).mul(ck_04).getImmutable();

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            System.out.printf("Computation time for x = %d: %.2f ms%n", x, duration);

            System.out.println("CK_pr = {");
            System.out.println("  ck_01_pr: " + ck_01_pr);
            System.out.println("  ck_02_pr: " + ck_02_pr);
            System.out.println("  ck_03_pr: " + ck_03_pr);
            System.out.println("  ck_04_pr: " + ck_04_pr);
            System.out.println("}");

            int storageCK = ck_01_pr.toBytes().length + ck_02_pr.toBytes().length + ck_03_pr.toBytes().length + ck_04_pr.toBytes().length;
            System.out.println("CK_pr storage size: " + storageCK + " bytes");
        }
    }
}
