package io.hobaskos.event.service.util;

/**
 * Created by alex on 2/15/17.
 */
public class ContentTypeUtil {

    public static final String AUDIO_MIME_REGEX = "^audio\\/(mpeg|mp3|mp4)$";
    public static final String IMAGE_MIME_REGEX = "^image\\/(jpeg|jpg|png|gif)$";

    /**
     * Generate file suffix out of mime/contentType for image files
     * @param fileContentType
     * @return the suffix
     */
    public static String defineImageName(String fileContentType) {
        final String result;
        if (fileContentType != null) {
            if (fileContentType.endsWith("/jpeg")) {
                result = "jpg";
            } else if (fileContentType.endsWith("/png")) {
                result = "png";
            } else if (fileContentType.endsWith("/gif")) {
                result = "gif";
            } else {
                result = "jpg";
            }
        } else {
            result = "jpg";
        }
        return result;
    }

    /**
     * Generate file suffix out of mime/contentType for audio files
     * @param fileContentType
     * @return the suffix
     */
    public static String defineAudioFile(String fileContentType) {
        if (fileContentType != null) {
            if (fileContentType.endsWith("/mpeg")) {
                return "mp3";
            } else if (fileContentType.endsWith("/mp4")) {
                return "mp4";
            } else {
                return "mp3";
            }
        } else {
            return "mp3";
        }
    }
}
