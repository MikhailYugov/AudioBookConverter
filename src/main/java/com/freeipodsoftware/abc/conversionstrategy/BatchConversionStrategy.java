package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.BatchModeOptionsDialog;
import com.freeipodsoftware.abc.Util;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.FFMpegConverter;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private boolean intoSameFolder;
    private String folder;
    private int currentFileNumber;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;

    public BatchConversionStrategy() {
    }

    public long getOutputSize() {
        return 0;
        /*return this.canceled ? 0L : (new File(this.outputFileName)).length();*/
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L ? (int) ((double) this.currentInputFileBytesProcessed / (double) this.currentInputFileSize * 100.0D) : 0;
    }

    public boolean makeUserInterview(Shell shell) {
        BatchModeOptionsDialog options = new BatchModeOptionsDialog(shell);
        options.setFolder(this.getSuggestedFolder());
        if (options.open()) {
            this.intoSameFolder = options.isIntoSameFolder();
            this.folder = options.getFolder();
            return true;
        } else {
            return false;
        }
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public void run() {
        List<Future> futures = new ArrayList<>();

        for (int i = 0; i < this.inputFileList.length; ++i) {
            this.currentFileNumber = i + 1;
            String outputFileName = this.determineOutputFilename(this.inputFileList[i]);
            MediaInfo mediaInfo = Utils.determineChannelsAndFrequency(this.inputFileList[i]);
            this.mp4Tags = Util.readTagsFromInputFile(this.inputFileList[i]);

            Future converterFuture =
                    Executors.newWorkStealingPool()
                            .submit(new FFMpegConverter(mediaInfo, outputFileName));
            futures.add(converterFuture);
        }
        try {
            for (Future future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            this.finishListener.finished();
        }
    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if (this.intoSameFolder) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.folder, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if (!outputFilename.endsWith(".m4b")) {
            outputFilename = outputFilename + ".m4b";
        }

        return Util.makeFilenameUnique(outputFilename);
    }

    public String getInfoText() {
        return Messages.getString("BatchConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.inputFileList.length;
    }

}
