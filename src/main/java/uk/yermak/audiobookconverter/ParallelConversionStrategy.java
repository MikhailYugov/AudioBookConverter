package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ParallelConversionStrategy implements ConversionStrategy {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ExecutorService executorService = Executors.newWorkStealingPool();
    private Conversion conversion;
    private Map<String, ProgressCallback> progressCallbacks;


    public ParallelConversionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
        this.conversion = conversion;
        this.progressCallbacks = progressCallbacks;
    }

    public void run() {
        List<Future<String>> futures = new ArrayList<>();
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, 999999, ".m4b");

        File fileListFile = null;
        File metaFile = null;
        try {
//            conversion.getOutputParameters().updateAuto(conversion.getMedia());

            fileListFile = prepareFiles(jobId);


            List<MediaInfo> prioritizedMedia = prioritiseMedia();
            for (MediaInfo mediaInfo : prioritizedMedia) {
                String tempOutput = getTempFileName(jobId, mediaInfo.hashCode(), ".m4b");
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName());
                Future<String> converterFuture = executorService.submit(new FFMpegNativeConverter(conversion, mediaInfo, tempOutput, callback));
                futures.add(converterFuture);
            }

            for (Future<String> future : futures) {
                if (conversion.getStatus().isOver()) return;
                String outputFileName = future.get();
                logger.debug("Waited for completion of {}", outputFileName);
            }
            if (conversion.getStatus().isOver()) return;
            metaFile = new MetadataBuilder().prepareMeta(jobId, conversion.getBookInfo(), conversion.getConverable());
            FFMpegConcatenator concatenator = new FFMpegConcatenator(conversion, tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), progressCallbacks.get("output"));
            concatenator.concat();

            if (conversion.getStatus().isOver()) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversion);
            artBuilder.coverArt(tempFile);

            if (conversion.getStatus().isOver()) return;
            File destFile = new File(conversion.getOutputDestination());
            if (destFile.exists()) FileUtils.deleteQuietly(destFile);
            FileUtils.moveFile(new File(tempFile), destFile);
            conversion.finished();
        } catch (Exception e) {
            logger.error("Error during parallel conversion", e);
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversion.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            for (MediaInfo mediaInfo : conversion.getMedia()) {
                FileUtils.deleteQuietly(new File(getTempFileName(jobId, mediaInfo.hashCode(), ".m4b")));
            }
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }

    private List<MediaInfo> prioritiseMedia() {
        return conversion.getMedia().stream().sorted((o1, o2) -> (int) (o2.getDuration() - o1.getDuration())).collect(Collectors.toList());
    }


    protected String getTempFileName(long jobId, int index, String extension) {
        return Utils.getTmp(jobId, index, extension);
    }

    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = conversion.getMedia().stream().map(mediaInfo -> "file '" + getTempFileName(jobId, mediaInfo.hashCode(), ".m4b") + "'").collect(Collectors.toList());
        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);
        return fileListFile;
    }
}
