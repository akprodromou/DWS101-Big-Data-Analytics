package Subtask_2_ProducerConsumer;

public class IterationLog {
    int generationIterations;
    int healingIterations;
    int incomingPatients;
    int patientsBeingTreated;
    int patientsRejected;
    int patientsAdmitted;
    int totalSickPatients;
    int healedPatients;
    int totalPatientsHealed;
    int totalPatientsRejected;
    String eventType;

    // not static: it requires an IterationLog() instance to be created first, in order to exist
    public IterationLog(int generationIterations, int healingIterations, int incomingPatients, int patientsBeingTreated, int patientsRejected, int patientsAdmitted,
                        int totalSickPatients, int healedPatients, int totalPatientsHealed, int totalPatientsRejected, String type) {
        this.generationIterations = generationIterations;
        this.healingIterations = healingIterations;
        this.incomingPatients = incomingPatients;
        this.patientsBeingTreated = patientsBeingTreated;
        this.patientsRejected = patientsRejected;
        this.patientsAdmitted = patientsAdmitted;
        this.totalSickPatients = totalSickPatients;
        this.healedPatients = healedPatients;
        this.totalPatientsHealed = totalPatientsHealed;
        this.totalPatientsRejected = totalPatientsRejected;
        this.eventType = type;
    }

    public String toCSV() {
        return String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s",
                generationIterations, healingIterations, incomingPatients,
                patientsBeingTreated, patientsRejected, patientsAdmitted,
                totalSickPatients, healedPatients, totalPatientsHealed,
                totalPatientsRejected, eventType);
    }
}
