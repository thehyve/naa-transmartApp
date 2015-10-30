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

}
