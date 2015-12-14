package org.transmartproject.export

import java.util.Map;

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Enables exporting high dimensional data. 
 */
interface HighDimTabularResultExporter extends HighDimExporter {

    /**
     * Returns the projection name to be used for retrieving
     * data from the database.
     * @return Projection name.
     */
    public String getProjection()

    /**
     * Exports the data in the TabularResult to the outputStream given.
     * @param data Data to be exported.
     * @param projection Projection that was used to retrieve the data.
     * @param outputStream Stream to write the data to.
     * @return a map with information about the exported values:
     *      the entry with key <code>rowsWritten</code> contains the number of rows written.
     */
    public Map<String, Object> export( TabularResult data, Projection projection, OutputStream outputStream )

    /**
     * Exports the data in the TabularResult to the outputStream given, 
     * although the export can be cancelled. Cancelling can be caused
     * by the user or by some other process.
     * @param data Data to be exported.
     * @param projection Projection that was used to retrieve the data.
     * @param outputStream Stream to write the data to.
     * @param isCancelled Closure that returns true iff the export is cancelled.
     * @return a map with information about the exported values:
     *      the entry with key <code>rowsWritten</code> contains the number of rows written.
     */
    public Map<String, Object> export( TabularResult data, Projection projection, OutputStream outputStream, Closure isCancelled )

}
