/* Copyright 2002-2019 CS Systèmes d'Information
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
package org.orekit.files.ccsds;

/** Orbit type used in CCSDS {@link OCMFile Orbit Comprehensive Messages}.
 * @author Luc Maisonobe
 * @since 10.1
 */
public enum CCSDSOrbitType {

    /** Extended Geostationary Orbit, 37948 < a < 46380 km, e < 0.25, i < 25°. */
    EGO("Extended Geostationary Orbit"),

    /** Escape Orbit. */
    ESO("Escape Orbit"),

    /** GEO-superGEO, Crossing Orbit 31570 < hp < 40002 km, 40002 km < ha. */
    GHO("GEO-superGEO, Crossing Orbit"),

    /** Geosynchronous Earth Orbit, with i > 3°, 35586 < hp < 35986 km, 35586 < ha < 35986 km. */
    GEO("Geosynchronous Earth Orbit"),

    /** GeoStationary Orbit, with 3° < i < 25°, 35586 < hp < 35986 km, 35586 < ha < 35986 km. */
    GSO("GeoStationary Orbit"),

    /** Geosynchronous Transfer Orbit, i < 90°, hp < 2000 km, 31570 < ha < 40002 km. */
    GTO("Geosynchronous Transfer Orbit"),

    /** High Altitude Earth Orbit, 40002 km < hp, 40002 km < ha. */
    HAO("High Altitude Earth Orbit"),

    /** Highly Eccentric Earth Orbit, hp < 31570 km, 40002 km < ha. */
    HEO("Highly Eccentric Earth Orbit"),

    /** Inclined Geosynchronous Orbit, 37948 < a < 46380 km, e < 0.25, 25° < i < 180°. */
    IGO("Inclined Geosynchronous Orbit"),

    /** Low Earth Orbit, hp < 2000 km, ha < 2000 km. */
    LEO("Low Earth Orbit"),

    /** LEO-MEO Crossing Orbit, hp < 2000 km, 2000 < ha < 31570 km. */
    LMO("LEO-MEO Crossing Orbit"),

    /** Medium Earth Orbit, 2000 < hp < 31570 km, 2000 < ha < 31570 km. */
    MEO("Medium Earth Orbit"),

    /** MEO-GEO Crossing Orbit, 2000 < hp < 31570 km, 31570 < ha < 40002 km. */
    MGO("MEO-GEO Crossing Orbit"),

    /** Navigation Satellites Orbit 50° < i < 70°, 18100 < hp < 24300 km, 18100 < ha < 4300 km. */
    NSO("Navigation Satellites Orbit"),

    /** UFO: Undefined Orbit. */
    UFO("Undefined Orbit");

    /** Description. */
    private final String description;

    /** Simple constructor.
     * @param description description
     */
    CCSDSOrbitType(final String description) {
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return description;
    }

}