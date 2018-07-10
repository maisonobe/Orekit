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
package org.orekit.propagation.semianalytical.dsst.forces;

import java.util.List;

import org.hipparchus.Field;
import org.hipparchus.RealFieldElement;
import org.hipparchus.util.MathArrays;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.FieldSpacecraftState;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.semianalytical.dsst.utilities.AuxiliaryElements;
import org.orekit.propagation.semianalytical.dsst.utilities.FieldAuxiliaryElements;
import org.orekit.utils.ParameterDriver;

/** This interface represents a force modifying spacecraft motion for a {@link
 *  org.orekit.propagation.semianalytical.dsst.DSSTPropagator DSSTPropagator}.
 *  <p>
 *  Objects implementing this interface are intended to be added to a {@link
 *  org.orekit.propagation.semianalytical.dsst.DSSTPropagator DSST propagator}
 *  before the propagation is started.
 *  </p>
 *  <p>
 *  The propagator will call at the very beginning of a propagation the {@link
 *  #initialize(AuxiliaryElements, boolean)} method allowing preliminary computation
 *  such as truncation if needed.
 *  </p>
 *  <p>
 *  Then the propagator will call at each step:
 *  <ol>
 *  <li>the {@link #initializeStep(AuxiliaryElements)} method.
 *  The force model instance will extract all the elements needed before
 *  computing the mean element rates.</li>
 *  <li>the {@link #getMeanElementRate(SpacecraftState)} method.
 *  The force model instance will extract all the state data needed to compute
 *  the mean element rates that contribute to the mean state derivative.</li>
 *  <li>the {@link #updateShortPeriodTerms(SpacecraftState...)} method,
 *  if osculating parameters are desired, on a sample of points within the
 *  last step.</li>
 *  </ol>
 *
 * @author Romain Di Constanzo
 * @author Pascal Parraud
 */
public interface DSSTForceModel {

    /** Performs initialization prior to propagation for the current force model.
     *  <p>
     *  This method aims at being called at the very beginning of a propagation.
     *  </p>
     *  @param auxiliaryElements auxiliary elements related to the current orbit
     *  @param meanOnly only mean elements are used during the propagation
     *  @param parameters values of the force model parameters
     *  @return a list of objects that will hold short period terms (the objects
     *  are also retained by the force model, which will update them during propagation)
     *  @throws OrekitException if some specific error occurs
     */
    List<ShortPeriodTerms> initialize(AuxiliaryElements auxiliaryElements, boolean meanOnly, double[] parameters)
        throws OrekitException;

    /** Performs initialization prior to propagation for the current force model.
     *  <p>
     *  This method aims at being called at the very beginning of a propagation.
     *  </p>
     *  @param <T> type of the elements
     *  @param auxiliaryElements auxiliary elements related to the current orbit
     *  @param meanOnly only mean elements are used during the propagation
     *  @param parameters values of the force model parameters
     *  @return a list of objects that will hold short period terms (the objects
     *  are also retained by the force model, which will update them during propagation)
     *  @throws OrekitException if some specific error occurs
     */
    <T extends RealFieldElement<T>> List<FieldShortPeriodTerms<T>> initialize(FieldAuxiliaryElements<T> auxiliaryElements, boolean meanOnly, T[] parameters)
        throws OrekitException;

    /** Get force model parameters.
     * @return force model parameters
     * @since 9.0
     */
    default double[] getParameters() {
        final ParameterDriver[] drivers = getParametersDrivers();
        final double[] parameters = new double[drivers.length];
        for (int i = 0; i < drivers.length; ++i) {
            parameters[i] = drivers[i].getValue();
        }
        return parameters;
    }

    /** Get force model parameters.
     * @param field field to which the elements belong
     * @param <T> type of the elements
     * @return force model parameters
     * @since 9.0
     */
    default <T extends RealFieldElement<T>> T[] getParameters(final Field<T> field) {
        final ParameterDriver[] drivers = getParametersDrivers();
        final T[] parameters = MathArrays.buildArray(field, drivers.length);
        for (int i = 0; i < drivers.length; ++i) {
            parameters[i] = field.getZero().add(drivers[i].getValue());
        }
        return parameters;
    }

    /** Computes the mean equinoctial elements rates da<sub>i</sub> / dt.
     *
     *  @param state current state information: date, kinematics, attitude
     *  @param auxiliaryElements auxiliary elements related to the current orbit
     *  @param parameters values of the force model parameters
     *  @return the mean element rates dai/dt
     *  @throws OrekitException if some specific error occurs
     */
    double[] getMeanElementRate(SpacecraftState state, AuxiliaryElements auxiliaryElements, double[] parameters)
         throws OrekitException;

    /** Computes the mean equinoctial elements rates da<sub>i</sub> / dt.
    *  @param <T> type of the elements
    *  @param state current state information: date, kinematics, attitude
    *  @param auxiliaryElements auxiliary elements related to the current orbit
    *  @param parameters values of the force model parameters
    *  @return the mean element rates dai/dt
    *  @throws OrekitException if some specific error occurs
    */
    <T extends RealFieldElement<T>> T[] getMeanElementRate(FieldSpacecraftState<T> state, FieldAuxiliaryElements<T> auxiliaryElements, T[] parameters)
        throws OrekitException;


    /** Get the discrete events related to the model.
     * @return array of events detectors or null if the model is not
     * related to any discrete events
     */
    EventDetector[] getEventsDetectors();

    /** Register an attitude provider.
     * <p>
     * Register an attitude provider that can be used by the force model.
     * </p>
     * @param provider the {@link AttitudeProvider}
     */
    void registerAttitudeProvider(AttitudeProvider provider);

    /** Update the short period terms.
     * <p>
     * The {@link ShortPeriodTerms short period terms} that will be updated
     * are the ones that were returned during the call to {@link
     * #initialize(AuxiliaryElements, boolean)}.
     * </p>
     * @param parameters values of the force model parameters
     * @param meanStates mean states information: date, kinematics, attitude
     * @throws OrekitException if some specific error occurs
     */
    void updateShortPeriodTerms(double[] parameters, SpacecraftState... meanStates)
        throws OrekitException;

    /** Update the short period terms.
     * <p>
     * The {@link ShortPeriodTerms short period terms} that will be updated
     * are the ones that were returned during the call to {@link
     * #initialize(AuxiliaryElements, boolean)}.
     * </p>
     * @param <T> type of the elements
     * @param parameters values of the force model parameters
     * @param meanStates mean states information: date, kinematics, attitude
     * @throws OrekitException if some specific error occurs
     */
    @SuppressWarnings("unchecked")
    <T extends RealFieldElement<T>> void updateShortPeriodTerms(T[] parameters, FieldSpacecraftState<T>... meanStates)
        throws OrekitException;

    /** Get the drivers for force model parameters.
     * @return drivers for force model parameters
     */
    ParameterDriver[] getParametersDrivers();

}
