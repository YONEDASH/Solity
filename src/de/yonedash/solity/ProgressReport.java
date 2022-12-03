package de.yonedash.solity;

public interface ProgressReport {

    int getProgressTotal();
    int getProgress();

    default double reportProgress() {
        return getProgressTotal() == 0 ? 0 : getProgress() / (double) getProgressTotal();
    }

}
