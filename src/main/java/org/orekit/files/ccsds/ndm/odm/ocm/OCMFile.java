/* Copyright 2002-2021 CS GROUP
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

package org.orekit.files.ccsds.ndm.odm.ocm;

import org.orekit.data.DataContext;
import org.orekit.files.ccsds.ndm.odm.ODMFile;
import org.orekit.files.ccsds.section.Segment;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.utils.IERSConventions;

/** This class gathers the informations present in the Orbit Comprehensive Message (OCM), and contains
 * methods to generate {@link CartesianOrbit}, {@link KeplerianOrbit}, {@link SpacecraftState}
 * or a full {@link Propagator}.
 * @author Luc Maisonobe
 * @since 11.0
 */
public class OCMFile extends ODMFile<Segment<OCMMetadata, OCMData>> {

    /** Key for format version. */
    public static final String FORMAT_VERSION_KEY = "CCSDS_OCM_VERS";

    /** Simple constructor.
     * @param conventions IERS conventions
     * @param dataContext used for creating frames, time scales, etc.
     */
    public OCMFile(final IERSConventions conventions, final DataContext dataContext) {
        super(conventions, dataContext);
    }

    /** Get the metadata from the single {@link #getSegments() segment}.
     * @return metadata from the single {@link #getSegments() segment}
     */
    public OCMMetadata getMetadata() {
        return getSegments().get(0).getMetadata();
    }

    /** Get the data from the single {@link #getSegments() segment}.
     * @return data from the single {@link #getSegments() segment}
     */
    public OCMData getData() {
        return getSegments().get(0).getData();
    }

}