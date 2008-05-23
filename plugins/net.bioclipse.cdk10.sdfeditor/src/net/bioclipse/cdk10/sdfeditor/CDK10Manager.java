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
package net.bioclipse.cdk10.sdfeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioList;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CDK10Manager{

    private static final Logger logger = Logger.getLogger(CDK10Manager.class);
    
    ReaderFactory readerFactory;

    public String getNamespace() {
        return "cdk";
    }




    /**
     * Load one or more molecules from an InputStream and return a CDKMoleculeList.
     */
    public List loadMolecules(InputStream instream) throws IOException, BioclipseException {

        if (readerFactory==null){
            readerFactory=new ReaderFactory();
            CDK10ManagerHelper.registerFormats(readerFactory);
        }

        System.out.println("no formats supported: " + readerFactory.getFormats().size());
//        System.out.println("format guess: " + readerFactory.guessFormat(instream).getFormatName());

        //Create the reader
        IChemObjectReader reader= readerFactory.createReader(instream);

        if (reader==null){
            throw new BioclipseException("Could not create reader in CDK. ");
        }

        IChemFile chemFile = new org.openscience.cdk.ChemFile();

        // Do some customizations...
        CDK10ManagerHelper.customizeReading(reader, chemFile);

        //Read file
        try {
            chemFile=(IChemFile)reader.read(chemFile);
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            LogUtils.debugTrace(logger, e);
        }

        //Store the chemFormat used for the reader
        IResourceFormat chemFormat=reader.getFormat();
        System.out.println("Rad CDK chemfile with format: " + chemFormat.getFormatName());

        List<IAtomContainer> atomContainersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        int nuMols=atomContainersList.size();
        System.out.println("This file contained: " + nuMols + " molecules");

        List moleculesList=new BioList<IBioObject>();
//        CDKMolecule[] moleculesData = new CDKMolecule[atomContainersList.size()];

        for (int i=0; i<atomContainersList.size();i++){
            IAtomContainer ac=null;
            Object obj=atomContainersList.get(i);
            if (obj instanceof org.openscience.cdk.interfaces.IMolecule) {
                ac=(org.openscience.cdk.interfaces.IMolecule)obj;
            }else if (obj instanceof IAtomContainer) {
                ac=(IAtomContainer)obj;
            }

            CDK10Molecule mol=new CDK10Molecule(ac);
            String moleculeName="Molecule " + i; 
            if (ac instanceof IMolecule) {
                org.openscience.cdk.interfaces.IMolecule imol = (org.openscience.cdk.interfaces.IMolecule) ac;
                String molName=(String) imol.getProperty(CDKConstants.TITLE);
                if (molName!=null && (!(molName.equals("")))){
                    moleculeName=molName;
                }
            }
            mol.setName(moleculeName);
            
            moleculesList.add(mol);
        }
        
        return moleculesList;
    }

    public String calculateSmiles(IMolecule molecule) throws BioclipseException {
        return molecule.getSmiles();
    }

    /**
     * Save the molecule to specified file
     * @param molecule
     * @param file
     * @throws IllegalStateException
     */
    public void saveMolecule(CDK10Molecule molecule, IFile file) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    /**
     * Create molecule from SMILES.
     * @throws BioclipseException 
     */
    public CDK10Molecule createMoleculeFromSMILES(String SMILES) throws BioclipseException {
        SmilesParser parser=new SmilesParser(DefaultChemObjectBuilder.getInstance());
        try {
            org.openscience.cdk.interfaces.IMolecule mol=parser.parseSmiles(SMILES);
            return new CDK10Molecule(mol);
        } catch (InvalidSmilesException e) {
            throw new BioclipseException("SMILES string is invalid");
        }
        
    }

    public Iterator<IMolecule> creatMoleculeIterator(InputStream instream) {
        return new IteratingBioclipseMDLReader(instream, NoNotificationChemObjectBuilder.getInstance());
    }

    class IteratingBioclipseMDLReader implements Iterator<IMolecule> {

        IteratingMDLReader reader;
        
        public IteratingBioclipseMDLReader(InputStream input, IChemObjectBuilder builder) {
            reader = new IteratingMDLReader(input, builder);
        }

        public boolean hasNext() {
            return reader.hasNext();
        }

        public IMolecule next() {
            org.openscience.cdk.interfaces.IMolecule cdkMol = (org.openscience.cdk.interfaces.IMolecule)reader.next();
            IMolecule bioclipseMol = new CDK10Molecule(cdkMol);
            return bioclipseMol;
        }

        public void remove() {
            reader.remove();
        }
    }

    public void saveMoleculesAsSDF( final List<CDK10Molecule> mols,
                                    final IFile file )
                        throws InvocationTargetException, InterruptedException {

        WorkspaceModifyOperation op = new WorkspaceModifyOperation(){

            @Override
            protected void execute( IProgressMonitor monitor )
                      throws CoreException, InvocationTargetException,
                                                         InterruptedException {

                monitor.beginTask( "Saving file: " 
                                   + file.getName(), 4 );
                monitor.subTask( "Serializing molecules" );
                monitor.worked( 1 );
                
                //Collect all ACs in chemfile
                IMoleculeSet ms= new MoleculeSet();
                for (CDK10Molecule cdk10mol : mols){
                    ms.addAtomContainer( cdk10mol.getAtomContainer() );
                }

                IChemModel model=new ChemModel();
                model.setMoleculeSet( ms );

                monitor.worked( 1 );

                //Serialize ChemFile to SDF as byte[]
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                MDLWriter writer=new MDLWriter(bos);
                
                //FIXME: CDK don't save properties, so need workaround
                
                try {
                    writer.write( model );
                } catch ( CDKException e ) {
                    throw new InvocationTargetException(e);
                }

                //Get result from outputStream
                byte[] buffer = bos.toByteArray();

                //Set up an inputStream
                ByteArrayInputStream bis=new ByteArrayInputStream(buffer);

                monitor.subTask( "Writing to file" );
                monitor.worked( 1 );

                //Write contents to file
                file.setContents( bis, false, false, monitor );

                monitor.done();

            }
            
        };

        PlatformUI.getWorkbench().getProgressService().run(true,false,op);

    }
}
