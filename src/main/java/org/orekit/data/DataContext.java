package org.orekit.data;

import org.orekit.bodies.CelestialBodies;
import org.orekit.bodies.CelestialBody;
import org.orekit.forces.gravity.potential.GravityFields;
import org.orekit.frames.Frames;
import org.orekit.frames.FramesFactory;
import org.orekit.models.earth.GeoMagneticFields;
import org.orekit.models.earth.ionosphere.KlobucharIonoCoefficientsLoader;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScales;
import org.orekit.time.TimeScalesFactory;

/**
 * Provides auxiliary data for portions of the application.
 *
 * @author Evan Ward
 * @since 10.1
 */
public interface DataContext {

    /**
     * Get the default data context that is used to implement the static factories ({@link
     * TimeScalesFactory}, {@link FramesFactory}, etc) and loaders that feed themselves
     * (e.g. {@link KlobucharIonoCoefficientsLoader}). It is used to maintain
     * compatibility with auxiliary data loading in Orekit 10.0.
     *
     * @return Orekit's default data context.
     */
    static LazyLoadedDataContext getDefault() {
        return DefaultDataContextHolder.INSTANCE;
    }

    /**
     * Set the default data context that is used to implement Orekit's static factories.
     *
     * <p> Calling this method will not modify any instances already retrieved from
     * Orekit's static factories. In general this method should only be called at
     * application start up before any of the static factories are used.
     *
     * @param context the new data context.
     * @see #getDefault()
     */
    static void setDefault(final LazyLoadedDataContext context) {
        DefaultDataContextHolder.INSTANCE = context;
    }

    /**
     * Get a factory for constructing {@link TimeScale}s based on the auxiliary data in
     * this context.
     *
     * @return the set of common time scales using this data context.
     */
    TimeScales getTimeScales();

    /**
     * Get a factory constructing {@link Frame}s based on the auxiliary data in this
     * context.
     *
     * @return the set of common reference frames using this data context.
     */
    Frames getFrames();

    /**
     * Get a factory constructing {@link CelestialBody}s based on the auxiliary data in
     * this context.
     *
     * @return the set of common celestial bodies using this data context.
     */
    CelestialBodies getCelestialBodies();

    /**
     * Get a factory constructing gravity fields based on the auxiliary data in this
     * context.
     *
     * @return the gravity fields using this data context.
     */
    GravityFields getGravityFields();

    /**
     * Get a factory constructing geomagnetic fields based on the auxiliary data in this
     * context.
     *
     * @return the geomagnetic fields using this data context.
     */
    GeoMagneticFields getGeoMagneticFields();

}