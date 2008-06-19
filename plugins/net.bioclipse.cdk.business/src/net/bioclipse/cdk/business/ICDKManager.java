 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeList;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.business.IBioclipseManager;
import net.bioclipse.core.domain.IMolecule;

public interface ICDKManager extends IBioclipseManager {

    /**
     * Create a CDKMolecule from SMILES
     * @param SMILES
     * @return
     * @throws BioclipseException
     */
    @Recorded
    public ICDKMolecule fromSmiles(String smiles)
        throws BioclipseException;

    /**
     * Loads a molecule from file using CDK
     *
     * @param path The path to the file
     * @return a BioJavaSequence object
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    public ICDKMolecule loadMolecule( String path )
        throws IOException, BioclipseException;

    /**
     * Load molecule from InputStream using CDK
     * @param instream to be loaded
     * @return loaded sequence
     * @throws IOException
     * @throws BioclipseException
     */
    @Recorded
    public ICDKMolecule loadMolecule(InputStream instream)
        throws IOException, BioclipseException;

    /**
     * Load a molecules from a file.
     */
    @Recorded
    public CDKMoleculeList loadMolecules(String path)
        throws IOException, BioclipseException;

    /**
     * Load one or more molecules from an InputStream and return a CDKMoleculeList.
     */
    @Recorded
    public CDKMoleculeList loadMolecules(InputStream instream)
        throws IOException, BioclipseException;

    /**
     *
     * @param mol
     * @throws IllegalStateException
     */
    @Recorded
    public void saveMolecule(CDKMolecule mol) throws IllegalStateException;


    /**
     * Calculate SMILES string for an IMolecule
     * @param molecule
     * @return
     * @throws BioclipseException
     */
    @Recorded
    public String calculateSmiles (IMolecule molecule) throws BioclipseException;

    /**
     * Returns an iterator to the molecules in an Inputstream
     *
     * @param instream
     * @return
     */
    @Recorded
    public Iterator<ICDKMolecule> creatMoleculeIterator(InputStream instream);

    /**
     * True if the two molecules has equal fingerprints
     * 
     * @param molecule
     * @param subStructure
     * @return
     * @throws BioclipseException
     */
    @PublishedMethod (params = "ICDKMolecule molecule, " +
    		                       "ICDKMolecule subStructure",
                      methodSummary = "Returns true if the two molecules has " +
                      		          "equal fingerprints")
    @Recorded
    public boolean fingerPrintMatches( ICDKMolecule molecule, 
                                       ICDKMolecule subStructure ) 
                   throws BioclipseException;

    
    /**
     * True if the paramater substructure is a substructure to the 
     * paramater molecule
     * 
     * @param molecule
     * @param subStructure
     * @return
     */
    @PublishedMethod (params = "ICDKMolecule molecule, " +
    		                   "ICDKMolecule subStructure",
    		          methodSummary = "Returns true if the paramater named " +
    		          		          "subStructure is a substructure of the " +
    		          		          "paramater named molecule")
    public boolean subStructureMatches( ICDKMolecule molecule,
                                        ICDKMolecule subStructure );

    
    /**
     * Creates a cdk molecule from an IMolecule
     * 
     * @param m
     * @return
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "IMolecule m",
                       methodSummary = "Creates a cdk molecule from a" +
                       		           " molecule" )
    public ICDKMolecule create( IMolecule m ) throws BioclipseException;

    /**
     * Creates a cdk molecule from a String
     * 
     * @param m
     * @return
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( params = "IMolecule m",
                       methodSummary = "Creates a cdk molecule from a" +
                                     " String" )
    public ICDKMolecule fromString( String cml ) throws BioclipseException, IOException;

    /**
     * 
     * @param molecule
     * @param smarts
     * @return whether the given SMARTS matches the given molecule
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "ICDKMolecule molecule, String smarts", 
                       methodSummary = "Tests whether the given SMARTS " +
                       		           "matches the given molecule")
    public boolean smartsMatches( ICDKMolecule molecule, String smarts ) 
                   throws BioclipseException;

    
    /**
     * @param filePath
     * @return the number of entries in the sdf file at the given path or
     *         0 if failed to read somehow.
     */
    @PublishedMethod ( params = "String filePath",
                       methodSummary = "Counts the number of entries " +
                       		             "in an SDF file at the given " +
                       		             "file path. Returns 0 in case " +
                       		             "of problem.")
    public int numberOfEntriesInSDF( String filePath );
}
