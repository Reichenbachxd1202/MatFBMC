public class Encrypt {
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("a.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> GT = pairing.getGT();
        Field<Element> Zp = pairing.getZr();

        Random random = new Random();

        Element g = G0.newRandomElement().getImmutable();
        Element w = G0.newRandomElement().getImmutable();
        Element u1 = G0.newRandomElement().getImmutable();
        Element h1 = G0.newRandomElement().getImmutable();
        Element u2 = G0.newRandomElement().getImmutable();
        Element h2 = G0.newRandomElement().getImmutable();
        Element Msg = GT.newRandomElement().getImmutable();

        Element alpha = Zp.newRandomElement().getImmutable();

        int d = 50;
        Element[] l = new Element[d+1];
        for (int i = 0; i <= d; i++) {
            l[i] = Zp.newRandomElement().getImmutable();
        }

        Element Alpha = pairing.pairing(g, g).powZn(alpha).getImmutable();

        for (int k = 10; k <= 100; k += 10) {
            Element[] R = new Element[k];
            Element[] r = new Element[k];
            for (int i = 0; i < k; i++) {
                R[i] = Zp.newRandomElement().getImmutable();
                r[i] = Zp.newRandomElement().getImmutable();
            }
            for (int kp = 10; kp <= 100; kp += 10) {
                Element[] S    = new Element[kp];
                Element[] tauI = new Element[kp];
                for (int i = 0; i < kp; i++) {
                    S[i]    = Zp.newRandomElement().getImmutable();
                    tauI[i] = Zp.newRandomElement().getImmutable();
                }

                Element tau = Zp.newRandomElement().getImmutable();
                Element s   = Zp.newRandomElement().getImmutable();
                Element chi = Zp.newRandomElement().getImmutable();
                Element t   = Zp.newRandomElement().getImmutable();

                Element F   = G0.newRandomElement().getImmutable();
                Element ek1 = G0.newRandomElement().getImmutable();
                Element ek2 = G0.newRandomElement().getImmutable();
                Element[] ek3 = new Element[kp];
                Element[] ek4 = new Element[kp];
                for (int i = 0; i < kp; i++) {
                    ek3[i] = G0.newRandomElement().getImmutable();
                    ek4[i] = G0.newRandomElement().getImmutable();
                }

                long startTime = System.nanoTime();

                Element C = Msg.mul(Alpha.powZn(s)).getImmutable();
                Element C1 = g.powZn(s).getImmutable();
                Element C2 = u1.powZn(t).mul(h1).powZn(s).getImmutable();

                Element[] C3 = new Element[k];
                Element wNegS = w.powZn(s.negate());
                for (int i = 0; i < k; i++) {
                    Element base = u2.powZn(R[i]).mul(h2);
                    C3[i] = base.powZn(r[i].negate()).mul(wNegS).getImmutable();
                }

                Element[] C4 = new Element[k];
                for (int i = 0; i < k; i++) {
                    C4[i] = g.powZn(r[i]).getImmutable();
                }

                Element[] C5 = new Element[d+1];
                for (int x = 1; x <= d; x++) {
                    C5[x] = g.powZn(s.mul(l[x])).getImmutable();
                }

                Element C6 = g.powZn(chi).getImmutable();
                Element C7 = ek2.mul(g.powZn(tau)).getImmutable();

                Element[] C8 = new Element[kp];
                for (int i = 0; i < kp; i++) {
                    C8[i] = ek3[i].mul(g.powZn(tauI[i])).getImmutable();
                }

                Element gNegTau = g.powZn(tau.negate());
                Element[] C9 = new Element[kp];
                for (int i = 0; i < kp; i++) {
                    Element tmp = u2.powZn(S[i]).mul(h2).powZn(tauI[i]);
                    C9[i] = ek4[i].mul(gNegTau).mul(tmp).getImmutable();
                }

                Element C0 = ek1.mul(w.powZn(tau)).mul(F.powZn(chi)).getImmutable();

                long endTime = System.nanoTime();
                double durationMs = (endTime - startTime) / 1_000_000.0;

                double totalSize = 0;
                totalSize += C.toBytes().length / 1024.0;
                totalSize += C1.toBytes().length / 1024.0;
                totalSize += C2.toBytes().length / 1024.0;
                for (Element e : C3) totalSize += e.toBytes().length / 1024.0;
                for (Element e : C4) totalSize += e.toBytes().length / 1024.0;
                for (int x = 1; x <= d; x++) totalSize += C5[x].toBytes().length / 1024.0;
                totalSize += C6.toBytes().length / 1024.0;
                totalSize += C7.toBytes().length / 1024.0;
                for (Element e : C8) totalSize += e.toBytes().length / 1024.0;
                for (Element e : C9) totalSize += e.toBytes().length / 1024.0;
                totalSize += C0.toBytes().length / 1024.0;

                System.out.printf("=== [k = %d, kp = %d] ===%n", k, kp);
                System.out.printf("Encryption time: %.2f ms%n", durationMs);
                System.out.printf("Ciphertext storage: %.2f KB%n", totalSize);
                System.out.println();
            }
        }
    }
}
