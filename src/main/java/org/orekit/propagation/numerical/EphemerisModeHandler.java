/* Copyright 2002-2011 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
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
package org.orekit.propagation.numerical;

import java.util.List;

import org.apache.commons.math.exception.MathUserException;
import org.apache.commons.math.ode.ContinuousOutputModel;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.precomputed.IntegratedEphemeris;
import org.orekit.time.AbsoluteDate;

/** This class stores sequentially generated orbital parameters for
 * later retrieval.
 *
 * <p>
 * Instances of this class are built and then must be fed with the results
 * provided by {@link org.orekit.propagation.Propagator Propagator} objects
 * configured in {@link org.orekit.propagation.Propagator#setEphemerisMode()
 * ephemeris generation mode}. Once propagation is over, a {@link
 * BoundedPropagator} can be built from the stored steps.
 * </p>
 *
 * @see org.orekit.propagation.numerical.NumericalPropagator
 * @author Luc Maisonobe
 */
class EphemerisModeHandler implements ModeHandler, StepHandler {

    /** Propagation orbit type. */
    private OrbitType orbitType;

    /** Position angle type. */
    private PositionAngle angleType;

    /** Attitude provider. */
    private AttitudeProvider attitudeProvider;

    /** List of additional state data. */
    private List<AdditionalStateData> stateData;

    /** Reference date. */
    private AbsoluteDate initializedReference;

    /** Frame. */
    private Frame initializedFrame;

    /** Central body gravitational constant. */
    private double initializedMu;

    /** Underlying raw mathematical model. */
    private ContinuousOutputModel model;

    /** Generated ephemeris. */
    private BoundedPropagator ephemeris;

    /** Flag for handler . */
    private boolean activate;

    /** Creates a new instance of EphemerisModeHandler which must be
     *  filled by the propagator.
     */
    public EphemerisModeHandler() {
    }

    /** {@inheritDoc} */
    public void initialize(final OrbitType orbitType, final PositionAngle angleType,
                           final AttitudeProvider attitudeProvider,
                           final List <AdditionalStateData> additionalStateData,
                           final boolean activateHandlers, final AbsoluteDate reference,
                           final Frame frame, final double mu) {
        this.orbitType            = orbitType;
        this.angleType            = angleType;
        this.attitudeProvider     = attitudeProvider;
        this.stateData            = additionalStateData;
        this.activate             = activateHandlers;
        this.initializedReference = reference;
        this.initializedFrame     = frame;
        this.initializedMu        = mu;
        this.model                = new ContinuousOutputModel();

        // ephemeris will be generated when last step is processed
        this.ephemeris = null;

    }

    /** Get the generated ephemeris.
     * @return a new instance of the generated ephemeris
     */
    public BoundedPropagator getEphemeris() {
        return ephemeris;
    }

    /** {@inheritDoc} */
    public void handleStep(final StepInterpolator interpolator, final boolean isLast)
        throws MathUserException {
        try {
            if (activate) {
                model.handleStep(interpolator, isLast);
                if (isLast) {

                    // set up the boundary dates
                    final double tI = model.getInitialTime();
                    final double tF = model.getFinalTime();
                    final AbsoluteDate startDate = initializedReference.shiftedBy(tI);
                    final AbsoluteDate minDate;
                    final AbsoluteDate maxDate;
                    if (tF < tI) {
                        minDate = initializedReference.shiftedBy(tF);
                        maxDate = startDate;
                    } else {
                        minDate = startDate;
                        maxDate = initializedReference.shiftedBy(tF);
                    }

                    // create the ephemeris
                    ephemeris = new IntegratedEphemeris(startDate, minDate, maxDate,
                                                        orbitType, angleType, attitudeProvider,
                                                        stateData, model,
                                                        initializedFrame, initializedMu);

                }
            }
        } catch (OrekitException oe) {
            throw new MathUserException(oe);
        }
    }

    /** {@inheritDoc} */
    public boolean requiresDenseOutput() {
        return model.requiresDenseOutput();
    }

    /** {@inheritDoc} */
    public void reset() {
        model.reset();
    }

}
