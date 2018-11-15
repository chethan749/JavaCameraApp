package application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.SparsePyrLKOpticalFlow;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class CameraAppFXController {
    @FXML
    private Button cameraButton;
    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView trackerFrame;
    @FXML
    private Button trackerButton;
    private ScheduledExecutorService timer;
    private ScheduledExecutorService timer2;
    private VideoCapture capture = new VideoCapture();
    private boolean blackAndWhite;
    private boolean blur;
    private boolean negative;
    private boolean cameraActive = false;
    private static int cameraId = 0;
    private Image imageToShow;
    private Mat frame;
    private String base_file = "snaps/";
    private FaceDetector faceDetector;
    private MatOfRect faces;
    private Utils utils;

    public CameraAppFXController()
    {
        blackAndWhite = false;
        blur = false;
        negative = false;
        faceDetector = new FaceDetector();
        utils = new Utils();
    }

    @FXML
    protected void startCamera(ActionEvent event)
    {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    public void run() {
                        frame = CameraAppFXController.this.grabFrame();

                        Mat frameCopy = frame.clone();

                        Imgproc.putText(frameCopy, "Faces detected: " + ((faces != null && !faces.empty())? faces.rows() : 0), new Point(400, 30), Core.FONT_HERSHEY_COMPLEX, 0.7, new Scalar(1, 0, 0), 2);
                        if(faces != null && !faces.empty())
                            imageToShow = Utils.mat2Image(faceDetector.drawFaces(frameCopy, faces));
                        else
                            imageToShow = Utils.mat2Image(frameCopy);

                        CameraAppFXController.this.updateImageView(CameraAppFXController.this.currentFrame, imageToShow);
                    }
                };

                Runnable faceDetectorRunnable = new Runnable() {
                    public void run() {
                        try {
                            if(cameraActive && !blur && !blackAndWhite && !negative)
                                faces = faceDetector.getFaces(frame);
                            else
                                faces.release();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e);
                        }
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0L, 1L, TimeUnit.MILLISECONDS);
                this.timer2 = Executors.newSingleThreadScheduledExecutor();
                this.timer2.scheduleAtFixedRate(faceDetectorRunnable, 1000L, 1L, TimeUnit.MILLISECONDS);

                this.cameraButton.setText("Stop Camera");
            }
            else
                System.err.println("Impossible to open the camera connection...");
        }
        else {
            this.cameraActive = false;
            this.cameraButton.setText("Start Camera");
            this.stopCamera();
        }
    }

    @FXML
    protected void startTracking(ActionEvent event)
    {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    public void run() {
                        frame = CameraAppFXController.this.grabFrame();

                        Mat frameCopy = frame.clone();

                        imageToShow = Utils.mat2Image(frameCopy);

                        CameraAppFXController.this.updateImageView(CameraAppFXController.this.trackerFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0L, 1L, TimeUnit.MILLISECONDS);

                this.trackerButton.setText("Stop Tracking");
            }
            else {
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else {
            this.cameraActive = false;
            this.trackerButton.setText("Start Tracking");
            this.stopTracking();
        }
    }

    @FXML
    protected void setBlackAndWhite(ActionEvent event)
    {
        blackAndWhite = true;
        blur = false;
        negative = false;
    }

    @FXML
    protected void setBlur(ActionEvent event)
    {
        blackAndWhite = false;
        blur = true;
        negative = false;
    }

    @FXML
    protected void setNegative(ActionEvent event)
    {
        blackAndWhite = false;
        blur = false;
        negative = true;
    }

    @FXML
    protected void setFaceswap(ActionEvent event)
    {
        blackAndWhite = false;
        blur = false;
        negative = false;
    }

    @FXML
    protected void noEffects(ActionEvent event) {
        blackAndWhite = false;
        blur = false;
        negative = false;
    }

    @FXML
    protected void save_image(ActionEvent event) {
        System.out.println(imageToShow);
        BufferedImage image;
        try {
            image = utils.Mat2BufferedImage(frame);
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss").format(new Date());
            String filename = "Snap-"+timeStamp;
            File file = new File(base_file + filename);
            try {
                ImageIO.write(image, "png", file);
                System.out.println("File " + filename + " saved!");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            System.out.println("Image not saved!");
            e.printStackTrace();
        }
    }

    @FXML
    protected void pushPoint(ActionEvent event)
    {
    }

    private Mat grabFrame() {
        Mat frame = new Mat();
        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if (!frame.empty()) {
                    if(blackAndWhite)
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    if(blur)
                        Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0);
                    if(negative)
                        frame = Utils.getNegative(frame);
//                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BayerGB2RGB_VNG);
                }
            } catch (Exception var3) {
                System.err.println(var3);
            }
        }

        return frame;
    }

    private void stopCamera() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.currentFrame.setImage(null);
            } catch (InterruptedException var2) {
                System.err.println(var2);
            }
        }

        if (this.timer2 != null && !this.timer2.isShutdown()) {
            try {
                this.timer2.shutdown();
                this.timer2.awaitTermination(33L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException var2) {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void stopTracking() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try
            {
                this.timer.shutdown();
                this.timer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.trackerFrame.setImage(null);
            }
            catch (InterruptedException var2)
            {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }

    }

    private void updateImageView(ImageView view, Image image) {
        view.setImage(image);
    }

    protected void setClosed() {
        this.stopCamera();
        this.stopTracking();
    }
}