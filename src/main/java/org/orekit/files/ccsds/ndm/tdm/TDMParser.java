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
package org.orekit.files.ccsds.ndm.tdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hipparchus.exception.DummyLocalizable;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.data.DataContext;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.ccsds.Keyword;
import org.orekit.files.ccsds.ndm.NDMParser;
import org.orekit.files.ccsds.ndm.NDMSegment;
import org.orekit.files.ccsds.utils.CcsdsTimeScale;
import org.orekit.files.ccsds.utils.KeyValue;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Class for CCSDS Tracking Data Message parsers.
 *
 * <p> This base class is immutable, and hence thread safe. When parts must be
 * changed, such as reference date for Mission Elapsed Time or Mission Relative
 * Time time systems, or the gravitational coefficient or the IERS conventions,
 * the various {@code withXxx} methods must be called, which create a new
 * immutable instance with the new parameters. This is a combination of the <a
 * href="https://en.wikipedia.org/wiki/Builder_pattern">builder design
 * pattern</a> and a <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent
 * interface</a>.
 *
 * <p> This class allow the handling of both "keyvalue" and "xml" TDM file formats.
 * Format can be inferred if file names ends respectively with ".txt" or ".xml".
 * Otherwise it must be explicitely set using {@link #withFileFormat(TDMFileFormat)}
 *
 * <p>ParseInfo subclass regroups common parsing functions; and specific handlers were added
 * for both file formats.
 *
 * <p>References:<p>
 *  - <a href="https://public.ccsds.org/Pubs/503x0b1c1.pdf">CCSDS 503.0-B-1 recommended standard</a> ("Tracking Data Message", Blue Book, Issue 1, November 2007).<p>
 *  - <a href="https://public.ccsds.org/Pubs/505x0b1.pdf">CCSDS 505.0-B-1 recommended standard</a> ("XML Specification for Navigation Data Message", Blue Book, Issue 1, December 2010).<p>
 *
 * @author Maxime Journot
 * @since 9.0
 */
public class TDMParser extends NDMParser<TDMFile, TDMParser> {

    /** Pattern for delimiting regular expressions. */
    private static final Pattern SEPARATOR = Pattern.compile("\\s+");

    /** Enumerate for the format. */
    public enum TDMFileFormat {

        /** Keyvalue (text file with Key = Value lines). */
        KEYVALUE,

        /** XML format. */
        XML,

        /** UKNOWN file format, default format, throw an Orekit Exception if kept this way. */
        UNKNOWN;
    }

    /** Format of the file to parse: KEYVALUE or XML. */
    private TDMFileFormat fileFormat;

    /** Reference date for Mission Elapsed Time or Mission Relative Time time systems. */
    private final AbsoluteDate missionReferenceDate;

    /** Simple constructor.
     * <p>
     * This class is immutable, and hence thread safe. When parts
     * must be changed, such as IERS conventions, EOP style or data context,
     * the various {@code withXxx} methods must be called,
     * which create a new immutable instance with the new parameters. This
     * is a combination of the
     * <a href="https://en.wikipedia.org/wiki/Builder_pattern">builder design
     * pattern</a> and a
     * <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent
     * interface</a>.
     * </p>
     * <p>
     * The initial date for Mission Elapsed Time and Mission Relative Time time systems is not set here.
     * If such time systems are used, it must be initialized before parsing by calling {@link
     * #withMissionReferenceDate(AbsoluteDate)}.
     * </p>
     * <p>
     * The IERS conventions to use is not set here. If it is needed in order to
     * parse some reference frames or UT1 time scale, it must be initialized before
     * parsing by calling {@link #withConventions(IERSConventions)}.
     * </p>
     * <p>
     * The TDM file format to use is not set here. It may be automatically inferred while parsing
     * if the name of the file to parse ends with ".txt" or ".xml".
     * Otherwise it must be initialized before parsing by calling {@link #withFileFormat(TDMFileFormat)}
     * </p>
     *
     * <p>This method uses the {@link DataContext#getDefault() default data context}. See
     * {@link #withDataContext(DataContext)}.
     */
    @DefaultDataContext
    public TDMParser() {
        this(null, true, DataContext.getDefault(), TDMFileFormat.UNKNOWN, AbsoluteDate.FUTURE_INFINITY);
    }

    /** Complete constructor.
     * @param conventions IERS Conventions
     * @param simpleEOP if true, tidal effects are ignored when interpolating EOP
     * @param dataContext used to retrieve frames, time scales, etc.
     * @param fileFormat The format of the file: KEYVALUE or XML
     * @param missionReferenceDate reference date for Mission Elapsed Time or Mission Relative Time time systems
     */
    private TDMParser(final IERSConventions conventions,
                      final boolean simpleEOP,
                      final DataContext dataContext,
                      final TDMFileFormat fileFormat,
                      final AbsoluteDate missionReferenceDate) {
        super(conventions, simpleEOP, dataContext);
        this.fileFormat           = fileFormat;
        this.missionReferenceDate = missionReferenceDate;
    }

    /** Build a new instance.
     * @param newConventions IERS conventions to use while parsing
     * @param newSimpleEOP if true, tidal effects are ignored when interpolating EOP
     * @param newDataContext data context used for frames, time scales, and celestial bodies
     * @return a new instance with changed parameters
     * @since 11.0
     */
    protected TDMParser create(final IERSConventions newConventions,
                               final boolean newSimpleEOP,
                               final DataContext newDataContext) {
        return create(newConventions, newSimpleEOP, newDataContext, fileFormat, missionReferenceDate);
    }

    /** Build a new instance.
     * @param newConventions IERS conventions to use while parsing
     * @param newSimpleEOP if true, tidal effects are ignored when interpolating EOP
     * @param newDataContext data context used for frames, time scales, and celestial bodies
     * @param newFileFormat The format of the file: KEYVALUE or XML
     * @param newMissionReferenceDate mission reference date to use while parsing
     * @return a new instance with changed parameters
     * @since 11.0
     */
    protected TDMParser create(final IERSConventions newConventions,
                               final boolean newSimpleEOP,
                               final DataContext newDataContext,
                               final TDMFileFormat newFileFormat,
                               final AbsoluteDate newMissionReferenceDate) {
        return new TDMParser(newConventions, newSimpleEOP, newDataContext, newFileFormat, newMissionReferenceDate);
    }

    /** Set file format.
     * @param newFileFormat The format of the file: KEYVALUE or XML
     * @return a new instance, with file format set to newFileFormat
     * @see #getFileFormat()
     */
    public TDMParser withFileFormat(final TDMFileFormat newFileFormat) {
        return create(getConventions(), isSimpleEOP(), getDataContext(), newFileFormat, getMissionReferenceDate());
    }

    /** Get file format.
     * @return the file format
     * @see #withFileFormat(TDMFileFormat)
     */
    public TDMFileFormat getFileFormat() {
        return fileFormat;
    }

    /** Set initial date.
     * @param newMissionReferenceDate mission reference date to use while parsing
     * @return a new instance, with mission reference date replaced
     * @see #getMissionReferenceDate()
     */
    public TDMParser withMissionReferenceDate(final AbsoluteDate newMissionReferenceDate) {
        return new TDMParser(getConventions(), isSimpleEOP(), getDataContext(),
                             getFileFormat(), newMissionReferenceDate);
    }

    /** Get initial date.
     * @return mission reference date to use while parsing
     * @see #withMissionReferenceDate(AbsoluteDate)
     */
    public AbsoluteDate getMissionReferenceDate() {
        return missionReferenceDate;
    }

    /** Parse a CCSDS Tracking Data Message.
     * @param stream stream containing message
     * @param fileName name of the file containing the message (for error messages)
     * @return parsed file content in a TDMFile object
     */
    public TDMFile parse(final InputStream stream, final String fileName) {

        // Set the format of the file automatically
        // If it is obvious and was not formerly specified
        // Then, use a different parsing method for each file format
        if (TDMFileFormat.UNKNOWN.equals(fileFormat)) {
            if (fileName.toLowerCase(Locale.US).endsWith(".txt")) {
                // Keyvalue format case
                return this.withFileFormat(TDMFileFormat.KEYVALUE).parse(stream, fileName);
            } else if (fileName.toLowerCase(Locale.US).endsWith(".xml")) {
                // XML format case
                return this.withFileFormat(TDMFileFormat.XML).parse(stream, fileName);
            } else {
                throw new OrekitException(OrekitMessages.CCSDS_TDM_UNKNOWN_FORMAT, fileName);
            }
        } else if (this.fileFormat.equals(TDMFileFormat.KEYVALUE)) {
            return parseKeyValue(stream, fileName);
        } else if (this.fileFormat.equals(TDMFileFormat.XML)) {
            return parseXml(stream, fileName);
        } else {
            throw new OrekitException(OrekitMessages.CCSDS_TDM_UNKNOWN_FORMAT, fileName);
        }
    }

    /** Parse a CCSDS Tracking Data Message with KEYVALUE format.
     * @param stream stream containing message
     * @param fileName name of the file containing the message (for error messages)
     * @return parsed file content in a TDMFile object
     */
    public TDMFile parseKeyValue(final InputStream stream, final String fileName) {

        final KeyValueHandler handler = new KeyValueHandler(new ParseInfo(this.getMissionReferenceDate(),
                                                                          this.getConventions(),
                                                                          this.isSimpleEOP(),
                                                                          fileName,
                                                                          getDataContext()));
        return handler.parse(stream, fileName);
    }



    /** Parse a CCSDS Tracking Data Message with XML format.
     * @param stream stream containing message
     * @param fileName name of the file containing the message (for error messages)
     * @return parsed file content in a TDMFile object
     */
    public TDMFile parseXml(final InputStream stream, final String fileName) {
        try {
            // Create the handler
            final XMLHandler handler = new XMLHandler(new ParseInfo(this.getMissionReferenceDate(),
                                                                    this.getConventions(),
                                                                    this.isSimpleEOP(),
                                                                    fileName,
                                                                    getDataContext()));

            // Create the XML SAX parser factory
            final SAXParserFactory factory = SAXParserFactory.newInstance();

            // Build the parser and read the xml file
            final SAXParser parser = factory.newSAXParser();
            handler.parseInfo.parsingHeader = true;
            parser.parse(stream, handler);

            // Get the content of the file
            final TDMFile tdmFile = handler.parseInfo.tdmFile;

            // Check time systems consistency
            tdmFile.checkTimeSystems();

            return tdmFile;
        } catch (SAXException se) {
            final OrekitException oe;
            if (se.getException() != null && se.getException() instanceof OrekitException) {
                oe = (OrekitException) se.getException();
            } else {
                oe = new OrekitException(se, new DummyLocalizable(se.getMessage()));
            }
            throw oe;
        } catch (ParserConfigurationException | IOException e) {
            // throw caught exception as an OrekitException
            throw new OrekitException(e, new DummyLocalizable(e.getMessage()));
        }
    }

    /** Private class used to store TDM parsing info.
     * @author sports
     */
    private class ParseInfo {

        /** Name of the file. */
        private String fileName;

        /** Metadata for current observation block. */
        private TDMMetadata currentMetadata;

        /** Current Observation Block being parsed. */
        private ObservationsBlock currentObservationsBlock;

        /** Current line number. */
        private int lineNumber;

        /** Current parsed line. */
        private String line;

        /** TDMFile object being filled. */
        private TDMFile tdmFile;

        /** Key value of the current line being read. */
        private KeyValue keyValue;

        /** Boolean indicating if the parser is currently parsing a header block. */
        private boolean parsingHeader;

        /** Boolean indicating if the parser is currently parsing a meta-data block. */
        private boolean parsingMetaData;

        /** Boolean indicating if the parser is currently parsing a data block. */
        private boolean parsingData;

        /** Complete constructor.
         * @param missionReferenceDate reference date for Mission Elapsed Time or Mission Relative Time time systems
         * @param conventions IERS Conventions
         * @param simpleEOP if true, tidal effects are ignored when interpolating EOP
         * @param fileName the name of the file being parsed
         * @param dataContext used to retrieve frames, time scales, etc.
         */
        private ParseInfo(final AbsoluteDate missionReferenceDate,
                          final IERSConventions conventions,
                          final boolean simpleEOP,
                          final String fileName,
                          final DataContext dataContext) {
            this.fileName                 = fileName;
            this.lineNumber               = 0;
            this.line                     = "";
            this.tdmFile                  = new TDMFile();
            this.currentMetadata          = null;
            this.currentObservationsBlock = null;
            this.parsingHeader            = false;
            this.parsingMetaData          = false;
            this.parsingData              = false;
        }

        /** Parse a meta-data entry.<p>
         * key = value (KEYVALUE file format)<p>
         * <&lt;key>value&lt;/key> (XML file format)
         */
        private void parseMetaDataEntry() {

            try {
                switch (keyValue.getKeyword()) {
                    case TIME_SYSTEM:
                        // Read the time system and ensure that it is supported by Orekit
                        final CcsdsTimeScale timeSystem = CcsdsTimeScale.parse(keyValue.getValue());
                        currentMetadata.setTimeSystem(timeSystem);

                        // Convert start/stop time to AbsoluteDate if they have been read already
                        if (currentMetadata.getStartTimeString() != null) {
                            currentMetadata.setStartTime(parseDate(currentMetadata.getStartTimeString(), timeSystem,
                                                                   lineNumber, fileName, line));
                        }
                        if (currentMetadata.getStopTimeString() != null) {
                            currentMetadata.setStopTime(parseDate(currentMetadata.getStopTimeString(), timeSystem,
                                                                  lineNumber, fileName, line));
                        }
                        break;

                    case START_TIME:
                        // Set the start time as a String first
                        currentMetadata.setStartTimeString(keyValue.getValue());

                        // If time system has already been defined, convert the start time to an AbsoluteDate
                        if (currentMetadata.getTimeSystem() != null) {
                            currentMetadata.setStartTime(parseDate(keyValue.getValue(), currentMetadata.getTimeSystem(),
                                                                   lineNumber, fileName, line));
                        }
                        break;

                    case STOP_TIME:
                        // Set the stop time as a String first
                        currentMetadata.setStopTimeString(keyValue.getValue());

                        // If time system has already been defined, convert the start time to an AbsoluteDate
                        if (currentMetadata.getTimeSystem() != null) {
                            currentMetadata.setStopTime(parseDate(keyValue.getValue(), currentMetadata.getTimeSystem(),
                                                                  lineNumber, fileName, line));
                        }
                        break;

                    case PARTICIPANT_1: case PARTICIPANT_2: case PARTICIPANT_3:
                    case PARTICIPANT_4: case PARTICIPANT_5:
                        // Get the participant number
                        String key = keyValue.getKey();
                        int participantNumber = Integer.parseInt(key.substring(key.length() - 1));

                        // Add the tuple to the map
                        currentMetadata.addParticipant(participantNumber, keyValue.getValue());
                        break;

                    case MODE:
                        currentMetadata.setMode(keyValue.getValue());
                        break;

                    case PATH:
                        currentMetadata.setPath(keyValue.getValue());
                        break;

                    case PATH_1:
                        currentMetadata.setPath1(keyValue.getValue());
                        break;

                    case PATH_2:
                        currentMetadata.setPath2(keyValue.getValue());
                        break;

                    case TRANSMIT_BAND:
                        currentMetadata.setTransmitBand(keyValue.getValue());
                        break;

                    case RECEIVE_BAND:
                        currentMetadata.setReceiveBand(keyValue.getValue());
                        break;

                    case TURNAROUND_NUMERATOR:
                        currentMetadata.setTurnaroundNumerator(keyValue.getIntegerValue());
                        break;

                    case TURNAROUND_DENOMINATOR:
                        currentMetadata.setTurnaroundDenominator(keyValue.getIntegerValue());
                        break;

                    case TIMETAG_REF:
                        currentMetadata.setTimetagRef(keyValue.getValue());
                        break;

                    case INTEGRATION_INTERVAL:
                        currentMetadata.setIntegrationInterval(keyValue.getDoubleValue());
                        break;

                    case INTEGRATION_REF:
                        currentMetadata.setIntegrationRef(keyValue.getValue());
                        break;

                    case FREQ_OFFSET:
                        currentMetadata.setFreqOffset(keyValue.getDoubleValue());
                        break;

                    case RANGE_MODE:
                        currentMetadata.setRangeMode(keyValue.getValue());
                        break;

                    case RANGE_MODULUS:
                        currentMetadata.setRangeModulus(keyValue.getDoubleValue());
                        break;

                    case RANGE_UNITS:
                        currentMetadata.setRangeUnits(keyValue.getValue());
                        break;

                    case ANGLE_TYPE:
                        currentMetadata.setAngleType(keyValue.getValue());
                        break;

                    case REFERENCE_FRAME:
                        currentMetadata.setReferenceFrameString(keyValue.getValue());
                        currentMetadata.setReferenceFrame(parseCCSDSFrame(keyValue.getValue()).
                                        getFrame(getConventions(), isSimpleEOP(), getDataContext()));
                        break;

                    case TRANSMIT_DELAY_1: case TRANSMIT_DELAY_2: case TRANSMIT_DELAY_3:
                    case TRANSMIT_DELAY_4: case TRANSMIT_DELAY_5:
                        // Get the participant number
                        key = keyValue.getKey();
                        participantNumber = Integer.parseInt(key.substring(key.length() - 1));

                        // Add the tuple to the map
                        currentMetadata.addTransmitDelay(participantNumber, keyValue.getDoubleValue());
                        break;

                    case RECEIVE_DELAY_1: case RECEIVE_DELAY_2: case RECEIVE_DELAY_3:
                    case RECEIVE_DELAY_4: case RECEIVE_DELAY_5:
                        // Get the participant number
                        key = keyValue.getKey();
                        participantNumber = Integer.parseInt(key.substring(key.length() - 1));

                        // Add the tuple to the map
                        currentMetadata.addReceiveDelay(participantNumber, keyValue.getDoubleValue());
                        break;

                    case DATA_QUALITY:
                        currentMetadata.setDataQuality(keyValue.getValue());
                        break;

                    case CORRECTION_ANGLE_1:
                        currentMetadata.setCorrectionAngle1(keyValue.getDoubleValue());
                        break;

                    case CORRECTION_ANGLE_2:
                        currentMetadata.setCorrectionAngle2(keyValue.getDoubleValue());
                        break;

                    case CORRECTION_DOPPLER:
                        currentMetadata.setCorrectionDoppler(keyValue.getDoubleValue());
                        break;

                    case CORRECTION_RANGE:
                        currentMetadata.setCorrectionRange(keyValue.getDoubleValue());
                        break;

                    case CORRECTION_RECEIVE:
                        currentMetadata.setCorrectionReceive(keyValue.getDoubleValue());
                        break;

                    case CORRECTION_TRANSMIT:
                        currentMetadata.setCorrectionTransmit(keyValue.getDoubleValue());
                        break;

                    case CORRECTIONS_APPLIED:
                        currentMetadata.setCorrectionsApplied(keyValue.getValue());
                        break;

                    default:
                        throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, lineNumber, fileName, line);
                }
            } catch (NumberFormatException nfe) {
                throw new OrekitException(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE,
                                          lineNumber, fileName, line);
            }
        }

    }

    /** Handler for parsing KEYVALUE file formats. */
    private class KeyValueHandler {

        /** ParseInfo object. */
        private ParseInfo parseInfo;

        /** Simple constructor.
         * @param parseInfo ParseInfo object
         */
        KeyValueHandler(final ParseInfo parseInfo) {
            this.parseInfo       = parseInfo;
        }

        /**
         * Parse an observation data line and add its content to the Observations Block
         * block.
         *
         */
        private void parseObservationsDataLine() {

            // Parse an observation line
            // An observation line should consist in the string "keyword = epoch value"
            // parseInfo.keyValue.getValue() should return the string "epoch value"
            final String[] fields = SEPARATOR.split(parseInfo.keyValue.getValue());

            // Check that there are 2 fields in the value of the key
            if (fields.length != 2) {
                throw new OrekitException(OrekitMessages.CCSDS_TDM_INCONSISTENT_DATA_LINE,
                                          parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
            }

            // Convert the date to an AbsoluteDate object (OrekitException if it fails)
            final AbsoluteDate epoch = parseDate(fields[0], parseInfo.currentMetadata.getTimeSystem(),
                                                 parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
            final double measurement;
            try {
                // Convert the value to double (NumberFormatException if it fails)
                measurement = Double.parseDouble(fields[1]);
            } catch (NumberFormatException nfe) {
                throw new OrekitException(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE,
                                          parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
            }

            // Adds the observation to current observation block
            parseInfo.currentObservationsBlock.addObservation(parseInfo.keyValue.getKeyword().name(),
                                                              epoch,
                                                              measurement);
        }

        /** Parse a CCSDS Tracking Data Message with KEYVALUE format.
         * @param stream stream containing message
         * @param fileName name of the file containing the message (for error messages)
         * @return parsed file content in a TDMFile object
         */
        public TDMFile parse(final InputStream stream, final String fileName) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                try {
                    // Initialize internal TDMFile
                    final TDMFile tdmFile = parseInfo.tdmFile;

                    // Read the file
                    parseInfo.parsingHeader = true;
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        ++parseInfo.lineNumber;
                        if (line.trim().length() == 0) {
                            continue;
                        }
                        parseInfo.line = line;
                        parseInfo.keyValue = new KeyValue(parseInfo.line, parseInfo.lineNumber, parseInfo.fileName);
                        if (parseInfo.keyValue.getKeyword() == null) {
                            throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
                        }
                        switch (parseInfo.keyValue.getKeyword()) {

                            // Header entries
                            case CCSDS_TDM_VERS:
                                // Set CCSDS TDM version
                                tdmFile.getHeader().setFormatVersion(parseInfo.keyValue.getDoubleValue());
                                break;

                                // Comments
                            case COMMENT:
                                if (parseInfo.parsingHeader) {
                                    tdmFile.getHeader().addComment(parseInfo.keyValue.getValue());
                                } else if (parseInfo.parsingMetaData) {
                                    parseInfo.currentMetadata.addComment(parseInfo.keyValue.getValue());
                                } else if (parseInfo.parsingData) {
                                    parseInfo.currentObservationsBlock.addComment(parseInfo.keyValue.getValue());
                                } else {
                                    throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD,
                                                              parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
                                }
                                break;

                            case CREATION_DATE:
                                // Set creation date
                                tdmFile.getHeader().setCreationDate(new AbsoluteDate(parseInfo.keyValue.getValue(),
                                                                                     getDataContext().getTimeScales().getUTC()));
                                break;

                            case ORIGINATOR:
                                // Set originator
                                tdmFile.getHeader().setOriginator(parseInfo.keyValue.getValue());
                                break;

                                // Start/Strop keywords
                            case META_START:
                                // Indicate the start of meta-data parsing for this block
                                parseInfo.currentMetadata = new TDMMetadata(getConventions(), getDataContext());
                                parseInfo.parsingHeader   = false;
                                parseInfo.parsingMetaData = true;
                                break;

                            case META_STOP:
                                // Indicate the end of meta-data parsing for this block
                                parseInfo.parsingMetaData = false;
                                break;

                            case DATA_START:
                                // Indicate the start of data parsing for this block
                                parseInfo.currentObservationsBlock = new ObservationsBlock();
                                parseInfo.parsingData = true;
                                break;

                            case DATA_STOP:
                                tdmFile.addSegment(new NDMSegment<>(parseInfo.currentMetadata, parseInfo.currentObservationsBlock));
                                // Indicate the end of data parsing for this block
                                parseInfo.parsingData = false;
                                break;

                            default:
                                // Parse a line that does not display the previous keywords
                                if (parseInfo.parsingMetaData) {
                                    // Parse a meta-data line
                                    parseInfo.parseMetaDataEntry();
                                } else if (parseInfo.parsingData) {
                                    // Parse an observation data line
                                    this.parseObservationsDataLine();
                                } else {
                                    throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD,
                                                              parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
                                }
                                break;
                        }
                    }
                    // Check time systems consistency before returning the parsed content
                    tdmFile.checkTimeSystems();
                    return tdmFile;
                } catch (IOException ioe) {
                    throw new OrekitException(ioe, new DummyLocalizable(ioe.getMessage()));
                }
            } catch (IOException ioe) {
                throw new OrekitException(ioe, new DummyLocalizable(ioe.getMessage()));
            }
        }
    }

    /** Handler for parsing XML file formats. */
    private class XMLHandler extends DefaultHandler {

        /** ParseInfo object. */
        private ParseInfo parseInfo;

        /** Locator used to get current line number. */
        private Locator locator;

        /** Current keyword being read. */
        private Keyword currentKeyword;

        /** Current observation keyword being read. */
        private Keyword currentObservationKeyword;

        /** Current observation epoch being read. */
        private AbsoluteDate currentObservationEpoch;

        /** Current observation measurement being read. */
        private double currentObservationMeasurement;

        /** Simple constructor.
         * @param parseInfo ParseInfo object
         */
        XMLHandler(final ParseInfo parseInfo) {
            this.parseInfo      = parseInfo;
            this.locator        = null;
            this.currentKeyword = null;
            this.currentObservationKeyword      = null;
            this.currentObservationEpoch        = null;
            this.currentObservationMeasurement  = Double.NaN;
        }

        @Override
        public void setDocumentLocator(final Locator documentLocator) {
            this.locator = documentLocator;
        }

        /**
         * Extract the content of an element.
         *
         * @param ch the characters
         * @param start the index of the first character of the desired content
         * @param length the length of the content
         * @throws SAXException in case of an error.
         *
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            try {
                // currentKeyword is set to null in function endElement every time an end tag is parsed.
                // Thus only the characters between a start and an end tags are parsed.
                if (currentKeyword != null) {
                    // Store the info in a KeyValue object so that we can use the common functions of parseInfo
                    // The SAX locator does not allow the retrieving of the line
                    // So a pseudo-line showing the keyword is reconstructed
                    final String value = new String(ch, start, length);
                    parseInfo.line = "<" + currentKeyword.name() + ">" + value + "<" + "/" + currentKeyword.name() + ">";
                    parseInfo.lineNumber = locator.getLineNumber();
                    parseInfo.keyValue = new KeyValue(currentKeyword, value, parseInfo.line, parseInfo.lineNumber, parseInfo.fileName);

                    // Scan the keyword
                    switch (currentKeyword) {

                        case CREATION_DATE:
                            // Set creation date
                            parseInfo.tdmFile.getHeader().setCreationDate(new AbsoluteDate(parseInfo.keyValue.getValue(),
                                                                                           getDataContext().getTimeScales().getUTC()));
                            break;

                        case ORIGINATOR:
                            // Set originator
                            parseInfo.tdmFile.getHeader().setOriginator(parseInfo.keyValue.getValue());
                            break;

                        case COMMENT:
                            // Comments
                            if (parseInfo.parsingHeader) {
                                parseInfo.tdmFile.getHeader().addComment(parseInfo.keyValue.getValue());
                            } else if (parseInfo.parsingMetaData) {
                                parseInfo.currentMetadata.addComment(parseInfo.keyValue.getValue());
                            } else if (parseInfo.parsingData) {
                                parseInfo.currentObservationsBlock.addComment(parseInfo.keyValue.getValue());
                            }
                            break;

                        case tdm: case header: case body: case segment:
                        case metadata: case data:case observation:
                            // Do nothing for this tags
                            break;

                        default:
                            // Parse a line that does not display the previous keywords
                            if (parseInfo.parsingMetaData) {
                                // Call meta-data parsing
                                parseInfo.parseMetaDataEntry();
                            } else if (parseInfo.parsingData) {
                                // Call data parsing
                                parseObservationDataLine();
                            } else {
                                throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD,
                                                          parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
                            }
                            break;
                    }
                }
            } catch (OrekitException e) {
                // Re-throw the exception as a SAXException
                throw new SAXException(e);
            }
        }

        /**
         * Detect the beginning of an element.
         *
         * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
         * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
         * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
         * @param attributes The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
         * @throws SAXException in case of an error
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            // Check if the start element belongs to the standard keywords
            try {
                try {
                    this.currentKeyword = Keyword.valueOf(qName);
                } catch (IllegalArgumentException e) {
                    throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD,
                                              locator.getLineNumber(),
                                              parseInfo.fileName,
                                              "<" + qName + ">");
                }
                switch (currentKeyword) {
                    case tdm:
                        // Get the version number
                        parseInfo.tdmFile.getHeader().setFormatVersion(Double.parseDouble(attributes.getValue("version")));
                        break;

                    case observation:
                        // Re-initialize the stored observation's attributes
                        this.currentObservationKeyword     = null;
                        this.currentObservationEpoch       = null;
                        this.currentObservationMeasurement = Double.NaN;
                        break;

                    case segment:
                        // nothing to do here
                        break;

                    case metadata:
                        // Indicate the start of meta-data parsing for this block
                        parseInfo.currentMetadata = new TDMMetadata(getConventions(), getDataContext());
                        parseInfo.parsingMetaData = true;
                        break;

                    case data:
                        // Indicate the start of data parsing for this block
                        parseInfo.currentObservationsBlock = new ObservationsBlock();
                        parseInfo.parsingData = true;
                        break;

                    default:
                        // Ignore the element.
                        break;
                }
            } catch (IllegalArgumentException | OrekitException e) {
                throw new SAXException(e);
            }
        }

        /**
         * Detect the end of an element and remove the stored keyword.
         *
         * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
         * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
         * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
         * @throws SAXException in case of an error
         *
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            // check if the start element belongs to the standard keywords
            try {
                // Set the stored keyword to null
                currentKeyword = null;
                // Ending keyword
                final Keyword endKeyword;
                try {
                    endKeyword = Keyword.valueOf(qName);
                } catch (IllegalArgumentException e) {
                    throw new OrekitException(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD,
                                              locator.getLineNumber(),
                                              parseInfo.fileName,
                                              "</" + qName + ">");
                }
                switch (endKeyword) {

                    case header:
                        parseInfo.parsingHeader = false;
                        break;

                    case observation:
                        // Check that stored observation's attributes were all found
                        if (currentObservationKeyword == null         ||
                            currentObservationEpoch == null           ||
                            Double.isNaN(currentObservationMeasurement)) {
                            throw new OrekitException(OrekitMessages.CCSDS_TDM_XML_INCONSISTENT_DATA_BLOCK,
                                                      locator.getLineNumber(),
                                                      parseInfo.fileName);
                        } else {
                            // Add current observation
                            parseInfo.currentObservationsBlock.addObservation(currentObservationKeyword.name(),
                                                                              currentObservationEpoch,
                                                                              currentObservationMeasurement);
                        }
                        break;

                    case segment:
                        parseInfo.tdmFile.addSegment(new NDMSegment<>(parseInfo.currentMetadata, parseInfo.currentObservationsBlock));
                        break;

                    case metadata:
                        parseInfo.parsingMetaData = false;
                        break;

                    case data:
                        parseInfo.parsingData = false;
                        break;

                    default:
                        // Ignore the element.
                }
            } catch (IllegalArgumentException | OrekitException e) {
                throw new SAXException(e);
            }
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) {
            // disable external entities
            return new InputSource();
        }

        /** Parse a line in an observation data block.
         */
        private void parseObservationDataLine() {

            // Parse an observation line
            // An XML observation line should consist in the string "<KEYWORD>value</KEYWORD>
            // Each observation block should display:
            //  - One line with the keyword EPOCH;
            //  - One line with a specific data keyword
            switch(currentKeyword) {
                case EPOCH:
                    // Convert the date to an AbsoluteDate object (OrekitException if it fails)
                    currentObservationEpoch = parseDate(parseInfo.keyValue.getValue(),
                                                        parseInfo.currentMetadata.getTimeSystem(),
                                                        parseInfo.lineNumber,
                                                        parseInfo.fileName,
                                                        parseInfo.line);
                    break;
                default:
                    try {
                        // Update current observation keyword
                        currentObservationKeyword = currentKeyword;
                        // Convert the value to double (NumberFormatException if it fails)
                        currentObservationMeasurement = Double.parseDouble(parseInfo.keyValue.getValue());
                    } catch (NumberFormatException nfe) {
                        throw new OrekitException(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE,
                                                  parseInfo.lineNumber, parseInfo.fileName, parseInfo.line);
                    }
                    break;
            }
        }
    }
}