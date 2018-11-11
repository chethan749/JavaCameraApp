package application;

import java.io.ByteArrayInputStream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public final class Utils {
    public Utils() {
    }

    public static Image mat2Image(Mat frame) {
        try {
            MatOfByte byteMat = new MatOfByte();
            Imgcodecs.imencode(".png", frame, byteMat);

            return new Image(new ByteArrayInputStream(byteMat.toArray()));
        } catch (Exception var2) {
            System.err.println(var2);
            return null;
        }
    }
}