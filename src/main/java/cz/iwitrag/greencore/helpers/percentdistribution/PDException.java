package cz.iwitrag.greencore.helpers.percentdistribution;

public class PDException extends Exception {

    private String message;

    PDException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
