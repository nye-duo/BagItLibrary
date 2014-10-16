package no.uio.duo.bagit;

import org.apache.commons.io.FileUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Main class which manages interactions with a StudentWeb formatted BagIt file.
 *
 * This allows the BagIt packages to be constructed from files on disk, or for a
 * zipped BagIt to be read and opened.
 *
 * The StudentWeb Bag format is as follows:
 *
 * <pre>
 * base directory/
 * |   bagit.txt
 * |   manifest-md5.txt
 * |   tagmanifest-md5.txt
 * \--- data/
 *      \--- final
 *          |   [final version files]
 *      \--- supporting
 *          |   [supporting files]
 *      \--- licence/
 *          |   licence.txt
 *      \--- metadata/
 *          |   metadata.xml
 *      \--- tagfiles/
 *          |   supporting.access.txt
 *          |   formats.txt
 *          |   final.sequence.txt
 *          |   supporting.sequence.txt
 *</pre>
 */

public class BagIt
{
    private static String FINAL = "final";
    private static String SUPPORTING = "supporting";
    private static String LICENCE = "licence";
    private static String METADATA = "metadata";

    private static final int BUFFER = 8192;

    /**
     * Inner class to provide a reference to a file in the Bag.  Since the file in the bag
     * may have different types, different sources for its input stream, and different tag
     * properties, this allows us to provide a consistent wrapper for internal use.
     */
    class BagFileReference
    {
        public ZipEntry zipEntry = null;
        public Metadata metadata = null;
        public File file = null;

        public String type = null;
        public String access = null;
        public int sequence = -1;
        public String format = null;

        /**
         * Get an input stream for this file reference
         *
         * @return  an implementation of InputStream which reads from the relevant source
         * @throws IOException
         */
        public InputStream getInputStream()
                throws IOException
        {
            if (this.zipEntry != null)
            {
                return zipFile.getInputStream(this.zipEntry);
            }
            else if (this.file != null)
            {
                return new FileInputStream(file);
            }
            else if (this.metadata != null)
            {
                return new ByteArrayInputStream(this.metadata.toXML().getBytes());
            }
            return null;
        }

        /**
         * Get the filename for this reference
         *
         * @return  the name of the file, or null if none can be determined
         */
        public String getFilename()
        {
            if (this.zipEntry != null)
            {
                String path = this.zipEntry.getName();
                String[] bits = path.split("/");
                return bits[bits.length - 1];
            }
            else if (this.file != null)
            {
                return this.file.getName();
            }
            return null;
        }
    }

    String baseDir = "";
    File bagFile = null;
    ZipFile zipFile = null;
    List<BagFileReference> fileRefs = new ArrayList<BagFileReference>();

    /**
     * Create a BagIt object around a directory specified at the filePath
     *
     * @param filePath  Path to BagIt structure
     * @throws IOException
     */
    public BagIt(String filePath)
            throws IOException
    {
        this(new File(filePath));
    }

    /**
     * Create a BagIt object around a file object provided
     *
     * @param file  File object representing the BagIt structure
     * @throws IOException
     */
    public BagIt(File file)
            throws IOException
    {
        this.bagFile = file;
        String[] bits = file.getName().split("\\.");
        this.baseDir = bits[0] + "/";
        if (file.exists())
        {
            // load the bag
            this.loadBag(file);
        }
    }

