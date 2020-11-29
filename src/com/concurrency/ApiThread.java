package com.concurrency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class ApiThread extends Thread {
    //  Στατικές μεταβλητές προγράμματος
    private static final int NUM_OF_THREADS = 8;
    private static final int API_REQUESTS = 50;
    private static final String API_URL = "https://sv443.net/jokeapi/v2/joke/Any?format=txt";
    private static int minText;
    private static int maxText;
    private static final HashMap<String, Integer> totalWordCount = new HashMap<>();

    //  Χαρακτηριστικά νήματος
    private final int numOfRequests;
    //  Αρχικοποίηση με τη μέγιστη δυνατή τιμή
    private int minStr = Integer.MAX_VALUE;
    private int maxStr;
    private final HashMap<String, Integer> wordCount = new HashMap<>();

    //  Κατασκευαστής
    public ApiThread (int numOfRequests) {
        this.numOfRequests = numOfRequests;
    }

    //  Getters
    public HashMap<String, Integer> getWordCount () { return wordCount; }

    public int getMinText () { return minStr; }

    public int getMaxText () { return maxStr; }

    @Override
    public void run() {
        //  Επανάληψη κλήσεων για κάθε νήμα
        for (int i = 0; i < numOfRequests; i++) {
            String text;
            try {
                //  Εξαγωγή συμβολοσειράς μετά από κάθε κλήση
                text = getApiData();
            } catch (IOException e) { //    Σε περίπτωση αδυναμίας κλήσης, λαμβάνουμε το κείμενο ως κενό
                text = "";
            }
            //  Υπολογισμός μεγίστου και ελάχιστου μήκους κειμένου για κάθε κλήση
            int len = text.length();

            if (minStr > len) minStr = len;

            if (maxStr < len) maxStr = len;
            //  Διαχωρισμός των λέξεων ενός κειμένου
            String[] words = text.split(" ");
            //  Καταμέτρηση πλήθους εμφάνισης λέξεων στο κείμενο
            Arrays.stream(words).forEach(word -> updateHashMap(wordCount, word, 1));
        }
    }

    //  Μέθοδος συλλογής δεδομένων
    private String getApiData() throws IOException {
        //  Άνοιγμα σύνδεσης με τη διεύθυνση URL του API
        URLConnection connection = new URL(API_URL).openConnection();
        //  Φόρτωση δεδομένων κειμένου σε buffer
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        //  Δημιουργία κατασκευαστή συμβολοσειράς
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        //  Επιστροφή σε μορφή συμβολοσειράς
        return content.toString();
    }

    //  Στατική μέθοδος υπολογισμού αποτελεσμάτων, με τη χρήση νημάτων
    private static void makeApiRequests() throws InterruptedException {
        //  Αρχικοποίηση και εκκίνηση των νημάτων για τον υπολογισμό αποτελεσμάτων
        ApiThread[] threads = new ApiThread[NUM_OF_THREADS];
        IntStream.range(0, threads.length).forEach(i -> {
            //  Καθορισμός ίδιου πλήθους κλήσεων για κάθε νήμα
            int numOfRequests = ((i + 1) * API_REQUESTS) / NUM_OF_THREADS - (i * API_REQUESTS) / NUM_OF_THREADS;
            //  Μετατροπή της λίστας σε πίνακα για την περεταίρω επεξεργασία του
            threads[i] = new ApiThread(numOfRequests);
            threads[i].start();
        });

        //  Συννένοση νημάτων και εξαγωγή συγκεντρωτικών στοιχείων
        for (ApiThread aThread : threads) {
            aThread.join();
            //  Συγκέντρωση όλων των εμφανίσεων των λέξεων
            aThread.getWordCount().forEach((k, v) -> updateHashMap(totalWordCount, k, v));
        }
        //  Υπολογισμός μέγιστου και ελάχιστου κειμένου
        maxText = threads[0].getMaxText();
        minText = threads[0].getMinText();
        Arrays.stream(threads).forEach(apiTread -> {
            if (maxText < apiTread.getMaxText()) maxText = apiTread.getMaxText();
            if (minText > apiTread.getMinText()) minText = apiTread.getMinText();
        });
    }

    //  Στατική μέθοδος ενημέρωσης hashMap
    private static void updateHashMap(HashMap<String, Integer> hm, String key, int val) {
        //  Προσθήκη του πλήθους εμφάνισης λέξεων εάν το κλειδί υπάρχει, διαφορετικά αρχικοποίησή του
        if (hm.containsKey(key)) {
            hm.replace(key, hm.get(key) + val);
        } else {
            hm.put(key, val);
        }
    }

    public static void main (String[] args) throws InterruptedException {
        //  Χρόνος εκκίνησης του προγράμματος
        long start = System.currentTimeMillis();
        makeApiRequests();
        //  Χρόνος περάτωσης του προγράμματος
        long finish = System.currentTimeMillis();
        //  Εκτύπωση λοιπών αποτελεσμάτων
        System.out.format("\nΧρόνος εκτέλεσης προγράμματος με %d %s: %d msecs", NUM_OF_THREADS,
                (NUM_OF_THREADS == 1) ? "νήμα" : "νήματα", finish - start);
        System.out.print("\n=============================================================================\n");
        System.out.format("Ελάχιστο μήκος κειμένου κλήσης: %d\t\tΜέγιστο μήκος κειμένου κλήσης: %d", minText, maxText);
        System.out.print("\n=============================================================================\n");
        System.out.format("Αναφορά λέξεων και πλήθος εμφάνισης από όλες τις κλήσεις");
        System.out.print("\n=============================================================================\n");
        TreeMap<String, Integer> sorted = new TreeMap<>(totalWordCount);
        sorted.forEach((key, val) -> System.out.format("Λέξη: %-30sΠλήθος: %d\n", key, val));
    }
}
