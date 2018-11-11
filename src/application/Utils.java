package application;

import java.io.ByteArrayInputStream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


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

    static BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi= ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }
}