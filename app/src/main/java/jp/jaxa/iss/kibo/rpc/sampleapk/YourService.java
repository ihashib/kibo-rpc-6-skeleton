package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.util.Log;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.sampleapk.common.enumeration.AreaEnum;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {
    private MovementService movementService;
    private VisionService visionService;
    private AreaProcessor areaProcessor;

    @Override
    protected void runPlan1(){
        movementService = new MovementService(api);
        visionService = new VisionService(api, movementService);
        areaProcessor = new AreaProcessor(api, movementService, visionService);

        // The mission starts
        api.startMission();

        /* **************************************************** */
        /* Let's move to each area and recognize the items. */
        /* **************************************************** */

        // Move Astrobee from KIZ2 into KIZ1
        Log.d("MISSION_START", "Moving out of KIZ2");
        Point kiz1EntryPoint = new Point(10.292d, -10d, 4.6d);
        Quaternion kiz1EntryQuaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        movementService.moveToTargetPosition(kiz1EntryPoint, kiz1EntryQuaternion);

        Point kiz1Point = new Point(10.7d, -10d, 4.6d);
        Quaternion kiz1Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        movementService.moveToTargetPosition(kiz1Point, kiz1Quaternion);

        // Area 1
        Log.d("AREA_1", "Processing Area 1");

        Point area1Point = new Point(11d, -9.8d, 4.5d);
        Quaternion area1Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);

        Mat area1SearchImage = areaProcessor.processSearchArea(AreaEnum.AREA_1, area1Point, area1Quaternion);

        List<Mat> area1ArCorners = new ArrayList<>();
        Mat area1ArIds = new Mat();

        visionService.readArTag(area1SearchImage, area1ArCorners, area1ArIds, AreaEnum.AREA_1);

        // Area 2
        Log.d("AREA_2", "Processing Area 2");

        Point area2Point = new Point(11d, -9.1d, 5.2);
        Quaternion area2Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);

        Mat area2SearchImage = areaProcessor.processSearchArea(AreaEnum.AREA_2, area2Point, area2Quaternion);

        List<Mat> area2ArCorners = new ArrayList<>();
        Mat area2ArIds = new Mat();

        visionService.readArTag(area2SearchImage, area2ArCorners, area2ArIds, AreaEnum.AREA_2);

        // Area 3
        Log.d("AREA_3", "Processing Area 3");

        Point area3Point = new Point(10.7d, -8.1d, 5.2d);
        Quaternion area3Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);

        Mat area3SearchImage = areaProcessor.processSearchArea(AreaEnum.AREA_3, area3Point, area3Quaternion);

        List<Mat> area3ArCorners = new ArrayList<>();
        Mat area3ArIds = new Mat();

        visionService.readArTag(area3SearchImage, area3ArCorners, area3ArIds, AreaEnum.AREA_3);

        // Area 4
        Log.d("AREA_4", "Processing Area 4");

        Point area4Point = new Point(11.1, -7d, 4.7d);
        Quaternion area4Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);

        Mat area4SearchImage = areaProcessor.processSearchArea(AreaEnum.AREA_4, area4Point, area4Quaternion);

        List<Mat> area4ArCorners = new ArrayList<>();
        Mat area4ArIds = new Mat();

        visionService.readArTag(area4SearchImage, area4ArCorners, area4ArIds, AreaEnum.AREA_4);

        /* ******************************************************************************** */
        /* Write your code to recognize the type and number of landmark items in each area! */
        /* If there is a treasure item, remember it.                                        */
        /* ******************************************************************************** */

        // When you recognize landmark items, let’s set the type and number.
//        api.setAreaInfo(1, "item_name", 1);


        // When you move to the front of the astronaut, report the rounding completion
        Point astronautPoint = new Point(9.866984d, -6.7d, 5d);
        Quaternion astronautQuaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        movementService.moveToTargetPosition(astronautPoint, astronautQuaternion);

        api.reportRoundingCompletion();

        /* ********************************************************** */
        /* Write your code to recognize which target item the astronaut has. */
        /* ********************************************************** */

        // Let's notify the astronaut when you recognize it.
//        api.notifyRecognitionItem();

        /* ******************************************************************************************************* */
        /* Write your code to move Astrobee to the location of the target item (what the astronaut is looking for) */
        /* ******************************************************************************************************* */

        // Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2(){
       // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }
}
