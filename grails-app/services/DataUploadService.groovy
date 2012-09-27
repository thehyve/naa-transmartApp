import com.recomdata.upload.DataUploadResult;

/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/

public class DataUploadService{

	def verifyFields(header, uploadType) {
		
		def requiredFields = RequiredUploadField.findAllByType(uploadType)*.field
		def providedFields = header.split(",")
		def missingFields = []
		for (field in requiredFields) {
			def found = false
			for (providedField in providedFields) {
				if (providedField.trim().toLowerCase().equals(field.trim().toLowerCase())) {
					found = true
					break
				}
			}
			if (!found) {
				missingFields.add(field)
			}
		}
		def success = (missingFields.size() == 0)
		def result = new DataUploadResult(success: success, requiredFields: requiredFields, providedFields: providedFields, missingFields: missingFields, error: "Required fields were missing from the uploaded file.")
		return (result);
	}
	
	def writeFile(location, file, upload) {
		//Open the given file and write it line by line to the storage location.
		OutputStream out = null;
		BufferedReader fr = new BufferedReader(new InputStreamReader(file.getInputStream()));
		
		String header = fr.readLine();
		
		//Verify fields and return immediately if we don't have a required one
		def result = verifyFields(header, upload.dataType)
		if (!result.success) {
			return result;
		}
		
		
		try {
			out = new FileWriter(new File(location))
			out.write(f.getBytes())
		}
		catch (Exception e) {
			upload.status = "ERROR"
			upload.save(flush: true)
			render(view: "complete", model: [result: new DataUploadResult(success:false, error: "Could not write file: " + e.getMessage()), uploadDataInstance: upload]);
			return;
		}
		finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			if (fr != null) {
				fr.close();
			}
		}
	}
}
