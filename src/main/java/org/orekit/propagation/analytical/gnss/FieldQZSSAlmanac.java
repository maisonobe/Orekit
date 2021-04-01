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
package org.orekit.propagation.analytical.gnss;

import org.hipparchus.Field;
import org.hipparchus.RealFieldElement;
import org.hipparchus.util.FastMath;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.data.DataContext;
import org.orekit.gnss.QZSSAlmanac;
import org.orekit.gnss.SatelliteSystem;
import org.orekit.time.FieldAbsoluteDate;

/**
 * This class holds a QZSS almanac as read from YUMA files.
 *
 * @author Bryan Cazabonne
 * @author Nicolas Fialton (field translation)
 *
 */
public class FieldQZSSAlmanac<T extends RealFieldElement<T>> implements FieldQZSSOrbitalElements<T> {
	private final T zero;
	// Fields
	/** Source of the almanac. */
	private final String src;

	/** PRN number. */
	private final int prn;

	/** Health status. */
	private final int health;

	/** QZSS week. */
	private final int week;

	/** Time of applicability. */
	private final T toa;

	/** Semi-major axis. */
	private final T sma;

	/** Eccentricity. */
	private final T ecc;

	/** Inclination. */
	private final T inc;

	/** Longitude of Orbital Plane. */
	private final T om0;

	/** Rate of Right Ascension. */
	private final T dom;

	/** Argument of perigee. */
	private final T aop;

	/** Mean anomaly. */
	private final T anom;

	/** Zeroth order clock correction. */
	private final T af0;

	/** First order clock correction. */
	private final T af1;

	/** Date of validity. */
	private final FieldAbsoluteDate<T> date;

	/**
	 * Constructor.
	 *
	 * <p>
	 * This method uses the {@link DataContext#getDefault() default data context}.
	 *
	 * @param source the source of the almanac (SEM, YUMA, user defined)
	 * @param prn    the PRN number
	 * @param week   the QZSS week
	 * @param toa    the Time of Applicability
	 * @param sqa    the Square Root of Semi-Major Axis (m^1/2)
	 * @param ecc    the eccentricity
	 * @param inc    the inclination (rad)
	 * @param om0    the geographic longitude of the orbital plane at the weekly
	 *               epoch (rad)
	 * @param dom    the Rate of Right Ascension (rad/s)
	 * @param aop    the Argument of Perigee (rad)
	 * @param anom   the Mean Anomaly (rad)
	 * @param af0    the Zeroth Order Clock Correction (s)
	 * @param af1    the First Order Clock Correction (s/s)
	 * @param health the Health status
	 * @see #FieldQZSSAlmanac(String, int, int, T, T, T, T, T, T, T, T, T, T, int,
	 *      FieldAbsoluteDate)
	 */
	@DefaultDataContext
	public FieldQZSSAlmanac(final String source, final int prn, final int week, final T toa, final T sqa, final T ecc,
			final T inc, final T om0, final T dom, final T aop, final T anom, final T af0, final T af1,
			final int health) {
		this(source, prn, week, toa, sqa, ecc, inc, om0, dom, aop, anom, af0, af1, health, new FieldGNSSDate<>(week,
				toa.multiply(1000), SatelliteSystem.QZSS, DataContext.getDefault().getTimeScales()).getDate());
	}

	/**
	 * Constructor.
	 *
	 * @param source the source of the almanac (SEM, YUMA, user defined)
	 * @param prn    the PRN number
	 * @param week   the QZSS week
	 * @param toa    the Time of Applicability
	 * @param sqa    the Square Root of Semi-Major Axis (m^1/2)
	 * @param ecc    the eccentricity
	 * @param inc    the inclination (rad)
	 * @param om0    the geographic longitude of the orbital plane at the weekly
	 *               epoch (rad)
	 * @param dom    the Rate of Right Ascension (rad/s)
	 * @param aop    the Argument of Perigee (rad)
	 * @param anom   the Mean Anomaly (rad)
	 * @param af0    the Zeroth Order Clock Correction (s)
	 * @param af1    the First Order Clock Correction (s/s)
	 * @param health the Health status
	 * @param date   of applicability corresponding to {@code week} and {@code toa}.
	 * @since 10.1
	 */
	public FieldQZSSAlmanac(final String source, final int prn, final int week, final T toa, final T sqa, final T ecc,
			final T inc, final T om0, final T dom, final T aop, final T anom, final T af0, final T af1,
			final int health, final FieldAbsoluteDate<T> date) {
		this.zero = toa.getField().getZero();
		this.src = source;
		this.prn = prn;
		this.week = week;
		this.toa = toa;
		this.sma = sqa.multiply(sqa);
		this.ecc = ecc;
		this.inc = inc;
		this.om0 = om0;
		this.dom = dom;
		this.aop = aop;
		this.anom = anom;
		this.af0 = af0;
		this.af1 = af1;
		this.health = health;
		this.date = date;
	}

	/**
	 * Constructor
	 * 
	 * @param field
	 * @param almanac
	 */
	public FieldQZSSAlmanac(Field<T> field, QZSSAlmanac almanac) {
		this.zero = field.getZero();
		this.src = almanac.getSource();
		this.prn = almanac.getPRN();
		this.week = almanac.getWeek();
		this.toa = zero.add(almanac.getTime());
		this.sma = zero.add(almanac.getSma());
		this.ecc = zero.add(almanac.getE());
		this.inc = zero.add(almanac.getI0());
		this.om0 = zero.add(almanac.getOmega0());
		this.dom = zero.add(almanac.getOmegaDot());
		this.aop = zero.add(almanac.getPa());
		this.anom = zero.add(almanac.getM0());
		this.af0 = zero.add(almanac.getAf0());
		this.af1 = zero.add(almanac.getAf1());
		this.health = almanac.getHealth();
		this.date = new FieldAbsoluteDate<>(field, almanac.getDate());
	}

	/**
	 * Gets the source of this QZSS almanac.
	 *
	 * @return the source of this QZSS almanac
	 */
	public String getSource() {
		return src;
	}

	@Override
	public int getPRN() {
		return prn;
	}

	@Override
	public int getWeek() {
		return week;
	}

	@Override
	public T getTime() {
		return toa;
	}

	@Override
	public T getSma() {
		return sma;
	}

	@Override
	public T getMeanMotion() {
		final T absA = FastMath.abs(sma);
		return FastMath.sqrt(absA.getField().getZero().add(QZSS_MU).divide(absA)).divide(absA);
	}

	@Override
	public T getE() {
		return ecc;
	}

	@Override
	public T getI0() {
		return inc;
	}

	@Override
	public T getOmega0() {
		return om0;
	}

	@Override
	public T getOmegaDot() {
		return dom;
	}

	@Override
	public T getPa() {
		return aop;
	}

	@Override
	public T getM0() {
		return anom;
	}

	@Override
	public T getAf0() {
		return af0;
	}

	@Override
	public T getAf1() {
		return af1;
	}

	@Override
	public FieldAbsoluteDate<T> getDate() {
		return date;
	}

	/**
	 * Gets the Health status.
	 *
	 * @return the Health status
	 */
	public int getHealth() {
		return health;
	}

	@Override
	public T getIDot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCuc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCrc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCrs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCic() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getCis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getAf2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getToc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIODC() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIODE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public T getTGD() {
		// TODO Auto-generated method stub
		return null;
	}

}
