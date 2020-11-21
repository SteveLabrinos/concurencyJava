package com.concurrency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class CovidReport extends Thread {
    //  Στατικές μεταβλητές
    private static final String INPUT_PATH = "src/com/concurrency/data.csv";
    private static final int NUM_OF_THREADS = 8;
    private static final HashMap<String, Integer> totalCountriesCases = new HashMap<>();
    private static final HashMap<String, Integer> totalDateCases = new HashMap<>();
    private static String maxCasesCountry;
    private static String maxCasesDate;
    private static int maxCases;

    //  Χαρακτηριστικά νήματος
    private final HashMap<String, Integer> countriesCases = new HashMap<>();
    private final HashMap<String, Integer> dateCases = new HashMap<>();
    private final ArrayList<String[]> data;
    private final int start;
    private final int finish;
    private String[] pos;
    private double percentage;

    //  Κατασκευαστής
    public CovidReport (ArrayList<String[]> data, int start, int finish) {
        this.data = data;
        this.start = start;
        this.finish = finish;
    }

    //  Getters
    public String[] getPos() { return pos; }

    public double getPercentage() { return percentage; }

    @Override
    public void run() {
        for (int i = start; i < finish; i++) {
            //  Στοιχεία χώρας, ημερομηνίας και κρουσμάτων από τον πίνακα
            String country = data.get(i)[6];
            int cases = Integer.parseInt(data.get(i)[4]);
            //  Ενημέρωση hashMap με τον αριθμό κρουσμάτων κάθε εγγραφής, ανά χώρα
            updateHashMap(countriesCases, country, cases);
            //  Ενημέρωση hashMap με τον αριθμό κρουσμάτων κάθε εγγραφής, ανά χώρα
            String date = data.get(i)[0];
            updateHashMap(dateCases, date, cases);
            //  Αγνόηση εγγραφών με κενό πληθυσμό και τροποποίηση της χώρας "Bonaire, Saint Eustatius and Saba"
            if (data.get(i)[9].startsWith("BES")) {
                data.get(i)[9] = data.get(i)[10];
            } else if (data.get(i)[9].equals("")) {
                continue;
            }
            //  Εύρεση ημερομηνίας με το μεγαλύτερο ποσοστό κρουσμάτων - row[4] / row [9]
            double curPercentage = Double.parseDouble(data.get(i)[4]) / Integer.parseInt(data.get(i)[9]);
            //  Εάν το ποσοστό της τρέχουσας εγγραφής είναι προσωρινό μέγιστο, αποθηκεύονται τα στοιχεία του
            if (curPercentage > percentage) {
                pos = data.get(i);
                percentage = curPercentage;
            }
        }
    }

    //  Μέθοδος φόρτωσης στοιχείων csv αρχείου στη μνήμη
    private static ArrayList<String[]> loadCsv () throws IOException {
        ArrayList<String[]> data = new ArrayList<>();
        //  Φόρτωση buffer με το αρχείο csv για επεξεργασία
        BufferedReader scvReader = new BufferedReader(new FileReader(INPUT_PATH));
        String row;
        while ((row = scvReader.readLine()) != null) {
            //  Αποφυγή επικεφαλίδων
            if (row.startsWith("dateRep")) continue;
            //  Εισαγωγή δεδομένων στη λίστα
            String [] rowData = row.split(",");
            data.add(rowData);
        }
        return data;
    }

    //  Μέθοδος δημιουργίας αναφορών covid με χρήση νημάτων
    private static void calcCases(ArrayList<String[]> data) throws InterruptedException {
        CovidReport[] covidThreads = new CovidReport[NUM_OF_THREADS];

        //  Δημιουργία νημάτων για την επεξεργασία των δεδομένων του πίνακα
        int len = data.size();
        IntStream.range(0, covidThreads.length).forEach(i ->{
            //  Μετατροπή της λίστας σε πίνακα για την περεταίρω επεξεργασία του
            covidThreads[i] = new CovidReport(data, i * len / NUM_OF_THREADS,
                    (i + 1) * len / NUM_OF_THREADS);
            covidThreads[i].start();
        });

        //  Συνένωση νημάτων για την εξαγωγή αποτελεσμάτων
        for (CovidReport covidThread : covidThreads) {
            covidThread.join();
            //  Δημιουργία hashMap με τα στοιχεία χωρών, όλων των νημάτων
            covidThread.countriesCases.keySet().forEach(country ->
                    updateHashMap(totalCountriesCases, country, covidThread.countriesCases.get(country)));

            //  Δημιουργία hashMap με τα στοιχεία ημερομηνιών, όλων των νημάτων
            covidThread.dateCases.keySet().forEach(date ->
                    updateHashMap(totalDateCases, date, covidThread.dateCases.get(date)));
        }

        //  Σύγκριση μέγιστου ποσοστού κρουσμάτων από όλα τα νήματα για τη διατήρηση του μεγαλύτερου
        double maxPer = covidThreads[0].getPercentage();
        String[] maxPos = covidThreads[0].getPos();
        for (CovidReport covidThread : covidThreads) {
            if (covidThread.getPercentage() > maxPer) {
                maxPos = covidThread.getPos();
                maxPer = covidThread.getPercentage();
            }
        }
        //  Ενημέρωση των στατικών μεταβλητών με τα δεδομένα μέγιστων κρουσμάτων
        maxCases = Integer.parseInt(maxPos[4]);
        maxCasesCountry = maxPos[6];
        maxCasesDate = maxPos[0];
    }

    //  Μέθοδος ενημέρωσης hashMaps
    private static void updateHashMap(HashMap<String, Integer> hm, String key, int val) {
        //  Προσθήκη των νέων κρουσμάτων εάν το κλειδί υπάρχει, διαφορετικά αρχικοποίησή του
        if (hm.containsKey(key)) {
            hm.replace(key, hm.get(key) + val);
        } else {
            hm.put(key, val);
        }
    }

    //  Μέθοδος εξαγωγής αρχείων csv με τα αποτελέσματα
    private static void writeFile (String category, HashMap<String, Integer> hm) throws IOException {
        FileWriter csvOutput = new FileWriter("src/com/concurrency/" + category + "_out.csv");
        //  Επικεφαλίδα αρχείου
        csvOutput.append(category);
        csvOutput.append(",");
        csvOutput.append("total_cases");
        csvOutput.append("\n");
        //  Ταξινόμηση με αύξουσα σειρά κλειδιού
        TreeMap<String, Integer> sortedHm = new TreeMap<>(hm);
        //  Εισαγωγή των δεδομένων σε κάθε γραμμή του αρχείου
        for (Map.Entry<String, Integer> entry : sortedHm.entrySet()) {
            csvOutput.append(entry.getKey());
            csvOutput.append(",");
            csvOutput.append(entry.getValue().toString());
            csvOutput.append("\n");
        }
        csvOutput.flush();
        csvOutput.close();
    }

    public static void main (String[] args) throws IOException, InterruptedException {
        //  Χρόνος εκκίνησης του προγράμματος
        long start = System.currentTimeMillis();
        //  Φόρτωση αρχείου csv στη μνήμη
        ArrayList<String[]> csvData = loadCsv();
        //  Χρόνος εκκίνησης επεξεργασίας με νήματα
        long middle = System.currentTimeMillis();
        //  Υπολογισμός αναφορών κρουσμάτων
        calcCases(csvData);
        //  Εξαγωγή αρχείων αποτελεσμάτων
        writeFile("country", totalCountriesCases);
        writeFile("date", totalDateCases);
        //  Χρόνος περάτωσης του προγράμματος
        long finish = System.currentTimeMillis();
        //  Εκτύπωση λοιπών αποτελεσμάτων
        System.out.format("Χρόνος εκτέλεσης προγράμματος: %d msecs", finish - start);
        System.out.format("\nΧρόνος υπολογισμού αναφορών κρουσμάτων με %d %s: %d msecs", NUM_OF_THREADS,
                (NUM_OF_THREADS == 1) ? "νήμα" : "νήματα", finish - middle);
        System.out.print("\n==========================================================================================\n");
        System.out.print("Στοιχεία χώρας με το μέγιστο αριθμό κρουσμάτων αναλογικά με τον πληθυσμό της, σε μία ημέρα");
        System.out.print("\n==========================================================================================\n");
        System.out.format("Χώρα: %s\t\t\tΗμερομηνία: %s\t\t\tΑριθμός Κρουσμάτων: %d", maxCasesCountry, maxCasesDate, maxCases);
    }
}
