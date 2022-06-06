package com.komorebi.video.DBUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RecordingDTO {

    private static final String AMAZON_CONNECT_AUDIO_PREFIX_FORMAT = "connect/ccm-connect-test/CallRecordings/%02d/%02d/%02d/%s";
    private static final String KOMOREBI_INPUT_RECORDING_PATH_FORMAT = "%02d/%02d/%02d/%s/%s.mp4";
    private static final String KOMOREBI_OUTPUT_VIDEO_PATH_FORMAT = "%02d/%02d/%02d/%s/%s/recording.mp4";
    private static final String KOMOREBI_OUTPUT_THUMBNAIL_FORMAT = "%02d/%02d/%02d/%s/%s/thumbnail.png";

    private final String videoID;
    private final String agentID;
    private final Calendar date;

    public RecordingDTO(Recording recording) throws Exception {
        this.videoID = recording.getVideoID();
        this.agentID = recording.getAgentID();
        this.date = Calendar.getInstance();
        this.parseDate(recording.getDate());
    }

    public String getVideoID() {
        return videoID;
    }

    public String getAgentID() {
        return agentID;
    }

    public Calendar getDate() {
        return date;
    }

    public String buildACRecordingPrefix(){
        return String.format(
            AMAZON_CONNECT_AUDIO_PREFIX_FORMAT,
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH)+1,
            date.get(Calendar.DAY_OF_MONTH),
            videoID
        );
    }

    public String buildKomorebiInputRecordingPath(){
        return buildS3FilePath(KOMOREBI_INPUT_RECORDING_PATH_FORMAT);
    }


    public String buildKomorebiOutputVideoPath(){
        return buildS3FilePath(KOMOREBI_OUTPUT_VIDEO_PATH_FORMAT);
    }

    public String buildKomorebiOutputThumbnailPath(){
        return buildS3FilePath(KOMOREBI_OUTPUT_THUMBNAIL_FORMAT);
    }

    private void parseDate(String dateStr)throws Exception{
        try{
            SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date simpleDate = dateFormat.parse(dateStr);
            this.date.setTime(simpleDate);
        } catch (Exception e){
            throw new Exception("error while parsing date", e);
        }
    }

    public String buildS3FilePath(String format){
        return String.format(
            format,
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH)+1,
            date.get(Calendar.DAY_OF_MONTH),
            agentID,
            videoID
        );
    }
}
