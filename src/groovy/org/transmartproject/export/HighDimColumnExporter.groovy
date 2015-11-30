package org.transmartproject.export

import java.util.Map;

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Enables exporting column information for high dimensional data.
 */
interface HighDimColumnExporter extends HighDimExporter {
    /**
     * Determines whether a datatype is supported.
     * @param dataType Name of the datatype.
     * @return true if the datatype is supported by this exporter, false otherwise.
     */
    public boolean isDataTypeSupported( String dataType )

    /**
     * @return A short string describing the format that is 
     *         produced by this exporter.
     */
    public String getFormat()

    /**
     * @return a longer human readable description for this exporter.
     */
    public String getDescription()

    /**
     * Exports column data to the outputStream.
     * @param assays The assay data to be exported.
     * @param outputStream Stream to write the data to.
     * @return a map with information about the exported values:
     *      the entry with key <code>rowsWritten</code> contains the number of rows written.
     */
    public Map<String, Object> export( Collection<Assay> assays, OutputStream outputStream )

    /**
     * Exports column data to the outputStream,
     * although the export can be cancelled. Cancelling can be caused
     * by the user or by some other process.
     * @param assays The assay data to be exported
     * @param outputStream Stream to write the data to.
     * @param isCancelled Closure that returns true iff the export is cancelled.
     * @return a map with information about the exported values:
     *      the entry with key <code>rowsWritten</code> contains the number of rows written.
     */
    public Map<String, Object> export( Collection<Assay> assays, OutputStream outputStream, Closure isCancelled )

}
