package connection.server;

public class Printer {
    private String printerId;

    public Printer(String printerId) {
        this.printerId = printerId;
    }

    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }
}