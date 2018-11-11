package application;

import java.io.ByteArrayInputStream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import static org.opencv.core.CvType.CV_8U;

public final class Utils {
    public Utils() {
    }

    protected static Image mat2Image(Mat frame) {
        try {
            MatOfByte byteMat = new MatOfByte();
            Imgcodecs.imencode(".png", frame, byteMat);

            return new Image(new ByteArrayInputStream(byteMat.toArray()));
        } catch (Exception var2) {
            System.err.println(var2);
            return null;
        }
    }

    protected static Mat getNegative(Mat frame)
    {
        Mat neg = Mat.zeros(frame.size(), CvType.CV_8UC3), white = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        System.out.println("Color:" + white.get(0, 0)[0] + ' ' + white.get(0, 0)[1] + ' ' + white.get(0, 0)[2]);
        Core.subtract(white, frame, neg);
        return neg;
    }
}