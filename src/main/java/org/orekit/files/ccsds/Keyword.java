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
package org.orekit.files.ccsds;

import org.orekit.files.ccsds.ndm.odm.ocm.OrbitCategory;

/** Keywords for CCSDS Navigation Data Messages.<p>
 * Only these should be used.<p>
 *  The enumeration is split in 3 parts:<p>
 *  - Common NDM keywords;<p>
 *  - Orbit Data Messages (ODM) specific keywords;<p>
 *  - Tracking Data Messages (TDM) specific keywords.<p>
 * References:<p>
 * - <a href="https://public.ccsds.org/Pubs/502x0b2c1.pdf">CCSDS 502.0-B-2 recommended standard</a> ("Orbit Data Messages", Blue Book, Issue 2.0, November 2009).<p>
 * - <a href="https://public.ccsds.org/Pubs/503x0b1c1.pdf">CCSDS 503.0-B-1 recommended standard</a> ("Tracking Data Message", Blue Book, Issue 1.0, November 2007).
 * @author sports
 * @author Maxime Journot
 * @since 6.1
 */
public enum Keyword {

    // ---------------------------------------------------
    // Common NDM (Navigation Data Message) CCSDS keywords
    // ---------------------------------------------------

    /** Comments specific to ODM and ADM file. */
    COMMENT,
    /** File creation date in UTC. */
    CREATION_DATE,
    /** Creating agency or operator. */
    ORIGINATOR,
    /** Unique ID identifying a message from a given originator.
     * @since 11.0
     */
    MESSAGE_ID,
    /** Classification of this message.
     * @since 11.0
     */
    CLASSIFICATION,
    /** Spacecraft name for which the orbit state is provided. */
    OBJECT_NAME,
    /** Alternate names fir this space object.
     * @since 11.0
     */
    ALTERNATE_NAMES,
    /** Unique satellite identification designator for the object.
     * @since 11.0
     */
    OBJECT_DESIGNATOR,
    /** Object identifier of the object for which the orbit state is provided. */
    OBJECT_ID,
    /** Origin of reference frame. */
    CENTER_NAME,
    /** Time system used for state vector, maneuver, and covariance data. */
    TIME_SYSTEM,
    /** Epoch of state vector and optional Keplerian elements.
     *  Or epoch of a TDM observation.
     */
    EPOCH,
    /** Start of total time span covered by: <p>
     * - Ephemerides data and covariance data;<p>
     * - Tracking data session.
     */
    START_TIME,
    /** End of total time span covered by: <p>
     * - Ephemerides data and covariance data;<p>
     * - Tracking data session.
     */
    STOP_TIME,
    /** User defined parameter, where X is replaced by a variable length user specified character
     *  string. Any number of user defined parameters may be included, if necessary to provide
     *  essential information that cannot be conveyed in COMMENT statements. */
    USER_DEFINED_X,
    /** Keyword used to delineate the start of a Meta-data block. */
    META_START,
    /** Keyword used to delineate the end of a Meta-data block. */
    META_STOP,
    /** Maneuver duration (If = 0, impulsive maneuver). */
    MAN_DURATION,
    /** Coordinate system for velocity increment vector. Its value can either be RSW, RTN (both
     * indicating "Radial, Transverse, Normal") or TNW. */
    MAN_REF_FRAME,
    /** Start of useable time span covered by ephemerides data, it may be
     * necessary to allow for proper interpolation. */
    USEABLE_START_TIME,
    /** End of useable time span covered by ephemerides data, it may be
     * necessary to allow for proper interpolation. */
    USEABLE_STOP_TIME,
    /** The interpolation degree. */
    INTERPOLATION_DEGREE,

    // -------------------------------------------
    // Orbit Data Messages (ODM) specific keywords
    // -------------------------------------------

