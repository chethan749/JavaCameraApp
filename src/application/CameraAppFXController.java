package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class CameraAppFXController
{
    @FXML
    private Button cameraButton;
    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView trackerFrame;
    @FXML
    private Button trackerButton;
    @FXML
    private Button choosePointsButton;

    private ScheduledExecutorService cameraTimer;
    private ScheduledExecutorService faceTimer;
    private ScheduledExecutorService trackerTimer;
    private ScheduledExecutorService pointsTimer;

    private VideoCapture capture;
    private boolean cameraActive;
    private boolean trackerActive;
    private static int cameraId = 0;
    private Image imageToShow;
    private Mat frame;
    private Utils utils;

    private boolean blackAndWhite;
    private boolean blur;
    private boolean negative;
    private String base_file = "snaps/";
    private FaceDetector faceDetector;
    private MatOfRect faces;

    private boolean readPoints;
    private ArrayList<Point> points;
    private MatOfPoint2f trackedPoints;
    private Mat oldFrame;

    public CameraAppFXController()
    {
        capture = new VideoCapture();
        cameraActive = false;
        blackAndWhite = false;
        blur = false;
        negative = false;
        faceDetector = new FaceDetector();
        utils = new Utils();
        readPoints = false;
        points = new ArrayList<>();
        trackedPoints = new MatOfPoint2f();
    }

    @FXML
    protected void startCamera(ActionEvent event)
    {
        if(trackerActive)
        {
            this.trackerActive = false;
            this.trackerButton.setText("Start Tracking");
            this.stopTracking();
        }

        if (!this.cameraActive)
        {
            this.capture.open(cameraId);
            if (this.capture.isOpened())
            {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable()
                {
                    public void run() {
                        frame = CameraAppFXController.this.grabFrame();

                        Mat frameCopy = frame.clone();

                        Imgproc.putText(frameCopy, "Faces detected: " + ((faces != null && !faces.empty())? faces.rows() : 0), new Point(400, 30), Core.FONT_HERSHEY_COMPLEX, 0.7, new Scalar(1, 0, 0), 2);
                        if(faces != null && !faces.empty())
                            imageToShow = Utils.mat2Image(faceDetector.drawFaces(frameCopy, faces));
                        else
                            imageToShow = Utils.mat2Image(frameCopy);

                        CameraAppFXController.this.currentFrame.setImage(imageToShow);
                    }
                };

                Runnable faceDetectorRunnable = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
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

                this.cameraTimer = Executors.newSingleThreadScheduledExecutor();
                this.cameraTimer.scheduleAtFixedRate(frameGrabber, 0L, 30L, TimeUnit.MILLISECONDS);
                this.faceTimer = Executors.newSingleThreadScheduledExecutor();
                this.faceTimer.scheduleAtFixedRate(faceDetectorRunnable, 1000L, 1L, TimeUnit.MILLISECONDS);

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
        if(cameraActive)
        {
            this.cameraActive = false;
            this.cameraButton.setText("Start Camera");
            this.stopCamera();
        }

        if(!pointsTimer.isShutdown())
        {
            this.choosePointsButton.setText("Choose Points");
            this.readPoints = false;
            stopReadPoints();
        }

        if (!this.trackerActive)
        {
            this.capture.open(cameraId);
            if (this.capture.isOpened())
            {
                this.trackerActive = true;
                Runnable frameGrabber = new Runnable()
                {
                    public void run()
                    {
                        frame = CameraAppFXController.this.grabFrame();

                        Mat frameCopy = frame.clone();

                        Video.calcOpticalFlowPyrLK(oldFrame, frameCopy, trackedPoints, trackedPoints, new MatOfByte(), new MatOfFloat());

                        Point[] pointsArray = trackedPoints.toArray();
                        for(int i = 0; i < pointsArray.length; i++)
                            Imgproc.circle(frameCopy, new Point(pointsArray[i].x, pointsArray[i].y), 5, new Scalar(0, 255, 0), -1);

                        imageToShow = Utils.mat2Image(frameCopy);
                        oldFrame = frameCopy;

                        CameraAppFXController.this.trackerFrame.setImage(imageToShow);
                    }
                };

                oldFrame = CameraAppFXController.this.grabFrame();
                trackedPoints.fromList(points);
                this.trackerTimer = Executors.newSingleThreadScheduledExecutor();
                this.trackerTimer.scheduleAtFixedRate(frameGrabber, 0L, 30L, TimeUnit.MILLISECONDS);

                this.trackerButton.setText("Stop Tracking");
            }
            else {
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else {
            this.trackerActive = false;
            this.trackerButton.setText("Start Tracking");
            points.clear();
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
    protected void noEffects(ActionEvent event)
    {
        blackAndWhite = false;
        blur = false;
        negative = false;
    }

    @FXML
    protected void save_image(ActionEvent event)
    {
        System.out.println(imageToShow);
        BufferedImage image;
        try
        {
            image = utils.Mat2BufferedImage(frame);
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss").format(new Date());
            String filename = "Snap-"+timeStamp;
            File file = new File(base_file + filename);
            try
            {
                ImageIO.write(image, "png", file);
                System.out.println("File " + filename + " saved!");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            System.out.println("Image not saved!");
            e.printStackTrace();
        }
    }

    @FXML
    protected void readPoints(ActionEvent event)
    {
        if(this.trackerActive)
        {
            this.trackerActive = false;
            this.trackerButton.setText("Start Tracking");
            this.stopTracking();
        }

        if(this.cameraActive)
        {
            this.cameraActive = false;
            this.cameraButton.setText("Start Camera");
            this.stopCamera();
        }

        this.capture.open(cameraId);
        points.clear();
        trackedPoints.release();

        Runnable frameGrabber = new Runnable()
        {
            public void run()
            {
                if(!capture.isOpened())
                    return;
                frame = CameraAppFXController.this.grabFrame();

                trackedPoints.fromList(CameraAppFXController.this.points);

                Point[] pointsArray = trackedPoints.toArray();
                for(int i = 0; i < pointsArray.length; i++)
                    Imgproc.circle(frame, new Point(pointsArray[i].x, pointsArray[i].y), 5, new Scalar(0, 255, 0), -1);
                imageToShow = Utils.mat2Image(frame);

                CameraAppFXController.this.trackerFrame.setImage(imageToShow);
            }
        };

        if(!readPoints)
        {
            readPoints = true;
            choosePointsButton.setText("Done");
            this.pointsTimer = Executors.newSingleThreadScheduledExecutor();
            this.pointsTimer.scheduleAtFixedRate(frameGrabber, 0L, 30L, TimeUnit.MILLISECONDS);
        }
        else
        {
            readPoints = false;
            choosePointsButton.setText("Choose Points");
            stopReadPoints();
        }

        trackerFrame.setOnMouseClicked(e -> {
            if(!readPoints)
                return;
            points.add(new Point(e.getX(), e.getY()));
        });
    }

    private Mat grabFrame() {
        Mat frame = new Mat();
        if (this.capture.isOpened())
        {
            try
            {
                this.capture.read(frame);
                if (!frame.empty())
                {
                    if(blackAndWhite)
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    if(blur)
                        Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0);
                    if(negative)
                        frame = Utils.getNegative(frame);
                }
            }
            catch (Exception var3)
            {
                System.err.println(var3);
            }
        }

        return frame;
    }

    private void stopCamera()
    {
        if (this.cameraTimer != null && !this.cameraTimer.isShutdown())
        {
            try
            {
                this.cameraTimer.shutdown();
                this.cameraTimer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.currentFrame.setImage(null);
            }
            catch (InterruptedException var2)
            {
                System.err.println(var2);
            }
        }

        if (this.faceTimer != null && !this.faceTimer.isShutdown())
        {
            try
            {
                this.faceTimer.shutdown();
                this.faceTimer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.currentFrame.setImage(null);
            }
            catch (InterruptedException var2)
            {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened())
        {
            this.capture.release();
        }
    }

    private void stopTracking()
    {
        if (this.trackerTimer != null && !this.trackerTimer.isShutdown())
        {
            try
            {
                this.trackerTimer.shutdown();
                this.trackerTimer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.trackerFrame.setImage(null);
            }
            catch (InterruptedException var2)
            {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened())
        {
            this.capture.release();
        }
    }

    private void stopReadPoints()
    {
        if (this.pointsTimer != null && !this.pointsTimer.isShutdown())
        {
            try
            {
                this.pointsTimer.shutdown();
                this.pointsTimer.awaitTermination(33L, TimeUnit.MILLISECONDS);
                this.trackerFrame.setImage(null);
            }
            catch (InterruptedException var2)
            {
                System.err.println(var2);
            }
        }

        if (this.capture.isOpened())
        {
            this.capture.release();
        }
    }

    protected void setClosed() {
        this.stopCamera();
        this.stopTracking();
    }
}