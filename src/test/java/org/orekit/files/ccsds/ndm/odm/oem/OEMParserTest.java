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
package org.orekit.files.ccsds.ndm.odm.oem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.Array2DRowRealMatrix;
import org.hipparchus.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orekit.OrekitMatchers;
import org.orekit.Utils;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.ccsds.utils.CcsdsTimeScale;
import org.orekit.frames.FactoryManagedFrame;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.ITRFVersion;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.CartesianDerivativesFilter;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;


public class OEMParserTest {

    @Before
    public void setUp()
        throws Exception {
        Utils.setDataRoot("regular-data");
    }

    @Test
    public void testParseOEM1() throws IOException {
        //
        final String ex = "/ccsds/odm/oem/OEMExample1.txt";
        final InputStream inEntry = getClass().getResourceAsStream(ex);
        final OEMParser parser = new OEMParser().withMu(CelestialBodyFactory.getMars().getGM());
        final OEMFile file = parser.parse(inEntry, "OEMExample1.txt");
        Assert.assertEquals(CcsdsTimeScale.UTC, file.getSegments().get(0).getMetadata().getTimeSystem());
        Assert.assertEquals("MARS GLOBAL SURVEYOR", file.getSegments().get(0).getMetadata().getObjectName());
        Assert.assertEquals("1996-062A", file.getSegments().get(0).getMetadata().getObjectID());
        Assert.assertEquals("MARS BARYCENTER", file.getSegments().get(0).getMetadata().getCenterName());
        Assert.assertEquals(1996, file.getSegments().get(0).getMetadata().getLaunchYear());
        Assert.assertEquals(62, file.getSegments().get(0).getMetadata().getLaunchNumber());
        Assert.assertEquals("A", file.getSegments().get(0).getMetadata().getLaunchPiece());
        Assert.assertFalse(file.getSegments().get(0).getMetadata().getHasCreatableBody());
        Assert.assertNull(file.getSegments().get(0).getMetadata().getCenterBody());
        Assert.assertEquals(new AbsoluteDate(1996, 12, 18, 12, 00, 0.331, TimeScalesFactory.getUTC()),
                            file.getSegments().get(0).getMetadata().getStartTime());
        Assert.assertEquals(new AbsoluteDate(1996, 12, 28, 21, 28, 0.331, TimeScalesFactory.getUTC()),
                            file.getSegments().get(0).getMetadata().getStopTime());
        Assert.assertEquals(new AbsoluteDate(1996, 12, 18, 12, 10, 0.331, TimeScalesFactory.getUTC()),
                            file.getSegments().get(0).getMetadata().getUseableStartTime());
        Assert.assertEquals(new AbsoluteDate(1996, 12, 28, 21, 23, 0.331, TimeScalesFactory.getUTC()),
                            file.getSegments().get(0).getMetadata().getUseableStopTime());
        Assert.assertEquals("HERMITE", file.getSegments().get(0).getMetadata().getInterpolationMethod());
        Assert.assertEquals(7, file.getSegments().get(0).getMetadata().getInterpolationDegree());
        ArrayList<String> ephemeridesDataLinesComment = new ArrayList<String>();
        ephemeridesDataLinesComment.add("This file was produced by M.R. Somebody, MSOO NAV/JPL, 1996NOV 04. It is");
        ephemeridesDataLinesComment.add("to be used for DSN scheduling purposes only.");
        Assert.assertEquals(ephemeridesDataLinesComment, file.getSegments().get(0).getData().getEphemeridesDataLinesComment());
        CartesianOrbit orbit = new CartesianOrbit(new PVCoordinates
                                                  (new Vector3D(2789.619 * 1000, -280.045 * 1000, -1746.755 * 1000),
                                                   new Vector3D(4.73372 * 1000, -2.49586 * 1000, -1.04195 * 1000)),
                                                   FramesFactory.getEME2000(),
                                                   new AbsoluteDate("1996-12-18T12:00:00.331", TimeScalesFactory.getUTC()),
                                                   CelestialBodyFactory.getEarth().getGM());
        Assert.assertArrayEquals(orbit.getPVCoordinates().getPosition().toArray(), file.getSegments().get(0).getData().getEphemeridesDataLines().get(0).getPosition().toArray(), 1e-10);
        Assert.assertArrayEquals(orbit.getPVCoordinates().getVelocity().toArray(), file.getSegments().get(0).getData().getEphemeridesDataLines().get(0).getVelocity().toArray(), 1e-10);
        Assert.assertArrayEquals((new Vector3D(1, 1, 1)).toArray(), file.getSegments().get(1).getData().getEphemeridesDataLines().get(0).getAcceleration().toArray(), 1e-10);
        Assert.assertEquals(Vector3D.ZERO, file.getSegments().get(1).getData().getEphemeridesDataLines().get(1).getAcceleration());
        final Array2DRowRealMatrix covMatrix = new Array2DRowRealMatrix(6, 6);
        final double[] column1 = {
            3.331349476038534e-04, 4.618927349220216e-04,
            -3.070007847730449e-04, -3.349365033922630e-07,
            -2.211832501084875e-07, -3.041346050686871e-07
        };
        final double[] column2 = {
            4.618927349220216e-04, 6.782421679971363e-04,
            -4.221234189514228e-04, -4.686084221046758e-07,
            -2.864186892102733e-07, -4.989496988610662e-07
        };
        final double[] column3 = {
            -3.070007847730449e-04, -4.221234189514228e-04,
            3.231931992380369e-04, 2.484949578400095e-07,
            1.798098699846038e-07, 3.540310904497689e-07
        };
        final double[] column4 = {
            -3.349365033922630e-07, -4.686084221046758e-07,
            2.484949578400095e-07, 4.296022805587290e-10,
            2.608899201686016e-10, 1.869263192954590e-10
        };
        final double[] column5 = {
            -2.211832501084875e-07, -2.864186892102733e-07,
            1.798098699846038e-07, 2.608899201686016e-10,
            1.767514756338532e-10, 1.008862586240695e-10
        };
        final double[] column6 = {
            -3.041346050686871e-07, -4.989496988610662e-07,
            3.540310904497689e-07, 1.869263192954590e-10,
            1.008862586240695e-10, 6.224444338635500e-10
        };
        covMatrix.setColumn(0, column1);
        covMatrix.setColumn(1, column2);
        covMatrix.setColumn(2, column3);
        covMatrix.setColumn(3, column4);
        covMatrix.setColumn(4, column5);
        covMatrix.setColumn(5, column6);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Assert.assertEquals(covMatrix.getEntry(i, j),
                                    file.getSegments().get(2).getData().getCovarianceMatrices().get(0).getMatrix().getEntry(i, j),
                                    1e-10);
            }
        }
        Assert.assertEquals(new AbsoluteDate("1996-12-28T21:29:07.267", TimeScalesFactory.getUTC()),
                            file.getSegments().get(2).getCovarianceMatrices().get(0).getEpoch());
        Assert.assertEquals(LOFType.QSW,
                            file.getSegments().get(2).getCovarianceMatrices().get(0).getLofType());
        Assert.assertNull(file.getSegments().get(2).getCovarianceMatrices().get(0).getFrame());
        Assert.assertNull(file.getSegments().get(2).getCovarianceMatrices().get(1).getLofType());
        Assert.assertEquals(FramesFactory.getEME2000(),
                            file.getSegments().get(2).getCovarianceMatrices().get(1).getFrame());
    }

    @Test
    public void testParseOEM1OrbitFile() throws IOException {

        final String ex = "/ccsds/odm/oem/OEMExample3.txt";
        final InputStream inEntry = getClass().getResourceAsStream(ex);
        final OEMParser parser = new OEMParser().
                                 withMu(CelestialBodyFactory.getMars().getGM()).
                                 withConventions(IERSConventions.IERS_2010);
        final OEMFile file = parser.parse(inEntry);
        Assert.assertEquals(CcsdsTimeScale.UTC, file.getSegments().get(0).getMetadata().getTimeSystem());
        Assert.assertEquals("MARS GLOBAL SURVEYOR", file.getSegments().get(0).getMetadata().getObjectName());
        Assert.assertEquals("1996-062A", file.getSegments().get(0).getMetadata().getObjectID());

        Assert.assertEquals(1, file.getSatellites().size());
        Assert.assertEquals(true, file.getSatellites().containsKey("1996-062A"));
        Assert.assertEquals(false, file.getSatellites().containsKey("MARS GLOBAL SURVEYOR"));
        Assert.assertEquals(1, file.getSatellites().size());
        Assert.assertEquals("1996-062A", file.getSatellites().values().iterator().next().getId());
        Assert.assertEquals(
                new AbsoluteDate("1996-12-18T12:00:00.331", TimeScalesFactory.getUTC()),
                file.getSegments().get(0).getMetadata().getStartTime());

        final OEMSatelliteEphemeris satellite = file.getSatellites().get("1996-062A");
        Assert.assertEquals(satellite.getId(), "1996-062A");
        final OEMSegment segment = satellite.getSegments().get(0);
        Assert.assertEquals(CelestialBodyFactory.getMars().getGM(), segment.getMu(), 1.0);
        Assert.assertEquals(segment.getFrameString(), "EME2000");
        Assert.assertEquals(segment.getFrameCenterString(), "MARS BARYCENTER");
        Assert.assertEquals(segment.getMetadata().getHasCreatableBody(), false);
        // Frame not creatable since it's center can't be created.
        try {
            segment.getFrame();
            Assert.fail("Expected Exception");
        } catch (OrekitException e){
            Assert.assertEquals(e.getSpecifier(),
                    OrekitMessages.NO_DATA_LOADED_FOR_CELESTIAL_BODY);
        }
        Assert.assertEquals(segment.getTimeScaleString(), "UTC");
        Assert.assertEquals(segment.getTimeScale(), TimeScalesFactory.getUTC());
        Assert.assertEquals(segment.getInterpolationSamples(), 3);
        Assert.assertEquals(segment.getAvailableDerivatives(),
                CartesianDerivativesFilter.USE_PV);
        // propagator can't be created since frame can't be created
        try {
            satellite.getPropagator();
            Assert.fail("Expected Exception");
        } catch (OrekitException e){
            Assert.assertEquals(e.getSpecifier(),
                    OrekitMessages.NO_DATA_LOADED_FOR_CELESTIAL_BODY);
        }
    }

    @Test
    public void testParseOemMissingOptionalData() throws IOException {

        final String ex = "/ccsds/odm/oem/OEMExample6.txt";
        final InputStream inEntry = getClass().getResourceAsStream(ex);
        final OEMParser parser = new OEMParser().
                                 withMu(CelestialBodyFactory.getMars().getGM()).
                                 withConventions(IERSConventions.IERS_2010);
        final OEMFile file = parser.parse(inEntry);
        Assert.assertEquals(CcsdsTimeScale.UTC, file.getSegments().get(0).getMetadata().getTimeSystem());
        Assert.assertEquals("MARS GLOBAL SURVEYOR", file.getSegments().get(0).getMetadata().getObjectName());
        Assert.assertEquals("1996-062A", file.getSegments().get(0).getMetadata().getObjectID());

        Assert.assertEquals(1, file.getSatellites().size());
        Assert.assertEquals(true, file.getSatellites().containsKey("1996-062A"));
        Assert.assertEquals(false, file.getSatellites().containsKey("MARS GLOBAL SURVEYOR"));
        Assert.assertEquals(1, file.getSatellites().size());
        Assert.assertEquals("1996-062A", file.getSatellites().values().iterator().next().getId());
        Assert.assertEquals(
                new AbsoluteDate("2002-12-18T12:00:00.331", TimeScalesFactory.getUTC()),
                file.getSegments().get(0).getMetadata().getStartTime());

        OEMSatelliteEphemeris satellite = file.getSatellites().get("1996-062A");
        Assert.assertEquals(satellite.getId(), "1996-062A");
        OEMSegment segment = satellite.getSegments().get(0);
        Assert.assertEquals(CelestialBodyFactory.getMars().getGM(), segment.getMu(), 1.0);
        FactoryManagedFrame eme2000 = FramesFactory.getEME2000();
        Frame actualFrame = segment.getFrame();
        AbsoluteDate actualStart = satellite.getStart();
        Transform actualTransform = eme2000.getTransformTo(actualFrame, actualStart);
        CelestialBody mars = CelestialBodyFactory.getMars();
        TimeStampedPVCoordinates marsPV = mars.getPVCoordinates(actualStart, eme2000);
        TimeStampedPVCoordinates marsPV_in_marscentered_frame = mars.getPVCoordinates(actualStart, actualFrame);
        MatcherAssert.assertThat(marsPV_in_marscentered_frame,
                                 OrekitMatchers.pvCloseTo(PVCoordinates.ZERO, 1e-3));
        Assert.assertEquals(actualTransform.getTranslation(), marsPV.getPosition().negate());
        Assert.assertEquals(actualTransform.getVelocity(), marsPV.getVelocity().negate());
        Assert.assertEquals(actualTransform.getAcceleration(), marsPV.getAcceleration().negate());
        Assert.assertEquals(
                Rotation.distance(actualTransform.getRotation(), Rotation.IDENTITY),
                0.0, 0.0);
        Assert.assertEquals(actualTransform.getRotationRate(), Vector3D.ZERO);
        Assert.assertEquals(actualTransform.getRotationAcceleration(), Vector3D.ZERO);
        Assert.assertEquals(actualFrame.getName(), "Mars/EME2000");
        Assert.assertEquals(segment.getFrameString(), "EME2000");
        Assert.assertEquals(segment.getTimeScaleString(), "UTC");
        Assert.assertEquals(segment.getTimeScale(), TimeScalesFactory.getUTC());
        Assert.assertEquals(segment.getAvailableDerivatives(),
                CartesianDerivativesFilter.USE_PV);
        Assert.assertEquals(satellite.getSegments().get(0).getMetadata().getStartTime(), actualStart);
        Assert.assertEquals(satellite.getSegments().get(2).getMetadata().getStopTime(), satellite.getStop());

        final BoundedPropagator propagator = satellite.getPropagator();
        Assert.assertEquals(propagator.getMinDate(), satellite.getStart());
        Assert.assertEquals(propagator.getMinDate(), satellite.getSegments().get(0).getStart());
        Assert.assertEquals(propagator.getMaxDate(), satellite.getStop());
        Assert.assertEquals(propagator.getMaxDate(), satellite.getSegments().get(2).getStop());

        final List<TimeStampedPVCoordinates> dataLines = new ArrayList<>();
        for (OEMSegment block : file.getSegments()) {
            for (TimeStampedPVCoordinates dataLine : block.getData().getEphemeridesDataLines()) {
                if (dataLine.getDate().compareTo(satellite.getStart()) >= 0) {
                    dataLines.add(dataLine);
                }
            }
        }

        final int ulps = 12;
        for (TimeStampedPVCoordinates coord : dataLines) {
            MatcherAssert.assertThat(propagator.getPVCoordinates(coord.getDate(), actualFrame),
                                     OrekitMatchers.pvCloseTo(coord, ulps));
            MatcherAssert.assertThat(propagator.propagate(coord.getDate()).getPVCoordinates(),
                                     OrekitMatchers.pvCloseTo(coord, ulps));
        }

    }

    @Test
    public void testParseOEM2() throws URISyntaxException {

        final String name = getClass().getResource("/ccsds/odm/oem/OEMExample2.txt").toURI().getPath();
        final AbsoluteDate missionReferenceDate = new AbsoluteDate("1996-12-17T00:00:00.000", TimeScalesFactory.getUTC());
        OEMParser parser = new OEMParser().
                           withConventions(IERSConventions.IERS_2010).
                           withSimpleEOP(true).
                           withMu(CelestialBodyFactory.getMars().getGM()).
                           withMissionReferenceDate(missionReferenceDate);

        final OEMFile file = parser.parse(name);
        final List<String> headerComment = new ArrayList<String>();
        headerComment.add("comment");
        Assert.assertEquals(headerComment, file.getHeader().getComments());
        final List<String> metadataComment = new ArrayList<String>();
        metadataComment.add("comment 1");
        metadataComment.add("comment 2");
        Assert.assertEquals(metadataComment, file.getSegments().get(0).getMetadata().getComments());
        Assert.assertEquals("TOD", file.getSegments().get(0).getFrameString());
        Assert.assertEquals("EME2000", file.getSegments().get(1).getFrameString());
        List<OEMSegment> blocks = file.getSegments();
        Assert.assertEquals(2, blocks.size());
        Assert.assertEquals(129600.331,
                            blocks.get(0).getMetadata().getFrameEpoch().durationFrom(missionReferenceDate),
                            1.0e-15);
        Assert.assertEquals(129600.331,
                            blocks.get(0).getMetadata().getStartTime().durationFrom(missionReferenceDate),
                            1.0e-15);
        Assert.assertEquals(941347.267,
                            blocks.get(1).getMetadata().getStartTime().durationFrom(missionReferenceDate),
                            1.0e-15);

    }

    @Test
    public void testWrongODMType() {
        try {
            new OEMParser().parse(getClass().getResourceAsStream("/ccsds/odm/opm/OPMExample1.txt"), "OPMExample1.txt");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, oe.getSpecifier());
            Assert.assertEquals(1, oe.getParts()[0]);
            Assert.assertEquals("OPMExample1.txt", oe.getParts()[1]);
            Assert.assertEquals("CCSDS_OPM_VERS = 3.0", oe.getParts()[2]);
        }
    }

    @Test
    public void testEphemerisNumberFormatErrorType() {
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).
            parse(getClass().getResourceAsStream("/ccsds/odm/oem/OEM-ephemeris-number-format-error.txt"),
                                                 "OEM-ephemeris-number-format-error.txt");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(44, oe.getParts()[0]);
            Assert.assertEquals("OEM-ephemeris-number-format-error.txt", oe.getParts()[1]);
            Assert.assertEquals("1996-12-28T21:59:02.267 -2445.234 -878.141 this-is-not-a-number 1.86043 -3.421256 -0.996366", oe.getParts()[2]);
        }
    }

    @Test
    public void testCovarianceNumberFormatErrorType() {
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).
            parse(getClass().getResourceAsStream("/ccsds/odm/oem/OEM-covariance-number-format-error.txt"),
                                                 "OEM-covariance-number-format-error.txt");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(52, oe.getParts()[0]);
            Assert.assertEquals("OEM-covariance-number-format-error.txt", oe.getParts()[1]);
            Assert.assertEquals("4.6189273e-04 this-is-not-a-number", oe.getParts()[2]);
        }
    }

    @Test
    public void testNonExistentFile() throws URISyntaxException {
        final String realName = getClass().getResource("/ccsds/odm/oem/OEMExample1.txt").toURI().getPath();
        final String wrongName = realName + "xxxxx";
        try {
            new OEMParser().parse(wrongName);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_FIND_FILE, oe.getSpecifier());
            Assert.assertEquals(wrongName, oe.getParts()[0]);
        }
    }

    @Test
    public void testInconsistentTimeSystems() {
        try {
            new OEMParser().
            withMu(CelestialBodyFactory.getMars().getGM()).
            parse(getClass().getResourceAsStream("/ccsds/odm/oem/OEM-inconsistent-time-systems.txt"));
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.CCSDS_INCONSISTENT_TIME_SYSTEMS, oe.getSpecifier());
            Assert.assertEquals(CcsdsTimeScale.UTC, oe.getParts()[0]);
            Assert.assertEquals(CcsdsTimeScale.TCG, oe.getParts()[1]);
        }
    }

    @Test
    public void testLowerCaseValue() {
        //setup
        String file = "/ccsds/odm/oem/oemLowerCaseValue.oem";
        InputStream in = getClass().getResourceAsStream(file);

        //action
        OEMFile actual = new OEMParser().parse(in, file);

        //verify
        Assert.assertEquals(
                CelestialBodyFactory.getEarth(),
                actual.getSegments().get(0).getMetadata().getCenterBody());
    }

    @Test
    public void testWrongKeyword()
        throws URISyntaxException {
        // simple test for OMM file, contains p/v entries and other mandatory
        // data.
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-wrong-keyword.txt").toURI().getPath();
        try {
            new OEMParser().parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, oe.getSpecifier());
            Assert.assertEquals(19, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith("WRONG_KEYWORD"));
        }
    }

    @Test
    public void testKeywordWithinEphemeris()
        throws URISyntaxException {
        // simple test for OMM file, contains p/v entries and other mandatory
        // data.
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-keyword-within-ephemeris.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, oe.getSpecifier());
            Assert.assertEquals(24, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith("USER_DEFINED_TEST_KEY"));
        }
    }

    @Test
    public void testKeywordWithinCovariance()
        throws URISyntaxException {
        // simple test for OMM file, contains p/v entries and other mandatory
        // data.
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-keyword-within-covariance.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.CCSDS_UNEXPECTED_KEYWORD, oe.getSpecifier());
            Assert.assertEquals(91, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith("USER_DEFINED_TEST_KEY"));
        }
    }

    @Test
    public void testTooLargeCovarianceDimension()
        throws URISyntaxException {
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-too-large-covariance-dimension.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(91, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith(" 1.0e-12"));
        }
    }

    @Test
    public void testTooSmallCovarianceDimension()
        throws URISyntaxException {
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-too-small-covariance-dimension.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(89, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith("EPOCH"));
        }
    }

    @Test
    public void testTooManyCovarianceColumns()
        throws URISyntaxException {
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-too-many-covariance-columns.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(51, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith(" 3.3313494e-04"));
        }
    }

    @Test
    public void testTooFewCovarianceColumns()
        throws URISyntaxException {
        final String name = getClass().getResource("/ccsds/odm/oem/OEM-too-few-covariance-columns.txt").toURI().getPath();
        try {
            new OEMParser().withMu(CelestialBodyFactory.getMars().getGM()).parse(name);
            Assert.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assert.assertEquals(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE, oe.getSpecifier());
            Assert.assertEquals(55, ((Integer) oe.getParts()[0]).intValue());
            Assert.assertTrue(((String) oe.getParts()[2]).startsWith("-2.2118325e-07"));
        }
    }

    /**
     * Check if the parser enters the correct interpolation degree
     * (the parsed one or the default if there is none)
     */
    @Test
    public void testDefaultInterpolationDegree()
        throws URISyntaxException {

        final String name = getClass().getResource("/ccsds/odm/oem/OEMExample8.txt").toURI().getPath();
        OEMParser parser = new OEMParser().withMu(CelestialBodyFactory.getMars().getGM());

        final OEMFile file = parser.parse(name);
        Assert.assertEquals(1, file.getSegments().get(0).getMetadata().getInterpolationDegree());
        Assert.assertEquals(7, file.getSegments().get(1).getMetadata().getInterpolationDegree());

        parser = parser.withInterpolationDegree(5);

        final OEMFile file2 = parser.parse(name);
        Assert.assertEquals(5, file2.getSegments().get(0).getMetadata().getInterpolationDegree());
        Assert.assertEquals(7, file2.getSegments().get(1).getMetadata().getInterpolationDegree());
    }

    /**
     * Check the parser can parse several ITRF frames. Test case for #361.
     */
    @Test
    public void testITRFFrames() {
        // setup
        Charset utf8 = StandardCharsets.UTF_8;
        IERSConventions conventions = IERSConventions.IERS_2010;
        boolean simpleEop = true;
        OEMParser parser = new OEMParser().
                           withSimpleEOP(simpleEop).
                           withConventions(conventions);
        // frames to check
        List<Pair<String, Frame>> frames = new ArrayList<>();
        frames.add(new Pair<>("ITRF-93",  FramesFactory.getITRF(ITRFVersion.ITRF_93,   conventions, simpleEop)));
        frames.add(new Pair<>("ITRF-97",  FramesFactory.getITRF(ITRFVersion.ITRF_97,   conventions, simpleEop)));
        frames.add(new Pair<>("ITRF2000", FramesFactory.getITRF(ITRFVersion.ITRF_2000, conventions, simpleEop)));
        frames.add(new Pair<>("ITRF2005", FramesFactory.getITRF(ITRFVersion.ITRF_2005, conventions, simpleEop)));
        frames.add(new Pair<>("ITRF2008", FramesFactory.getITRF(ITRFVersion.ITRF_2008, conventions, simpleEop)));
        frames.add(new Pair<>("ITRF2014", FramesFactory.getITRF(ITRFVersion.ITRF_2014, conventions, simpleEop)));

        for (Pair<String, Frame> frame : frames) {
            final String frameName = frame.getFirst();

            InputStream pre    = OEMParserTest.class.getResourceAsStream("/ccsds/odm/oem/OEMExample7.txt.pre");
            InputStream middle = new ByteArrayInputStream(("REF_FRAME = " + frameName).getBytes(utf8));
            InputStream post   = OEMParserTest.class.getResourceAsStream("/ccsds/odm/oem/OEMExample7.txt.post");
            InputStream input  = new SequenceInputStream(pre, new SequenceInputStream(middle, post));

            // action
            OEMFile actual = parser.parse(input);

            // verify
            OEMSegment segment = actual.getSegments().get(0);
            Assert.assertEquals(segment.getFrameString(), frameName);
            // check expected frame
            Frame actualFrame = segment.getFrame();
            Frame expectedFrame = frame.getSecond();
            Assert.assertEquals(expectedFrame, actualFrame);
            Assert.assertEquals(expectedFrame.getTransformProvider(),
                                actualFrame.getTransformProvider());
        }
    }

}