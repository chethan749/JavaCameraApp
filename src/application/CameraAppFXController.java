package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class CameraAppFXController {
    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;

    public CameraAppFXController() {
    }

    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    public void run() {
                        Mat frame = CameraAppFXController.this.grabFrame();
                        Image imageToShow = Utils.mat2Image(frame);
//                        System.out.println(frame.get(0, 0)[0]);
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

    private Mat grabFrame() {
        Mat frame = new Mat();
        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if (!frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
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