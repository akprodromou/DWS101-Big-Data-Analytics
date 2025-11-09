// package (φάκελος) στον οποίο ανήκει η κλάση
package Subtask_1_Matrices;

// Εισάγω τα packages που θα χρειαστώ
// Για να δημιουργήσω τους τυχαίους αριθμούς
import java.util.Random;
// Για πράξεις μεταξύ πινάκων
import java.util.Arrays;

// runnable: αυτή η κλάση μπορεί να τρέξει από ένα thread
public class MatrixMultiplication implements Runnable {
    // Μεταβλητές που ορίζουν τον πίνακα
    // το n (αριθμός γραμμών) πρέπει να είναι δύναμη του 2
    int n = 16;
    // για το m (αριθμός στηλών του πίνακα και αριθμός γραμμών του διανύσματος) δεν υπάρχει περιορισμός
    int m = 10;
    // ο αριθμός των νημάτων πρέπει επίσης να είναι δύναμη του 2 και n > k
    int k = 2;

    // Κάνω initialize τα object fields
    // Ορίζω τον πίνακά μου n x m
    int[][] array = new int[n][m];
    // Και το διάνυσμα m x 1
    int[] vector = new int[m];
    // Τα οποία θα δημιουργήσουν ένα διάνυσμα n x 1
    int[] result = new int[n];
    // το γινόμενο της σειράς που θα υπολογίζει κάθε thread
    int row;

    // Ας δημιουργήσω ένα instance του Random για τους τυχαίους αριθμούς
    Random rand = new Random();

    // για να αποθηκεύσω χρόνο για κάθε thread
    long[] threadTimes;

    // Για να ξέρω πού βρίσκομαι με κάθε thread
    int startRow;
    int endRow;

    // Ο constructor της κλάσης για τα threads
    public MatrixMultiplication(int[][] array, int[] vector, int[] result, int startRow, int endRow, long[] threadTimes, int threadId) {
        // initialize the fields of the object using the parameters we pass on
        this.array = array;
        this.vector = vector;
        this.result = result;
        this.startRow = startRow;
        this.endRow = endRow;
        this.threadTimes = threadTimes;
        this.row = threadId;
    }

    // Δημιούργησε έναν empty constructor στο main() για να κάνω initialize τις μεταβλητές μου
    public MatrixMultiplication() {}

    // κάθε thread θα τρέχει τον πολλαπλασιασμό μιας σειράς με το διάνυσμα
    // Χρησιμοποιώ override γιατί ανατρέπω μια μέθοδο (την run()) της Runnable() superclass
    @Override
    public void run() {
        // nanoTime() for measuring elapsed time
        long startTime = System.nanoTime();
        // Κάθε thread υπολογίζει πολλές γραμμές
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < vector.length; j++) {
                result[i] += array[i][j] * vector[j];
            }
        }
        long endTime = System.nanoTime();
        // αποθήκευσε τον χρόνο σε microseconds
        threadTimes[row] = (endTime - startTime) / 1000;
    }

    // Δημιουργώ έναν constructor για το initialization logic
    // void: no return argument
    // main: από εδώ θα ξεκινήσει την εκτέλεση του προγράμματος η JVM
    // String[] args: receive command-line arguments
    public static void main(String[] args) {
        // Δημιουργώ ένα object instance της κλάσης MatrixMultiplication
        MatrixMultiplication mm = new MatrixMultiplication();
        // «γεμίζω» τον πίνακα Α με τυχαίους αριθμούς μεταξύ 0 και 10
        for (int i = 0; i < mm.n; i++) {
            for (int j = 0; j < mm.m; j++) {
                mm.array[i][j] = mm.rand.nextInt(0, 11);
            }
        }
        // «γεμίζω» το διάνυσμα v με τυχαίους αριθμούς μεταξύ 0 και 10
        for (int i = 0; i < mm.m; i++) {
            mm.vector[i] = mm.rand.nextInt(0, 11);
        }

        // τύπωσε τον πίνακα
        System.out.println("Matrix A:");
        for (int i = 0; i < mm.n; i++) {
            System.out.println(Arrays.toString(mm.array[i]));
        }
        // τύπωσε το διάνυσμα
        System.out.println("Vector v:");
        System.out.println(Arrays.toString(mm.vector));
        // Τρέξε το πείραμα για 1, 2, 4, 8 threads
        int[] threadCounts = {1, 2, 4, 8};
        // δημιουργώ ένα array για αποθήκευση των χρόνων
        long[] totalTimes = new long[threadCounts.length];

        for (int idx = 0; idx < threadCounts.length; idx++) {
            int k = threadCounts[idx];
            System.out.println("\n");
            System.out.println("Εκτέλεση με " + k + " threads:\n");

            // Reset το result vector για κάθε πείραμα
            int[] result = new int[mm.n];

            // αποθήκευσε τον χρόνο για κάθε run
            totalTimes[idx] = runExperiment(mm.array, mm.vector, result, mm.n, k);

            // Τύπωσε το αποτέλεσμα (μόνο την πρώτη φορά για έλεγχο)
            if (idx == 0) {
                System.out.println("Result vector:");
                System.out.println(Arrays.toString(result));
            }
        }

        // Τύπωσε σύνοψη όλων των αποτελεσμάτων
        System.out.println("\n");
        System.out.println("Αποτελέσματα:\n");
        for (int i = 0; i < threadCounts.length; i++) {
            System.out.println(threadCounts[i] + " threads: " + totalTimes[i] + " microseconds");
        }
    }

    // Μέθοδος που τρέχει το πείραμα
    // static: it belongs to the class rather than an instance of the class
    public static long runExperiment(int[][] array, int[] vector, int[] result, int n, int k) {
        // Δημιούργησε ένα array object «threads» με k threads
        Thread[] threads = new Thread[k];
        // Δημιούργησε array για χρόνους
        long[] threadTimes = new long[k];
        // Υπολόγισε πόσες γραμμές θα επεξεργάζεται κάθε thread
        int rowsPerThread = n / k;

        // Κάθε thread θα υπολογίζει το γινόμενο μιας σειράς, ώστε να αποφύγουμε race conditions
        // για κάθε γραμμή του πίνακα, δημιούργησε ένα thread χρησιμοποιώντας τον constructor
        for (int i = 0; i < k; i++) {
            int startRow = i * rowsPerThread;
            int endRow = (i + 1) * rowsPerThread;
            // As soon as we create a new thread, it’s in the NEW state
            threads[i] = new Thread(new MatrixMultiplication(array, vector, result, startRow, endRow, threadTimes, i));
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

        System.out.println("Συνολικός χρόνος: " + executionTime + " microseconds");

        System.out.println("Χρόνοι ανά thread:");
        for (int i = 0; i < k; i++) {
            System.out.println("  Thread " + i + " took " + threadTimes[i] + " microseconds");
        }

        return executionTime;
    }
}
