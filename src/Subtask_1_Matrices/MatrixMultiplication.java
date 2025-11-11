// package (φάκελος) στον οποίο ανήκει η κλάση
package Subtask_1_Matrices;

// Εισάγω τα packages που θα χρειαστώ
// Για να δημιουργήσω τους τυχαίους αριθμούς
import java.util.Random;
// Για πράξεις μεταξύ πινάκων


public class MatrixMultiplication {
    // Τα κοινά δεδομένα για όλα τα threads
    // Ορίζω τις μεταβλητές μου ως private, γιατί δεν χρειάζονται κάπου αλλού
    private int[][] array;
    // Και το διάνυσμα m x 1
    private int[] vector;
    // Τα οποία θα δημιουργήσουν ένα διάνυσμα n x 1
    private int[] result;
    private int m;
    private int n;

    // our constructor
    public MatrixMultiplication(int m, int n) {
        // Κάνε initialize βάση των m και n που περνάω ως arguments
        this.m = m;
        this.n = n;
        // array allocation
        this.array = new int[n][m];
        this.vector = new int[m];
        this.result = new int[n];
        // Χρησιμοποιώ την κλάση Random() για να δημιουργήσω τους τυχαίους ακέραιους με το nextInt()
        Random random = new Random();

        // «γεμίζω» τον πίνακα array με τυχαίους αριθμούς μεταξύ 0 και 10
        for (int i = 0; i < n; i++){
            for (int j = 0; j < m; j++){
                array[i][j] = random.nextInt(10);
            }
        }
        // «γεμίζω» το διάνυσμα vector με τυχαίους αριθμούς μεταξύ 0 και 10
        for (int j = 0; j < m; j++){
            vector[j] = random.nextInt(10);
        }
    }

    // runnable: αυτή η κλάση μπορεί να τρέξει από ένα thread (the worker)
    private static class Worker implements Runnable {
        // τις δηλώνω ως final για να αποφύγω τυχόν τροποποίηση από λάθος
        private final int[][] array;
        private final int[] vector;
        private final int[] result;
        // Για να ξέρω πού βρίσκομαι με κάθε thread
        private final int startRow;
        private final int endRow;
        // για να αποθηκεύσω χρόνο για κάθε thread χρησιμοποιώ μεταβλητή τύπου long
        private final long threadTimes [];
        private final int row;

        // Ο constructor για τα threads
        public Worker(int[][] array, int[] vector, int[] result, int startRow, int endRow, long[] threadTimes, int row){
            this.array = array;
            this.vector = vector;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
            this.threadTimes = threadTimes;
            this.row = row;
        }

        // στο run() θα τρέξουν οι πολλαπλασιασμοί για κάθε thread
        // κάθε thread θα τρέχει τον πολλαπλασιασμό μιας σειράς με το διάνυσμα
        // Χρησιμοποιώ override γιατί ανατρέπω μια μέθοδο (την run()) της Runnable() superclass
        @Override
        public void run() {
            // nanoTime() for measuring elapsed time
            long startTime = System.nanoTime();
            // Κάθε thread υπολογίζει πολλές γραμμές
            for (int i = startRow; i < endRow; i++) {
                // j: ο αριθμός στηλών
                for (int j = 0; j < vector.length; j++) {
                    result[i] += array[i][j] * vector[j];
                }
            }
            long endTime = System.nanoTime();
            // αποθήκευσε τον χρόνο σε microseconds
            threadTimes[row] = (endTime - startTime) / 1000;
        }
    }

    // main: από εδώ θα ξεκινήσει την εκτέλεση του προγράμματος η JVM
    // controls the overall flow of the program, i.e. it's the orchestrator
    // String[] args: receive command-line arguments
    public static void main(String[] args) {
        // Μεταβλητές που ορίζουν τον πίνακα
        // το n (αριθμός γραμμών) πρέπει να είναι δύναμη του 2
        int n = 4096;
        // για το m (αριθμός στηλών του πίνακα και αριθμός γραμμών του διανύσματος) δεν υπάρχει περιορισμός
        int m = 1000;
        // get the matrix and vector
        MatrixMultiplication mm = new MatrixMultiplication(m, n);

        // Τρέξε το πείραμα για 1, 2, 4, 8 threads (array of integers)
        // ο αριθμός των νημάτων πρέπει επίσης να είναι δύναμη του 2 και n > k
        int[] threadCounts = {1, 2, 4, 8};
        // δημιουργώ ένα array για αποθήκευση των χρόνων
        long[] totalTimes = new long[threadCounts.length];

        for (int i = 0; i < threadCounts.length; i++) {
            int k = threadCounts[i];
            // Reset το result vector για κάθε πείραμα
            int[] result = new int[n];
            // αποθήκευσε τον χρόνο για κάθε run
            // calls multiplier to get the actual work done
            totalTimes[i] = multiplier(mm.array, mm.vector, result, n, k);
        }

        // Τύπωσε σύνοψη όλων των αποτελεσμάτων
        System.out.println("\n");
        System.out.println("Αποτελέσματα:\n");
        System.out.println("For a (" + n + " x " + m + ") matrix:\n");
        for (int i = 0; i < threadCounts.length; i++) {
            System.out.println(threadCounts[i] + " threads: " + totalTimes[i] + " microseconds");
        }
    }

    // Μέθοδος που τρέχει το πείραμα (ο manager)
    // static: it belongs to the class rather than an instance of the class
    private static long multiplier(int[][] array, int[] vector, int[] result, int n, int k) {
        // Δημιούργησε ένα array object «threads» με k threads
        Thread[] threads = new Thread[k];
        // Δημιούργησε array για χρόνους
        long[] threadTimes = new long[k];
        // Υπολόγισε πόσες γραμμές θα επεξεργάζεται κάθε thread
        int rowsPerThread = n / k;

        // Δημιουργία threads
        // Κάθε thread θα υπολογίζει το γινόμενο ενός τμήματος του μητρώου, ώστε να αποφύγουμε race conditions
        // για κάθε γραμμή του πίνακα, δημιούργησε ένα thread χρησιμοποιώντας τον constructor
        for (int row = 0; row < k; row++) {
            // αρχή του block
            int startRow = row * rowsPerThread;
            // τέλος του block
            int endRow = (row + 1) * rowsPerThread;
            // As soon as we create a new thread, it’s in the new state
            // Δημιουργεί νήμα "Thread-#" για το αντικείμενο
            threads[row] = new Thread(new Worker(array, vector, result, startRow, endRow, threadTimes, row));
        }

        // Ξεκίνα τη μέτρηση
        long startTime = System.nanoTime();

        // Ξεκίνα threads
        for (int i = 0; i < k; i++) {
            // Με το start, το thread θα τρέξει τη μέθοδο που έκανα Override νωρίτερα (δηλαδή τον πολλαπλασιασμό)
            // The thread remains in the NEW state until the program starts the thread using the start() method.
            threads[i].start();
        }
        // Περίμενε threads
        // Βεβαιώσου πως τα threads έχουν τελειώσει με το join()
        for (int i = 0; i < k; i++) {
            try {
                // Λέει στο main thread (το πρόγραμμα) να περιμένει ώστε να ολοκληρωθεί
                // αυτό το thread πριν προχωρήσει
                // join() puts the current thread on wait until the thread on which it is called is dead
                threads[i].join();
            // when a thread is interrupted, it will throw InterruptedException
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1000;
        return executionTime;
    }
}
