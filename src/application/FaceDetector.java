package application;

import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;

public class FaceDetector {
    CascadeClassifier faceDetector;
    MatOfRect faceDetections;

    public FaceDetector() {
        faceDetector = new CascadeClassifier();
        faceDetector.load("haarcascade_frontalface_alt.xml");
    }

    public MatOfRect getFaces(Mat image) {
        faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        return faceDetections;
    }

    public Mat drawFaces(Mat image, MatOfRect faces) {
        for (Rect rect : faces.toArray())
        {
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }
        return image;
    }
}
