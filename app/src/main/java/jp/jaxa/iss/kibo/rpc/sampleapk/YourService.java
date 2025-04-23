package jp.jaxa.iss.kibo.rpc.sampleapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.core.Mat;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {
    private final MovementService movementService = new MovementService(this.api);
    private final VisionService visionService = new VisionService(this.api, movementService);
    private final AreaProcessor areaProcessor = new AreaProcessor(
            this.api,
            this.movementService,
            this.visionService
    );

    @Override
    protected void runPlan1(){
        // The mission starts
        api.startMission();

        /* **************************************************** */
        /* Let's move to each area and recognize the items. */
        /* **************************************************** */

        // Area 1
        Point area1Point = new Point(11.1d, -10.58d, 5.1d);
        Quaternion area1Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Mat Area1LostItemImage = areaProcessor.processAreaForArTags(
                1,
                area1Point,
                area1Quaternion
        );

        // Area 2
        Point area2Point = new Point(10.5d, -8d, 3.76203d);
        Quaternion area2Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Mat Area2LostItemImage = areaProcessor.processAreaForArTags(
                2,
                area2Point,
                area2Quaternion
        );

        // Area 3
        Point area3Point = new Point(11d, -7d, 3.76203d);
        Quaternion area3Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Mat Area3LostItemImage = areaProcessor.processAreaForArTags(
                3,
                area3Point,
                area3Quaternion
        );

        // Area 4
        Point area4Point = new Point(9.866984d, -6.7d, 5d);
        Quaternion area4Quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        Mat Area4LostItemImage = areaProcessor.processAreaForArTags(
                4,
                area4Point,
                area4Quaternion
        );

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
