package GUIP5;

import java.util.*;

/**
 * Generate dataset simulasi dari skema PenjualanRumahForm
 * Atribut sesuai form: Area, TipeRumah, LuasTanah, DP%, LamaCicilan, HargaTotal
 * Label: DISETUJUI / DITOLAK
 */
public class DatasetGenerator {

    // Mapping sesuai cbArea di PenjualanRumahForm
    private static final Map<String, Double> HARGA_TANAH = new LinkedHashMap<>();
    // Mapping sesuai RadioButton di PenjualanRumahForm
    private static final Map<String, double[]> TIPE_RUMAH = new LinkedHashMap<>();

    static {
        HARGA_TANAH.put("Bougenvile", 500_000.0);
        HARGA_TANAH.put("Melati",     600_000.0);
        HARGA_TANAH.put("Flamboyan",  700_000.0);

        // {luas_asli, harga_bangunan}
        TIPE_RUMAH.put("Tipe-36", new double[]{90,  90_000_000});
        TIPE_RUMAH.put("Tipe-49", new double[]{120, 120_000_000});
        TIPE_RUMAH.put("Tipe-90", new double[]{150, 150_000_000});
    }

    // Encoding kategorikal → angka
    public static final Map<String, Double> AREA_ENC = new LinkedHashMap<>();
    public static final Map<String, Double> TIPE_ENC = new LinkedHashMap<>();

    static {
        int i = 0;
        for (String k : HARGA_TANAH.keySet()) AREA_ENC.put(k, (double) i++);
        i = 0;
        for (String k : TIPE_RUMAH.keySet())  TIPE_ENC.put(k, (double) i++);
    }

    // Satu baris data
    public static class DataRow {
        public String area, tipe, status;
        public double luasTanah, hargaTotal, dp, dpPct, cicilanBulan;
        public int    lamaCicilan;

        // Fitur untuk model: [areaEnc, tipeEnc, luasTanah, dpPct, lamaCicilan, hargaTotal]
        public double[] toFeatureArray() {
            return new double[]{
                AREA_ENC.get(area),
                TIPE_ENC.get(tipe),
                luasTanah, dpPct, lamaCicilan, hargaTotal
            };
        }
    }

    public static List<DataRow> generate(int n, long seed) {
        Random rng = new Random(seed);
        List<DataRow> rows = new ArrayList<>();
        String[] areas = HARGA_TANAH.keySet().toArray(new String[0]);
        String[] types = TIPE_RUMAH.keySet().toArray(new String[0]);

        for (int i = 0; i < n; i++) {
            DataRow r = new DataRow();
            r.area = areas[rng.nextInt(areas.length)];
            r.tipe = types[rng.nextInt(types.length)];

            double luasAsli    = TIPE_RUMAH.get(r.tipe)[0];
            double hargaBangunan = TIPE_RUMAH.get(r.tipe)[1];
            r.luasTanah        = luasAsli * (0.8 + rng.nextDouble() * 0.4);

            double hargaTanah  = HARGA_TANAH.get(r.area);
            double hargaTotal  = (r.luasTanah * hargaTanah) + hargaBangunan;
            double ppn         = 0.10 * hargaTotal;
            r.hargaTotal       = hargaTotal + ppn;

            r.dpPct            = 10 + rng.nextDouble() * 30; // 10% – 40%
            r.dp               = (r.dpPct / 100.0) * r.hargaTotal;
            r.lamaCicilan      = 12 + rng.nextInt(169);      // 12 – 180 bln
            r.cicilanBulan     = (r.hargaTotal - r.dp) / r.lamaCicilan;

            // ── Aturan bisnis label ──────────────────────
            boolean ok = (r.dpPct >= 20.0) && (r.lamaCicilan <= 120);
            r.status = ok ? "DISETUJUI" : "DITOLAK";

            rows.add(r);
        }
        return rows;
    }

    /** Konversi list DataRow → array fitur X dan label y */
    public static double[][] toX(List<DataRow> rows) {
        double[][] X = new double[rows.size()][6];
        for (int i = 0; i < rows.size(); i++) X[i] = rows.get(i).toFeatureArray();
        return X;
    }

    public static String[] toY(List<DataRow> rows) {
        String[] y = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) y[i] = rows.get(i).status;
        return y;
    }
}