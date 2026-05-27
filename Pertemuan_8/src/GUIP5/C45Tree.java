package GUIP5;

import java.util.*;

/**
 * ============================================================
 *  C4.5 Decision Tree — Murni Java (tanpa library eksternal)
 *  Digunakan untuk klasifikasi status pembelian rumah:
 *    DISETUJUI / DITOLAK
 * ============================================================
 */
public class C45Tree {

    // ── Node pohon ─────────────────────────────────────────
    public static class Node {
        public String featureName;   // nama fitur split
        public int    featureIndex;  // index kolom fitur
        public double threshold;     // nilai batas (numerik)
        public String leafLabel;     // label jika leaf
        public boolean isLeaf;

        public Node leftChild;       // <= threshold
        public Node rightChild;      // >  threshold

        // Untuk fitur kategorikal
        public boolean isCategorical;
        public Map<String, Node> categoryChildren = new HashMap<>();

        public Node() {}
    }

    // ── Nama fitur ─────────────────────────────────────────
    public static final String[] FEATURE_NAMES = {
        "Area", "Tipe Rumah", "Luas Tanah (m2)",
        "DP (%)", "Lama Cicilan (bln)", "Harga Total (Rp)"
    };

    // Indeks fitur kategorikal
    private static final Set<Integer> CATEGORICAL = new HashSet<>(Arrays.asList(0, 1));

    public Node root;
    private int maxDepth;
    private int minSamples;

    public C45Tree(int maxDepth, int minSamples) {
        this.maxDepth   = maxDepth;
        this.minSamples = minSamples;
    }

    // ══════════════════════════════════════════════════════
    //  TRAINING
    // ══════════════════════════════════════════════════════
    public void fit(double[][] X, String[] y) {
        root = buildTree(X, y, 0);
    }

    private Node buildTree(double[][] X, String[] y, int depth) {
        Node node = new Node();

        // ── Kondisi berhenti ───────────────────────────────
        if (y.length <= minSamples || depth >= maxDepth || isPure(y)) {
            node.isLeaf    = true;
            node.leafLabel = majorityClass(y);
            return node;
        }

        // ── Cari split terbaik (Information Gain Ratio) ───
        BestSplit best = findBestSplit(X, y);

        if (best == null) {
            node.isLeaf    = true;
            node.leafLabel = majorityClass(y);
            return node;
        }

        node.featureIndex = best.featureIndex;
        node.featureName  = FEATURE_NAMES[best.featureIndex];
        node.isLeaf       = false;

        if (CATEGORICAL.contains(best.featureIndex)) {
            // Kategorikal: pecah per nilai unik
            node.isCategorical = true;
            Map<String, List<Integer>> groups = groupByCategory(X, best.featureIndex);
            for (Map.Entry<String, List<Integer>> e : groups.entrySet()) {
                List<Integer> idx = e.getValue();
                double[][] subX   = subset(X, idx);
                String[]   subY   = subset(y, idx);
                node.categoryChildren.put(e.getKey(), buildTree(subX, subY, depth + 1));
            }
        } else {
            // Numerik: threshold split
            node.threshold = best.threshold;
            List<Integer> leftIdx  = new ArrayList<>();
            List<Integer> rightIdx = new ArrayList<>();
            for (int i = 0; i < X.length; i++) {
                if (X[i][best.featureIndex] <= best.threshold) leftIdx.add(i);
                else                                             rightIdx.add(i);
            }
            if (leftIdx.isEmpty() || rightIdx.isEmpty()) {
                node.isLeaf    = true;
                node.leafLabel = majorityClass(y);
                return node;
            }
            node.leftChild  = buildTree(subset(X, leftIdx),  subset(y, leftIdx),  depth + 1);
            node.rightChild = buildTree(subset(X, rightIdx), subset(y, rightIdx), depth + 1);
        }
        return node;
    }

    // ══════════════════════════════════════════════════════
    //  PREDIKSI
    // ══════════════════════════════════════════════════════
    public String predict(double[] sample) {
        return traverse(root, sample);
    }

    private String traverse(Node node, double[] sample) {
        if (node.isLeaf) return node.leafLabel;
        if (node.isCategorical) {
            String val = categoryLabel(sample[node.featureIndex], node.featureIndex);
            Node child = node.categoryChildren.get(val);
            if (child == null) child = node.categoryChildren.values().iterator().next();
            return traverse(child, sample);
        } else {
            if (sample[node.featureIndex] <= node.threshold)
                return traverse(node.leftChild,  sample);
            else
                return traverse(node.rightChild, sample);
        }
    }

    // ══════════════════════════════════════════════════════
    //  CARI SPLIT TERBAIK (C4.5 — Information Gain Ratio)
    // ══════════════════════════════════════════════════════
    private static class BestSplit {
        int    featureIndex;
        double threshold;
        double gainRatio;
    }

    private BestSplit findBestSplit(double[][] X, String[] y) {
        BestSplit best = null;
        double parentEntropy = entropy(y);

        for (int f = 0; f < X[0].length; f++) {
            if (CATEGORICAL.contains(f)) {
                double gr = gainRatioCategorical(X, y, f, parentEntropy);
                if (best == null || gr > best.gainRatio) {
                    best = new BestSplit();
                    best.featureIndex = f;
                    best.gainRatio    = gr;
                }
            } else {
                double[] thresholds = getCandidateThresholds(X, f);
                for (double t : thresholds) {
                    double gr = gainRatioNumeric(X, y, f, t, parentEntropy);
                    if (best == null || gr > best.gainRatio) {
                        best = new BestSplit();
                        best.featureIndex = f;
                        best.threshold    = t;
                        best.gainRatio    = gr;
                    }
                }
            }
        }
        return (best != null && best.gainRatio > 0) ? best : null;
    }

