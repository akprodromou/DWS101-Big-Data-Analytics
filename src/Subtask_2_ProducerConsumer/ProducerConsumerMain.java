// package (φάκελος) στον οποίο ανήκει η κλάση
package Subtask_2_ProducerConsumer;

// Εισάγω τα packages που θα χρειαστώ
// ώστε να δημιουργήσω τους τυχαίους ακέραιους
import java.util.Random;
// Για να σταματήσω τα thread όταν το ένα έχει τελειώσει
import java.util.concurrent.atomic.AtomicBoolean;
// to create a list of totals
import java.util.*;
// για να κάνω export τα αποτελέσματα σε *.csv
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// the top-level class
public class ProducerConsumerMain {
    // Ορισμός παραμέτρων με static final, γιατί είναι παράμετροι του μοντέλου και όχι καταγραφής
    // Οι constants στη Java ορίζονται με κεφαλαία
    // static: there is only one copy shared by all instances
    // final: the variable's value can't change after it's been assigned - it becomes a constant
    // Αριθμός επαναλήψεων για την ολοκλήρωση του προγράμματος
    static final int ITERATIONS = 30;
    // Περίοδος δημιουργίας κρουσμάτων (σε ms)
    static final int CASE_CREATION_TIME = 1000;
    // Μέγιστος αριθμός εμφάνισης νέων κρουσμάτων k
    static final int CASES_CREATION_RATE = 10;
    // Χωρητικότητα συστήματος υγείας σε κλίνες Μ.Ε.Θ.
    static final int SYSTEM_CAPACITY = 20;
    // Περίοδος θεραπείας κρούσματος
    static final int CASE_HEAL_TIME = 5000;
    // Μέγιστος αριθμός θεραπείας κρουσμάτων h, με h < k
    static final int CASE_HEAL_RATE = 8;

