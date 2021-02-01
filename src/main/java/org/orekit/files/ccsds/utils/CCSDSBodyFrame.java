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
package org.orekit.files.ccsds.utils;

import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;

/** Frames used in CCSDS Attitude Data Messages for the spacecraft body.
 * @author Luc Maisonobe
 * @since 11.0
 */
public class CCSDSBodyFrame {

    /** Equipment on which the frame is located. */
    public enum BaseEquipment {
        /** Actuator reference frame: could denote reaction wheels, solar arrays, thrusters, etc.. */
        ACTUATOR,

        /** Coarse Sun Sensor. */
        CSS,

        /** Digital Sun Sensor. */
        DSS,

        /** Gyroscope. */
        GYRO,

        /** Instrument. */
        INSTRUMENT,

        /** Spacecraft Body. */
        SC_BODY,

        /** Star Tracker. */
        STARTRACKER,

        /** Three Axis Magnetometer. */
        TAM;

    }

    /** Equipment on which the frame is located. */
    private final BaseEquipment baseEquipment;

    /** Frame label. */
    private final String label;

    /** Simple constructor.
     * @param baseEquipment equipment on which the frame is located
     * @param label frame label
     */
    public CCSDSBodyFrame(final BaseEquipment baseEquipment, final String label) {
        this.baseEquipment = baseEquipment;
        this.label         = label;
    }

    /** Get the quipment on which the frame is located.
     * @return equipment on which the frame is located
     */
    public BaseEquipment getBaseEquipment() {
        return baseEquipment;
    }

    /** Get the frame label.
     * @return frame label
     */
    public String getLabel() {
        return label;
    }

    /** Build an instance from a normalized descriptor.
     * <p>
     * Normalized strings have '_' characters replaced by spaces,
     * and multiple spaces collapsed as one space only.
     * </p>
     * @param descriptor normalized descriptor
     * @return parsed body frame
     */
    public static CCSDSBodyFrame parse(final String descriptor) {
        final int separatorIndex = descriptor.lastIndexOf(' ');
        if (separatorIndex >= 0) {
            try {
                final String equipmentName = descriptor.substring(0, separatorIndex).replace(' ', '_');
                return new CCSDSBodyFrame(BaseEquipment.valueOf(equipmentName),
                                          descriptor.substring(separatorIndex + 1));
            } catch (IllegalArgumentException iae) {
                // ignored, errors are handled below
            }
        }
        throw new OrekitException(OrekitMessages.CCSDS_INVALID_FRAME, descriptor);
    }

}