    /** CCSDS OPM format version. */
    CCSDS_OPM_VERS,
    /** CCSDS OMM format version. */
    CCSDS_OMM_VERS,
    /** CCSDS OEM format version. */
    CCSDS_OEM_VERS,
    /** CCSDS OCM format version.
     * @since 11.0
     */
    CCSDS_OCM_VERS,
    /** Programmatic Point Of Contact at originator.
     * @since 11.0
     */
    ORIGINATOR_POC,
    /** Position of Programmatic Point Of Contact at originator.
     * @since 11.0
     */
    ORIGINATOR_POSITION,
    /** Phone number of Programmatic Point Of Contact at originator.
     * @since 11.0
     */
    ORIGINATOR_PHONE,
    /** Address of Programmatic Point Of Contact at originator.
     * @since 11.0
     */
    ORIGINATOR_ADDRESS,
    /** Creating agency or operator.
     * @since 11.0
     */
    TECH_ORG,
    /** Technical Point Of Contact at originator.
     * @since 11.0
     */
    TECH_POC,
    /** Position of Technical Point Of Contact at originator.
     * @since 11.0
     */
    TECH_POSITION,
    /** Phone number of Technical Point Of Contact at originator.
     * @since 11.0
     */
    TECH_PHONE,
    /** Address of Technical Point Of Contact at originator.
     * @since 11.0
     */
    TECH_ADDRESS,
    /** Unique ID identifying previous message from a given originator.
     * @since 11.0
     */
    PREVIOUS_MESSAGE_ID,
    /** Unique ID identifying next message from a given originator.
     * @since 11.0
     */
    NEXT_MESSAGE_ID,
    /** Names of Attitude Data Messages link to this Orbit Data Message.
     * @since 11.0
     */
    ADM_MESSAGE_LINK,
    /** Names of Conjunction Data Messages link to this Orbit Data Message.
     * @since 11.0
     */
    CDM_MESSAGE_LINK,
    /** Names of Pointing Request Messages link to this Orbit Data Message.
     * @since 11.0
     */
    PRM_MESSAGE_LINK,
    /** Names of Reentry Data Messages link to this Orbit Data Message.
     * @since 11.0
     */
    RDM_MESSAGE_LINK,
    /** International designator for the object as assigned by the UN Committee
     * on Space Research (COSPAR) and the US National Space Science Data Center (NSSDC).
     * @since 11.0
     */
    INTERNATIONAL_DESIGNATOR,
    /** Operator of the space object.
     * @since 11.0
     */
    OPERATOR,
    /** Owner of the space object.
     * @since 11.0
     */
    OWNER,
    /** Name of the country where the space object owner is based.
     * @since 11.0
     */
    COUNTRY,
    /** Name of the constellation this space object belongs to.
     * @since 11.0
     */
    CONSTELLATION,
    /** Type of object.
     * @see CCSDSObjectType
     * @since 11.0
     */
    OBJECT_TYPE,
    /** Operational status.
     * @see CCSDSOpsStatus
     * @since 11.0
     */
    OPS_STATUS,
    /** Orbit category.
     * @see OrbitCategory
     * @since 11.0
     */
    ORBIT_CATEGORY,
    /** Specification of satellite catalog source.
     * @since 11.0
     */
    CATALOG_NAME,
    /** List of elements of information data blocks included in this message.
     * @since 11.0
     */
    OCM_DATA_ELEMENTS,
    /** Default epoch to which <em>all</em> relative times are referenced in data blocks,
     * unless overridden by block-specific {@link #EPOCH_TZERO} values.
     * @since 11.0
     */
    EPOCH_TZERO,
    /** Epoch corresponding to t=0 for the spacecraft clock.
     * @since 11.0
     */
    SCLK_EPOCH,
    /** Number of clock seconds occurring during one SI second.
     * @since 11.0
     */
    SCLK_SEC_PER_SI_SEC,
    /** Creation date of previous message from a given originator.
     * @since 11.0
     */
    PREVIOUS_MESSAGE_EPOCH,
    /** Creation date of next message from a given originator.
     * @since 11.0
     */
    NEXT_MESSAGE_EPOCH,
    /** Span of time that the OCM covers.
     * @since 11.0
     */
    TIME_SPAN,
    /** Difference (TAI – UTC) in seconds at epoch {@link #DEF_EPOCH_TZERO}.
     * @since 11.0
     */
    TAIMUTC_AT_TZERO,
    /** Difference (UT1 – UTC) in seconds at epoch {@link #DEF_EPOCH_TZERO}.
     * @since 11.0
     */
    UT1MUTC_AT_TZERO,
    /** Source and version of Earth Orientation Parameters.
     * @since 11.0
     */
    EOP_SOURCE,
    /** Interpolation method for Earth Orientation Parameters.
     * @since 11.0
     */
    INTERP_METHOD_EOP,
    /** Interpolation method for space weather data.
     * @since 11.0
     */
    INTERP_METHOD_SW,
    /** Source and version of celestial body (e.g. Sun/Earth/Planetary).
     * @since 11.0
     */
    CELESTIAL_SOURCE,
    /** Start of orbit data section.
     * @since 11.0
     */
    ORB_START,
    /** Stop of orbit data section.
     * @since 11.0
     */
    ORB_STOP,
    /** Orbit identification number.
     * @since 11.0
     */
    ORB_ID,
    /** Identification number of previous orbit.
     * @since 11.0
     */
    ORB_PREV_ID,
    /** Identification number of next orbit.
     * @since 11.0
     */
    ORB_NEXT_ID,
    /** Basis of this orbit state time history data.
     * @see CCSDSOrbitBasis
     * @since 11.0
     */
    ORB_BASIS,
    /** Identification number of the orbit determination or simulation upon which this orbit is based.
     * @since 11.0
     */
    ORB_BASIS_ID,
    /** Type of averaging (Osculating, mean Brouwer, other...).
     * @since 11.0
     */
    ORB_AVERAGING,
    /** Reference epoch for all relative times in the orbit state block.
     * @since 11.0
     */
    ORB_EPOCH_TZERO,
    /** Time system for {@link #ORB_EPOCH_TZERO}.
     * @since 11.0
     */
    ORB_TIME_SYSTEM,
    /** Reference frame of the orbit.
     * @since 11.0
     */
    ORB_REF_FRAME,
    /** Epoch of the {@link #ORB_REF_FRAME orbit reference frame}.
     * @since 11.0
     */
    ORB_FRAME_EPOCH,
    /** Orbit element set type.
     * @see CCSDSElementsType
     * @since 11.0
     */
    ORB_TYPE,
    /** Number of elements (excluding time) contain in the element set.
     * @since 11.0
     */
    ORB_N,
    /** Definition of orbit elements.
     * @since 11.0
     */
    ORB_ELEMENTS,
    /** Start of object physical characteristics data section.
     * @since 11.0
     */
    PHYS_START,
    /** Stop of object physical characteristics data section.
     * @since 11.0
     */
    PHYS_STOP,
    /** Start of covariance data section.
     * @since 11.0
     */
    COV_START,
    /** Stop of covariance data section.
     * @since 11.0
     */
    COV_STOP,
    /** Start of state transition matrix data section.
     * @since 11.0
     */
    STM_START,
    /** Stop of state transition matrix data section.
     * @since 11.0
     */
    STM_STOP,
    /** Start of maneuver data section.
     * @since 11.0
     */
    MAN_START,
    /** Stop of maneuver data section.
     * @since 11.0
     */
    MAN_STOP,
    /** Start of perturbations data section.
     * @since 11.0
     */
    PERT_START,
    /** Stop of perturbations data section.
     * @since 11.0
     */
    PERT_STOP,
    /** Start of orbit determination data section.
     * @since 11.0
     */
    OD_START,
    /** Stop of orbit determination data section.
     * @since 11.0
     */
    OD_STOP,
    /** Start of user-defined parameters data section.
     * @since 11.0
     */
    USER_START,
    /** Stop of user-defined parameters data section.
     * @since 11.0
     */
    USER_STOP,
    /** Name of the reference frame in which the state vector and optional Keplerian element data are given. */
    REF_FRAME,
    /** Epoch of reference frame, if not intrinsic to the definition of the reference frame. */
    REF_FRAME_EPOCH,
    /** Mean element theory. */
    MEAN_ELEMENT_THEORY,
    /** Position vector X-component. */
    X,
    /** Position vector Y-component. */
    Y,
    /** Position vector Z-component. */
    Z,
    /** Velocity vector X-component. */
    X_DOT,
    /** Velocity vector Y-component. */
    Y_DOT,
    /** Velocity vector Z-component. */
    Z_DOT,
    /** Orbit semi-major axis. */
    SEMI_MAJOR_AXIS,
    /** Mean Motion. */
    MEAN_MOTION,
    /** Orbit eccentricity. */
    ECCENTRICITY,
    /** Orbit inclination. */
    INCLINATION,
    /** Orbit right ascension of ascending node. */
    RA_OF_ASC_NODE,
    /** Orbit argument of pericenter. */
    ARG_OF_PERICENTER,
    /** Orbit true anomaly. */
    TRUE_ANOMALY,
    /** Orbit mean anomaly.*/
    MEAN_ANOMALY,
    /** Gravitational coefficient. */
    GM,
    /** Spacecraft mass. */
    MASS,
    /** Solar radiation pressure area. */
    SOLAR_RAD_AREA,
    /** Solar radiation pressure coefficient. */
    SOLAR_RAD_COEFF,
    /** Drag area. */
    DRAG_AREA,
    /** Drag coefficient. */
    DRAG_COEFF,
    /** Ephemeris type. */
    EPHEMERIS_TYPE,
    /** Classification type. */
    CLASSIFICATION_TYPE,
    /** NORAD catalogue number. */
    NORAD_CAT_ID,
    /** Element set number of the satellite. */
    ELEMENT_SET_NO,
    /** Revolution Number. */
    REV_AT_EPOCH,
    /** SGP/SGP4 drag-like coefficient. */
    BSTAR,
    /** First Time Derivative of the Mean Motion. */
    MEAN_MOTION_DOT,
    /** Second Time Derivative of the Mean Motion. */
    MEAN_MOTION_DDOT,
    /** Coordinate system for covariance matrix. Its value can either be RSW, RTN (both indicating
    /* "Radial, Transverse, Normal") or TNW. */
    COV_REF_FRAME,
    /** Covariance matrix [1, 1] element. */
    CX_X,
    /** Covariance matrix [2, 1] element. */
    CY_X,
    /** Covariance matrix [2, 2] element. */
    CY_Y,
    /** Covariance matrix [3, 1] element. */
    CZ_X,
    /** Covariance matrix [3, 2] element. */
    CZ_Y,
    /** Covariance matrix [3, 3] element. */
    CZ_Z,
    /** Covariance matrix [4, 1] element. */
    CX_DOT_X,
    /** Covariance matrix [4, 2] element. */
    CX_DOT_Y,
    /** Covariance matrix [4, 3] element. */
    CX_DOT_Z,
    /** Covariance matrix [4, 4] element. */
    CX_DOT_X_DOT,
    /** Covariance matrix [5, 1] element. */
    CY_DOT_X,
    /** Covariance matrix [5, 2] element. */
    CY_DOT_Y,
    /** Covariance matrix [5, 3] element. */
    CY_DOT_Z,
    /** Covariance matrix [5, 4] element. */
    CY_DOT_X_DOT,
    /** Covariance matrix [5, 5] element. */
    CY_DOT_Y_DOT,
    /** Covariance matrix [6, 1] element. */
    CZ_DOT_X,
    /** Covariance matrix [6, 2] element. */
    CZ_DOT_Y,
    /** Covariance matrix [6, 3] element. */
    CZ_DOT_Z,
    /** Covariance matrix [6, 4] element. */
    CZ_DOT_X_DOT,
    /** Covariance matrix [6, 5] element. */
    CZ_DOT_Y_DOT,
    /** Covariance matrix [6, 6] element. */
    CZ_DOT_Z_DOT,
    /** Epoch of ignition. */
    MAN_EPOCH_IGNITION,
    /** Mass change during maneuver (value is &lt; 0). */
    MAN_DELTA_MASS,
    /** First component of the velocity increment. */
    MAN_DV_1,
    /** Second component of the velocity increment. */
    MAN_DV_2,
    /** Third component of the velocity increment. */
    MAN_DV_3,
    /** This keyword must appear before the first line of the covariance matrix data. */
    COVARIANCE_START,
    /** The interpolation method to be used. */
    INTERPOLATION,
    /** This keyword must appear after the last line of the covariance matrix data. */
    COVARIANCE_STOP,

