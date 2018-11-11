package application;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

    protected static Mat getNegative(Mat frame) {
        Mat neg = Mat.zeros(frame.size(), CvType.CV_8UC3), white = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
//        System.out.println("Color:" + white.get(0, 0)[0] + ' ' + white.get(0, 0)[1] + ' ' + white.get(0, 0)[2]);
        Core.subtract(white, frame, neg);
        return neg;
    }

    static BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi= ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }
}