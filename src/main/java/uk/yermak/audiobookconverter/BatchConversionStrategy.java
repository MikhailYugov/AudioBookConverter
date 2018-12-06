package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private final ExecutorService executorService = Executors.newWorkStealingPool();
    private final StatusChangeListener listener;
    private Conversion conversion;


    public BatchConversionStrategy(Conversion conversion) {
        this.conversion = conversion;
        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
    }

    @Override
    protected String getTempFileName(long jobId, int index, String extension) {
        return "";
    }

    public void run() {
        List<Future<ConverterOutput>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < this.media.size(); ++i) {
                MediaInfo mediaInfo = this.media.get(i);
                String outputFileName = this.determineOutputFilename(mediaInfo.getFileName());
                Future<ConverterOutput> converterFuture =
                        executorService
                                .submit(new FFMpegConverter(conversion, outputParameters, mediaInfo, outputFileName, progressCallbacks.get(mediaInfo.getFileName())));
                futures.add(converterFuture);
            }

            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversion);
            for (Future<ConverterOutput> future : futures) {
                ConverterOutput output = future.get();
                ArtWork artWork = output.getMediaInfo().getArtWork();
                if (artWork != null) {
                    artBuilder.updateSinglePoster(artWork, 0, output.getOutputFileName());
                }
            }
            conversion.finished();
        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversion.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            conversion.removeStatusChangeListener(listener);
        }
    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if (outputDestination == null) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.outputDestination, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if (!outputFilename.endsWith(".m4b")) {
            outputFilename = outputFilename + ".m4b";
        }

        return Utils.makeFilenameUnique(outputFilename);
    }


    @Override
    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = IntStream.range(0, media.size()).mapToObj(i -> "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList());

        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

        return fileListFile;
    }


    @Override
    public void setOutputDestination(String outputDestination) {
        this.outputDestination = outputDestination;
    }
}