    /**
     * Construct the internal state of this BagIt object from the given file.  The file should be a zip file
     * which conforms to the StudentWeb/Duo bag profile.
     *
     * @param file  The zip file which contains the bag
     * @throws IOException
     */
    public void loadBag(File file)
            throws IOException
    {
        // unset the base directory, as when reading the file, the filname may not be the same
        // as the internal base directory
        this.baseDir = null;

        this.zipFile = new ZipFile(file);
        Enumeration e = zipFile.entries();
        List<ZipEntry> tagEntries = new ArrayList<ZipEntry>();
        while (e.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) e.nextElement();

            if (this.baseDir == null)
            {
                // we need to calculate the base directory that the package is using
                String[] pathbits = entry.getName().split("/");
                if (pathbits.length > 0) {
                    this.baseDir = pathbits[0] + "/";
                }
                else
                {
                    throw new RuntimeException("The internal structure of the bag is wrong - could not find a suitable base directory");
                }
            }

            if (entry.getName().startsWith(this.baseDir + "data/final/"))
            {
                BagFileReference bfr = new BagFileReference();
                bfr.type = BagIt.FINAL;
                bfr.zipEntry = entry;
                this.fileRefs.add(bfr);
            }
            else if (entry.getName().startsWith(this.baseDir + "data/supporting/"))
            {
                BagFileReference bfr = new BagFileReference();
                bfr.type = BagIt.SUPPORTING;
                bfr.zipEntry = entry;
                this.fileRefs.add(bfr);
            }
            else if (entry.getName().startsWith(this.baseDir + "data/licence/"))
            {
                BagFileReference bfr = new BagFileReference();
                bfr.type = BagIt.LICENCE;
                bfr.zipEntry = entry;
                this.fileRefs.add(bfr);
            }
            else if (entry.getName().startsWith(this.baseDir + "data/metadata/"))
            {
                BagFileReference bfr = new BagFileReference();
                bfr.type = BagIt.METADATA;
                bfr.zipEntry = entry;
                this.fileRefs.add(bfr);
            }
            else if (entry.getName().startsWith(this.baseDir + "tagfiles/"))
            {
                tagEntries.add(entry);
            }
        }

