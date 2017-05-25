package photo;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.io.IOException;
import java.util.*;

import java.text.SimpleDateFormat;
import java.io.*;
import acm.program.*;

/**
 * Sample code that copies files in a similar manner to the cp(1) program.
 */

public class CopyPhotos extends ConsoleProgram{
    /**
     * 
	 */
	private static final long serialVersionUID = 1L;
	public static int copied_files = 0;
    public static int target_files = 0;
    public static int already_exist_files = 0;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
    public static boolean files_count_done = false;
    /**
     * Returns {@code true} if okay to overwrite a  file ("cp -i")
     */
    public boolean okayToOverwrite(Path file) {
        String answer = readLine("overwrite "+ file.toString() +" (yes/no)? ");
        return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
    }

    /**
     * Copy source file to target location. If {@code prompt} is true then
     * prompt user to overwrite target if it exists. The {@code preserve}
     * parameter determines if file attributes should be copied/preserved.
     */
    public void copyFile(Path source, Path target, boolean prompt, boolean preserve) {
        CopyOption[] options = (preserve) ?
            new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING } :
            new CopyOption[] { REPLACE_EXISTING };
        if (Files.notExists(target)) {
            try {
            	if( files_count_done ) {
            		Files.copy(source, target, options);
                    copied_files++;
                    
                    
                    // Progress
                    dprintln("copyFile","source : "+ source.toString());
                    dprintln("copyFile","target : "+ target.toString());
                    dprintln("Progress", copied_files +"/"+ target_files);
                    
            	} else {
            		target_files++;
            	}
                
            } catch (IOException x) {
                dprintln("copyFile","Unable to copy: " + source.toString() +": "+ x.toString());
            }
        } else {
          //copy skip
        }
    }
    /**
     * This is for debugging. {@code function} is function name
     * {@code result} is result message
     */
    public void dprintln( String function, String result) {
    	println( "["+ function + "] "+ result);
    }

    /**
     * A {@code FileVisitor} that copies a file-tree ("cp -r")
     */
    public class TreeCopier implements FileVisitor<Path> {
        private final Path target;
        private final boolean prompt;
        private final boolean preserve;

        TreeCopier(Path source, Path target, boolean prompt, boolean preserve) {
            this.target = target;
            this.prompt = prompt;
            this.preserve = preserve;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
           /* CopyOption[] options = (preserve) ?
                    new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];

                Path newdir = target.resolve(source.relativize(dir));
                try {
                    Files.copy(dir, newdir, options);
                } catch (FileAlreadyExistsException x) {
                    // ignore
                } catch (IOException x) {
                    System.err.format("Unable to create: %s: %s%n", newdir, x);
                    return SKIP_SUBTREE;
                }
                */
                return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

            File myFile = file.toFile(); 
            

            Date modifyDate = new Date(myFile.lastModified()); //sdf = SimpleDateFormat("yyyy_MM_dd HH:mm:ss")
            
            String dateString = sdf.format(modifyDate);
            
            String year = dateString.substring(0, 4); // yyyy from yyyy_MM_dd
            String month = dateString.substring(5, 7); // mm from yyyy_MM_dd
            String day = dateString.substring(8,10); // dd from yyyy_MM_dd

            
            Path dir = target.resolve( Paths.get( year + "_년" + "\\" + month + "_월" + "\\" + day + "_일") ); 
            //Path dir = target.resolve( Paths.get( year + "_년" + "\\" + month + "_월") );
            
            // create target directory
            try {
            	  if( dir.toFile().exists()) {
            		  //ignore
            	  } else {
            	     Files.createDirectories(dir);
            	  }
                  
            } catch (FileAlreadyExistsException x) {
                            // ignore
               dprintln("visitFile", dir.toString() + "already Exists");
            } catch (IOException x) {
               dprintln("visitFile","Unable to create: " + dir.toString() + ": "+ x.toString());
               return SKIP_SUBTREE;
            }            
            
            String fileName = file.getFileName().toString();
            File newFile = new File( dir + "\\" + fileName);
            Path newPath = newFile.toPath();
            

            copyFile(file, target.resolve(newPath), prompt, preserve);
            

            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
          /*  if (exc == null && preserve) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException x) {
                    System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
                }
            } */
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                println("[Error] cycle detected: " + file.toString());
            } else {
                println("[Error] Unable to copy: "+ file.toString() + ": " + exc.toString());
            }
            return CONTINUE;
        }
    }

    public void usage() {
        println("[Usage] java Copy [-ip] source... target");
        println("[Usage] java Copy -r [-ip] source-dir... target");
    }

    public void run() {  	
        boolean recursive = false;
        boolean prompt = false;
        boolean preserve = false;
        
        //String [] args = { "-r", "-p", "H:\\01_자료Backup\\kimyunhee\\DCIM\\100CANON", "F:\\Photo" };
         //String [] args = { "-r", "-p", "D:\\보관자료\\10_사진자료\\분류전", "D:\\보관자료\\10_사진자료" };
        String [] args = { "-r", "-p", "G:\\99_사진\\사진", "G:\\99_사진\\분류" };	//test	
        
        // process options
        int argi = 0;
        while (argi < args.length) {
            String arg = args[argi];
            if (!arg.startsWith("-"))
                break;
            if (arg.length() < 2) {
                usage();
                break;
            }
            for (int i=1; i<arg.length(); i++) {
                char c = arg.charAt(i);
                switch (c) {
                    case 'r' : recursive = true; break;
                    case 'i' : prompt = true; break;
                    case 'p' : preserve = true; break;
                    default : usage(); break;
                }
            }
            argi++;
        }

        // remaining arguments are the source files(s) and the target location
        int remaining = args.length - argi;
        if (remaining < 2){
            usage();
            println( "Try it again!!");
        }
        else {
        	Path[] source = new Path[remaining-1];
        	int i=0;
        	while (remaining > 1) {
        		source[i++] = Paths.get(args[argi++]);
        		remaining--;
        	}
        	Path target = Paths.get(args[argi]);

        	// check if target is a directory
        	// boolean isDir = Files.isDirectory(target);

        	// copy each source file/directory to target
        	for (i=0; i<source.length; i++) {
        		//Path dest = (isDir) ? target.resolve(source[i].getFileName()) : target;
        		Path dest = target;

        		if (recursive) {
        			// follow links when counting target files
        			EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        			TreeCopier tc = new TreeCopier(source[i], dest, prompt, preserve);
        			try {
        				Files.walkFileTree(source[i], opts, Integer.MAX_VALUE, tc);
        				files_count_done = true;
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        			
        			// follow links when copying files
        			opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        			tc = new TreeCopier(source[i], dest, prompt, preserve);
        			try {
        				Files.walkFileTree(source[i], opts, Integer.MAX_VALUE, tc);
        			} catch (IOException e) {
        				e.printStackTrace();
        			}        			
        			
        		} else {
        			// not recursive so source must not be a directory
        			if (Files.isDirectory(source[i])) {
        				println( "[Error] "+ source[i].toString() + ": is a directory");
        				continue;
        			}
        			//copyFile(source[i], dest, prompt, preserve);
        		}
        	}
        	println( "copy done : " + copied_files + " files copied!! ");
        }
    }
}
