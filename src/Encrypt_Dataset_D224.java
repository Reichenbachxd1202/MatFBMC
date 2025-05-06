public class Encrypt_Dataset_D224 {
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("d224.properties");
        Field<Element> G1 = pairing.getG1();
        Field<Element> G2 = pairing.getG2();
        Field<Element> GT = pairing.getGT();
        Field<Element> Zp = pairing.getZr();

        Element g1 = G1.newRandomElement().getImmutable();
        Element w1 = G1.newRandomElement().getImmutable();
        Element u1 = G1.newRandomElement().getImmutable();
        Element h1 = G1.newRandomElement().getImmutable();

        Element g2 = G2.newRandomElement().getImmutable();
        Element w2 = G2.newRandomElement().getImmutable();
        Element u2 = G2.newRandomElement().getImmutable();
        Element h2 = G2.newRandomElement().getImmutable();

        Element alpha = Zp.newRandomElement().getImmutable();
        Element vartheta = Zp.newRandomElement().getImmutable();
        Element Alpha = pairing.pairing(g1, g2).powZn(alpha).getImmutable();

        Element C1 = g1.powZn(vartheta).getImmutable();
        Element C2 = u1.powZn(vartheta).mul(h1).powZn(vartheta).getImmutable();

        int k = 50;
        int kp = 50;
        int d = 50;

        Element[] R = new Element[k];
        Element[] r = new Element[k];
        for (int i = 0; i < k; i++) {
            R[i] = Zp.newRandomElement().getImmutable();
            r[i] = Zp.newRandomElement().getImmutable();
        }

        Element[] S = new Element[kp];
        Element[] tauI = new Element[kp];
        for (int i = 0; i < kp; i++) {
            S[i] = Zp.newRandomElement().getImmutable();
            tauI[i] = Zp.newRandomElement().getImmutable();
        }

        Element tau = Zp.newRandomElement().getImmutable();
        Element chi = Zp.newRandomElement().getImmutable();

        Element F = G1.newRandomElement().getImmutable();
        Element ek1 = G1.newRandomElement().getImmutable();
        Element ek2 = G1.newRandomElement().getImmutable();
        Element[] ek3 = new Element[kp];
        Element[] ek4 = new Element[kp];
        for (int i = 0; i < kp; i++) {
            ek3[i] = G1.newRandomElement().getImmutable();
            ek4[i] = G2.newRandomElement().getImmutable();
        }

        Element[] C3 = new Element[k];
        Element w2NegS = w2.powZn(vartheta.negate());
        for (int i = 0; i < k; i++) {
            Element base = u2.powZn(R[i]).mul(h2);
            C3[i] = base.powZn(r[i].negate()).mul(w2NegS).getImmutable();
        }

        Element[] C4 = new Element[k];
        for (int i = 0; i < k; i++) {
            C4[i] = g1.powZn(r[i]).getImmutable();
        }

        Element[] l = new Element[d + 1];
        for (int i = 0; i <= d; i++) {
            l[i] = Zp.newRandomElement().getImmutable();
        }

        Element[] C5 = new Element[d + 1];
        for (int x = 1; x <= d; x++) {
            C5[x] = g1.powZn(vartheta.mul(l[x])).getImmutable();
        }

        Element C6 = g1.powZn(chi).getImmutable();
        Element C7 = ek2.mul(g1.powZn(tau)).getImmutable();

        Element[] C8 = new Element[kp];
        for (int i = 0; i < kp; i++) {
            C8[i] = ek3[i].mul(g1.powZn(tauI[i])).getImmutable();
        }

        Element g2NegTau = g2.powZn(tau.negate());
        Element[] C9 = new Element[kp];
        for (int i = 0; i < kp; i++) {
            Element tmp = u2.powZn(S[i]).mul(h2).powZn(tauI[i]);
            C9[i] = ek4[i].mul(g2NegTau).mul(tmp).getImmutable();
        }

        Element C0 = ek1.mul(w1.powZn(tau)).mul(F.powZn(chi)).getImmutable();

        List<String> plaintexts = readPlaintextsFromFiles("E:\\MTS_FBMC\\src\\Dataset\\DLBCL-Stanford.rar");
        List<Element> MsgList = new ArrayList<>();
        for (String line : plaintexts) {
            Element Msg = GT.newElement()
                    .setFromHash(line.getBytes(), 0, line.length())
                    .getImmutable();
            MsgList.add(Msg);
        }

        long startTime = System.nanoTime();

        List<Element> encryptedMessages = new ArrayList<>();
        for (Element Msg : MsgList) {
            Element C = Msg.mul(Alpha.powZn(vartheta)).getImmutable();
            encryptedMessages.add(C);
        }

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        double totalSize = 0;
        totalSize += C1.toBytes().length / 1024.0;
        totalSize += C2.toBytes().length / 1024.0;
        for (Element e : C4) totalSize += e.toBytes().length / 1024.0;
        for (int x = 1; x <= d; x++) totalSize += C5[x].toBytes().length / 1024.0;
        totalSize += C6.toBytes().length / 1024.0;
        totalSize += C7.toBytes().length / 1024.0;
        totalSize += C0.toBytes().length / 1024.0;
        for (Element e : C3) totalSize += e.toBytes().length / 1024.0;
        for (Element e : C8) totalSize += e.toBytes().length / 1024.0;
        for (Element e : C9) totalSize += e.toBytes().length / 1024.0;
        for (Element enc : encryptedMessages) {
            totalSize += enc.toBytes().length / 1024.0;
        }

        System.out.printf("Dataset size: %d records%n", MsgList.size());
        System.out.printf("Total encryption time: %.2f ms%n", durationMs);
        System.out.printf("Total ciphertext storage overhead: %.2f KB%n", totalSize);
    }

    public static List<String> readPlaintextsFromFiles(String filePath) throws Exception {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath),
                        Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }
}
