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
package org.orekit.files.ccsds.ndm.adm.aem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.orekit.data.DataContext;
import org.orekit.errors.OrekitIllegalArgumentException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.ccsds.Keyword;
import org.orekit.files.ccsds.ndm.adm.aem.StreamingAemWriter.SegmentWriter;
import org.orekit.files.ccsds.section.Header;
import org.orekit.files.general.AttitudeEphemerisFile;
import org.orekit.files.general.AttitudeEphemerisFile.AttitudeEphemerisSegment;
import org.orekit.files.general.AttitudeEphemerisFile.SatelliteAttitudeEphemeris;
import org.orekit.files.general.AttitudeEphemerisFileWriter;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedAngularCoordinates;

/**
 * A writer for Attitude Ephemeris Messsage (AEM) files.
 * @author Bryan Cazabonne
 * @since 10.2
 */
public class AEMWriter implements AttitudeEphemerisFileWriter {

    /** IERS Conventions. */
    private final  IERSConventions conventions;

    /** Indicator for simple or accurate EOP interpolation. */
    private final  boolean simpleEOP;

    /** Data context used for obtain frames and time scales. */
    private final DataContext dataContext;

    /** Reference date for Mission Elapsed Time or Mission Relative Time time systems. */
    private final AbsoluteDate missionReferenceDate;

    /** File header. */
    private final Header header;

    /** Metadata for the ephemeris. */
    private final AEMMetadata metadata;

    /** Format for attitude ephemeris data output. */
    private final String attitudeFormat;

    /**
     * Standard default constructor that creates a writer with default configurations
     * including {@link StreamingAemWriter#DEFAULT_ATTITUDE_FORMAT Default formatting}.
     * @param conventions IERS Conventions
     * @param simpleEOP if true, tidal effects are ignored when interpolating EOP
     * @param dataContext used to retrieve frames, time scales, etc.
     * @param missionReferenceDate reference date for Mission Elapsed Time or Mission Relative Time time systems
     * (may be null if time system is absolute)
     * @param header file header
     * @param metadata metadata for the ephemeris
     * @since 11.0
     */
    public AEMWriter(final IERSConventions conventions, final boolean simpleEOP,
                     final DataContext dataContext, final AbsoluteDate missionReferenceDate,
                     final Header header, final AEMMetadata metadata) {
        this(conventions, simpleEOP, dataContext, missionReferenceDate,
             header, metadata, StreamingAemWriter.DEFAULT_ATTITUDE_FORMAT);
    }

