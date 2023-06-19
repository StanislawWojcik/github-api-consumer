package stanislaw.wojcik.githubapiconsumer.exception;

public class InvalidHeaderException extends RuntimeException {
    public InvalidHeaderException(final String message) {
        super(message);
    }
}
