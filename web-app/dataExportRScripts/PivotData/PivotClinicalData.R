###########################################################################
 # tranSMART - translational medicine data mart
 # 
 # Copyright 2008-2012 Janssen Research & Development, LLC.
 # 
 # This product includes software developed at Janssen Research & Development, LLC.
 # 
 # This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 # as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 # 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 # 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 # 
 # This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 # 
 # You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 # 
 #
 ##########################################################################


###########################################################################
#PivotClinicalData
#Parse the i2b2 output file and create input files for Cox/Survival Curve.
###########################################################################

PivotClinicalData.pivot <- function(input.dataFile,
               snpDataExists=FALSE, multipleStudies=FALSE, study) {
  message("entering PivotClinicalData.pivot")
  df <- read.delim(input.dataFile, as.is=TRUE, check.names=FALSE)
  message(sprintf("read %d records from %s", nrow(df), input.dataFile))
   if (snpDataExists) {
    snpPEDFileData <- unique(subset(df[c("PATIENT.ID", "SNP.PED.File")], SNP.PED.File != ""))
    colnames(snpPEDFileData) <- c("PATIENT.ID", "SNP.PED.File")
  }
  conceptList <- unique(df[,"CONCEPT PATH"])
  dt <- tapply(as.character(df[,"VALUE"]), list(df[,"PATIENT ID"], df[,"CONCEPT PATH"]), function (z) paste(z,collapse=","))
  dt <- cbind(rownames(dt), dt)
  colnames(dt)[1] <- "PATIENT ID"
  if (snpDataExists) {
    dt <- merge(dt, snpPEDFileData, by="PATIENT.ID", all.x=TRUE)
    colnames(finalData)[ncol(finalData)] <- c("SNP PED File")
  }
  filename <- "clinical_i2b2trans.txt"
  if (multipleStudies)
    filename <- paste(study, "_clinical_i2b2trans.txt")
  write.table(dt, filename, row.names=FALSE, sep="\t", quote=FALSE)
  message(sprintf("wrote file %s (%d x %d dimension)", filename, nrow(dt), ncol(dt)))
 message("exiting PivotClinicalData.pivot")
 ret <- list(df=df, dt=dt)
}