    /**
     * Constructor used to create a new AEM writer configured with the necessary parameters
     * to successfully fill in all required fields that aren't part of a standard object
     * and using {@link StreamingAemWriter#DEFAULT_ATTITUDE_FORMAT default formatting}
     * for attitude ephemeris data output.
     *
     * @param conventions IERS Conventions
     * @param simpleEOP if true, tidal effects are ignored when interpolating EOP
     * @param dataContext used to retrieve frames, time scales, etc.
     * @param missionReferenceDate reference date for Mission Elapsed Time or Mission Relative Time time systems
     * (may be null if time system is absolute)
     * @param header file header
     * @param metadata metadata for the ephemeris
     * @param attitudeFormat {@link java.util.Formatter format parameters} for
     *                       attitude ephemeris data output
     * @since 11.0
     */
    public AEMWriter(final IERSConventions conventions, final boolean simpleEOP,
                     final DataContext dataContext, final AbsoluteDate missionReferenceDate,
                     final Header header, final AEMMetadata metadata, final String attitudeFormat) {
        this.conventions          = conventions;
        this.simpleEOP            = simpleEOP;
        this.dataContext          = dataContext;
        this.missionReferenceDate = missionReferenceDate;
        this.header               = header;
        this.metadata             = metadata;
        this.attitudeFormat       = attitudeFormat;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final Appendable writer, final AttitudeEphemerisFile ephemerisFile)
        throws IOException {

        if (writer == null) {
            throw new OrekitIllegalArgumentException(OrekitMessages.NULL_ARGUMENT, "writer");
        }

        if (ephemerisFile == null) {
            return;
        }

        final String idToProcess;
        if (metadata.getObjectID() != null) {
            if (ephemerisFile.getSatellites().containsKey(metadata.getObjectID())) {
                idToProcess = metadata.getObjectID();
            } else {
                throw new OrekitIllegalArgumentException(OrekitMessages.VALUE_NOT_FOUND,
                                                         metadata.getObjectID(), "ephemerisFile");
            }
        } else if (ephemerisFile.getSatellites().keySet().size() == 1) {
            idToProcess = ephemerisFile.getSatellites().keySet().iterator().next();
        } else {
            throw new OrekitIllegalArgumentException(OrekitMessages.EPHEMERIS_FILE_NO_MULTI_SUPPORT);
        }

        // Get satellite and attitude ephemeris segments to output.
        final SatelliteAttitudeEphemeris satEphem = ephemerisFile.getSatellites().get(idToProcess);
        final List<? extends AttitudeEphemerisSegment> segments = satEphem.getSegments();
        if (segments.isEmpty()) {
            // No data -> No output
            return;
        }
        // First segment
        final AttitudeEphemerisSegment firstSegment = segments.get(0);

        // Header comments. If header comments are presents, they are assembled together in a single line
        if (ephemerisFile instanceof AEMFile) {
            // Cast to AEMFile
            final AEMFile aemFile = (AEMFile) ephemerisFile;
            if (!aemFile.getHeader().getComments().isEmpty()) {
                // Loop on comments
                final StringBuffer buffer = new StringBuffer();
                for (String comment : aemFile.getHeader().getComments()) {
                    buffer.append(comment);
                }
                // Update metadata
                // TODO: comments should not be assembled in a single line
                metadata.put(Keyword.COMMENT, buffer.toString());
            }
        }

        // Writer for AEM files
        final StreamingAemWriter aemWriter =
                        new StreamingAemWriter(writer, conventions, dataContext, header, metadata, attitudeFormat);
        aemWriter.writeHeader();

        // Loop on segments
        for (final AttitudeEphemerisSegment segment : segments) {
            // Segment specific metadata
            metadata.clear();
            metadata.put(Keyword.CENTER_NAME,          segment.getCenterName());
            metadata.put(Keyword.REF_FRAME_A,          segment.getRefFrameAString());
            metadata.put(Keyword.REF_FRAME_B,          segment.getRefFrameBString());
            metadata.put(Keyword.ATTITUDE_DIR,         segment.getAttitudeDirection());
            metadata.put(Keyword.START_TIME,           segment.getStart().toString(timeScale));
            metadata.put(Keyword.STOP_TIME,            segment.getStop().toString(timeScale));
            metadata.put(Keyword.ATTITUDE_TYPE,        segment.getAttitudeType());
            metadata.put(Keyword.INTERPOLATION_METHOD, segment.getInterpolationMethod());
            metadata.put(Keyword.INTERPOLATION_DEGREE,
                         String.valueOf(segment.getInterpolationSamples() - 1));

            final SegmentWriter segmentWriter = aemWriter.newSegment(metadata);
            segmentWriter.writeMetadata();
            segmentWriter.startAttitudeBlock();
            // Loop on attitude data
            for (final TimeStampedAngularCoordinates coordinates : segment.getAngularCoordinates()) {
                segmentWriter.writeAttitudeEphemerisLine(coordinates, segment.isFirst(),
                                                         segment.getAttitudeType(), segment.getRotationOrder());
            }
            segmentWriter.endAttitudeBlock();
        }

    }

    /**
     * Write the passed in {@link AEMFile} using the passed in {@link Appendable}.
     * @param writer a configured Appendable to feed with text
     * @param aemFile a populated aem file to serialize into the buffer
     * @throws IOException if any buffer writing operations fail or if the underlying
     *         format doesn't support a configuration in the EphemerisFile
     *         for example having multiple satellites in one file, having
     *         the origin at an unspecified celestial body, etc.)
     */
    public void write(final Appendable writer, final AEMFile aemFile) throws IOException {
        write(writer, (AttitudeEphemerisFile) aemFile);
    }

    /**
     * Write the passed in {@link AEMFile} to a file at the output path specified.
     * @param outputFilePath a file path that the corresponding file will be written to
     * @param aemFile a populated aem file to serialize into the buffer
     * @throws IOException if any file writing operations fail or if the underlying
     *         format doesn't support a configuration in the EphemerisFile
     *         (for example having multiple satellites in one file, having
     *         the origin at an unspecified celestial body, etc.)
     */
    public void write(final String outputFilePath, final AEMFile aemFile)
        throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath), StandardCharsets.UTF_8)) {
            write(writer, (AttitudeEphemerisFile) aemFile);
        }
    }

}