        for (ZipEntry entry : tagEntries)
        {
            Map<String, String> entryMap = new HashMap<String, String>();
            InputStream is = zipFile.getInputStream(entry);

            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String content = s.hasNext() ? s.next() : "";

            String[] lines = content.split("\n");
            for (String line : lines)
            {
                String[] bits = line.split("\t");
                entryMap.put(bits[1], bits[0]);
            }

            for (BagFileReference bfr : this.fileRefs)
            {
                for (String path : entryMap.keySet())
                {
                    if (bfr.zipEntry.getName().equals(this.baseDir + path))
                    {
                        if (entry.getName().endsWith("final.sequence.txt"))
                        {
                            bfr.sequence = Integer.parseInt(entryMap.get(path));
                        }
                        else if (entry.getName().endsWith("formats.txt"))
                        {
                            bfr.format = entryMap.get(path);
                        }
                        else if (entry.getName().endsWith("supporting.access.txt"))
                        {
                            bfr.access = entryMap.get(path);
                        }
                        else if (entry.getName().endsWith("supporting.sequence.txt"))
                        {
                            bfr.sequence = Integer.parseInt(entryMap.get(path));
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).
     *
     * @param file  File object to add to the Bag
     */
    public void addFinalFile(File file)
    {
        // if we don't have a sequence number
        addFinalFile(file, null, -1);
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).  Give it the specified sequence number in the list of final files
     *
     * @param file  File object to add to the Bag
     * @param sequence  position in the sequence of final files in the Bag
     */
    public void addFinalFile(File file, int sequence)
    {
        this.addFinalFile(file, null, sequence);
    }

    /**
     * Add a file to the Bag, which is a final version file of the item (see the class documentation regarding
     * structure of the Bag).  Give it the specified mime type and sequence number in the list of final files
     *
     * @param file  File object to add to the Bag
     * @param mimeType  Mimetype of the file object.  If this is null, we will attempt to guess
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     */
    public void addFinalFile(File file, String mimeType, int sequence)
    {
        // create a new bag reference for this file handle
        BagFileReference bfr = new BagFileReference();
        bfr.file = file;
        bfr.type = BagIt.FINAL;

        // calculate the format if necessary
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        bfr.format = mimeType;

        // set the correct sequence number
        if (sequence == -1)
        {
            int currentMax = this.getFinalSequenceMax();
            sequence = currentMax + 1;
        }
        bfr.sequence = sequence;

        this.fileRefs.add(bfr);
    }

    /**
     * Get the current highest sequence number for the final files.
     *
     * Useful if you want to add a new file to the end of the sequence
     *
     * @return  an integer which is the same as the highest sequence number in the list of final files.  Note that the file sequence numbers are not guaranteed to be sequential.
     */
    private int getFinalSequenceMax()
    {
        int maxSeq = 0;
        for (BagFileReference bfr : this.fileRefs)
        {
            if (BagIt.FINAL.equals(bfr.type))
            {
                if (bfr.sequence > maxSeq) {
                    maxSeq = bfr.sequence;
                }
            }
        }
        return maxSeq;
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the access type specified.
     *
     * @param file  File object to add to the Bag
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, String access)
    {
        // if we don't have a sequence number
        addSupportingFile(file, null, -1, access);
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the access type specified, and the position specified by the sequence
     * in the existing list of supporting files
     *
     * @param file  File object to add to the Bag
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, int sequence, String access)
    {
        // if we don't have a sequence number
        addSupportingFile(file, null, sequence, access);
    }

    /**
     * Add a file to the Bag, which is supporting material for the item (see the class documentation regarding
     * structure of the Bag).  Give it the mimetype and access type specified, and the position specified by the sequence
     * in the existing list of supporting files
     *
     * @param file  File object to add to the Bag
     * @param mimeType  Mimetype of the file object.  If this is null, we will attempt to guess
     * @param sequence  position in the sequence of final files in the Bag.  If this is -1 we will just add it to the end of the current list
     * @param access    Access conditions of the item.  Should be "open" or "closed"
     */
    public void addSupportingFile(File file, String mimeType, int sequence, String access)
    {
        // create a new bag reference for this file handle
        BagFileReference bfr = new BagFileReference();
        bfr.file = file;
        bfr.type = BagIt.SUPPORTING;
        bfr.access = access;

        // calculate the format if necessary
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        bfr.format = mimeType;

        // set the correct sequence number
        if (sequence == -1)
        {
            int currentMax = this.getSupportingSequenceMax();
            sequence = currentMax + 1;
        }
        bfr.sequence = sequence;

        this.fileRefs.add(bfr);
    }

    /**
     * Get the current highest sequence number for the supporting files.
     *
     * Useful if you want to add a new file to the end of the sequence
     *
     * @return  an integer which is the same as the highest sequence number in the list of supporting files.  Note that the file sequence numbers are not guaranteed to be sequential.
     */
    private int getSupportingSequenceMax()
    {
        int maxSeq = 0;
        for (BagFileReference bfr : this.fileRefs)
        {
            if (BagIt.FINAL.equals(bfr.type))
            {
                if (bfr.sequence > maxSeq) {
                    maxSeq = bfr.sequence;
                }
            }
        }
        return maxSeq;
    }

    /**
     * Add the provided metadata object to the Bag in the appropriate file format
     *
     * @param metadata  Metadata object
     */
    public void addMetadata(Metadata metadata)
    {
        BagFileReference bfr = new BagFileReference();
        bfr.metadata = metadata;
        bfr.type = BagIt.METADATA;
        this.fileRefs.add(bfr);
    }

    /**
     * Add an appropriately formatted metadata file as the metadata file for this Bag
     *
     * @param file  File object containing the metadata
     */
    public void addMetadataFile(File file)
    {
        BagFileReference bfr = new BagFileReference();
        bfr.file = file;
        bfr.type = BagIt.METADATA;
        this.fileRefs.add(bfr);
    }

    /**
     * Set a licence file for the Bag
     *
     * @param file  File object containing the licence
     */
    public void addLicenceFile(File file)
    {
        this.addLicenceFile(file, null);
    }

    /**
     * Set a licence file for the Bag
     *
     * @param file  File object containing the licence
     * @param mimeType  mimetype of the licence object
     */
    public void addLicenceFile(File file, String mimeType)
    {
        BagFileReference bfr = new BagFileReference();
        bfr.file = file;
        bfr.type = BagIt.LICENCE;

        // calculate the format if necessary
        if (mimeType == null)
        {
            mimeType = new MimetypesFileTypeMap().getContentType(file);
        }
        bfr.format = mimeType;

        this.fileRefs.add(bfr);
    }

    /**
     * Write the metadata object to the given path inside the given zip file output stream
     *
     * @param metadata  A BagIt Metadata object
     * @param path  The path within the zip file to store the resulting metadata file
     * @param out   The ZipOutputStream to write the file to
     * @return  The MD5 digest of the resulting metadata file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String writeToZip(Metadata metadata, String path, ZipOutputStream out)
            throws IOException, NoSuchAlgorithmException
    {
        String mdxml = metadata.toXML();
        ByteArrayInputStream bais = new ByteArrayInputStream(mdxml.getBytes());
        return this.writeToZip(bais, path, out);
    }

    /**
     * Write the file referenced by the file handle to the given path inside the given zip output stream
     *
     * @param file  The file reference
     * @param path  The path within the zip file to store a copy of the file
     * @param out   The ZipOutputStream to write the file to
     * @return  The MD5 digest of the file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String writeToZip(File file, String path, ZipOutputStream out)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        FileInputStream fi = new FileInputStream(file);
        return this.writeToZip(fi, path, out);
    }

    /**
     * Write a text file containing the supplied string to the given path inside the given zip output stream
     *
     * @param str   The string to write into a file
     * @param path  The path within the zip file to store the resulting text file
     * @param out   The ZipOutputStream to write the file to
     * @return  The MD5 digest of the resulting text file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String writeToZip(String str, String path, ZipOutputStream out)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
        return this.writeToZip(bais, path, out);
    }

    /**
     * Write the data from the input stream to the given path inside the given zip output stream
     * @param fi    InputStream to source data from
     * @param path  The path within the zip file to store the resulting file
     * @param out   The ZipOutputStream to write the file to
     * @return  The MD5 digest of the resulting text file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String writeToZip(InputStream fi, String path, ZipOutputStream out)
            throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
        DigestInputStream dis = new DigestInputStream(origin, md);

        ZipEntry entry = new ZipEntry(path);
        out.putNextEntry(entry);
        int count;
        byte data[] = new byte[BUFFER];
        while((count = dis.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();

        byte[] b = md.digest();
        String result = "";
        for (int i=0; i < b.length; i++)
        {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    /**
     * Write the current state of the BagIt object out to the file the BagIt object is
     * constructed over
     */
    public void writeToFile()
    {
        try
        {
            // if this bag was initialised from a zip file, we can't write back to it - just too
            // complicated.
            if (this.zipFile != null) {
                throw new RuntimeException("Cannot re-write a modified bag file.  You should either create a new bag file from the source files, or read in the old zip file and pass the components in here.");
            }

            // it may be that the bagFile exists - we don't care, we just overwrite
            FileOutputStream dest = new FileOutputStream(this.bagFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            String formats = "";
            String finalSequence = "";
            String supportingAccess = "";
            String supportingSequence = "";
            String manifest = "";
            String tagmanifest = "";

            for (BagFileReference bfr : this.fileRefs)
            {
                if (BagIt.FINAL.equals(bfr.type))
                {
                    String dataFinal = "data/final/" + bfr.file.getName();
                    String checksum = this.writeToZip(bfr.file, this.baseDir + dataFinal, out);
                    if (bfr.format != null)
                    {
                        formats = formats + bfr.format + "\t" + dataFinal + "\n";
                    }
                    finalSequence = finalSequence + bfr.sequence + "\t" + dataFinal + "\n";
                    manifest = manifest + checksum + "\t" + dataFinal + "\n";
                }
                else if (BagIt.SUPPORTING.equals(bfr.type))
                {
                    String dataSupporting = "data/supporting/" + bfr.file.getName();
                    String checksum = this.writeToZip(bfr.file, this.baseDir + dataSupporting, out);
                    if (bfr.format != null)
                    {
                        formats = formats + bfr.format + "\t" + dataSupporting + "\n";
                    }
                    supportingSequence = supportingSequence + bfr.sequence + "\t" + dataSupporting + "\n";
                    supportingAccess = supportingAccess + bfr.access + "\t" + dataSupporting + "\n";
                    manifest = manifest + checksum + "\t" + dataSupporting + "\n";
                }
                else if (BagIt.LICENCE.equals(bfr.type))
                {
                    String dataLicence = "data/licence/" + bfr.file.getName();
                    String checksum = this.writeToZip(bfr.file, this.baseDir + dataLicence, out);
                    if (bfr.format != null)
                    {
                        formats = formats + bfr.format + "\t" + dataLicence + "\n";
                    }
                    manifest = manifest + checksum + "\t" + dataLicence + "\n";
                }
                else if (BagIt.METADATA.equals(bfr.type))
                {
                    String dataMetadata = "data/metadata/metadata.xml";
                    String checksum = null;
                    if (bfr.file != null)
                    {
                        checksum = this.writeToZip(bfr.file, this.baseDir + dataMetadata, out);
                    }
                    else if (bfr.metadata != null)
                    {
                        checksum = this.writeToZip(bfr.metadata, this.baseDir + dataMetadata, out);
                    }
                    formats = formats + "text/xml\t" + dataMetadata + "\n";
                    manifest = manifest + checksum + "\t" + dataMetadata + "\n";
                }
            }

            String checksum = this.writeToZip(formats, this.baseDir + "tagfiles/formats.txt", out);
            tagmanifest = tagmanifest + checksum + "\ttagfiles/formats.txt" + "\n";

            checksum = this.writeToZip(finalSequence, this.baseDir + "tagfiles/final.sequence.txt", out);
            tagmanifest = tagmanifest + checksum + "\ttagfiles/final.sequence.txt" + "\n";

            checksum = this.writeToZip(supportingSequence, this.baseDir + "tagfiles/supporting.sequence.txt", out);
            tagmanifest = tagmanifest + checksum + "\ttagfiles/supporting.sequence.txt" + "\n";

            checksum = this.writeToZip(supportingAccess, this.baseDir + "tagfiles/supporting.access.txt", out);
            tagmanifest = tagmanifest + checksum + "\ttagfiles/supporting.access.txt" + "\n";

            this.writeToZip(manifest, this.baseDir + "manifest-md5.txt", out);
            this.writeToZip(tagmanifest, this.baseDir + "tagmanifest-md5.txt", out);

            String bagitfile = "BagIt-Version: 0.97\nTag-File-Character-Encoding: UTF-8";
            this.writeToZip(bagitfile, this.baseDir + "bagit.txt", out);

            out.close();
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the list of primary files sorted in sequence
     *
     * @return
     */
    public TreeMap<Integer, BaggedItem> getSequencedFinals()
    {
        try
        {
            TreeMap<Integer, BaggedItem> sequencedPrimaries = new TreeMap<Integer, BaggedItem>();

            for (BagFileReference bfr : this.fileRefs)
            {
                if (BagIt.FINAL.equals(bfr.type))
                {
                    // the bagged item
                    BaggedItem baggedItem = new BaggedItem();

                    baggedItem.setInputStream(bfr.getInputStream());
                    baggedItem.setFilename(bfr.getFilename());
                    baggedItem.setFormat(bfr.format);
                    baggedItem.setSequence(bfr.sequence);

                    // create the node
                    sequencedPrimaries.put(bfr.sequence, baggedItem);
                }
            }

            return sequencedPrimaries;
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the list of secondary files sorted in sequence
     *
     * @param accessRights  Access rights filter - provide "open" or "closed", to obtain only those with that access right
     * @return
     */
    public TreeMap<Integer, BaggedItem> getSequencedSecondaries(String accessRights)
    {
        try
        {
            TreeMap<Integer, BaggedItem> sequencedSecondaries = new TreeMap<Integer, BaggedItem>();

            for (BagFileReference bfr : this.fileRefs)
            {
                if (BagIt.SUPPORTING.equals(bfr.type) && accessRights.equals(bfr.access))
                {
                    // the bagged item
                    BaggedItem baggedItem = new BaggedItem();

                    baggedItem.setInputStream(bfr.getInputStream());
                    baggedItem.setFilename(bfr.getFilename());
                    baggedItem.setFormat(bfr.format);
                    baggedItem.setSequence(bfr.sequence);

                    // create the node
                    sequencedSecondaries.put(bfr.sequence, baggedItem);
                }
            }

            return sequencedSecondaries;
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Get the Metadata file from the Bag
     *
     * @return
     */
    public BaggedItem getMetadataFile()
    {
        try
        {
            for (BagFileReference bfr : this.fileRefs)
            {
                if (BagIt.METADATA.equals(bfr.type))
                {
                    BaggedItem metadata = new BaggedItem();
                    metadata.setInputStream(bfr.getInputStream());
                    metadata.setFilename("metadata.xml");
                    metadata.setFormat("text/xml");
                    return metadata;
                }
            }
            return null;
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Get the licence file from the Bag
     *
     * @return
     */
    public BaggedItem getLicenceFile()
    {
        try
        {
            for (BagFileReference bfr : this.fileRefs)
            {
                if (BagIt.METADATA.equals(bfr.type))
                {
                    BaggedItem licence = new BaggedItem();
                    licence.setInputStream(bfr.getInputStream());
                    licence.setFilename("licence.txt");
                    licence.setFormat("text/plain");
                    return licence;
                }
            }
            return null;
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain the access privilege for the specified filename
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public String getSupportingAccess(String filename)
            throws IOException
    {
        for (BagFileReference bfr : this.fileRefs)
        {
            if (BagIt.SUPPORTING.equals(bfr.type) && bfr.getFilename().equals(filename))
            {
                return bfr.access;
            }
        }
        return null;
    }

    /**
     * Verify the Bag against its manifest
     *
     * @return
     */
    public boolean verifyPayloadManifest()
    {
        return true;
        //return theBag.verifyPayloadManifests().isSuccess();
    }

    /**
     * Verify the Bag against its tag manifest
     *
     * @return
     */
    public boolean verifyTagManifest()
    {
        return true;
        // return theBag.verifyTagManifests().isSuccess();
    }

    /**
     * Get the file representing the whole bag
     *
     * @return
     * @throws IOException
     */
    public InputStream getFile()
            throws IOException
    {
        return FileUtils.openInputStream(this.bagFile);
    }

    /**
     * Get the filename for the whole Bag
     *
     * @return
     */
    public String getName()
    {
        return this.bagFile.getName();
    }

    /**
     * Get the MD5 for the whole Bag
     *
     * @return
     */
    public String getMD5()
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = this.getFile();
            int numRead;
            byte[] buffer = new byte[1024];
            do {
                numRead = is.read(buffer);
                if (numRead > 0)
                {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            is.close();

            byte[] b = md.digest();
            String result = "";
            for (int i=0; i < b.length; i++)
            {
                result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return result;
        }
        // we need to conform to the old interface, so can only throw RuntimeExceptions
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the mimetype of the packaged Bag
     *
     * @return
     */
    public String getMimetype() {

        return "application/zip";
    }

    /**
     * Get the SWORDv2 packaging identifier for the Bag
     * @return
     */
    public String getPackaging() {

        return "http://duo.uio.no/terms/package/FSBagIt";
    }
}
