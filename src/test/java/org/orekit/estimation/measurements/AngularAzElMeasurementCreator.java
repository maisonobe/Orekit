/* Copyright 2002-2021 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.estimation.measurements;

import java.util.Arrays;

import org.hipparchus.analysis.UnivariateFunction;
import org.hipparchus.analysis.solvers.BracketingNthOrderBrentSolver;
import org.hipparchus.analysis.solvers.UnivariateSolver;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.estimation.Context;
import org.orekit.frames.Frame;
import org.orekit.frames.Transform;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.ParameterDriver;

public class AngularAzElMeasurementCreator extends MeasurementCreator {

    private final Context context;
    private final ObservableSatellite satellite;

    public AngularAzElMeasurementCreator(final Context context) {
        this.context   = context;
        this.satellite = new ObservableSatellite(0);
    }

    public void init(SpacecraftState s0, AbsoluteDate t, double step) {
        for (final GroundStation station : context.stations) {
            for (ParameterDriver driver : Arrays.asList(station.getClockOffsetDriver(),
                                                        station.getEastOffsetDriver(),
                                                        station.getNorthOffsetDriver(),
                                                        station.getZenithOffsetDriver(),
                                                        station.getPrimeMeridianOffsetDriver(),
                                                        station.getPrimeMeridianDriftDriver(),
                                                        station.getPolarOffsetXDriver(),
                                                        station.getPolarDriftXDriver(),
                                                        station.getPolarOffsetYDriver(),
                                                        station.getPolarDriftYDriver())) {
                if (driver.getReferenceDate() == null) {
                    driver.setReferenceDate(s0.getDate());
                }
            }

        }
    }

    public void handleStep(final SpacecraftState currentState) {
        for (final GroundStation station : context.stations) {

            final AbsoluteDate     date      = currentState.getDate();
            final Frame            inertial  = currentState.getFrame();
            final Vector3D         position  = currentState.getPVCoordinates().getPosition();

            if (station.getBaseFrame().getElevation(position, inertial, date) > FastMath.toRadians(30.0)) {
                final UnivariateSolver solver = new BracketingNthOrderBrentSolver(1.0e-12, 5);

                final double downLinkDelay  = solver.solve(1000, new UnivariateFunction() {
                    public double value(final double x) {
                        final Transform t = station.getOffsetToInertial(inertial, date.shiftedBy(x));
                        final double d = Vector3D.distance(position, t.transformPosition(Vector3D.ZERO));
                        return d - x * Constants.SPEED_OF_LIGHT;
                    }
                }, -1.0, 1.0);

                // Satellite position at signal departure
                final Vector3D satelliteAtDeparture = currentState.shiftedBy(-downLinkDelay).getPVCoordinates().getPosition();

                // Initialize measurement
                final double[] angular = new double[2];
                final double[] sigma = {1.0, 1.0};
                final double[] baseweight = {10.0, 10.0};

                // Compute measurement
                // Elevation
                angular[1] = station.getBaseFrame().getElevation(satelliteAtDeparture,
                                                                 currentState.getFrame(),
                                                                 currentState.getDate());
                // Azimuth
                angular[0] = station.getBaseFrame().getAzimuth(satelliteAtDeparture,
                                                               currentState.getFrame(),
                                                               currentState.getDate());

                addMeasurement(new AngularAzEl(station, date, angular, sigma, baseweight, satellite));
            }

        }
    }

}
