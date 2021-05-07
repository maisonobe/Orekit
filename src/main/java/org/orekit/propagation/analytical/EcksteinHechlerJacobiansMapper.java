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
package org.orekit.propagation.analytical;

import org.hipparchus.analysis.differentiation.Gradient;
import org.orekit.orbits.FieldOrbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.FieldPVCoordinates;
import org.orekit.utils.ParameterDriversList;

/** Mapper between two-dimensional Jacobian matrices and one-dimensional {@link
 * SpacecraftState#getAdditionalState(String) additional state arrays}.
 * @author Nicolas Fialton
 */
public class EcksteinHechlerJacobiansMapper extends AbstractAnalyticalJacobiansMapper {

    /** Placeholder for the derivatives of state. */
    private double[] stateTransition;

    /** Name. */
    private String name;

    /** Propagator computing state evolution. */
    private final EcksteinHechlerPropagator propagator;

    /** Simple constructor.
     * @param name name of the Jacobians
     * @param propagator the propagator that will handle the orbit propagation
     */
    public EcksteinHechlerJacobiansMapper(final String name,
                                          final EcksteinHechlerPropagator propagator) {
        // No propagation parameters estimated with Eckstein-Hechler propagator
        super(name, new ParameterDriversList(), EcksteinHechlerGradientConverter.FREE_STATE_PARAMETERS);
        // Initialize fields
        this.name            = name;
        this.propagator      = propagator;
        this.stateTransition = null;
    }

    /** {@inheritDoc} */
    @Override
    public void getStateJacobian(final SpacecraftState state, final double[][] dYdY0) {
        // Number of state parameters
        final int stateDimension = getStateDimension();

        // Fill dYdY0 matrix
        for (int i = 0; i < stateDimension; i++) {
            final double[] row = dYdY0[i];
            for (int j = 0; j < getStateDimension(); j++) {
                row[j] = stateTransition[i * stateDimension + j];
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getParametersJacobian(final SpacecraftState state, final double[][] dYdP) {
        // Nothing to do: No propagation parameters with Eckstein-Hechler model
    }

    /** {@inheritDoc} */
    @Override
    public void analyticalDerivatives(final SpacecraftState s) {

        final double[] p = s.getAdditionalState(name);
        if (stateTransition == null) {
            stateTransition = new double[p.length];
        }

        // initialize Jacobians to zero
        final int dim = getStateDimension();
        final double[][] stateGrad = new double[dim][dim];
        final EcksteinHechlerGradientConverter converter = new EcksteinHechlerGradientConverter(s, propagator);
        final FieldEcksteinHechlerPropagator<Gradient> gPropagator = converter.getPropagator();
        final Gradient[] gParameters = converter.getParameters();

        // Compute Jacobian
        final AbsoluteDate init = getInitialState().getDate();
        final AbsoluteDate end  = s.getDate();
        final double dt = end.durationFrom(init);
        final FieldOrbit<Gradient> orbit = gPropagator.propagateOrbit(gPropagator.getInitialState().getDate().shiftedBy(dt), gParameters);
        final FieldPVCoordinates<Gradient> pv = orbit.getPVCoordinates();

        final double[] derivativesX    = pv.getPosition().getX().getGradient();
        final double[] derivativesY    = pv.getPosition().getY().getGradient();
        final double[] derivativesZ    = pv.getPosition().getZ().getGradient();
        final double[] derivativesXDot = pv.getVelocity().getX().getGradient();
        final double[] derivativesYDot = pv.getVelocity().getY().getGradient();
        final double[] derivativesZDot = pv.getVelocity().getZ().getGradient();

        // update Jacobian with respect to state
        addToRow(derivativesX,    0, stateGrad);
        addToRow(derivativesY,    1, stateGrad);
        addToRow(derivativesZ,    2, stateGrad);
        addToRow(derivativesXDot, 3, stateGrad);
        addToRow(derivativesYDot, 4, stateGrad);
        addToRow(derivativesZDot, 5, stateGrad);

        // the previous derivatives correspond to the state transition matrix
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                stateTransition[j + dim * i] = stateGrad[i][j];
            }
        }

    }

   /** Getter for initial propagator state.
    * @return the propagator initial state
    */
    public SpacecraftState getInitialState() {
        return propagator.getInitialState();
    }
}
