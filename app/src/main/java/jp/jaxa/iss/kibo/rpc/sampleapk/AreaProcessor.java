package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;

public class AreaProcessor {
    private final KiboRpcApi api;
    private final MovementService movementService;
    private final VisionService visionService;

    public AreaProcessor(KiboRpcApi api, MovementService movementService, VisionService visionService) {
        this.api = api;
        this.movementService = movementService;
        this.visionService = visionService;
    }
    /**
     * Rotates the Astrobee in place to face -X, +X, -Z, and +Z directions sequentially,
     * captures a NavCam image at each orientation, and returns a map with the orientation labels and images.
     *
     * @return A Map where keys are orientation labels (e.g., "negX", "posX") and values are the corresponding Mat images.
     */
    private Map<String, Mat> rotateAndCaptureNavCamImages() {
        // Map to store the orientation labels and the captured images
        Map<String, Mat> capturedImages = new HashMap<>();

        // Get current position to rotate in place
        Point currentPosition = api.getRobotKinematics().getPosition();

        if (currentPosition == null) {
            Log.e("ROTATE_CAPTURE", "Failed to get current position");
            return capturedImages; // Return empty map if position is unavailable
        }

        // Define orientations for -X, +X, -Z, +Z
        Quaternion facingNegX = new Quaternion(0f, 0f, 0f, 1f);
        Quaternion facingPosX = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Quaternion facingNegZ = new Quaternion(0f, 0.707f, 0f, 0.707f);
        Quaternion facingPosZ = new Quaternion(0f, -0.707f, 0f, 0.707f);

        // Store orientations with labels
        String[] labels = {"negX", "posX", "negZ", "posZ"};
        Quaternion[] quaternions = {facingNegX, facingPosX, facingNegZ, facingPosZ};

        for (int i = 0; i < quaternions.length; i++) {
            Log.d("ROTATE_CAPTURE", "Rotating to face " + labels[i]);

            // Move to the target position and rotate to the correct orientation
            boolean success = movementService.moveToTargetPosition(currentPosition, quaternions[i]);
            movementService.wait(1000); // Wait 1s to stabilize orientation

            if (!success) {
                Log.e("ROTATE_CAPTURE", "Failed to rotate to " + labels[i]);
                continue;
            }

            // Capture NavCam image
            Mat navImage = visionService.getMatNavCamImage();
            if (navImage != null) {
                capturedImages.put(labels[i], navImage); // Store the captured image with its label
                Log.d("ROTATE_CAPTURE", "Captured and stored image for orientation " + labels[i]);
            } else {
                Log.e("ROTATE_CAPTURE", "Failed to capture image for orientation " + labels[i]);
            }
        }

        return capturedImages;
    }

    /**
     * Moves to a specified area, rotates to capture images in multiple directions,
     * and detects ArUco markers in each captured image.
     *
     * @param areaNumber An integer label for the area (for logging or debugging).
     * @param position The target Point to move to.
     * @param orientation The initial orientation (Quaternion) to assume before rotation.
     * @return The first image (Mat) that contains an ArUco marker, or null if none found.
     */
    public Mat processAreaForArTags(int areaNumber, Point position, Quaternion orientation) {
        Log.d("AREA_PROCESS", "Processing Area " + areaNumber);

        // Move to the area
        boolean moved = movementService.moveToTargetPosition(position, orientation);
        if (!moved) {
            Log.e("AREA_PROCESS", "Failed to move to Area " + areaNumber);
            return null;
        }

        // Rotate and capture images
        Map<String, Mat> capturedImages = rotateAndCaptureNavCamImages();

        // Detect ArUco tags in captured images
        for (Map.Entry<String, Mat> entry : capturedImages.entrySet()) {
            String orientationLabel = entry.getKey();
            Mat image = entry.getValue();

            Log.d("ARUCO_SEARCH", "Searching in Area " + areaNumber + ", Orientation: " + orientationLabel);

            List<Mat> corners = new ArrayList<>();
            Mat ids = new Mat();
            visionService.readArTag(image, corners, ids);

            if (ids.rows() > 0) {
                Log.d("ARUCO_DETECTION", "Area " + areaNumber + " [" + orientationLabel + "] - Found " + ids.rows() + " marker(s)");
                for (int i = 0; i < ids.rows(); i++) {
                    Log.d("ARUCO_DETECTION", "Marker ID: " + ids.get(i, 0)[0]);
                }
                return image;
            } else {
                Log.d("ARUCO_DETECTION", "Area " + areaNumber + " [" + orientationLabel + "] - No markers detected");
            }
        }

        return null; // No image contained an ArUco marker
    }

}
