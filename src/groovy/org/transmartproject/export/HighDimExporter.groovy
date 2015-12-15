package org.transmartproject.export

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Enables exporting high dimensional data 
 */
interface HighDimExporter {
    /**
     * Determines whether a datatype is supported
     * @param dataType Name of the datatype
     * @return true if the datatype is supported by this exporter, false otherwise
     */
    public boolean isDataTypeSupported( String dataType )
    
    /**
     * @return A short string describing the format that is 
     *         produced by this exporter
     */
    public String getFormat()
    
    /**
     * @return a longer human readable description for this exporter
     */
    public String getDescription()


    /**
     * @return a map with display attributes, used in the user interface.
     *
     * Recognized keys:
     * - selectOnFilterPriority (number): The exports with the highest values within a filtered datatype
     *   are selected when the user drops a concept on the export row. If multiple exports have the same value they are
     *   all selected. With a value < 0 an exporter is never auto-selected.
     * - group (string, optional): Exporters that have the same group will be displayed together. This is intended
     *   for exporters that are expected to be used together. The GUI code also assumes that exporters in the same group
     *   have the same number of data rows available.
     */
    public Map<String, Object> getDisplayAttributes()
}
