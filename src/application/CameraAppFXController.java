package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;

public class CameraAppFXController {
    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;
    private Image imageToShow;
    private Mat frame;
    private String base_file = "snaps/";
    private String filter = null;
    private FaceDetector faceDetector;
    private MatOfRect faces;
    private Utils utils;

    public CameraAppFXController() {
        faceDetector = new FaceDetector();
        utils = new Utils();
    }

    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    public void run() {
                        frame = CameraAppFXController.this.grabFrame();

//                        System.out.println(frame.get(0, 0)[0]);
                        faces = faceDetector.getFaces(frame);
                        imageToShow = Utils.mat2Image(faceDetector.drawFaces(frame, faces));

                        CameraAppFXController.this.updateImageView(CameraAppFXController.this.currentFrame, imageToShow);
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0L, 10L, TimeUnit.MILLISECONDS);
                this.button.setText("Stop Camera");
            } else {
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            this.cameraActive = false;
            this.button.setText("Start Camera");
            this.stopAcquisition();
        }

    }

    @FXML
    protected void save_image(ActionEvent event) {
        System.out.println(imageToShow);
        BufferedImage image;
        try {
            image = utils.Mat2BufferedImage(frame);
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss").format(new Date());
            String filename = "Snap-"+timeStamp;
            File file = new File(base_file+filename);
            try {
                ImageIO.write(image, "png", file);
                System.out.println("File "+filename+" saved!");
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

    protected void black_white(ActionEvent event) {
        
    }

    private Mat grabFrame() {
        Mat frame = new Mat();
        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if (!frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BayerGB2RGB_VNG);
                }
            } catch (Exception var3) {
                System.err.println(var3);
            }
        }

        return frame;
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException var2) {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }

    }

    private void updateImageView(ImageView view, Image image) {
        currentFrame.setImage(image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }
}