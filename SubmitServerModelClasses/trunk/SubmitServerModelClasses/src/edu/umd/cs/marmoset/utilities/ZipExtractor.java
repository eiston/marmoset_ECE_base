/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 1, 2004
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Extract a zip file into a directory.
 * @author David Hovemeyer
 */
public class ZipExtractor {
	private File zipFile;
	private File directory;
	private int numFilesExtacted;
	private Set<String> entriesExtractedFromZipArchive = new HashSet<String>();
	private Logger log;
	
	private Logger getLog()
	{
		if (log!=null)
			return log;
		// FIXME: Shouldn't hard-code the name of the logs
        log = Logger.getLogger("edu.umd.cs.buildServer.BuildServer");
		return log;
	}
	
	/**
	 * Constructor.
	 * @param zipFile the zip file to extract
	 * @param directory the directory where the extracted files should be created
	 * @throws BuilderException
	 */
	public ZipExtractor(File zipFile, File directory) throws ZipExtractorException {
		this.zipFile = zipFile;
		this.directory = directory;
		this.numFilesExtacted = 0;
		
		// Paranoia
		if (!zipFile.isFile())
			throw new ZipExtractorException("File " + zipFile + " is not a file");
		if (!directory.isDirectory())
			throw new ZipExtractorException("Directory " + directory + " is not a directory");
	}
	
	/**
	 * Get the directory the zipfile is being extracted into.
	 * @return the directory
	 */
	protected File getDirectory() {
		return directory;
	}
	
	/**
	 * Extract the zip file.
	 * @throws IOException
	 * @throws BuilderException
	 */
	public void extract() throws IOException, ZipExtractorException {
		ZipFile z = new ZipFile(zipFile);
		
		try {
			Enumeration<? extends ZipEntry> entries = z.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();
				
				/*
				if (entryName.startsWith("/"))
					throw new ZipExtractorException("Entry " + entryName + " in " + zipFile.toString() +
					" is absolute");
				*/
				
				if (entry.isDirectory())
					continue;
				
				if (!shouldExtract(entryName))
					continue;
				
				// Get the filename to extract the entry into.
				// Subclasses may define this to be something other
				// than the entry name.
				String entryFileName = transformFileName(entryName);
				if (!entryFileName.equals(entryName)) {
					getLog().debug("Transformed zip entry name: " + entryName + " ==> " +
							entryFileName);
				}
				entriesExtractedFromZipArchive.add(entryFileName);
				
				File entryOutputFile = new File(directory, entryFileName).getAbsoluteFile();
				
				File parentDir = entryOutputFile.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						throw new ZipExtractorException("Couldn't make directory for entry output file " +
							entryOutputFile.getPath());
					}
				}

				if (!parentDir.isDirectory()) {
					throw new ZipExtractorException("Parent directory for entry " +
							entryOutputFile.getPath() + " is not a directory");
				}
				
				// Make sure the entry output file lies within the build directory.
				// A malicious zip file might have ".." components in it.
				
				getLog().warn("entryOutputFile path: " +entryOutputFile.getCanonicalPath());
                if (!entryOutputFile.getCanonicalPath().startsWith(directory.getCanonicalPath() + "/")) {
					throw new ZipExtractorException("Zip entry " + entryOutputFile.getPath() + " accesses a path " +
							"outside the build directory " + directory.getPath());
				}
				
				// Extract the entry
				InputStream entryInputStream = null;
				OutputStream entryOutputStream = null;
				try {
					entryInputStream = z.getInputStream(entry);
					entryOutputStream =
						new BufferedOutputStream(new FileOutputStream(entryOutputFile));
					
					CopyUtils.copy(entryInputStream, entryOutputStream);
				} finally {
                    IOUtils.closeQuietly(entryInputStream);
                    IOUtils.closeQuietly(entryOutputStream);
				}
				
				// Hook for subclasses, to specify when entries are
				// successfully extracted.
				successfulFileExtraction(entryName, entryFileName);
				++numFilesExtacted;
			}
		} finally {
			z.close();
		}
		
	}
	
	/**
	 * Get the number of files extracted.
	 * @return the number of files extracted
	 */
	public int getNumFilesExtracted() {
		return numFilesExtacted;
	}
	
	/**
	 * Called before we attempt to extract each entry.
	 * If it returns false, then we won't try to extract the entry.
	 * Subclasses may override.
	 * 
	 * @param entryName name of the entry
	 * @return true if the entry should be extracted, false if not
	 */
	protected boolean shouldExtract(String entryName) {
		return true;
	}
	
	/**
	 * Called before extracting an entry, in order to transform
	 * the entry name into the actual filename which will be created.
	 * @param entryName name of the entry to be extracted
	 * @return actual filename to be created for the entry
	 */
	protected String transformFileName(String entryName) {
		return entryName;
	}
	
	/**
	 * Called when an entry has been successfully extracted.
	 * 
	 * @param entryName the name of the zip entry extracted
	 * @param filename the filename of the extracted entry (relative
	 *   to the directory where the entry was extracted)
	 */
	protected void successfulFileExtraction(String entryName, String filename) {
        getLog().trace("Extracted zip entry " +entryName+ " to file " +filename);
	}

	/**
	 * @return Returns the entriesExtractedFromZipArchive.
	 */
	public Set<String> getEntriesExtractedFromZipArchive() {
		return entriesExtractedFromZipArchive;
	}
}