    // ── Information Gain Ratio (numerik) ──────────────────
    private double gainRatioNumeric(double[][] X, String[] y,
                                    int f, double t, double parentEntropy) {
        List<Integer> left  = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int i = 0; i < X.length; i++) {
            if (X[i][f] <= t) left.add(i); else right.add(i);
        }
        if (left.isEmpty() || right.isEmpty()) return 0;

        double n    = y.length;
        double gain = parentEntropy
                    - (left.size()  / n) * entropy(subset(y, left))
                    - (right.size() / n) * entropy(subset(y, right));

        double splitInfo = -((left.size()/n)  * log2(left.size()/n))
                         -  ((right.size()/n) * log2(right.size()/n));
        return splitInfo == 0 ? 0 : gain / splitInfo;
    }

    // ── Information Gain Ratio (kategorikal) ──────────────
    private double gainRatioCategorical(double[][] X, String[] y,
                                        int f, double parentEntropy) {
        Map<String, List<Integer>> groups = groupByCategory(X, f);
        double n    = y.length;
        double gain = parentEntropy;
        double splitInfo = 0;
        for (List<Integer> idx : groups.values()) {
            double p  = idx.size() / n;
            gain     -= p * entropy(subset(y, idx));
            splitInfo -= p * log2(p);
        }
        return splitInfo == 0 ? 0 : gain / splitInfo;
    }

    // ══════════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════════
    private double entropy(String[] y) {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : y) counts.merge(s, 1, Integer::sum);
        double e = 0, n = y.length;
        for (int c : counts.values()) {
            double p = c / n;
            if (p > 0) e -= p * log2(p);
        }
        return e;
    }

    private double log2(double x) {
        return x <= 0 ? 0 : Math.log(x) / Math.log(2);
    }

    private boolean isPure(String[] y) {
        String first = y[0];
        for (String s : y) if (!s.equals(first)) return false;
        return true;
    }

    private String majorityClass(String[] y) {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : y) counts.merge(s, 1, Integer::sum);
        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double[] getCandidateThresholds(double[][] X, int f) {
        Set<Double> vals = new TreeSet<>();
        for (double[] row : X) vals.add(row[f]);
        Double[] sorted = vals.toArray(new Double[0]);
        double[] thresholds = new double[sorted.length - 1];
        for (int i = 0; i < thresholds.length; i++)
            thresholds[i] = (sorted[i] + sorted[i+1]) / 2.0;
        return thresholds;
    }

    private Map<String, List<Integer>> groupByCategory(double[][] X, int f) {
        Map<String, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < X.length; i++) {
            String key = categoryLabel(X[i][f], f);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }
        return groups;
    }

    // Konversi kode numerik ke label asli
    public static String categoryLabel(double code, int featureIndex) {
        if (featureIndex == 0) { // Area
            switch ((int) code) {
                case 0: return "Bougenvile";
                case 1: return "Flamboyan";
                default: return "Melati";
            }
        } else { // Tipe Rumah
            switch ((int) code) {
                case 0: return "Tipe-36";
                case 1: return "Tipe-49";
                default: return "Tipe-90";
            }
        }
    }

    // ── Array subset helpers ───────────────────────────────
    private double[][] subset(double[][] X, List<Integer> idx) {
        double[][] out = new double[idx.size()][X[0].length];
        for (int i = 0; i < idx.size(); i++) out[i] = X[idx.get(i)];
        return out;
    }

    private String[] subset(String[] y, List<Integer> idx) {
        String[] out = new String[idx.size()];
        for (int i = 0; i < idx.size(); i++) out[i] = y[idx.get(i)];
        return out;
    }

    // ══════════════════════════════════════════════════════
    //  CETAK POHON (text)
    // ══════════════════════════════════════════════════════
    public void printTree() {
        System.out.println("\n🌳 STRUKTUR POHON KEPUTUSAN C4.5:");
        System.out.println(new String(new char[50]).replace("\0", "="));
        printNode(root, "", true);
    }

    private void printNode(Node node, String prefix, boolean isLeft) {
        if (node == null) return;
        String branch = isLeft ? "├── " : "└── ";
        if (node.isLeaf) {
            System.out.println(prefix + branch + "🏷  [" + node.leafLabel + "]");
            return;
        }
        if (node.isCategorical) {
            System.out.println(prefix + branch + "📂 " + node.featureName);
            List<String> keys = new ArrayList<>(node.categoryChildren.keySet());
            for (int i = 0; i < keys.size(); i++) {
                boolean last = (i == keys.size() - 1);
                String  newPrefix = prefix + (isLeft ? "│   " : "    ");
                System.out.println(newPrefix + (last ? "└── " : "├── ") + "= " + keys.get(i));
                printNode(node.categoryChildren.get(keys.get(i)),
                          newPrefix + (last ? "    " : "│   "), !last);
            }
        } else {
            System.out.println(prefix + branch + "📊 " + node.featureName
                             + " ≤ " + String.format("%.2f", node.threshold));
            String newPrefix = prefix + (isLeft ? "│   " : "    ");
            printNode(node.leftChild,  newPrefix, true);
            printNode(node.rightChild, newPrefix, false);
        }
    }
}