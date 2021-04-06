/* Contributed in the public domain.
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
package org.orekit.files.ccsds.ndm.odm.oem;

import java.io.IOException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.ccsds.definitions.FrameFacade;
import org.orekit.files.ccsds.section.Header;
import org.orekit.files.ccsds.utils.generation.Generator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;

/**
 * A writer for OEM files.
 *
 * <p> Each instance corresponds to a single OEM file. A new OEM ephemeris segment is
 * started by calling {@link #newSegment()}.
 *
 * <p> This class can be used as a step handler for a {@link Propagator}.
 *
 * <pre>{@code
 * Propagator propagator = ...; // pre-configured propagator
 * OEMWriter  oemWriter  = ...; // pre-configured writer with header and metadata
 *   try (Generator out = ...;) { // set-up output stream
 *     propagator.setMasterMode(step, new StreamingOemWriter(out, oemWriter).newSegment());
 *     propagator.propagate(startDate, stopDate);
 *   }
 * }</pre>
 *
 *
 * @author Evan Ward
 * @see <a href="https://public.ccsds.org/Pubs/502x0b2c1.pdf">CCSDS 502.0-B-2 Orbit Data
 *      Messages</a>
 * @see <a href="https://public.ccsds.org/Pubs/500x0g4.pdf">CCSDS 500.0-G-4 Navigation
 *      Data Definitions and Conventions</a>
 * @see OemWriter
 */
public class StreamingOemWriter {

    /** Generator for OEM output. */
    private final Generator generator;

    /** Writer for the OEM message format. */
    private final OemWriter writer;

    /** Header. */
    private final Header header;

    /** Current metadata. */
    private final OemMetadata metadata;

    /** Indicator for writing header. */
    private boolean headerWritePending;

    /** Simple constructor.
     * @param generator generator for OEM output
     * @param writer writer for the AEM message format
     * @param header file header (may be null)
     * @param template template for metadata
     * @since 11.0
     */
    public StreamingOemWriter(final Generator generator, final OemWriter writer,
                              final Header header, final OemMetadata template) {
        this.generator          = generator;
        this.writer             = writer;
        this.header             = header;
        this.metadata           = template.copy();
        this.headerWritePending = true;
    }

    /**
     * Create a writer for a new OEM ephemeris segment.
     * <p> The returned writer can only write a single ephemeris segment in an OEM.
     * This method must be called to create a writer for each ephemeris segment.
     * @return a new OEM segment writer, ready for use.
     */
    public SegmentWriter newSegment() {
        return new SegmentWriter();
    }

    /** A writer for a segment of an OEM. */
    public class SegmentWriter implements OrekitFixedStepHandler {

        /**
         * {@inheritDoc}
         *
         * <p> Sets the {@link OemMetadataKey#START_TIME} and {@link OemMetadataKey#STOP_TIME} in this
         * segment's metadata if not already set by the user. Then calls {@link OemWriter#writeHeader(Generator)
         * writeHeader} if it is the first segment) and {@link OemWriter#writeMetadata()} to start the segment.
         */
        @Override
        public void init(final SpacecraftState s0, final AbsoluteDate t, final double step) {
            try {
                if (t.isBefore(s0)) {
                    throw new OrekitException(OrekitMessages.NON_CHRONOLOGICALLY_SORTED_ENTRIES, s0.getDate(), t);
                }

                if (headerWritePending) {
                    // we write the header only for the first segment
                    writer.writeHeader(generator, header);
                    headerWritePending = false;
                }

                metadata.setStartTime(s0.getDate());
                metadata.setUseableStartTime(null);
                metadata.setUseableStopTime(null);
                metadata.setStopTime(t);
                metadata.setReferenceFrame(FrameFacade.map(s0.getAttitude().getReferenceFrame()));
                writer.writeMetadata(generator, metadata);
                writer.startData(generator);
            } catch (IOException e) {
                throw new OrekitException(e, LocalizedCoreFormats.SIMPLE_MESSAGE, e.getLocalizedMessage());
            }
        }

        /** {@inheritDoc}. */
        @Override
        public void handleStep(final SpacecraftState currentState, final boolean isLast) {
            try {
                writer.writeOrbitEphemerisLine(generator, metadata, currentState.getPVCoordinates(), true);
                if (isLast) {
                    writer.endData(generator);
                }
            } catch (IOException e) {
                throw new OrekitException(e, LocalizedCoreFormats.SIMPLE_MESSAGE, e.getLocalizedMessage());
            }
        }

    }

}
