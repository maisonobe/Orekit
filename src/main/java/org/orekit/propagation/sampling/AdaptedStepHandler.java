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
package org.orekit.propagation.sampling;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.math.exception.MathUserException;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.errors.PropagationException;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.AdditionalStateData;
import org.orekit.propagation.numerical.ModeHandler;
import org.orekit.time.AbsoluteDate;

/** Adapt an {@link org.orekit.propagation.sampling.OrekitStepHandler}
 * to commons-math {@link StepHandler} interface.
 * @author Luc Maisonobe
 * @version $Revision$ $Date$
 */
public class AdaptedStepHandler
    implements OrekitStepInterpolator, StepHandler, ModeHandler, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = -8067262257341902186L;

    /** Propagation orbit type. */
    private OrbitType orbitType;

    /** Position angle type. */
    private PositionAngle angleType;

    /** Attitude provider. */
    private AttitudeProvider attitudeProvider;

    /** Additional state data list. */
    private List <AdditionalStateData> addStateData;

    /** Reference date. */
    private AbsoluteDate initializedReference;

    /** Reference frame. */
    private Frame initializedFrame;

    /** Central body attraction coefficient. */
    private double initializedMu;

    /** Underlying handler. */
    private final OrekitStepHandler handler;

    /** Flag for handler . */
    private boolean activate;

    /** Underlying raw rawInterpolator. */
    private StepInterpolator rawInterpolator;

    /** Build an instance.
     * @param handler underlying handler to wrap
     */
    public AdaptedStepHandler(final OrekitStepHandler handler) {
        this.handler = handler;
    }

    /** {@inheritDoc} */
    public void initialize(final OrbitType orbitType, final PositionAngle angleType,
                           final AttitudeProvider attitudeProvider,
                           final List <AdditionalStateData> addStateData,
                           final boolean activateHandlers,
                           final AbsoluteDate reference, final Frame frame, final double mu) {
        this.orbitType            = orbitType;
        this.angleType            = angleType;
        this.attitudeProvider     = attitudeProvider;
        this.addStateData         = addStateData;
        this.activate             = activateHandlers;
        this.initializedReference = reference;
        this.initializedFrame     = frame;
        this.initializedMu        = mu;
    }

    /** {@inheritDoc} */
    public boolean requiresDenseOutput() {
        return handler.requiresDenseOutput();
    }

    /** {@inheritDoc} */
    public void reset() {
        handler.reset();
    }

    /** {@inheritDoc} */
    public void handleStep(final StepInterpolator interpolator, final boolean isLast)
        throws MathUserException {
        try {
            this.rawInterpolator = interpolator;
            if (activate) {
                handler.handleStep(this, isLast);
            }
        } catch (PropagationException pe) {
            throw new MathUserException(pe);
        }
    }

    /** Get the current grid date.
     * @return current grid date
     */
    public AbsoluteDate getCurrentDate() {
        return initializedReference.shiftedBy(rawInterpolator.getCurrentTime());
    }

    /** Get the previous grid date.
     * @return previous grid date
     */
    public AbsoluteDate getPreviousDate() {
        return initializedReference.shiftedBy(rawInterpolator.getPreviousTime());
    }

    /** Get the interpolated date.
     * <p>If {@link #setInterpolatedDate(AbsoluteDate) setInterpolatedDate}
     * has not been called, the date returned is the same as  {@link
     * #getCurrentDate() getCurrentDate}.</p>
     * @return interpolated date
     * @see #setInterpolatedDate(AbsoluteDate)
     * @see #getInterpolatedState()
     */
    public AbsoluteDate getInterpolatedDate() {
        return initializedReference.shiftedBy(rawInterpolator.getInterpolatedTime());
    }

    /** Set the interpolated date.
     * <p>It is possible to set the interpolation date outside of the current
     * step range, but accuracy will decrease as date is farther.</p>
     * @param date interpolated date to set
     * @see #getInterpolatedDate()
     * @see #getInterpolatedState()
     */
    public void setInterpolatedDate(final AbsoluteDate date) {
        rawInterpolator.setInterpolatedTime(date.durationFrom(initializedReference));
    }

    /** Get the interpolated state.
     * @return interpolated state at the current interpolation date
     * @exception OrekitException if state cannot be interpolated or converted
     * @see #getInterpolatedDate()
     * @see #setInterpolatedDate(AbsoluteDate)
     */
    public SpacecraftState getInterpolatedState() throws OrekitException {
        try {
            final double[] y = rawInterpolator.getInterpolatedState();
            final AbsoluteDate interpolatedDate = initializedReference.shiftedBy(rawInterpolator.getInterpolatedTime());
            final Orbit orbit =
                orbitType.mapArrayToOrbit(y, angleType, interpolatedDate, initializedMu, initializedFrame);
            final Attitude attitude = attitudeProvider.getAttitude(orbit, interpolatedDate, initializedFrame);
            return new SpacecraftState(orbit, attitude, y[6]);
        } catch (MathUserException mue) {
            throw PropagationException.unwrap(mue);
        }
    }

    /** {@inheritDoc} */
    public double[] getInterpolatedAdditionalState(final String name)
        throws OrekitException {
        try {

            // propagate the whole state vector
            final double[] y = rawInterpolator.getInterpolatedState();

            // get portion of additional state to update
            int index = 7;
            for (final AdditionalStateData stateData : addStateData) {
                if (stateData.getName().equals(name)) {
                    final double[] state = stateData.getAdditionalState();
                    System.arraycopy(y, index, state, 0, state.length);
                    return state;
                }
                // incrementing index
                index += stateData.getAdditionalState().length;
            }

            throw new OrekitException(OrekitMessages.UNKNOWN_ADDITIONAL_EQUATION);

        } catch (MathUserException mue) {
            throw PropagationException.unwrap(mue);
        }

    }

    /** Check is integration direction is forward in date.
     * @return true if integration is forward in date
     */
    public boolean isForward() {
        return rawInterpolator.isForward();
    }

}
