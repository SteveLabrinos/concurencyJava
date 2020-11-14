package com.concurrency;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class CharCountThread extends Thread {
    final static int NUM_OF_THREADS = 2;
    final static int ALPHABET_SIZE = 26;

    //  Χαρακτηριστικά νήματος
    private final int[] charCount = new int[ALPHABET_SIZE];
    private final String[] passArray;
    private final int start;
    private final int finish;

    //  Κατασκευαστής
    public CharCountThread(String[] passArray, int start, int finish) {
        this.passArray = passArray;
        this.start = start;
        this.finish = finish;
    }

    //  Getter
    public int[] getCharCount() { return charCount; }

    @Override
    public void run() {
        //  Επανάληψη για όλες τις λέξεις, για το διάστημα του πίνακα που εξετάζεται
        int bound = finish;
        IntStream.range(start, finish).forEach(i -> {
            //  Για κάθε χαρακτήρα της λέξης αυξάνεται ο αντίστοιχος μετρητής στον charCount
            IntStream.range(0, passArray[i].length()).forEach(j -> charCount[passArray[i].charAt(j) - 'a']++);
        });
    }

    //  Στατική μέθοδος συμπλήρωσης πίνακα με τυχαίες κωδικές λέξεις
    private static void populateArray(String[] arr) {
        //  Αρχικοποίηση στοιχείων του πίνακα με χρήση γεννήτριας
        Random r = new Random();
        Arrays.setAll(arr, i -> generateRandomPass(r));
    }

    //  Στατική μέθοδος - γεννήτρια τυχαίων κωδικών λέξεων μήκους 8 - 16 χαρακτήρων
    private static String generateRandomPass(Random r) {
        //  Τυχαίο μήκος λέξης μεγέθους από 8 έως 16 χαρακτήρες
        char[] str = new char[r.nextInt(8) + 8];
        //  Ανάθεση τυχαίου χαρακτήρα 'a - z' σε κάθε γράμμα
        IntStream.range(0, str.length).forEach(i -> str[i] = (char) (r.nextInt(ALPHABET_SIZE) + 'a'));

        return new String(str);
    }

    //  Στατική μέθοδος υπολογισμού αποτελεσμάτων, με την κλήση νημάτων
    private static int[] calcCharCount(String[] arr) throws InterruptedException {
        //  Δημιουργία πίνακα με τα νήματα που θα εκτελεστούν
        CharCountThread[] threads = new CharCountThread[NUM_OF_THREADS];
        int len = arr.length;
        //  Αρχικοποίηση thread δίνοντας το ανάλογο κομμάτι του πίνακα για υπολογισμό και έναρξη νημάτων
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new CharCountThread(arr, i * len / NUM_OF_THREADS, (i + 1) * len / NUM_OF_THREADS);
            threads[i].start();
        });

        int[] charCnt = new int[ALPHABET_SIZE];
        //  Συνένωση νημάτων και συγκεντρωτικός υπολογισμός εμφάνισης χαρακτήρων
        for (CharCountThread thread : threads) {
            thread.join();
            IntStream.range(0, charCnt.length).forEach(i -> charCnt[i] += thread.getCharCount()[i]);
        }

        return charCnt;
    }

    public static void main(String[] args) throws InterruptedException {
        //  Χρόνος εκκίνησης του προγράμματος
        long start = System.currentTimeMillis();

        //  Δημιουργία πίνακα 2^20 στοιχείων και αρχικοποίησή του
        String[] passwords = new String[(int) Math.pow(2, 20)];
        populateArray(passwords);

        //  Χρόνος έναρξης υπολογισμού χαρακτήρων με νήματα
        long middle = System.currentTimeMillis();

        //  Δημιουργία πίνακα και καταμέτρηση εμφάνισης χαρακτήρων στον πίνακα passwords
        int[] charCount = calcCharCount(passwords);

        //  Χρόνος περάτωσης του προγράμματος
        long finish = System.currentTimeMillis();

        //  Εκτύπωση αποτελεσμάτων
        System.out.format("Χρόνος εκτέλεσης προγράμματος: %d msecs", finish - start);
        System.out.format("\nΧρόνος υπολογισμού εμφάνισης χαρακτήρων με %d %s: %d msecs", NUM_OF_THREADS,
                (NUM_OF_THREADS == 1) ? "νήμα" : "νήματα", finish - middle);
        System.out.print("\n==========================\n");
        System.out.print("Πλήθος εμφάνισης γραμμάτων");
        System.out.print("\n==========================\n");
        IntStream.range(0, charCount.length).forEach(i -> System.out.format("\t%c - %d\n", (char) (i + 'a'), charCount[i]));
    }
}
