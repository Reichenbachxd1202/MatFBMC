public class OutPun {
    public static void main(String[] args) {
        Pairing pairing = PairingFactory.getPairing("d159.properties");
        Field<Element> G0 = pairing.getG1();
        Field<Element> G1 = pairing.getGT();
        Field<Element> Zp = pairing.getZr();

        Element g = G0.newRandomElement().getImmutable();
        Element beta = Zp.newRandomElement().getImmutable();
        Element gamma = Zp.newRandomElement().getImmutable();

        Element Beta = g.powZn(beta).getImmutable();
        Element ck = Beta.powZn(gamma).getImmutable();

        int d = 100;
        Element[] coeffs = new Element[d + 1];
        coeffs[0] = beta;
        for (int j = 1; j <= d; j++) {
            coeffs[j] = Zp.newRandomElement().getImmutable();
        }

        for (int x = 10; x <= 100; x += 10) {
            Element[] l = new Element[x];
            Element[] z = new Element[x];
            Element[] y1 = new Element[x];
            for (int i = 1; i <= x; i++) {
                l[i - 1] = Zp.newRandomElement().getImmutable();
                z[i - 1] = Zp.newRandomElement().getImmutable();
                y1[i - 1] = Zp.newRandomElement().getImmutable();
            }

            long start = System.nanoTime();

            for (int i = 1; i <= x; i++) {
                Element li = l[i - 1];
                Element zi = z[i - 1];
                Element y1i = y1[i - 1];

                Element qi = Zp.newZeroElement();
                for (int j = 0; j <= d; j++) {
                    Element exponent = Zp.newElement(j);
                    Element term = coeffs[j].mul(li.powZn(exponent));
                    qi = qi.add(term);
                }

                Element iElem = Zp.newElement(i);
                Element numerator = y1i.mul(zi);
                Element exponent = numerator.div(iElem);

                Element ck1 = li.getImmutable();
                Element ck2 = g.powZn(exponent).getImmutable();
                Element term1 = Beta.powZn(exponent);
                Element term2 = ck.powZn(zi.div(iElem));
                Element ck3 = term1.mul(term2).getImmutable();
                Element g_qi = g.powZn(qi);
                Element ck4 = g_qi.powZn(exponent).getImmutable();
            }

            long end = System.nanoTime();
            double durationMs = (end - start) / 1e6;
            System.out.println("x = " + x + ", Time cost = " + durationMs + " ms");
        }
    }
}
