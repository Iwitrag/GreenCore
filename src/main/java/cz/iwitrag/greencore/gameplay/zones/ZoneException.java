package cz.iwitrag.greencore.gameplay.zones;

public class ZoneException extends Exception {

    private String message;

    public ZoneException() {
        this.message = null;
    }
    public ZoneException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
