package com.concurrency.CovidTreatment;

import java.util.Random;

public class Hospital extends Thread {
    //  Χαρακτηριστικά νήματος νοσοκομείου
    private TreatmentQueue queue;

    //  Κατασκευαστής
    public Hospital(TreatmentQueue queue) { this.queue = queue; }

    @Override
    public void run() {
        // Επώαση κρουσμάτων 0 έως h με συχνότητα CURE_FREQUENCY
        Random r = new Random();
        for (int i = 0; i < CovidTreatment.LOOPS; i++) {
            try {
                sleep(CovidTreatment.CURE_FREQUENCY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.solveCovidCases(r.nextInt(CovidTreatment.MAX_TREATMENT));
        }
    }
}
