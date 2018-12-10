/* Copyright 2002-2018 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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
package org.orekit.estimation.measurements.generation;

import java.util.SortedSet;

import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.random.CorrelatedRandomVectorGenerator;
import org.hipparchus.random.GaussianRandomGenerator;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well19937a;
import org.hipparchus.util.FastMath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orekit.estimation.Context;
import org.orekit.estimation.EstimationTestUtils;
import org.orekit.estimation.Force;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.Position;
import org.orekit.estimation.measurements.modifiers.Bias;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.BurstSelector;
import org.orekit.time.TimeScalesFactory;

public class PositionBuilderTest {

    private static final double SIGMA = 10.0;
    private static final double BIAS  =  5.0;

    private MeasurementBuilder<Position> getBuilder(final RandomGenerator random) {
        final RealMatrix covariance = MatrixUtils.createRealDiagonalMatrix(new double[] {
            SIGMA * SIGMA, SIGMA * SIGMA, SIGMA * SIGMA
        });
        MeasurementBuilder<Position> pb =
                        new PositionBuilder(random == null ? null : new CorrelatedRandomVectorGenerator(covariance,
                                                                                                        1.0e-10,
                                                                                                        new GaussianRandomGenerator(random)),
                                            SIGMA, 1.0, 0);
         pb.addModifier(new Bias<>(new String[] { "pxBias", "pyBias", "pzBias" },
                                   new double[] { BIAS, BIAS, BIAS },
                                   new double[] { 1.0, 1.0, 1.0 },
                                   new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY },
                                   new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY }));
         return pb;
    }

    @Test
    public void testForward() {
        doTest(0x292b6e87436fe4c7l, 0.0, 1.2, 3.3 * SIGMA);
    }

    @Test
    public void testBackward() {
        doTest(0x2f3285aa70b83c47l, 0.0, -1.0, 3.3 * SIGMA);
    }

    private Propagator buildPropagator() {
        return EstimationTestUtils.createPropagator(context.initialOrbit, propagatorBuilder);
    }

    private void doTest(long seed, double startPeriod, double endPeriod, double tolerance) {
        Generator    generator    = new Generator();
        final int    maxBurstSize = 10;
        final double highRateStep = 5.0;
        final double burstPeriod  = 300.0;

        generator.addScheduler(new ContinuousScheduler<>(getBuilder(new Well19937a(seed)),
                                                         new BurstSelector(maxBurstSize, highRateStep, burstPeriod,
                                                                           TimeScalesFactory.getUTC()),
                                                         buildPropagator()));
        final double period = context.initialOrbit.getKeplerianPeriod();
        AbsoluteDate t0     = context.initialOrbit.getDate().shiftedBy(startPeriod * period);
        AbsoluteDate t1     = context.initialOrbit.getDate().shiftedBy(endPeriod   * period);
        SortedSet<ObservedMeasurement<?>> measurements = generator.generate(t0, t1);
        Propagator propagator = buildPropagator();
        double maxError = 0;
        AbsoluteDate previous = null;
        AbsoluteDate tInf = t0.compareTo(t1) < 0 ? t0 : t1;
        AbsoluteDate tSup = t0.compareTo(t1) < 0 ? t1 : t0;
        int count = 0;
        for (ObservedMeasurement<?> measurement : measurements) {
            AbsoluteDate date = measurement.getDate();
            double[] m = measurement.getObservedValue();
            Assert.assertTrue(date.compareTo(tInf) >= 0);
            Assert.assertTrue(date.compareTo(tSup) <= 0);
            if (previous != null) {
                // measurements are always chronological, even with backward propagation,
                // due to the SortedSet (which is intended for combining several
                // measurements types with different builders and schedulers)
                final double expected = (count % maxBurstSize == 0) ?
                                        burstPeriod - (maxBurstSize - 1) * highRateStep :
                                        highRateStep;
                Assert.assertEquals(expected, date.durationFrom(previous), 1.0e-10 * expected);
            }
            previous = date;
            ++count;
            SpacecraftState state = propagator.propagate(date);
            double[] e = measurement.estimate(0, 0, new SpacecraftState[] { state }).getEstimatedValue();
            for (int i = 0; i < m.length; ++i) {
                maxError = FastMath.max(maxError, FastMath.abs(e[i] - m[i]));
            }
        }
        Assert.assertEquals(0.0, maxError, tolerance);
     }

     @Before
     public void setUp() {
         context = EstimationTestUtils.eccentricContext("regular-data:potential:tides");

         propagatorBuilder = context.createBuilder(OrbitType.KEPLERIAN, PositionAngle.TRUE, true,
                                                   1.0e-6, 300.0, 0.001, Force.POTENTIAL,
                                                   Force.THIRD_BODY_SUN, Force.THIRD_BODY_MOON);
     }

     Context context;
     NumericalPropagatorBuilder propagatorBuilder;

}
