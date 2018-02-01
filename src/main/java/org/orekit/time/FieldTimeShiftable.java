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
package org.orekit.time;

import org.hipparchus.RealFieldElement;

/** This interface represents objects that can be shifted in time.
 * @param <T> Type of the object.
 * @param <KK> type of the field elements
 * @author Luc Maisonobe
 * @since 9.0
 */
public interface FieldTimeShiftable<T extends FieldTimeInterpolable<T, KK>, KK extends RealFieldElement<KK>> {

    /** Get a time-shifted instance.
     * @param dt time shift in seconds
     * @return a new instance, shifted with respect to instance (which is not changed)
     */
    T shiftedBy(double dt);

    /** Get a time-shifted instance.
     * @param dt time shift in seconds
     * @return a new instance, shifted with respect to instance (which is not changed)
     */
    T shiftedBy(KK dt);

}
