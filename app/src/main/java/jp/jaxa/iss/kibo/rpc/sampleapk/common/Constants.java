package jp.jaxa.iss.kibo.rpc.sampleapk.common;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.sampleapk.common.enumeration.AreaEnum;
import jp.jaxa.iss.kibo.rpc.sampleapk.common.enumeration.CartesianDirection;

public class Constants {
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_WAIT_MS = 1000;
    public static final int MOVE_TO_COMPLETE_WAIT_MS = 2000;
    public static final int CAM_STABILIZATION_WAIT_MS = 3000;
    public static final boolean DEBUG_MODE = true;

    public static final Map<AreaEnum, QuaternionPoint> LOST_ITEM_SEARCH_PLANE_PER_AREA;

    static {
        //     [ xMin,    yMin,   zMin,    xMax,    yMax,   zMax ]
        double[][] BOUNDS = {
                {10.42,  -10.58,  4.82,    11.48,  -10.58,  5.57  },   // AREA_1
                {10.30,   -9.25,  3.76203, 11.55,  -8.50,   3.76203},  // AREA_2
                {10.30,   -8.40,  3.76093, 11.55,  -7.45,   3.76093},  // AREA_3
                {9.866984, -7.34, 4.32,    9.866984, -6.365, 5.57  }   // AREA_4
        };

        Map<AreaEnum, QuaternionPoint> m = new EnumMap<>(AreaEnum.class);
        for (AreaEnum area : AreaEnum.values()) {
            double[] bounds = BOUNDS[area.ordinal()];
            double xMin = bounds[0], yMin = bounds[1], zMin = bounds[2];
            double xMax = bounds[3], yMax = bounds[4], zMax = bounds[5];

            // calc center
            Point center = new Point(
                    (xMin + xMax) / 2.0,
                    (yMin + yMax) / 2.0,
                    (zMin + zMax) / 2.0
            );

            // store enum orientation
            CartesianDirection cartesianDirection;

            // choose orientation based on axis
            Quaternion orientation;

            // y constant == target orientation
            if (yMin == yMax) {
                // face -Y based on rule book
                orientation = new Quaternion(0f, 0f, -0.7071068f, 0.7071068f);
                cartesianDirection = CartesianDirection.Y_NEG;
            }
            // x constant == target orientation
            else if (xMin == xMax) {
                // face -X based on rule book
                orientation = new Quaternion(0f, 0f,  1.0f,0f);
                cartesianDirection = CartesianDirection.X_NEG;
            }
            // z constant == target orientation
            else if (zMin == zMax) {
                // face -Z based on rule book
                orientation = new Quaternion(0f, -0.7071068f, 0f, 0.7071068f);
                cartesianDirection = CartesianDirection.Z_NEG;
            }
            // default
            else {
                orientation = new Quaternion(0f, 0f, 0f, 1f);
                cartesianDirection = CartesianDirection.X_POS;
            }

            m.put(area, new QuaternionPoint(center, orientation, cartesianDirection));
        }

        LOST_ITEM_SEARCH_PLANE_PER_AREA = Collections.unmodifiableMap(m);
    }
}
