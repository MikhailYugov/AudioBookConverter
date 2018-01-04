package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Mp4Tags;
import com.freeipodsoftware.abc.StateListener;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.ProgressCallback;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.util.List;
import java.util.Map;

public abstract class AbstractConversionStrategy implements ConversionStrategy, StateListener {
    protected boolean finished;
    protected boolean canceled;
    protected boolean paused;
    protected Mp4Tags mp4Tags;
    protected List<MediaInfo> media;
    protected Map<String, ProgressCallback> progressCallbacks;


    protected AbstractConversionStrategy() {
    }

    public void setMp4Tags(Mp4Tags mp4Tags) {
        this.mp4Tags = mp4Tags;
    }

    @Override
    public String getAdditionalFinishedMessage() {
        return "";
    }

    public void start(Shell shell) {
        this.canceled = false;
        this.finished = false;
        StateDispatcher.getInstance().addListener(this);
        this.startConversion();
    }

    protected static String selectOutputFile(Shell shell, String filenameSuggestion) {
        FileDialog fileDialog = new FileDialog(shell, 8192);
        fileDialog.setFilterNames(new String[]{" (*.m4b)"});
        fileDialog.setFilterExtensions(new String[]{"*.m4b"});
        fileDialog.setFileName(filenameSuggestion);
        String fileName = fileDialog.open();
        if (fileName == null) return null;
        if (!fileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
            fileName = fileName + ".m4b";
        }
        return fileName;
    }


    protected abstract void startConversion();


    protected String getOuputFilenameSuggestion(String fileName) {
        String mp3Filename = fileName;
        return mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
    }

    @Override
    public void setMedia(List<MediaInfo> media) {
        this.media = media;
    }

    @Override
    public void setCallbacks(Map<String, ProgressCallback> progressCallbacks) {
        this.progressCallbacks = progressCallbacks;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {
        canceled = true;
    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }

    protected void prepareFilesAndFillMeta(long jobId, List<String> outFiles, List<String> metaData, Mp4Tags mp4Tags, List<MediaInfo> mediaInfos) {
        metaData.add(";FFMETADATA1");
        metaData.add("major_brand=M4A");
        metaData.add("minor_version=512");
        metaData.add("compatible_brands=isomiso2");
        metaData.add("title=" + mp4Tags.getTitle());
        metaData.add("artist=" + mp4Tags.getWriter());
        metaData.add("album=" + (StringUtils.isNotBlank(mp4Tags.getSeries()) ? mp4Tags.getSeries() : mp4Tags.getTitle()));
        metaData.add("composer=" + mp4Tags.getNarrator());
        metaData.add("comment=" + mp4Tags.getComment());
        metaData.add("track=" + mp4Tags.getTrack() + "/" + mp4Tags.getTotalTracks());
        metaData.add("media_type=2");
        metaData.add("genre=Audiobook");
        metaData.add("encoder=" + "https://github.com/yermak/AudioBookConverter");

        long totalDuration = 0;
        for (int i = 0; i < mediaInfos.size(); i++) {
            metaData.add("[CHAPTER]");
            metaData.add("TIMEBASE=1/1000");
            metaData.add("START=" + totalDuration);
            totalDuration += mediaInfos.get(i).getDuration();
            metaData.add("END=" + totalDuration);
            metaData.add("title=Chapter " + (i + 1));
            outFiles.add("file '" + getTempFileName(jobId, mediaInfos.get(i).hashCode(), ".m4b") + "'");
        }
    }

    protected abstract String getTempFileName(long jobId, int currentFileNumber, String extension);

}
