package com.concurrency.CovidTreatment;

import java.util.Random;
import java.util.stream.IntStream;

public class Hospital extends Thread {
    //  Χαρακτηριστικά νήματος νοσοκομείου
    private final TreatmentQueue queue;

    //  Κατασκευαστής
    public Hospital(TreatmentQueue queue) { this.queue = queue; }

    @Override
    public void run() {
        // Επώαση κρουσμάτων 0 έως h με συχνότητα CURE_FREQUENCY
        Random r = new Random();
        IntStream.range(0, CovidTreatment.LOOPS).forEach(i -> {
            try {
                sleep(CovidTreatment.CURE_FREQUENCY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.solveCovidCases(r.nextInt(CovidTreatment.MAX_TREATMENT));
        });
    }
}
