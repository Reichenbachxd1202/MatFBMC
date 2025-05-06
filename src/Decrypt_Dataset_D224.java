public class Decrypt_Dataset_D224 {

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

    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("d224.properties");

        Field<Element> G0 = pairing.getG1();
        Field<Element> G1 = pairing.getG2();
        Field<Element> GT = pairing.getGT();
        Field<Element> Zp = pairing.getZr();

        List<String> plaintexts = readPlaintextsFromFiles("E:\\MTS_FBMC\\src\\Dataset\\mimic-iii.rar");
        List<Element> CipherList = new ArrayList<>();
        for (String line : plaintexts) {
            Element Cipher = GT.newElement()
                    .setFromHash(line.getBytes(), 0, line.length())
                    .getImmutable();
            CipherList.add(Cipher);
        }

        Element ck_02_pr = G1.newRandomElement().getImmutable();
        Element ck_03_pr = G1.newRandomElement().getImmutable();
        Element ck_04_pr = G1.newRandomElement().getImmutable();

        Element D_1 = GT.newRandomElement().getImmutable();
        Element V = Zp.newRandomElement().getImmutable();

        int d = 50;
        Element[] C_5 = new Element[d];
        for (int i = 0; i < d; i++) {
            C_5[i] = G0.newRandomElement().getImmutable();
        }

        Element phi_star = Zp.newRandomElement().getImmutable();
        Element[] phi = new Element[d];
        for (int i = 0; i < d; i++) {
            phi[i] = Zp.newRandomElement().getImmutable();
        }

        Element C_1 = G0.newRandomElement().getImmutable();
        Element C_2 = G0.newRandomElement().getImmutable();
        Element ck_1 = G1.newRandomElement().getImmutable();
        Element ck_2 = G1.newRandomElement().getImmutable();

        double totalTimeCost = 0.0;
        double totalStorageKB = 0.0;

        long startTime1 = System.nanoTime();

        Element[] Y_5 = new Element[d];
        for (int j = 0; j < d; j++) {
            Y_5[j] = C_5[j].powZn(phi[j]).getImmutable();
        }

        Element product_Y = G0.newOneElement().getImmutable();
        for (Element y : Y_5) {
            product_Y = product_Y.mul(y).getImmutable();
        }

        Element X = pairing.pairing(C_1, ck_03_pr).getImmutable();
        Element W = pairing.pairing(C_1, ck_04_pr).powZn(phi_star).getImmutable();
        Element Y = pairing.pairing(product_Y, ck_02_pr).getImmutable();

        Element D_2 = X.div(W.mul(Y)).getImmutable();
        Element D_1_V = D_1.powZn(V.invert()).getImmutable();

        Element pair1 = pairing.pairing(C_1, ck_2).getImmutable();
        Element pair2 = pairing.pairing(C_2, ck_1).getImmutable();

        long endTime1 = System.nanoTime();
        double durationSec1 = endTime1 - startTime1;

        for (int i = 0; i < CipherList.size(); i++) {
            Element Q = CipherList.get(i);

            long startTime = System.nanoTime();

            Element M = Q.mul(D_1_V).mul(D_2).mul(pair1.div(pair2)).getImmutable();

            long endTime = System.nanoTime();
            double durationSec = endTime - startTime;
            totalTimeCost += durationSec;

            double storageKB = M.toBytes().length / 1024.0;
            totalStorageKB += storageKB;

            System.out.printf("Decryption time for Q[%d]: %.4f ms%n", i, durationSec);
            System.out.printf("Storage cost for M[%d]: %.2f KB%n", i, storageKB);
        }

        System.out.printf("Total decryption time for all Q: %.4f s%n", (totalTimeCost + durationSec1) / 1_000_000_000.0);
        System.out.printf("Total storage cost for all M: %.2f KB%n", totalStorageKB);
    }
}
