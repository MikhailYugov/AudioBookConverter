package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 1/11/2018.
 */
public interface ArtWork {
    String getFormat();

    void setFormat(String format);

    Long getCrc32();

    void setCrc32(Long crc32);

    String getFileName();

    void setFileName(String fileName);
}
