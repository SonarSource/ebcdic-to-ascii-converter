This program allows to convert some source files from an EBCDIC format to an ASCII one. 

The input of this program is a directory containing the source files to be converted and the output is another directory containing all the converted source files. The input directory can contain a tree of sub-directory and this tree is kept in the output directory. 

Installation
------------ 

1 - The only pre-requisite is to have a JVM (Java Virtual Machine) 6+.
2 - Download the ebcdic-ascii-converter-X.Y.jar archive 
3 - Execute the following command to display the usage information :
<code>java -jar ebcdic-ascii-converter-X.Y.jar -h</code>
4 - Lauch for instance the following command to convert all source files contained in dir1 and to put the converted  files in dir2 :
<code>java -jar ebcdic-ascii-converter-X.Y.jar dir1 dir2</code>

