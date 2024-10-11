package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import connection.PrinterSettings;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PrinterSettingsTest {

    PrinterSettings printerSettings = new PrinterSettings(9600, null);
    String DEFAULT_SETTINGS = PrinterSettings.DEFAULT_SETTINGS;
    String DEFAULT_PROFILE_PATH = PrinterSettings.DEFAULT_SETTINGS;
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void checkSerialization(){
        String json = printerSettings.serializeProfile();
        assertEquals(DEFAULT_SETTINGS, json);
    }
    @Test
    void checkReadProfileFromFile() {
        PrinterSettings test = PrinterSettings.readProfileFromFile(DEFAULT_PROFILE_PATH, printerSettings.getProfileName());
        System.out.println(test);
    }


    @Test
    void checkDeserialization(){
        PrinterSettings printerSettings = PrinterSettings.deserializeProfile(DEFAULT_SETTINGS);
        System.out.println(printerSettings.getProfileName());
        assertEquals(9600, printerSettings.getBaudRate());
        assertEquals(0, printerSettings.getxMin());
        assertEquals(200, printerSettings.getxMax());
        assertEquals(0, printerSettings.getyMin());
        assertEquals(220, printerSettings.getyMax());
        assertEquals(0, printerSettings.getzMin());
        assertEquals(150, printerSettings.getzMax());
        assertEquals(30, printerSettings.getSpeed());
        assertEquals(260, printerSettings.getMaxTempExt());
        assertEquals(100, printerSettings.getDefaultTempExt());
        assertEquals(80, printerSettings.getMaxTempBed());
        assertEquals(50, printerSettings.getDefaultTempBed());
        assertEquals(5, printerSettings.getCheckTempInterval());
    }

    @Test
    void saveProfileToFile(){
        printerSettings.saveProfileToFile(DEFAULT_PROFILE_PATH);
        try {
            // read the whole file content
            String text = Files.readString(Paths.get(DEFAULT_PROFILE_PATH + printerSettings.getProfileName() + ".json"), StandardCharsets.US_ASCII);
            // check if the content is correct
            assertEquals(DEFAULT_SETTINGS, text);
        } catch (Exception e){
            fail("File not found");
        }
    }
}