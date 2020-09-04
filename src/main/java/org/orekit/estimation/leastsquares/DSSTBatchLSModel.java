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
package org.orekit.estimation.leastsquares;

import java.util.List;

import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.propagation.AbstractPropagator;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.conversion.ODPropagatorBuilder;
import org.orekit.propagation.integration.AbstractJacobiansMapper;
import org.orekit.propagation.semianalytical.dsst.DSSTJacobiansMapper;
import org.orekit.propagation.semianalytical.dsst.DSSTPartialDerivativesEquations;
import org.orekit.propagation.semianalytical.dsst.DSSTPropagator;
import org.orekit.utils.ParameterDriversList;

/** Bridge between {@link ObservedMeasurement measurements} and {@link
 * org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem
 * least squares problems}.
 * <p>
 * This class is an adaption of the {@link BatchLSModel} class
 * but for the {@link DSSTPropagator DSST propagator}.
 * </p>
 * @author Luc Maisonobe
 * @author Bryan Cazabonne
 * @since 10.0
 *
 */
public class DSSTBatchLSModel extends AbstractBatchLSModel {


    /** Type of the orbit used for the propagation.*/
    private PropagationType propagationType;

    /** Type of the elements used to define the orbital state.*/
    private PropagationType stateType;

    /** Simple constructor.
     * @param propagatorBuilders builders to use for propagation
     * @param measurements measurements
     * @param estimatedMeasurementsParameters estimated measurements parameters
     * @param observer observer to be notified at model calls
     * @param propagationType type of the orbit used for the propagation (mean or osculating)
     * @param stateType type of the elements used to define the orbital state (mean or osculating)
     */
    public DSSTBatchLSModel(final ODPropagatorBuilder[] propagatorBuilders,
                     final List<ObservedMeasurement<?>> measurements,
                     final ParameterDriversList estimatedMeasurementsParameters,
                     final ModelObserver observer,
                     final PropagationType propagationType,
                     final PropagationType stateType) {

        super(propagatorBuilders, measurements,
                                  estimatedMeasurementsParameters,
                                  observer);
        this.propagationType = propagationType;
        this.stateType       = stateType;
    }


    /** Configure the propagator to compute derivatives.
     * @param propagators {@link Propagator} to configure
     * @return mapper for this propagator
     */
    protected DSSTJacobiansMapper configureDerivatives(final AbstractPropagator propagators) {

        final String equationName = DSSTBatchLSModel.class.getName() + "-derivatives";

        final DSSTPartialDerivativesEquations partials = new DSSTPartialDerivativesEquations(equationName, (DSSTPropagator) propagators, propagationType);

        // add the derivatives to the initial state
        final SpacecraftState rawState = propagators.getInitialState();
        final SpacecraftState stateWithDerivatives = partials.setInitialJacobians(rawState);
        ((DSSTPropagator) propagators).setInitialState(stateWithDerivatives, stateType);

        return partials.getMapper();

    }


    @Override
    protected AbstractJacobiansMapper[] buildMappers() {
        return new DSSTJacobiansMapper[getBuilders().length];
    }

    @Override
    protected AbstractPropagator[] buildPropagators() {
        return new DSSTPropagator[getBuilders().length];
    }

    @Override
    protected void computeDerivatives(final AbstractJacobiansMapper mapper,
                                      final SpacecraftState state) {
        ((DSSTJacobiansMapper) mapper).setShortPeriodJacobians(state);
    }

}