    // -------------------------------------------
    // Attitude Data Messages (ADM) specific keywords
    // -------------------------------------------

    /** CCSDS APM format version. */
    CCSDS_APM_VERS,
    /** CCSDS AEM format version. */
    CCSDS_AEM_VERS,
    /** Name of the reference frame specifying one frame of the transformation. */
    Q_FRAME_A,
    /** Name of the reference frame specifying the second portion of the transformation. */
    Q_FRAME_B,
    /** Rotation direction of the attitude quaternion. */
    Q_DIR,
    /** e1 * sin(φ/2)   φ = rotation angle. */
    Q1,
    /** e2 * sin(φ/2)   φ = rotation angle. */
    Q2,
    /** e3 * sin(φ/2)   φ = rotation angle. */
    Q3,
    /** cos(φ/2)   φ = rotation angle. */
    QC,
    /** Derivative of Q1. */
    Q1_DOT,
    /** Derivative of Q2. */
    Q2_DOT,
    /** Derivative of Q3. */
    Q3_DOT,
    /** Derivative of QC. */
    QC_DOT,
    /**  Name of the reference frame specifying one frame of the transformation. */
    EULER_FRAME_A,
    /** Name of the reference frame specifying the second portion of the transformation. */
    EULER_FRAME_B,
    /** Rotation direction of the attitude Euler angle. */
    EULER_DIR,
    /** Rotation order of the EULER_FRAME_A to EULER_FRAME_B or vice versa. */
    EULER_ROT_SEQ,
    /** The value of this keyword expresses the relevant keyword to use that denotes the
     *  frame of reference in which the X_RATE, Y_RATE and Z_RATE are expressed. */
    RATE_FRAME,
    /** X body rotation angle. */
    X_ANGLE,
    /** Y body rotation angle. */
    Y_ANGLE,
    /** Z body rotation angle. */
    Z_ANGLE,
    /** X body rotation rate. */
    X_RATE,
    /** Y body rotation rate. */
    Y_RATE,
    /** Z body rotation rate. */
    Z_RATE,
    /**  Name of the reference frame specifying one frame of the transformation. */
    SPIN_FRAME_A,
    /** Name of the reference frame specifying the second portion of the transformation. */
    SPIN_FRAME_B,
    /** Rotation direction of the Spin angles .*/
    SPIN_DIR,
    /** Right ascension of spin axis vector. */
    SPIN_ALPHA,
    /** Declination of the spin axis vector.*/
    SPIN_DELTA,
    /** Phase of the satellite about the spin axis. */
    SPIN_ANGLE,
    /** Angular velocity of satellite around spin axis. */
    SPIN_ANGLE_VEL,
    /** Nutation angle of spin axis. */
    NUTATION,
    /** Body nutation period of the spin axis. */
    NUTATION_PER,
    /** Inertial nutation phase. */
    NUTATION_PHASE,
    /** Coordinate system for the inertia tensor. */
    INERTIA_REF_FRAME,
    /** Moment of Inertia about the 1-axis. */
    I11,
    /** Moment of Inertia about the 2-axis. */
    I22,
    /** Moment of Inertia about the 3-axis. */
    I33,
    /** Inertia Cross Product of the 1 and 2 axes. */
    I12,
    /** Inertia Cross Product of the 1 and 3 axes. */
    I13,
    /** Inertia Cross Product of the 2 and 3 axes. */
    I23,
    /** Epoch of start of maneuver. */
    MAN_EPOCH_START,
    /** First component of the torque vector. */
    MAN_TOR_1,
    /** Second component of the torque vector. */
    MAN_TOR_2,
    /** Third component of the torque vector. */
    MAN_TOR_3,
    /** Name of the reference frame specifying one frame of the transformation. */
    REF_FRAME_A,
    /** Name of the reference frame specifying the second portion of the transformation. */
    REF_FRAME_B,
    /** Rotation direction of the attitude. */
    ATTITUDE_DIR,
    /** The format of the data lines in the message. */
    ATTITUDE_TYPE,
    /** The placement of the scalar portion of the quaternion (QC) in the attitude data. */
    QUATERNION_TYPE,
    /** Recommended interpolation method for attitude ephemeris data. */
    INTERPOLATION_METHOD,

    // Miscellaneous KEYVALUE keywords
    /** Keyword used to delineate the start of a Data block in Keyvalue files. */
    DATA_START,
    /** Keyword used to delineate the end of a Data block in Keyvalue files.. */
    DATA_STOP;

}
