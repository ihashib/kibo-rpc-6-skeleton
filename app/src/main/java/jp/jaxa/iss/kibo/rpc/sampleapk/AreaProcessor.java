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
import jp.jaxa.iss.kibo.rpc.sampleapk.common.Constants;
import jp.jaxa.iss.kibo.rpc.sampleapk.common.QuaternionPoint;
import jp.jaxa.iss.kibo.rpc.sampleapk.common.enumeration.AreaEnum;

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

            if (!success) {
                Log.e("ROTATE_CAPTURE", "Failed to rotate to " + labels[i]);
                continue;
            }

            // Capture NavCam image
            Mat navImage = visionService.getMatNavCamImage();
            if (navImage != null) {
                capturedImages.put(labels[i], navImage);

                if(Constants.DEBUG_MODE){
                    api.saveMatImage(navImage, "ROTATE_CAPTURE_NAV_"+labels[i]+ "_" + System.currentTimeMillis() + ".jpg");
                }

                Log.d("ROTATE_CAPTURE", "Captured and stored image for orientation " + labels[i]);
            } else {
                Log.e("ROTATE_CAPTURE", "Failed to capture image for orientation " + labels[i]);
            }
        }

        return capturedImages;
    }

    /**
     * Moves Astrobee to the pre-defined search area for the given area,
     * waits for the NavCam to stabilize, and returns the captured image.
     *
     * @param area the search area enum
     * @return the captured NavCam image, or an empty Mat if any step fails
     */
    public Mat getLostItemPlaneNavImage(AreaEnum area) {
        Log.d("LOST_ITEM_IMAGE", "Attempting to get image of lost item plane: " + area);

        Mat emptyImage = new Mat();

        // Get current position to rotate in place
        Point currentPosition = api.getRobotKinematics().getPosition();

        if (currentPosition == null) {
            Log.e("LOST_ITEM_SEARCH", "Failed to get current position");
            return emptyImage;
        }

        QuaternionPoint lostItemSearchPlane;
        if(Constants.LOST_ITEM_SEARCH_PLANE_PER_AREA.containsKey(area)) {
            lostItemSearchPlane = Constants.LOST_ITEM_SEARCH_PLANE_PER_AREA.get(area);
        } else {
            Log.d("LOST_ITEM_SEARCH", "Given Area is not supported");

            return emptyImage;
        }

        // Move to the lost item position and rotate to the correct orientation
        boolean moveSuccess = movementService.moveToTargetPosition(
                lostItemSearchPlane.getPoint(),
                lostItemSearchPlane.getQuaternion()
        );

        if (!moveSuccess) {
            Log.e("LOST_ITEM_SEARCH", "MoveTo failed for area " + area + "  " + lostItemSearchPlane.getPoint());

            return emptyImage;
        }

        // Capture NavCam image
        Mat navImage = visionService.getMatNavCamImage();
        if (navImage != null) {
            Log.d("LOST_ITEM_SEARCH", "Captured and stored image for position " + lostItemSearchPlane);

            if(Constants.DEBUG_MODE){
                api.saveMatImage(navImage, "SEARCH_CAPTURE_NAV_"+area+ "_" + System.currentTimeMillis() + ".jpg");
            }

            return navImage;
        } else {
            Log.e("LOST_ITEM_SEARCH", "Failed to capture image for position " + lostItemSearchPlane);

            return emptyImage;
        }
    }


    public Mat processSearchArea(AreaEnum area, Point position, Quaternion orientation) {
        Log.d("PROCESS_SEARCH_AREA", "Processing area: " + area);

        boolean moved = movementService.moveToTargetPosition(position, orientation);

        if (!moved) {
            Log.e("MOVE", "Failed to move to Area " + area);
        }

        // Rotate and capture image
        return getLostItemPlaneNavImage(area);
    }

}