    // main: από εδώ θα ξεκινήσει την εκτέλεση του προγράμματος η JVM
    public static void main(String[] args) throws InterruptedException {
        // initiate a new instance of the monitoring class
        Registry registry = new Registry();
        // initiate the producer and consumer
        DiseaseGeneration diseaseGeneration = new DiseaseGeneration(registry);
        DiseaseHealing diseaseHealing = new DiseaseHealing(registry);

        // Responsibility: Thread management
        // Create producer thread
        class Disease implements Runnable{
            DiseaseGeneration diseaseGeneration;
            AtomicBoolean raceFinished;
            // constructor
            public Disease(DiseaseGeneration diseaseGeneration,  AtomicBoolean raceFinished) {
                this.diseaseGeneration = diseaseGeneration;
                this.raceFinished = raceFinished;
            }
            @Override public void run() {
                for (int i = 0; i < ITERATIONS && !raceFinished.get(); i++) {
                    try {
                        Thread.sleep(CASE_CREATION_TIME);
                        diseaseGeneration.produce();
                    } catch (InterruptedException e) {
                        // print the error
                        e.printStackTrace();
                        // mark the thread as interrupted and break
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            raceFinished.set(true);
            }
        }

        // Create consumer thread
        class Hospital implements Runnable {
            DiseaseHealing diseaseHealing;
            AtomicBoolean raceFinished;
            // constructor
            public Hospital(DiseaseHealing diseaseHealing, AtomicBoolean raceFinished) {
                this.diseaseHealing = diseaseHealing;
                this.raceFinished = raceFinished;
            }
            @Override public void run() {
                for (int i = 0; i < ITERATIONS && !raceFinished.get(); i++) {
                    try {
                        Thread.sleep(CASE_HEAL_TIME);
                        diseaseHealing.heal();
                    } catch (InterruptedException e) {
                        // print the error
                        e.printStackTrace();
                        // mark the thread as interrupted and break
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            raceFinished.set(true);
            }
        }
        // The AtomicBoolean class provides atomic access to a boolean variable.
        // Unlike a regular boolean variable, which can be accessed and modified by
        // multiple threads in an unsafe manner, AtomicBoolean ensures that the updates are atomic
        AtomicBoolean raceFinished = new AtomicBoolean(false);

        // πέρνα το raceFinished στους constructors
        Thread disease = new Thread(new Disease(diseaseGeneration, raceFinished), "disease");
        Thread hospital = new Thread(new Hospital(diseaseHealing, raceFinished), "hospital");

        disease.start();
        hospital.start();

        disease.join();
        hospital.join();

        // Για να κάνω export σε CSV
        try (FileWriter writer = new FileWriter("producer_consumer_results_" + ITERATIONS
                + "_" + CASE_CREATION_TIME + ".csv")) {
            // Write header
            writer.append("GenerationIter,HealingIter,IncomingPatients,PatientsBeingTreated,PatientsRejected,PatientsAdmitted,TotalSickPatients,HealedPatients,TotalPatientsHealed,TotalPatientsRejected,EventType\n");

            // Write each log entry
            for (IterationLog log : registry.totalsLog) {
                writer.append(log.toCSV());
                writer.append("\n");
            }

            System.out.println("Data exported to hospital_data.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CLASS DEFINITIONS
    // Hospital registry class
    // Responsibility: to monitor and control admitted and released patients
    // static: you don’t need an instance of the outer class to create them
    public static class Registry {
        // Create a list shared by producer and consumer
        int systemCapacity = SYSTEM_CAPACITY;
        int incomingPatients;
        int patientsBeingTreated;
        int patientsRejected;
        int patientsAdmitted;
        int totalSickPatients = 0;
        int healedPatients = 0;
        int totalPatientsHealed = 0;
        int totalPatientsRejected = 0;
        int generationIterations = 0;
        int healingIterations = 0;
        // για να κρατάω τα σύνολα
        List<IterationLog> totalsLog = new ArrayList<>();
    }

    // Disease generation class
    // Responsibility: to generate new cases in order to be admitted to the hospital
    public static class DiseaseGeneration {
        // create a slot for a registry object
        Registry registry;
        public DiseaseGeneration(Registry registry) {
            // Store the reference
            this.registry = registry;
        }
        // Function called by producer thread
        public synchronized void produce() throws InterruptedException {
            registry.generationIterations ++;
            System.out.println("Generation iterations: " + registry.generationIterations);
            // Τυχαίος αριθμός κρουσμάτων από 0 έως k από 0 έως k (συμπεριλαμβανομένου)
            Random randomCaseNumber = new Random();
            registry.incomingPatients = randomCaseNumber.nextInt(0, CASES_CREATION_RATE + 1);
            registry.totalSickPatients += registry.incomingPatients;
            // allow only one thread to execute a block of code or a method at a time
            // With synchronized, Java internally uses a monitor lock or intrinsic lock
            synchronized (registry) {
                while (registry.patientsBeingTreated == registry.systemCapacity) {
                    System.out.println("Hospital is full. Producer waiting...");
                    registry.wait();
                }
                // if the available space is sufficient for all incoming patients
                if (registry.incomingPatients + registry.patientsBeingTreated <= registry.systemCapacity) {
                    // then all incoming patients can be admitted and none will be rejected
                    registry.patientsAdmitted = registry.incomingPatients;
                } else {
                    // only patients as many as the available spaces will be admitted
                    registry.patientsAdmitted = registry.systemCapacity - registry.patientsBeingTreated;
                    // those left out are
                    registry.patientsRejected = registry.incomingPatients - registry.patientsAdmitted;
                    // add them to the total count of rejected patients
                    registry.totalPatientsRejected += registry.patientsRejected;
                    System.out.println("Incoming patients: " + registry.incomingPatients + ". Rejected: " + registry.patientsRejected);
                }
                registry.patientsBeingTreated += registry.patientsAdmitted;
                System.out.println("Patients admitted: " + registry.patientsAdmitted);
                System.out.println("Currently hospitalized: "+ registry.patientsBeingTreated);
                IterationLog logEntry = new IterationLog(
                        registry.generationIterations,
                        registry.healingIterations,
                        registry.incomingPatients,
                        registry.patientsBeingTreated,
                        registry.patientsRejected,
                        registry.patientsAdmitted,
                        registry.totalSickPatients,
                        registry.healedPatients,
                        registry.totalPatientsHealed,
                        registry.totalPatientsRejected,
                        "PRODUCE"
                );
                registry.totalsLog.add(logEntry);
                // notify the consumer thread that now it can start consuming
                registry.notify();
            }
        }
    }

    // Disease healing class
    // Responsibility: to drop healed cases and release them from the hospital
    public static class DiseaseHealing {
        Registry registry;
        public DiseaseHealing(Registry registry) {
            this.registry = registry;
        }
        public synchronized void heal() throws InterruptedException {
            registry.healingIterations++;
            System.out.println("Healing iterations: " + registry.healingIterations);
            // Τυχαίος αριθμός θεραπείας κρουσμάτων κάθε case_heal_time από 0 έως h
            Random randomHealNumber = new Random();
            int healingAbility = randomHealNumber.nextInt(0, CASE_HEAL_RATE + 1);
            synchronized (registry) {
                while (registry.patientsBeingTreated == 0) {
                    System.out.println("Hospital is empty, waiting for patients...");
                    registry.notify();
                    // wait until producer notifies
                    registry.wait();
                }
                if (registry.patientsBeingTreated - healingAbility >= 0) {
                    // then the ones that can be healed are indeed healed
                    registry.healedPatients = healingAbility;
                    // and update the inhouse patients, by releasing the healed
                    registry.patientsBeingTreated -= registry.healedPatients;
                    // add them to the total that have been healed
                    registry.totalPatientsHealed += registry.healedPatients;
                } else {
                    // if the healing ability is greater than the patients receiving care, all patients will be healed
                    registry.healedPatients = registry.patientsBeingTreated;
                    // update the patients in the clinic (should be 0 in this case)
                    registry.patientsBeingTreated -= registry.healedPatients;
                    // add them to the total that have been healed
                    registry.totalPatientsHealed += registry.healedPatients;
                }
                System.out.println("Patients released from hospital: " + registry.healedPatients);
                System.out.println("Currently hospitalized: " + registry.patientsBeingTreated);
                IterationLog logEntry = new IterationLog(
                        registry.generationIterations,
                        registry.healingIterations,
                        registry.incomingPatients,
                        registry.patientsBeingTreated,
                        registry.patientsRejected,
                        registry.patientsAdmitted,
                        registry.totalSickPatients,
                        registry.healedPatients,
                        registry.totalPatientsHealed,
                        registry.totalPatientsRejected,
                        "HEAL"
                );
                registry.totalsLog.add(logEntry);
                // wake producer thread
                registry.notify();
            }
        }
    }
}

