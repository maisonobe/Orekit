/* Copyright 2002-2020 CS GROUP
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
package org.orekit.propagation.analytical.tle;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orekit.Utils;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.SHMFormatReader;
import org.orekit.forces.gravity.potential.UnnormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.models.earth.atmosphere.HarrisPriester;
import org.orekit.models.earth.atmosphere.SimpleExponentialAtmosphere;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.AnalyticalJacobiansMapper;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitStepHandler;
import org.orekit.propagation.sampling.OrekitStepInterpolator;
import org.orekit.propagation.semianalytical.dsst.DSSTPropagator;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTAtmosphericDrag;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTForceModel;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTTesseral;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTThirdBody;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTZonal;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;

public class TLEPartialDerivativesTest {

    @Test
    public void testPropagationwrtKeplerian() throws FileNotFoundException, UnsupportedEncodingException {
        doTestPropagationwrtKeplerian(5.0e-3);
    }
    
    @Test
    public void testProgationwrtDSST() throws FileNotFoundException, UnsupportedEncodingException {
        doTestPropagationwrtDSST(5.0e-3);
    }
  
    private void doTestPropagationwrtKeplerian(double tolerance)
        throws FileNotFoundException, UnsupportedEncodingException {

        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);
               
        double dt = 900;
        double dP = 0.001;        
        
        // compute state Jacobian using PartialDerivatives
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        KeplerianOrbit orbit = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(propagator.getInitialState().getOrbit());
        TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
        SpacecraftState initialState = partials.setInitialJacobians(propagator.getInitialState());
        final double[] stateVector = new double[6];
        OrbitType.KEPLERIAN.mapOrbitToArray(initialState.getOrbit(), PositionAngle.MEAN, stateVector, null);
        final AbsoluteDate target = initialState.getDate().shiftedBy(dt);
        final AnalyticalJacobiansMapper mapper = partials.getMapper();
        PickUpHandler pickUp = new PickUpHandler(mapper, null);
        propagator.setMasterMode(pickUp);
        propagator.propagateOrbit(target);
        double[][] dYdY0 = pickUp.getdYdY0();

        // compute reference state Jacobian using finite differences
        double[][] dYdY0Ref = new double[6][6];
        KeplerianPropagator propagator2;
        double[] steps = NumericalPropagator.tolerances(10000 * dP, orbit, OrbitType.KEPLERIAN)[0];
        for (int i = 0; i < 6; ++i) {
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, -4 * steps[i], i).getOrbit());
            SpacecraftState sM4h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, -3 * steps[i], i).getOrbit());
            SpacecraftState sM3h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, -2 * steps[i], i).getOrbit());
            SpacecraftState sM2h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, -1 * steps[i], i).getOrbit());
            SpacecraftState sM1h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, +1 * steps[i], i).getOrbit());
            SpacecraftState sP1h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, +2 * steps[i], i).getOrbit());
            SpacecraftState sP2h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, +3 * steps[i], i).getOrbit());
            SpacecraftState sP3h = propagator2.propagate(target);
            propagator2 = new KeplerianPropagator(shiftState(initialState, OrbitType.KEPLERIAN, +4 * steps[i], i).getOrbit());
            SpacecraftState sP4h = propagator2.propagate(target);
            fillJacobianColumn(dYdY0Ref, i, OrbitType.KEPLERIAN, steps[i],
                               sM4h, sM3h, sM2h, sM1h, sP1h, sP2h, sP3h, sP4h);
        }

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                if (stateVector[i] != 0) {
                    double error = FastMath.abs((dYdY0[i][j] - dYdY0Ref[i][j]) / stateVector[i]) * steps[j];
                    Assert.assertEquals(0, error, tolerance);
                }
            }
        }
    }
    
    private void doTestPropagationwrtDSST(double tolerance)
                    throws FileNotFoundException, UnsupportedEncodingException {

                    String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
                    String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
                    TLE tle = new TLE(line1, line2);
                           
                    double dt = 900;
                    double dP = 0.001;        
                    
                    // compute state Jacobian using PartialDerivatives
                    TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
                    KeplerianOrbit orbit = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(propagator.getInitialState().getOrbit());
                    TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
                    SpacecraftState initialState = partials.setInitialJacobians(propagator.getInitialState());
                    final double[] stateVector = new double[6];
                    OrbitType.KEPLERIAN.mapOrbitToArray(initialState.getOrbit(), PositionAngle.MEAN, stateVector, null);
                    final AbsoluteDate target = initialState.getDate().shiftedBy(dt);
                    final AnalyticalJacobiansMapper mapper = partials.getMapper();
                    PickUpHandler pickUp = new PickUpHandler(mapper, null);
                    propagator.setMasterMode(pickUp);
                    propagator.propagateOrbit(target);
                    double[][] dYdY0 = pickUp.getdYdY0();

                    // compute reference state Jacobian using finite differences
                    double[][] dYdY0Ref = new double[6][6];
                    
                    //setup DSST propagator
                    UnnormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getUnnormalizedProvider(4, 4);
                    final OneAxisEllipsoid earth = new OneAxisEllipsoid(provider.getAe(),
                                                                        Constants.WGS84_EARTH_FLATTENING,
                                                                        CelestialBodyFactory.getEarth().getBodyOrientedFrame());
                    
                    // Drag Force Model
                    final Atmosphere atm = new HarrisPriester(CelestialBodyFactory.getSun(), earth, 6);
                    final double cd = 1.0;
                    final double area = 25.0;
                    DSSTForceModel drag = new DSSTAtmosphericDrag(atm, cd, area, provider.getMu());
                    
                    // Third Bodies (Moon and Sun) Force Models
                    Frame earthFrame = CelestialBodyFactory.getEarth().getBodyOrientedFrame();
                    DSSTForceModel moon = new DSSTThirdBody(CelestialBodyFactory.getMoon(), provider.getMu());
                    DSSTForceModel sun  = new DSSTThirdBody(CelestialBodyFactory.getSun(), provider.getMu());

                    // Earth Gravity Field
                    DSSTForceModel tesseral = new DSSTTesseral(earthFrame,
                                                               Constants.WGS84_EARTH_ANGULAR_VELOCITY, provider,
                                                               4, 4, 4, 8, 4, 4, 2);
              
                    DSSTForceModel zonal = new DSSTZonal(provider, 4, 3, 9);
                    
                    
                    DSSTPropagator propagator2 = setUpPropagator(PropagationType.MEAN, orbit, dP, OrbitType.KEPLERIAN, drag, tesseral, zonal, moon, sun);
                    double[] steps = NumericalPropagator.tolerances(10000 * dP, orbit, OrbitType.KEPLERIAN)[0];
                    for (int i = 0; i < 6; ++i) {
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN, -4 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sM4h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN, -3 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sM3h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN, -2 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sM2h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN, -1 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sM1h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN,  1 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sP1h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN,  2 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sP2h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN,  3 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sP3h = propagator2.propagate(target);
                        propagator2.setInitialState(shiftState(initialState, OrbitType.KEPLERIAN,  4 * steps[i], i), PropagationType.MEAN);
                        SpacecraftState sP4h = propagator2.propagate(target);
                        fillJacobianColumn(dYdY0Ref, i, OrbitType.KEPLERIAN, steps[i],
                                           sM4h, sM3h, sM2h, sM1h, sP1h, sP2h, sP3h, sP4h);
                    }

                    for (int i = 0; i < 6; ++i) {
                        for (int j = 0; j < 6; ++j) {
                            if (stateVector[i] != 0) {
                                double error = FastMath.abs((dYdY0[i][j] - dYdY0Ref[i][j]) / stateVector[i]) * steps[j];
                                Assert.assertEquals(0, error, tolerance);
                            }
                        }
                    }
                }
    
    

    @Test(expected=OrekitException.class)
    public void testNotInitialized() {
        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        new TLEPartialDerivativesEquations("partials", propagator).getMapper();
     }
    
    @Test(expected=OrekitException.class)
    public void testTooSmallDimension() {
        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
        partials.setInitialJacobians(propagator.getInitialState(),
                                     new double[5][6], new double[6][2]);
     }

    @Test(expected=OrekitException.class)
    public void testTooLargeDimension() {
        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
        partials.setInitialJacobians(propagator.getInitialState(),
                                     new double[8][6], new double[6][2]);
     }
    
    @Test(expected=OrekitException.class)
    public void testMismatchedDimensions() {
        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
        partials.setInitialJacobians(propagator.getInitialState(),
                                     new double[6][6], new double[7][2]);
     }
    
    @Test
    public void testWrongParametersDimension() {
        String line1 = "1 37753U 11036A   12090.13205652 -.00000006  00000-0  00000+0 0  2272";
        String line2 = "2 37753  55.0032 176.5796 0004733  13.2285 346.8266  2.00565440  5153";       
        TLE tle = new TLE(line1, line2);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
        TLEPartialDerivativesEquations partials = new TLEPartialDerivativesEquations("partials", propagator);
        try {
            partials.setInitialJacobians(propagator.getInitialState(),
                                         new double[6][6], new double[6][3]);
            partials.computeDerivatives(propagator.getInitialState(), new double[6]);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.INITIAL_MATRIX_AND_PARAMETERS_NUMBER_MISMATCH,
                                oe.getSpecifier());
        }
    }

    private void fillJacobianColumn(double[][] jacobian, int column,
                                    OrbitType orbitType, double h,
                                    SpacecraftState sM4h, SpacecraftState sM3h,
                                    SpacecraftState sM2h, SpacecraftState sM1h,
                                    SpacecraftState sP1h, SpacecraftState sP2h,
                                    SpacecraftState sP3h, SpacecraftState sP4h) {
        double[] aM4h = stateToArray(sM4h, orbitType)[0];
        double[] aM3h = stateToArray(sM3h, orbitType)[0];
        double[] aM2h = stateToArray(sM2h, orbitType)[0];
        double[] aM1h = stateToArray(sM1h, orbitType)[0];
        double[] aP1h = stateToArray(sP1h, orbitType)[0];
        double[] aP2h = stateToArray(sP2h, orbitType)[0];
        double[] aP3h = stateToArray(sP3h, orbitType)[0];
        double[] aP4h = stateToArray(sP4h, orbitType)[0];
        for (int i = 0; i < jacobian.length; ++i) {
            jacobian[i][column] = ( -3 * (aP4h[i] - aM4h[i]) +
                                    32 * (aP3h[i] - aM3h[i]) -
                                   168 * (aP2h[i] - aM2h[i]) +
                                   672 * (aP1h[i] - aM1h[i])) / (840 * h);
        }
    }

    private SpacecraftState shiftState(SpacecraftState state, OrbitType orbitType,
                                       double delta, int column) {

        double[][] array = stateToArray(state, orbitType);
        array[0][column] += delta;

        return arrayToState(array, state.getFrame(), state.getDate(),
                            state.getMu(), state.getAttitude());

    }

    private double[][] stateToArray(SpacecraftState state, OrbitType orbitType) {
          double[][] array = new double[2][6];

          orbitType.mapOrbitToArray(state.getOrbit(), PositionAngle.MEAN, array[0], array[1]);
          return array;
      }

      private SpacecraftState arrayToState(double[][] array, 
                                           Frame frame, AbsoluteDate date, double mu,
                                           Attitude attitude) {
          KeplerianOrbit orbit = (KeplerianOrbit) OrbitType.KEPLERIAN.mapArrayToOrbit(array[0], array[1], PositionAngle.MEAN, date, mu, frame);
          return new SpacecraftState(orbit, attitude);
      }


      private DSSTPropagator setUpPropagator(PropagationType type, Orbit orbit, double dP,
                                             OrbitType orbitType,
                                             DSSTForceModel... models) {

          final double minStep = 0.001;
          final double maxStep = 1000;
          
          double[][] tol = NumericalPropagator.tolerances(dP, orbit, orbitType);
          DSSTPropagator propagator =
              new DSSTPropagator(new DormandPrince853Integrator(minStep, maxStep, tol[0], tol[1]), type);
          for (DSSTForceModel model : models) {
              propagator.addForceModel(model);
          }
          return propagator;
      }
      
    private static class PickUpHandler implements OrekitStepHandler {

        private final AnalyticalJacobiansMapper mapper;
        private final AbsoluteDate pickUpDate;
        private final double[][] dYdY0;
        private final double[][] dYdP;

        public PickUpHandler(AnalyticalJacobiansMapper mapper, AbsoluteDate pickUpDate) {
            this.mapper = mapper;
            this.pickUpDate = pickUpDate;
            dYdY0 = new double[AnalyticalJacobiansMapper.STATE_DIMENSION][AnalyticalJacobiansMapper.STATE_DIMENSION];
            dYdP  = new double[AnalyticalJacobiansMapper.STATE_DIMENSION][mapper.getParameters()];
        }

        public double[][] getdYdY0() {
            return dYdY0;
        }

        public void init(SpacecraftState s0, AbsoluteDate t) {
        }

        public void handleStep(OrekitStepInterpolator interpolator, boolean isLast) {
            final SpacecraftState interpolated;
            if (pickUpDate == null) {
                // we want to pick up the Jacobians at the end of last step
                if (isLast) {
                    interpolated = interpolator.getCurrentState();
                } else {
                    return;
                }
            } else {
                // we want to pick up some intermediate Jacobians
                double dt0 = pickUpDate.durationFrom(interpolator.getPreviousState().getDate());
                double dt1 = pickUpDate.durationFrom(interpolator.getCurrentState().getDate());
                if (dt0 * dt1 > 0) {
                    // the current step does not cover the pickup date
                    return;
                } else {
                    interpolated = interpolator.getInterpolatedState(pickUpDate);
                }
            }

            Assert.assertEquals(1, interpolated.getAdditionalStates().size());
            Assert.assertTrue(interpolated.getAdditionalStates().containsKey(mapper.getName()));
            mapper.getStateJacobian(interpolated, dYdY0);
            mapper.getParametersJacobian(interpolated, dYdP);

        }

    }

    @Before
    public void setUp() {
        Utils.setDataRoot("regular-data:potential/shm-format");
        GravityFieldFactory.addPotentialCoefficientsReader(new SHMFormatReader("^eigen_cg03c_coef$", false));
    }

}
