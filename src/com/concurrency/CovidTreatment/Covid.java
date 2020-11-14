package com.concurrency.CovidTreatment;

import java.util.Random;

public class Covid extends Thread {
    //  Χαρακτηριστικά νήματος covid
    private TreatmentQueue queue;

    //  Κατασκευαστής
    public Covid (TreatmentQueue queue) { this.queue = queue; }

    @Override
    public void run() {
        // Δημιουργία νέων κρουσμάτων 0 έως k με συχνότητα COVID_FREQUENCY
        Random r = new Random();
        for (int i =0; i < CovidTreatment.LOOPS; i++) {
            try {
                sleep(CovidTreatment.COVID_FREQUENCY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.newCovidCases(r.nextInt(CovidTreatment.MAX_CASES));
        }
    }
}
