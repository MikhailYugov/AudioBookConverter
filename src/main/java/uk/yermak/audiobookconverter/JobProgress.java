package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.ProgressView;
import com.freeipodsoftware.abc.StateListener;
import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class JobProgress implements Runnable, StateListener {
    private ProgressView progressView;
    private Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
    private long startTime = System.currentTimeMillis();
    private boolean finished;
    private int totalFiles;
    private int completedFiles;
    private long totalDuration;
    private Map<String, Long> durations = new HashMap<>();
    private Map<String, Long> sizes = new HashMap<>();
    private boolean paused;
    private boolean cancelled;
    private long pausedDuration;

    public JobProgress(ConversionStrategy conversionStrategy, ProgressView progressView, List<MediaInfo> media) {
        this.progressView = progressView;

        for (MediaInfo mediaInfo : media) {
            progressCallbacks.put(mediaInfo.getFileName(), new ProgressCallback(mediaInfo.getFileName(), this));
            totalFiles++;
            totalDuration += mediaInfo.getDuration();
        }
        progressCallbacks.put("output", new ProgressCallback("output", this));
        conversionStrategy.setCallbacks(progressCallbacks);
        StateDispatcher.getInstance().addListener(this);
    }


    @Override
    public void run() {
        progressView.getDisplay().syncExec(() -> {
            progressView.setInfoText(Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles);
            progressView.setProgress(0);
            progressView.setRemainingTime(10 * 60 * 1000);
        });

        while (!finished && !cancelled && !paused) {
            progressView.getDisplay().syncExec(() -> {
                progressView.setElapsedTime(System.currentTimeMillis() - startTime);
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void converted(String fileName, long timeInMillis, long size) {
        if (paused || cancelled) return;
        int currentDuration = 0;
        durations.put(fileName, timeInMillis);

        for (Long l : durations.values()) {
            currentDuration += l;
        }

        long estimatedSize = 0;
        sizes.put(fileName, size);
        for (Long l : sizes.values()) {
            estimatedSize += l;
        }

        if (currentDuration > 0 && totalDuration > 0) {
            double progress = (double) currentDuration / totalDuration;
            long now = System.currentTimeMillis();
            long remainingTime = (long) ((now - startTime) / progress - (now - startTime));
            long finalSize = estimatedSize;
            progressView.getDisplay().syncExec(() -> {
                progressView.setProgress((int) (progress * 100));
                progressView.setRemainingTime(remainingTime);
                progressView.setEstimatedFinalOutputSize((long) (finalSize / progress));
            });
        }

    }

    public synchronized void incCompleted(String fileName) {
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles != totalFiles) {
            progressView.getDisplay().syncExec(() -> {
                progressView.setInfoText(Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles);
            });
        } else {
            progressView.getDisplay().syncExec(() -> {
                progressView.setInfoText("Updating media information...");
            });
            finished = true;
        }
    }

    @Override
    public void finishedWithError(String error) {
        finished = true;
    }

    @Override
    public void finished() {
        finished = true;
    }

    @Override
    public void canceled() {
        cancelled = true;
    }

    @Override
    public void paused() {
        pausedDuration = System.currentTimeMillis() - startTime;
        paused = true;
    }

    @Override
    public void resumed() {
        startTime = System.currentTimeMillis() + pausedDuration;
        paused = false;
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progressView.getDisplay().syncExec(() -> {
            progressView.setProgress(0);
            progressView.setRemainingTime(60 * 1000);
        });
    }
}