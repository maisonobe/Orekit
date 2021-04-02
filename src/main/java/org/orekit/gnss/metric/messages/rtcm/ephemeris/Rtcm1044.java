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
package org.orekit.gnss.metric.messages.rtcm.ephemeris;

/**
 * RTCM 1044 message: QZSS Satellite Ephemeris Data.
 * @author Bryan Cazabonne
 * @since 11.0
 */
public class Rtcm1044 extends RtcmEphemerisMessage<Rtcm1044Data> {

    /**
     * Constructor.
     * @param typeCode message number
     * @param rtcm1044Data RTCM 1044 message data
     */
    public Rtcm1044(final int typeCode, final Rtcm1044Data rtcm1044Data) {
        super(typeCode, rtcm1044Data);
    }

}
