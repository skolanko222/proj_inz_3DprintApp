package main;

import com.mycompany.gui_proj_inz.MainGui;
import printer.BaseTransmHandler;
import printer.PrinterSettings;

public class Main {
    public static String DEFAULT_SETTINGS = "{\"baudRate\":9600,\"xMin\":0,\"xMax\":200,\"yMin\":0,\"yMax\":220,\"zMin\":0,\"zMax\":150,\"speed\":30,\"maxTempExt\":260,\"defaultTempExt\":100,\"maxTempBed\":80,\"defaultTempBed\":50,\"checkTempInterval\":5}";
    public static String DEFAULT_PROFILE_PATH = "C:\\Users\\Szymon\\Documents\\ProjektyStudia\\proj_inz_3DprintApp\\profiles\\";
    MainGui mainGui = new MainGui();


    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainGui().setVisible(true);
            }
        });

    }
}
